package viewstack.internal;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import viewstack.ViewComponent;
import viewstack.ViewStack;

import java.lang.reflect.Constructor;
import java.util.List;

class StateHelper {

    private static final String PACKAGE = ViewStack.class.getPackage().getName();

    static StateHelper saveStateHelper(int stackId, @NonNull Bundle outState) {
        Bundle state = new Bundle();
        outState.putBundle(PACKAGE + "_" + stackId, state);
        return new StateHelper(stackId, state);
    }

    static StateHelper restoreStateHelper(int stackId, @NonNull Bundle savedInstanceState) {
        Bundle state = savedInstanceState.getBundle(PACKAGE + "_" + stackId);
        if (state == null) throw new IllegalStateException("state null");
        return new StateHelper(stackId, state);
    }

    private final int stackId;
    private final Bundle state;

    private StateHelper(int stackId, Bundle state) {
        this.stackId = stackId;
        this.state = state;
    }

    void saveStack(ComponentsStack stack) {
        ComponentParcelableDescriptor[] arr = new ComponentParcelableDescriptor[stack.size()];
        List<ViewComponent> list = stack.internalList();
        for (int i = 0; i < list.size(); i++) {
            ViewHolder component = list.get(i);
            arr[i] = new ComponentParcelableDescriptor(component.getClass(), component.getComponentId(), component.hasArguments() ? component.getArguments() : null, component.getViewState());
        }
        state.putParcelableArray("stack", arr);
    }

    ComponentsStack restoreStack() {
        Parcelable[] arr = state.getParcelableArray("stack");
        ComponentsStack stack = new ComponentsStack(stackId);
        if (arr == null) return stack;
        List<ViewComponent> componentList = stack.internalList();
        for (Parcelable p : arr) {
            componentList.add(((ComponentParcelableDescriptor) p).createComponent());
        }
        return stack;
    }

    void saveLifecycleManager(LifecycleManager lifecycleManager) {
        state.putInt("lm", lifecycleManager.saveState());
    }

    void restoreLifecycleManager(LifecycleManager lifecycleManager) {
        lifecycleManager.restoreState(state.getInt("lm"));
    }


    @SuppressWarnings("unchecked")
    static class ComponentParcelableDescriptor implements Parcelable {

        final Class clazz;
        final int id;

        final Bundle arguments;

        @Nullable final SparseArray viewHierarchyState;


        ComponentParcelableDescriptor(Class clazz, int id, @Nullable Bundle arguments, @Nullable SparseArray<Parcelable> viewHierarchyState) {
            this.clazz = clazz;
            this.id = id;
            this.arguments = arguments;
            this.viewHierarchyState = viewHierarchyState;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeSerializable(clazz);
            dest.writeInt(id);
            dest.writeBundle(arguments);
            dest.writeSparseArray(viewHierarchyState);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        ViewComponent createComponent() {
            try {
                Constructor constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);
                ViewHolder instance = (ViewHolder) constructor.newInstance();
                instance.setComponentId(id);
                instance.setArguments(arguments);
                instance.setViewState(viewHierarchyState);
                return (ViewComponent) instance;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        public static final Creator<ComponentParcelableDescriptor> CREATOR = new Creator<ComponentParcelableDescriptor>() {
            @Override
            public ComponentParcelableDescriptor createFromParcel(Parcel in) {
                Class clazz = (Class) in.readSerializable();
                int id = in.readInt();
                Bundle arguments = in.readBundle(clazz.getClassLoader());
                SparseArray<Parcelable> sparseArray = in.readSparseArray(clazz.getClassLoader());
                return new ComponentParcelableDescriptor(clazz, id, arguments, sparseArray);
            }

            @Override
            public ComponentParcelableDescriptor[] newArray(int size) {
                return new ComponentParcelableDescriptor[size];
            }
        };
    }

}
