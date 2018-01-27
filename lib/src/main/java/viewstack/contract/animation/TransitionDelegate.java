package viewstack.contract.animation;


import android.transition.Transition;
import android.view.ViewGroup;

import viewstack.ViewComponent;

public interface TransitionDelegate extends AnimationContract {

    @Override
    Transition animate(ViewComponent from, ViewComponent to, ViewGroup container, boolean stackIncreased);

}
