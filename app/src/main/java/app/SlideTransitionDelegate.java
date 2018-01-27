package app;


import android.os.Build;
import android.support.annotation.RequiresApi;
import android.transition.Slide;
import android.transition.Transition;
import android.view.Gravity;
import android.view.ViewGroup;

import viewstack.ViewComponent;
import viewstack.contract.animation.TransitionDelegate;

public class SlideTransitionDelegate implements TransitionDelegate {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public Transition animate(ViewComponent from, ViewComponent to, ViewGroup container, boolean stackIncreased) {
        return new Slide(stackIncreased ? Gravity.START : Gravity.END);
    }
}
