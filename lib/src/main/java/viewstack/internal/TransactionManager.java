package viewstack.internal;


import android.view.ViewGroup;

import java.util.Iterator;
import java.util.LinkedList;

import viewstack.ViewComponent;
import viewstack.utils.StackChangedListener;

import static viewstack.internal.AnimationHandler.animatedTransaction;

@SuppressWarnings("WeakerAccess")
public final class TransactionManager {

    final LifecycleManager lm;
    final ComponentsStack stack;
    final ViewGroup container;
    final StackChangedListener stackChangedListener;

    public TransactionManager(LifecycleManager lifecycleManager, ComponentsStack stack, ViewGroup container, StackChangedListener stackChangedListener) {
        this.lm = lifecycleManager;
        this.stack = stack;
        this.container = container;
        this.stackChangedListener = stackChangedListener;
    }

    public Transaction addRootTransaction(ViewComponent component) {
        stack.add(component);
        onAddToStack(component);
        return coordinator -> lm.attachNew(component);
    }

    public Transaction addTransaction(ViewComponent component) {
        final ViewComponent current = stack.getTop();
        stack.add(component);
        onAddToStack(component);
        return coordinator -> {
            lm.attachNew(component);
            lm.detach(current, false);
            coordinator.execute(animatedTransaction(current, component, true, container), () -> lm.detachView(current, true));
        };
    }

    public Transaction replaceTransaction(ViewComponent component) {
        final ViewComponent current = stack.removeTop();
        stack.add(component);
        onAddToStack(component);
        onRemoveFromStack(current);
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
        onAddToStack(component);
        onRemoveFromStack(sublist);
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
        if (stack.getTop() == next) {
            return emptyTransaction();
        }
        ViewComponent current = stack.removeTop();
        onRemoveFromStack(current);
        final LinkedList<ViewComponent> sublist = stack.removeAllFrom(next);
        onRemoveFromStack(sublist);
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
        onRemoveFromStack(current);
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

    private void onAddToStack(ViewComponent component) {
        stackChangedListener.onComponentAdded(component);
    }

    private void onRemoveFromStack(ViewComponent component) {
        stackChangedListener.onComponentRemoved(component);
    }

    private void onRemoveFromStack(LinkedList<ViewComponent> list) {
        Iterator<ViewComponent> reverse = list.descendingIterator();
        while (reverse.hasNext()) {
            onRemoveFromStack(reverse.next());
        }
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
