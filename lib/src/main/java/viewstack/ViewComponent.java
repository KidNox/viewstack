package viewstack;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import viewstack.contract.AnimationDelegate;
import viewstack.contract.Component;
import viewstack.contract.ViewInterface;
import viewstack.internal.ViewHolder;

public abstract class ViewComponent extends ViewHolder implements Component, ViewInterface {

    @Override
    public void onCreate() { }

    @Override
    public void onAttach() { }

    @Override
    public abstract View render(LayoutInflater inflater, ViewGroup parent);

    @Override
    public void afterRender(View view) { }

    @Override
    public void onDetach(boolean removedFromStack) { }

    @Override
    public void onDestroyView() { }

    @Override
    public void onDestroy() { }

    @Override
    @Nullable
    public AnimationDelegate getAnimationDelegate() {
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { }

    @Override
    protected void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) { }
}
