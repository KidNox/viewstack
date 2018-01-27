package viewstack.internal;


import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import java.util.Iterator;
import java.util.LinkedList;

import viewstack.ViewComponent;
import viewstack.contract.ActivityCallbacks;
import viewstack.contract.Component;

class LifecycleManager implements ActivityCallbacks {

    private final int stackId;

    private int componentIdsCounter;

    private final Activity activity;
    private final ViewGroup container;

    private ViewComponent currentActive;
    private PendingAction pendingAction;

    LifecycleManager(Activity activity, ViewGroup container) {
        this.activity = activity;
        this.container = container;
        stackId = ComponentsStack.idOf(container);
    }

    void attachNew(ViewComponent component) {
        createIfNeed(component);
        render(component);
        attach(component);
    }

    void render(ViewComponent component) {
        createIfNeed(component);
        View view = component.render(activity.getLayoutInflater(), container);
        ((ViewHolder) component).setView(view);
        component.afterRender(view);
    }

    void attach(ViewComponent component) {
        createIfNeed(component);
        switchActive(component);
        component.onAttach();
        container.addView(component.getView());
    }

    private <C extends ContextHolder & Component> void createIfNeed(C component) {
        if (!component.hasContext()) {
            component.setBaseContext(activity);
            component.setStackId(stackId);
            if (component.getComponentId() == 0) {
                component.setComponentId(++componentIdsCounter);
            }
            component.onCreate();
        }
    }

    void detach(ViewComponent component, boolean removedFromStack) {
        component.onDetach(removedFromStack);
    }

    <C extends ViewHolder & Component> void detachView(C component, boolean saveState) {
        if (saveState) {
            component.prepareViewState();
        }
        container.removeView(component.getView());
        component.removeViewReference();
    }

    void destroy(ViewComponent component, boolean removedFromStack) {
        if (component.hasContext()) {
            component.onDestroy(removedFromStack);
            component.setBaseContext(null);
        }
    }

    void destroyList(LinkedList<ViewComponent> list, boolean removeFromStack) {
        Iterator<ViewComponent> reverse = list.descendingIterator();
        while (reverse.hasNext()) {
            destroy(reverse.next(), removeFromStack);
        }
    }

    void restore(ComponentsStack stack) {
        attachNew(stack.getTop());
    }

    private void switchActive(ViewComponent component) {
        currentActive = component;
        dispatchPendingAction();
    }

    boolean isActive(ContextHolder component) {
        return component == currentActive;
    }

    @SuppressWarnings("WeakerAccess")
    int currentId() {
        if (currentActive == null) return -1;
        return ((ContextHolder) currentActive).getComponentId();
    }

    void restoreState(int componentIdsCounter) {
        this.componentIdsCounter = componentIdsCounter;
    }

    int saveState() {
        return componentIdsCounter;
    }

    void onActivityResult(int componentId, int requestCode, int resultCode, Intent data) {
        if (pendingAction != null) throw new IllegalStateException();
        pendingAction = () -> {
            if (componentId == currentId()) {
                ((ContextHolder) currentActive).onActivityResult(requestCode, resultCode, data);
                return true;
            }
            return false;
        };
        dispatchPendingAction();
    }

    void onRequestPermissionsResult(int componentId, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (pendingAction != null) throw new IllegalStateException();
        pendingAction = () -> {
            if (componentId == currentId()) {
                ((ContextHolder) currentActive).onRequestPermissionsResult(requestCode, permissions, grantResults);
                return true;
            }
            return false;
        };
        dispatchPendingAction();
    }

    private void dispatchPendingAction() {
        if (pendingAction != null) {
            if (pendingAction.dispatch()) {
                pendingAction = null;
            }
        }
    }

    interface PendingAction {
        boolean dispatch();
    }

    interface ActivityAction {
        void dispatch(ActivityCallbacks callbacks);
    }

    @Override
    public void onActivityStart() {
        dispatchActivityAction(ActivityCallbacks::onActivityStart);
    }

    @Override
    public void onActivityResume() {
        dispatchActivityAction(ActivityCallbacks::onActivityResume);
    }

    @Override
    public void onActivityPause() {
        dispatchActivityAction(ActivityCallbacks::onActivityPause);
    }

    @Override
    public void onActivityStop() {
        dispatchActivityAction(ActivityCallbacks::onActivityStop);
    }

    private void dispatchActivityAction(ActivityAction activityAction) {
        Component component = currentActive;
        if (component instanceof ActivityCallbacks) {
            activityAction.dispatch((ActivityCallbacks) component);
        }
    }

}
