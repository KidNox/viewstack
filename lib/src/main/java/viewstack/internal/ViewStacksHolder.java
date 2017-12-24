package viewstack.internal;


import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import java.util.HashMap;
import java.util.Map;

import viewstack.ViewStack;
import viewstack.internal.fragments.FragmentCallbacks;
import viewstack.internal.fragments.FragmentDelegate;
import viewstack.internal.fragments.FragmentProvider;

@SuppressWarnings("WeakerAccess")
public final class ViewStacksHolder implements FragmentCallbacks {

    private static Map<Activity, ViewStacksHolder> map = new HashMap<>();

    static ViewStacksHolder getOrCreate(Activity activity) {
        ViewStacksHolder holder = map.get(activity);
        if(holder == null) {
            holder = new ViewStacksHolder(activity);
            holder.initActivityCallbacks();
            holder.initFragmentCallbacks();
            map.put(activity, holder);
        }
        return holder;
    }

    private SparseArray<ViewStack> viewStacks = new SparseArray<>();

    private Activity context;
    private FragmentDelegate fragmentInterface;

    public ViewStacksHolder(Activity activity) {
        context = activity;
    }

    private void initActivityCallbacks() {
        context.getApplication().registerActivityLifecycleCallbacks(new ActivityCallbacks());
    }

    private void initFragmentCallbacks() {
        fragmentInterface = FragmentProvider.getOrCreateInterface(this, context);
    }

    FragmentDelegate getFragmentInterface() {
        return fragmentInterface;
    }

    @Nullable
    ViewStack getViewStack(int stackId) {
        return viewStacks.get(stackId);
    }

    void putViewStack(int id, ViewStack stack) {
        viewStacks.put(id, stack);
    }

    void onSaveInstanceState(Bundle outState) {
        for (int i = 0; i < viewStacks.size(); i++) {
            AbstractViewStack stack = viewStacks.valueAt(i);
            stack.saveState(outState);
        }
    }

    void onDestroy() {
        for (int i = 0; i < viewStacks.size(); i++) {
            AbstractViewStack stack = viewStacks.valueAt(i);
            stack.destroy();
        }
    }

    @Override
    public void onActivityResult(ComponentDescriptor descriptor,
                                 int requestCode, int resultCode, Intent data) {
        getLifecycleManager(descriptor.getStackId()).onActivityResult(descriptor.getComponentId(), requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(ComponentDescriptor descriptor,
                                           int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        getLifecycleManager(descriptor.getStackId()).onRequestPermissionsResult(descriptor.getComponentId(), requestCode, permissions, grantResults);
    }

    LifecycleManager getLifecycleManager(int stackId) {
        AbstractViewStack viewStack = viewStacks.get(stackId);
        return viewStack.getLifecycleManager();
    }

    private class ActivityCallbacks implements Application.ActivityLifecycleCallbacks {

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {
            for (int i = 0; i < viewStacks.size(); i++) {
                AbstractViewStack stack = viewStacks.valueAt(i);
                stack.getLifecycleManager().onActivityStart();
            }
        }

        @Override
        public void onActivityResumed(Activity activity) {
            for (int i = 0; i < viewStacks.size(); i++) {
                AbstractViewStack stack = viewStacks.valueAt(i);
                stack.getLifecycleManager().onActivityResume();
            }
        }

        @Override
        public void onActivityPaused(Activity activity) {
            for (int i = 0; i < viewStacks.size(); i++) {
                AbstractViewStack stack = viewStacks.valueAt(i);
                stack.getLifecycleManager().onActivityPause();
            }
        }

        @Override
        public void onActivityStopped(Activity activity) {
            for (int i = 0; i < viewStacks.size(); i++) {
                AbstractViewStack stack = viewStacks.valueAt(i);
                stack.getLifecycleManager().onActivityStop();
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            if(context == activity) {
                onSaveInstanceState(outState);
            }
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            if(context == activity) {
                activity.getApplication().unregisterActivityLifecycleCallbacks(this);
                onDestroy();
                map.remove(activity);
                fragmentInterface = null;
                context = null;
            }
        }
    }
}
