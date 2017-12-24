package viewstack.internal.fragments;


import android.content.Intent;
import android.support.annotation.NonNull;

import viewstack.internal.ComponentDescriptor;

public interface FragmentCallbacks {

    void onActivityResult(ComponentDescriptor descriptor, int requestCode, int resultCode, Intent data);

    void onRequestPermissionsResult(ComponentDescriptor descriptor, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);

}
