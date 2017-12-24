package viewstack.internal;


import android.app.Activity;
import android.content.Intent;
import android.content.MutableContextWrapper;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import viewstack.ViewStack;

class ContextHolder extends MutableContextWrapper {

    private int stackId;
    private int componentId;

    private Bundle arguments;

    private Handler handler;

    public ContextHolder() {
        super(null);
    }

    @NonNull
    public final Bundle getArguments() {
        if(arguments == null) {
            arguments = new Bundle();
        }
        return arguments;
    }

    public final boolean hasArguments() {
        return arguments != null;
    }

    void setArguments(Bundle arguments) {
        this.arguments = arguments;
    }

    public void goBack() {
        if(hasContext()) {
            getActivity().onBackPressed();
        }
    }

    //nullable
    public final ViewStack getViewStack() {
        return viewStacksHolder().getViewStack(stackId);
    }

    private ViewStacksHolder viewStacksHolder() {
        if(!hasContext()) throw new IllegalStateException();
        return ViewStacksHolder.getOrCreate(getActivity());
    }

    public final boolean hasContext() {
        return getBaseContext() != null;
    }

    //nullable
    public final Activity getActivity() {
        return (Activity) getBaseContext();
    }

    void setStackId(int stackId) {
        this.stackId = stackId;
    }

    void setComponentId(int componentId) {
        this.componentId = componentId;
    }

    int getComponentId() {
        return componentId;
    }

    ComponentDescriptor descriptor() {
        return new ComponentDescriptor(stackId, componentId);
    }

    public void startActivityForResult(Intent intent, int requestCode) {
        viewStacksHolder().getFragmentInterface().startActivityForResult(descriptor(), intent, requestCode);
    }

    public void startActivityForResult(Intent intent, int requestCode, @Nullable Bundle options) {
        viewStacksHolder().getFragmentInterface().startActivityForResult(descriptor(), intent, requestCode, options);
    }

    public void requestPermissions(@NonNull String[] permissions, int requestCode) {
        viewStacksHolder().getFragmentInterface().requestPermissions(descriptor(), permissions, requestCode);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) { }

    protected void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) { }

    public void post(Runnable action) {
        if(handler == null) {
            handler = new Handler(Looper.getMainLooper());
        }
        handler.post(action);
    }

    public void postDelayed(Runnable action, int delayMillis) {
        if(handler == null) {
            handler = new Handler(Looper.getMainLooper());
        }
        handler.postDelayed(action, delayMillis);
    }

    public void removeDelayedActions() {
        if(handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}
