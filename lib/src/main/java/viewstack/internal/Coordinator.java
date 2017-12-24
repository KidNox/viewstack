package viewstack.internal;

import java.util.LinkedList;

import viewstack.internal.TransactionManager.AsyncTransaction;
import viewstack.internal.TransactionManager.Transaction;


public final class Coordinator {

    private final LinkedList<CoordinatedAction> stack = new LinkedList<>();

    private CoordinatedAction currentAction;
    private boolean forcedState;
    private boolean restoreFromForcedState;

    private boolean isExecuting;

    public void execute(Transaction transaction) {
        execute(new CoordinatedTransaction(transaction));
    }

    void execute(AsyncTransaction asyncTransaction, Runnable endCallback) {
        execute(new CoordinatedAsyncTransaction(asyncTransaction, endCallback));
    }

    void executeForced(Transaction transaction, boolean restoreFromForcedState) {
        forcedState = true;
        cancelCurrentAction();
        execute(new CoordinatedTransaction(transaction));
        this.restoreFromForcedState = restoreFromForcedState;
    }

    private void execute(CoordinatedAction action) {
        stack.add(action);
        executeNext();
    }

    @SuppressWarnings("WeakerAccess")
    void onExecuted(CoordinatedAction action) {
        isExecuting = false;
        if (action == currentAction) {
            currentAction = null;
        }
        executeNext();
    }

    private void cancelCurrentAction() {
        if (currentAction != null) {
            currentAction.cancel(forcedState);
            currentAction = null;
        }
    }

    private void executeNext() {
        if (isExecuting) return;
        cancelCurrentAction();
        if (stack.isEmpty()) return;
        CoordinatedAction action = currentAction = stack.removeFirst();
        if (action != null) {
            isExecuting = true;
            action.execute();
            if (forcedState) {
                action.cancel(true);
            }
        } else if (restoreFromForcedState) {
            forcedState = false;
            restoreFromForcedState = false;
        }
    }

    interface CoordinatedAction extends Cancellable {
        void execute();
    }

    class CoordinatedTransaction implements CoordinatedAction {

        final Transaction transaction;

        CoordinatedTransaction(Transaction transaction) {
            this.transaction = transaction;
        }

        @Override
        public void execute() {
            transaction.execute(Coordinator.this);
            onExecuted(this);
        }

        @Override
        public void cancel(boolean force) {

        }
    }

    class CoordinatedAsyncTransaction implements CoordinatedAction {

        final AsyncTransaction asyncTransaction;
        final Runnable endCallback;

        private Cancellable cancellable;

        CoordinatedAsyncTransaction(AsyncTransaction asyncTransaction, Runnable endAction) {
            this.asyncTransaction = asyncTransaction;
            this.endCallback = endAction;
        }

        @Override
        public void execute() {
            cancellable = asyncTransaction.execute(() -> {
                endCallback.run();
                onExecuted(this);
            });
        }

        @Override
        public void cancel(boolean force) {
            Cancellable local = cancellable;
            if (local != null) {
                local.cancel(force);
                cancellable = null;
            }
        }
    }

    interface Cancellable {
        void cancel(boolean force);
    }

}
