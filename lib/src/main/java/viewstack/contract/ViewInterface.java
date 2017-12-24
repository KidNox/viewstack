package viewstack.contract;


import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public interface ViewInterface {

    View render(LayoutInflater inflater, ViewGroup parent);

    void afterRender(View view);

    void onDestroyView();

    @Nullable
    AnimationDelegate getAnimationDelegate();

}
