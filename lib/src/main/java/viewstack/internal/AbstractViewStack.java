package viewstack.internal;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

import viewstack.ViewComponent;
import viewstack.ViewStack;
import viewstack.utils.Options;
import viewstack.utils.StackChangedListener;

public class AbstractViewStack {

    protected final ComponentsStack stack;
    protected final TransactionManager transactionManager;
    protected final Coordinator coordinator;

    protected AbstractViewStack(ComponentsStack stack, TransactionManager transactionManager, Coordinator coordinator) {
        this.stack = stack;
        this.transactionManager = transactionManager;
        this.coordinator = coordinator;
    }

    void saveState(Bundle outState) {
        coordinator.executeForced(coordinator -> {
            StateHelper stateHelper = StateHelper.saveStateHelper(stack.getId(), outState);
            stateHelper.saveStack(stack);
            stateHelper.saveLifecycleManager(transactionManager.getLifecycleManager());
        }, true);
    }

    void destroy() {
        coordinator.executeForced(transactionManager.destroyAllTransaction(), false);
    }

    LifecycleManager getLifecycleManager() {
        return transactionManager.getLifecycleManager();
    }

    protected static abstract class AbstractViewStackProvider {

        private final Activity activity;
        private final ViewGroup container;
        @Nullable private final Options options;

        public AbstractViewStackProvider(Activity activity, @Nullable ViewGroup container, @Nullable Options options) {
            this.activity = activity;
            this.container = container == null ? (ViewGroup) activity.findViewById(android.R.id.content) : container;
            this.options = options;
        }

        public ViewStack getOrCreate(@Nullable Bundle state) {
            int stackId = ComponentsStack.idOf(container);
            ViewStacksHolder stateHolder = ViewStacksHolder.getOrCreate(activity);
            ViewStack result = stateHolder.getViewStack(stackId);
            if (result != null) {
                return result;
            }
            LifecycleManager lifecycleManager = new LifecycleManager(activity, container);
            ComponentsStack stack;
            if (state == null) {
                stack = new ComponentsStack(stackId);
            } else {
                StateHelper stateHelper = StateHelper.restoreStateHelper(stackId, state);
                stack = stateHelper.restoreStack();
                stateHelper.restoreLifecycleManager(lifecycleManager);
                lifecycleManager.restore(stack);
            }
            StackChangedListener stackChangedListener = options == null ? null : options.getStackChangedListener();
            if (stackChangedListener == null) {
                stackChangedListener = stackChangedListenerStub();
            }
            TransactionManager transactionManager = new TransactionManager(lifecycleManager, stack, container, stackChangedListener);
            result = newInstance(stack, transactionManager, new Coordinator());
            stateHolder.putViewStack(stackId, result);
            return result;
        }

        protected abstract ViewStack newInstance(ComponentsStack stack, TransactionManager transactionManager, Coordinator coordinator);

    }

    static StackChangedListener stackChangedListenerStub() {
        return new StackChangedListener() {
            @Override
            public void onComponentAdded(ViewComponent component) { }

            @Override
            public void onComponentRemoved(ViewComponent component) { }
        };
    }

}
