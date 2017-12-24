package viewstack.internal.fragments;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;

import viewstack.internal.ComponentDescriptor;

public class StateHolderSupportFragment extends Fragment implements FragmentDelegate {

    private final ActivityRequestHelper activityRequestHelper = new ActivityRequestHelper();
    private FragmentCallbacks callbacks;

    void setCallbacks(FragmentCallbacks callbacks) {
        this.callbacks = callbacks;
    }

    public void startActivityForResult(ComponentDescriptor componentDescriptor, Intent intent, int requestCode) {
        activityRequestHelper.activityRequests().put(requestCode, componentDescriptor);
        startActivityForResult(intent, requestCode);
    }


    public void startActivityForResult(ComponentDescriptor componentDescriptor, Intent intent, int requestCode, Bundle options) {
        activityRequestHelper.activityRequests().put(requestCode, componentDescriptor);
        startActivityForResult(intent, requestCode, options);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void requestPermissions(ComponentDescriptor componentDescriptor, @NonNull String[] permissions, int requestCode) {
        activityRequestHelper.permissionRequests().put(requestCode, componentDescriptor);
        requestPermissions(permissions, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        ComponentDescriptor descriptor = activityRequestHelper.removeActivityRequest(requestCode);
        callbacks.onActivityResult(descriptor, requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        ComponentDescriptor descriptor = activityRequestHelper.removePermissionRequest(requestCode);
        callbacks.onRequestPermissionsResult(descriptor, requestCode, permissions, grantResults);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            activityRequestHelper.restoreState(savedInstanceState);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        activityRequestHelper.saveState(outState);
    }
}
