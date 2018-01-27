package viewstack.contract.animation;


import android.view.ViewGroup;

import viewstack.ViewComponent;

public interface AnimationContract {

    Object animate(ViewComponent from, ViewComponent to, ViewGroup container, boolean stackIncreased);

}
