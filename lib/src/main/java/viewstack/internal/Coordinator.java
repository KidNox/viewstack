package viewstack.internal;

import java.util.LinkedList;

import viewstack.internal.TransactionManager.AsyncTransaction;
import viewstack.internal.TransactionManager.Transaction;


public final class Coordinator {

    private final LinkedList<CoordinatedAction> stack = new LinkedList<>();

    private CoordinatedAction currentAction;
    private boolean forcedState;
    private boolean restoreFromForcedState;

    public void execute(Transaction transaction) {
        execute(new CoordinatedTransaction(transaction));
    }

    void execute(AsyncTransaction asyncTransaction, Runnable endCallback) {
        execute(new CoordinatedAsyncTransaction(asyncTransaction, endCallback));
    }

    void executeForced(Transaction transaction, boolean restoreFromForcedState) {
        this.forcedState = true;
        this.restoreFromForcedState = restoreFromForcedState;
        execute(new CoordinatedTransaction(transaction));
    }

    private void execute(CoordinatedAction action) {
        stack.add(action);
        executeNext();
    }

    @SuppressWarnings("WeakerAccess")
    void onExecuted(CoordinatedAction action) {
        if (action == currentAction) {
            currentAction = null;
        }
    }

    private void executeNext() {
        if (currentAction != null) {
            currentAction.cancel();
        }
        CoordinatedAction action = currentAction = stack.poll();
        if (action != null) {
            action.execute();
            if (forcedState) {
                action.cancel();
            }
        }
        if (restoreFromForcedState && stack.isEmpty()) {
            forcedState = false;
            restoreFromForcedState = false;
        }
    }

    public boolean isExecuting() {
        return currentAction != null;
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
        public void cancel() {
            onExecuted(this);
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
        public void cancel() {
            Cancellable local = cancellable;
            if (local != null) {
                local.cancel();
                cancellable = null;
                onExecuted(this);
            }
        }
    }

    interface Cancellable {
        void cancel();
    }

}
