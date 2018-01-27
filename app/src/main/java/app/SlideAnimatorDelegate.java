package app;


import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.ViewGroup;

import viewstack.ViewComponent;
import viewstack.contract.animation.AnimatorDelegate;

public class SlideAnimatorDelegate implements AnimatorDelegate {

    public static final int DEFAULT_ANIMATION_TIME = 600;

    @Override
    public Animator animate(ViewComponent current, ViewComponent next, ViewGroup container, boolean stackIncreased) {
        AnimatorSet animatorSet = new AnimatorSet();
        View from = current.getView();
        View to = next.getView();
        int width = container.getWidth();
        if (stackIncreased) {
            if (from != null) {
                animatorSet.play(ObjectAnimator.ofFloat(from, View.TRANSLATION_X, -width));
            }
            if (to != null) {
                animatorSet.play(ObjectAnimator.ofFloat(to, View.TRANSLATION_X, width, 0));
            }
        } else {
            if (from != null) {
                animatorSet.play(ObjectAnimator.ofFloat(from, View.TRANSLATION_X, width));
            }
            if (to != null) {
                float fromLeft = from != null ? from.getTranslationX() : 0;
                animatorSet.play(ObjectAnimator.ofFloat(to, View.TRANSLATION_X, fromLeft - width, 0));
            }
        }
        animatorSet.setDuration(DEFAULT_ANIMATION_TIME);
        return animatorSet;
    }

}
