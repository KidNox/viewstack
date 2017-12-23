package viewstack.internal;


import android.animation.Animator;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

import viewstack.ViewComponent;
import viewstack.contract.AnimationDelegate;

class AnimationHandler {

    static TransactionManager.AsyncTransaction animatedTransaction(ViewComponent current, ViewComponent next, boolean stackIncreased, ViewGroup container) {
        return endCallback -> {
            AnimationDelegate detach = current.getAnimationDelegate();
            AnimationDelegate attach = next.getAnimationDelegate();
            Animator detachAnimation = null;
            Animator attachAnimation = null;
            if (detach != null) {
                detachAnimation = detach.detachAnimation(current, next, container, stackIncreased);
            }
            if (attach != null) {
                attachAnimation = attach.attachAnimation(next, current, container, stackIncreased);
            }
            return new AnimationHandler(attachAnimation, detachAnimation).animate(endCallback);
        };
    }


    @SuppressWarnings("WeakerAccess") final @Nullable Animator first, second;

    private AnimationHandler(@Nullable Animator first, @Nullable Animator second) {
        this.first = first;
        this.second = second;
    }

    private Coordinator.Cancellable animate(Runnable endCallback) {
        final Callback callback = getAnimationEndCallback(endCallback);
        if (first != null) {
            validateAnimation(first);
            attachListener(first, 1, callback);
            first.start();
        } else {
            callback.onAnimationEnd(1);
        }
        if (second != null) {
            validateAnimation(second);
            attachListener(second, 2, callback);
            second.start();
        } else {
            callback.onAnimationEnd(2);
        }
        return force -> {
            if (force) {
                callback.onForceCancel();
            }
            if (first != null) {
                first.end();
            }
            if (second != null) {
                second.end();
            }
        };
    }

    private static Callback getAnimationEndCallback(Runnable endCallback) {
        final Runnable callback = MutableRunnable.wrap(endCallback);
        return new Callback() {
            boolean firstEnded;
            boolean secondEnded;

            @Override
            public void onAnimationEnd(int animationIndex) {
                firstEnded = firstEnded || animationIndex == 1;
                secondEnded = secondEnded || animationIndex == 2;
                if (firstEnded && secondEnded) {
                    callback.run();
                }
            }

            @Override
            public void onForceCancel() {
                callback.run();
            }
        };
    }

    private static void validateAnimation(Animator animator) {
        if (animator.getDuration() == Animator.DURATION_INFINITE) {
            throw new IllegalArgumentException("invalid animator: DURATION_INFINITE");
        }
    }

    private static void attachListener(Animator anim, int index, Callback callback) {
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) { }

            @Override
            public void onAnimationEnd(Animator animation) {
                callback.onAnimationEnd(index);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                callback.onAnimationEnd(index);
            }

            @Override
            public void onAnimationRepeat(Animator animation) { }
        });
    }

    private interface Callback {
        void onAnimationEnd(int animationIndex);

        void onForceCancel();
    }

    static class MutableRunnable implements Runnable {

        static MutableRunnable wrap(Runnable runnable) {
            MutableRunnable result = new MutableRunnable();
            result.runnable = runnable;
            return result;
        }

        private Runnable runnable;

        @Override
        public void run() {
            Runnable local = runnable;
            if (local != null) {
                runnable = null;
                local.run();
            }
        }

    }
}
