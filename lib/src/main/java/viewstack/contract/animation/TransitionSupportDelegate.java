package viewstack.contract.animation;


import android.os.Build;
import android.view.ViewGroup;

import viewstack.ViewComponent;

public abstract class TransitionSupportDelegate implements AnimationContract {

    @Override
    public Object animate(ViewComponent from, ViewComponent to, ViewGroup container, boolean stackIncreased) {
        if (supportTransition()) {
            return getTransitionDelegate().animate(from, to, container, stackIncreased);
        } else {
            return getFallbackAnimationDelegate().animate(from, to, container, stackIncreased);
        }
    }

    public boolean supportTransition() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    protected abstract AnimationContract getTransitionDelegate();

    protected AnimationContract getFallbackAnimationDelegate() {
        return (AnimatorDelegate) (from, to, container, stackIncreased) -> null;
    }

}
