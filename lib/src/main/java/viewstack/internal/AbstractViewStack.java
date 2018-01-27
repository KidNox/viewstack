package viewstack.internal;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

import viewstack.ViewStack;
import viewstack.contract.animation.AnimationContract;
import viewstack.utils.StackOptions;
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

    void destroy(boolean isFinishing) {
        coordinator.executeForced(transactionManager.destroyAllTransaction(isFinishing), false);
    }

    LifecycleManager getLifecycleManager() {
        return transactionManager.getLifecycleManager();
    }

    protected static abstract class AbstractViewStackProvider {

        private final Activity activity;
        private final ViewGroup container;
        private final StackOptions options;

        public AbstractViewStackProvider(Activity activity, @Nullable ViewGroup container, @Nullable StackOptions options) {
            this.activity = activity;
            this.container = container == null ? (ViewGroup) activity.findViewById(android.R.id.content) : container;
            this.options = options == null ? new StackOptions() : options;
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
            StackChangedListener stackChangedListener = options.getStackChangedListener();
            AnimationContract animationContract = options.getDefaultAnimationContract();
            TransactionManager transactionManager = new TransactionManager(lifecycleManager, stack, container, stackChangedListener, animationContract);
            result = newInstance(stack, transactionManager, new Coordinator(), options);
            stateHolder.putViewStack(stackId, result);
            return result;
        }

        protected abstract ViewStack newInstance(ComponentsStack stack, TransactionManager transactionManager, Coordinator coordinator,
                                                 StackOptions options);

    }

}
