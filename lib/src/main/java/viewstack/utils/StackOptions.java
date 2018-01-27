package viewstack.utils;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import viewstack.ViewComponent;
import viewstack.contract.animation.AnimationContract;

public final class StackOptions {

    private StackChangedListener stackChangedListener;
    private AnimationContract defaultAnimationContract;

    private boolean ignoreBackPressedOnTransaction;//TODO add option for delay nextTransaction when current is executing

    public StackOptions withStackChangedListener(StackChangedListener stackChangedListener) {
        this.stackChangedListener = stackChangedListener;
        return this;
    }

    public StackOptions withDefaultAnimationDelegate(AnimationContract animationContract) {
        this.defaultAnimationContract = animationContract;
        return this;
    }

    public StackOptions ignoreBackPressedOnTransaction() {
        ignoreBackPressedOnTransaction = true;
        return this;
    }

    @NonNull
    public StackChangedListener getStackChangedListener() {
        return stackChangedListener == null ? stackChangedListenerStub() : stackChangedListener;
    }

    @Nullable
    public AnimationContract getDefaultAnimationContract() {
        return defaultAnimationContract;
    }

    public boolean isIgnoreBackPressedOnTransaction() {
        return ignoreBackPressedOnTransaction;
    }

    private static StackChangedListener stackChangedListenerStub() {
        return new StackChangedListener() {
            @Override
            public void onComponentAdded(ViewComponent component) { }

            @Override
            public void onComponentRemoved(ViewComponent component) { }
        };
    }
}
