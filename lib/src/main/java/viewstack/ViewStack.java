package viewstack;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

import viewstack.internal.AbstractViewStack;
import viewstack.internal.ComponentsStack;
import viewstack.internal.Coordinator;
import viewstack.internal.TransactionManager;
import viewstack.internal.TransactionManager.Transaction;
import viewstack.utils.ComponentFinder;

public final class ViewStack extends AbstractViewStack {

    public static ViewStack of(Activity activity, @Nullable Bundle savedInstanceState) {
        return new ViewStackProvider(activity, null).getOrCreate(savedInstanceState);
    }

    public static ViewStack of(Activity activity, ViewGroup container, @Nullable Bundle savedInstanceState) {
        return new ViewStackProvider(activity, container).getOrCreate(savedInstanceState);
    }

    public void show(ViewComponent component) {
        Transaction transaction;
        if (stack.isEmpty()) {
            transaction = transactionManager.addRootTransaction(component);
        } else {
            transaction = transactionManager.addTransaction(component);
        }
        coordinator.execute(transaction);
    }

    public void replaceCurrent(ViewComponent component) {
        Transaction transaction;
        if (stack.isEmpty()) {
            transaction = transactionManager.addRootTransaction(component);
        } else {
            transaction = transactionManager.replaceTransaction(component);
        }
        coordinator.execute(transaction);
    }

    public void replaceAll(ViewComponent component) {
        Transaction transaction;
        if (stack.isEmpty()) {
            transaction = transactionManager.addRootTransaction(component);
        } else {
            transaction = transactionManager.replaceAllTransaction(component);
        }
        coordinator.execute(transaction);
    }

    public boolean handleBack() {
        if (stack.isEmpty()) return false;
        Transaction transaction;
        boolean handleBack;
        if (stack.size() == 1) {
            //transaction = transactionManager.removeRootTransaction();
            handleBack = false;
        } else {
            transaction = transactionManager.removeLastTransaction();
            coordinator.execute(transaction);
            handleBack = true;
        }
        return handleBack;
    }

    public <T extends ViewComponent> T goTo(Class<T> keyClass) {
        T instance = findUnique(keyClass);
        if (instance == null) {
            throw new IllegalStateException("can't find instance of " + keyClass + " in stack " + stack);
        }
        coordinator.execute(transactionManager.goToTransaction(instance));
        return instance;
    }

    public void goTo(ViewComponent component) {
        if (!stack.contains(component)) {
            throw new IllegalArgumentException("component " + component + ", not in stack " + stack);
        }
        coordinator.execute(transactionManager.goToTransaction(component));
    }

    @Nullable
    public <T extends ViewComponent> T findUnique(Class<T> keyClass) {
        return stack.findUnique(keyClass);
    }

    @Nullable
    public ViewComponent find(ComponentFinder finder) {
        return stack.find(finder);
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    ViewStack(ComponentsStack stack, TransactionManager transactionManager, Coordinator coordinator) {
        super(stack, transactionManager, coordinator);
    }

    private static class ViewStackProvider extends AbstractViewStackProvider {

        ViewStackProvider(Activity activity, @Nullable ViewGroup container) {
            super(activity, container);
        }

        @Override
        protected ViewStack newInstance(ComponentsStack stack, TransactionManager transactionManager, Coordinator coordinator) {
            return new ViewStack(stack, transactionManager, coordinator);
        }

    }
}
