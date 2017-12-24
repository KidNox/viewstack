package viewstack.internal;


import android.app.Activity;
import android.view.ViewGroup;

import java.util.LinkedList;

import viewstack.ViewComponent;

import static viewstack.internal.AnimationHandler.animatedTransaction;

@SuppressWarnings("WeakerAccess")
public final class TransactionManager {

    final Activity activity;
    final LifecycleManager lm;
    final ComponentsStack stack;
    final ViewGroup container;

    public TransactionManager(Activity activity, LifecycleManager lifecycleManager, ComponentsStack stack, ViewGroup container) {
        this.activity = activity;
        this.lm = lifecycleManager;
        this.stack = stack;
        this.container = container;
    }

    public Transaction addRootTransaction(ViewComponent component) {
        stack.add(component);
        return coordinator -> lm.attachNew(component);
    }

    public Transaction addTransaction(ViewComponent component) {
        final ViewComponent current = stack.getTop();
        stack.add(component);
        return coordinator -> {
            lm.attachNew(component);
            lm.detach(current, false);
            coordinator.execute(animatedTransaction(current, component, true, container), () -> lm.detachView(current, true));
        };
    }

    public Transaction replaceTransaction(ViewComponent component) {
        final ViewComponent current = stack.removeTop();
        stack.add(component);
        return coordinator -> {
            lm.attachNew(component);
            lm.detach(current, true);
            coordinator.execute(animatedTransaction(current, component, false, container), () -> {
                lm.detachView(current, false);
                lm.destroy(current);
            });
        };
    }

    public Transaction replaceAllTransaction(ViewComponent component) {
        final ViewComponent current = stack.removeTop();
        final LinkedList<ViewComponent> sublist = stack.clear();
        stack.add(component);
        return coordinator -> {
            lm.attachNew(component);
            lm.detach(current, true);
            lm.destroyList(sublist);
            coordinator.execute(animatedTransaction(current, component, false, container), () -> {
                lm.detachView(current, false);
                lm.destroy(current);
            });
        };
    }

    public Transaction goToTransaction(ViewComponent next) {
        final ViewComponent current = stack.getTop();
        if (current == next) {
            return emptyTransaction();
        }
        stack.removeTop();
        final LinkedList<ViewComponent> sublist = stack.removeAllFrom(next);
        return coordinator -> {
            lm.render(next);
            lm.attach(next);
            lm.detach(current, true);
            lm.destroyList(sublist);
            coordinator.execute(animatedTransaction(current, next, false, container), () -> {
                lm.detachView(current, false);
                lm.destroy(current);
            });
        };
    }

    public Transaction removeLastTransaction() {
        final ViewComponent current = stack.removeTop();
        final ViewComponent next = stack.getTop();
        return coordinator -> {
            lm.render(next);
            lm.attach(next);
            lm.detach(current, true);
            coordinator.execute(animatedTransaction(current, next, false, container), () -> {
                lm.detachView(current, false);
                lm.destroy(current);
            });
        };
    }

    public Transaction destroyAllTransaction() {
        final ViewComponent current = stack.removeTop();
        final LinkedList<ViewComponent> list = stack.clear();
        return coordinator -> {
            lm.destroyList(list);
            lm.detach(current, true);
            lm.destroy(current);
        };
    }

    LifecycleManager getLifecycleManager() {
        return lm;
    }

    private Transaction emptyTransaction() {
        return coordinator -> {
        };
    }

    public interface Transaction {
        void execute(Coordinator coordinator);
    }

    interface AsyncTransaction {
        Coordinator.Cancellable execute(Runnable endCallback);
    }
}
