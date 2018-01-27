package viewstack.contract.animation;


import android.animation.Animator;
import android.view.ViewGroup;

import viewstack.ViewComponent;

public interface AnimatorDelegate extends AnimationContract {

    @Override
    Animator animate(ViewComponent from, ViewComponent to, ViewGroup container, boolean stackIncreased);

}
