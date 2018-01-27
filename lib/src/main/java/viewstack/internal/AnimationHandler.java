package viewstack.internal;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;

import viewstack.ViewComponent;
import viewstack.contract.animation.AnimationContract;
import viewstack.contract.animation.AnimatorDelegate;
import viewstack.contract.animation.TransitionDelegate;
import viewstack.contract.animation.TransitionSupportDelegate;
import viewstack.utils.TransitionListenerAdapter;

//TODO transition first api
abstract class AnimationHandler {

    static TransactionManager.AsyncTransaction animatedTransaction(ViewComponent from, ViewComponent to, ViewGroup container,
                                                                   boolean stackIncreased,
                                                                   @Nullable AnimationContract defaultAnimationContract) {
        return endCallback -> {
            AnimationContract delegate = to.getAnimationContract();
            if (delegate == null) {
                delegate = defaultAnimationContract;
            }
            AnimationHandler animationHandler;
            if (delegate == null) {
                animationHandler = new StubAnimationHandler();
            } else if (delegate instanceof TransitionSupportDelegate) {
                animationHandler = create((TransitionSupportDelegate) delegate, from, to, container, stackIncreased);
            } else if (delegate instanceof TransitionDelegate) {
                animationHandler = TransitionHandler.create((TransitionDelegate) delegate, from, to, container, stackIncreased);
            } else if (delegate instanceof AnimatorDelegate) {
                animationHandler = AnimatorHandler.create((AnimatorDelegate) delegate, from, to, container, stackIncreased);
            } else {
                throw new IllegalArgumentException("unsupported animation type " + delegate + ", default " + defaultAnimationContract);
            }
            return animationHandler.animate(endCallback);
        };
    }


    abstract Coordinator.Cancellable animate(Runnable endCallback);

    private static class StubAnimationHandler extends AnimationHandler {

        @Override
        Coordinator.Cancellable animate(Runnable endCallback) {
            endCallback.run();
            return () -> {};
        }
    }

    private static class AnimatorHandler extends AnimationHandler {
        static AnimationHandler create(AnimatorDelegate delegate, ViewComponent from, ViewComponent to, ViewGroup container,
                                       boolean stackIncreased) {
            return new AnimatorHandler(delegate.animate(from, to, container, stackIncreased));
        }

        final Animator animator;

        private AnimatorHandler(Animator animator) {
            this.animator = animator;
        }

        @Override
        Coordinator.Cancellable animate(Runnable endCallback) {
            final Callback callback = getAnimationEndCallback(endCallback);
            attachListener(animator, callback);
            animator.start();
            return () -> {
                animator.end();
                callback.onForceCancel();
            };
        }

        private static void attachListener(Animator anim, Callback callback) {
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    callback.onAnimationEnd();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    callback.onAnimationEnd();
                }
            });
        }
    }

    static AnimationHandler create(TransitionSupportDelegate delegate, ViewComponent from, ViewComponent to, ViewGroup container,
                                   boolean stackIncreased) {
        if(delegate.supportTransition()) {
            return new TransitionHandler((Transition) delegate.animate(from, to, container, stackIncreased), container);
        } else {
            Animator animator = (Animator) delegate.animate(from, to, container, stackIncreased);
            if(animator == null) {
                return new StubAnimationHandler();
            } else {
                return new AnimatorHandler(animator);
            }
        }
    }

    private static class TransitionHandler extends AnimationHandler {
        static AnimationHandler create(TransitionDelegate delegate, ViewComponent from, ViewComponent to, ViewGroup container,
                                       boolean stackIncreased) {
            return new TransitionHandler(delegate.animate(from, to, container, stackIncreased), container);
        }

        final Transition transition;
        final WeakReference<ViewGroup> containerRef;

        private TransitionHandler(Transition transition, ViewGroup container) {
            this.transition = transition;
            this.containerRef = new WeakReference<>(container);
        }

        @SuppressLint("NewApi")
        @Override
        Coordinator.Cancellable animate(Runnable endCallback) {
            final Callback callback = getAnimationEndCallback(endCallback);
            attachListener(transition, callback);
            ViewGroup container = containerRef.get();
            TransitionManager.beginDelayedTransition(container, transition);
            endCallback.run();
            return getCancelable(containerRef, callback);
        }

        private static Coordinator.Cancellable getCancelable(WeakReference<ViewGroup> containerRef, Callback callback) {
            return () -> {
                /*ViewGroup container = containerRef.get();
                if(container != null) {
                    TransitionManager.endTransitions(container);
                }*/
            };
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        private static void attachListener(Transition transition, Callback callback) {
            transition.addListener(new TransitionListenerAdapter() {
                @Override
                public void onTransitionEnd(Transition transition) {

                }

                @Override
                public void onTransitionCancel(Transition transition) {

                }
            });
        }
    }

    private static Callback getAnimationEndCallback(Runnable endCallback) {
        final Runnable callback = MutableRunnable.wrap(endCallback);
        return new Callback() {
            @Override
            public void onAnimationEnd() {
                callback.run();
            }

            @Override
            public void onForceCancel() {
                callback.run();
            }
        };
    }

    private interface Callback {
        void onAnimationEnd();

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
