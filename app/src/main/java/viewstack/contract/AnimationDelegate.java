package viewstack.contract;


import android.animation.Animator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

import viewstack.ViewComponent;

public interface AnimationDelegate {

    Animator attachAnimation(@NonNull ViewComponent self, @Nullable ViewComponent previous,
                             @NonNull ViewGroup container, boolean stackIncreased);

    Animator detachAnimation(@NonNull ViewComponent self, @Nullable ViewComponent next,
                             @NonNull ViewGroup container, boolean stackIncreased);

}
