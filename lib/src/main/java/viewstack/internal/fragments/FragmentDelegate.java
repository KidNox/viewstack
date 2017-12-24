package viewstack.internal.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import viewstack.internal.ComponentDescriptor;

public interface FragmentDelegate {

    void startActivityForResult(ComponentDescriptor descriptor, Intent intent, int requestCode);

    void startActivityForResult(ComponentDescriptor descriptor, Intent intent, int requestCode, Bundle options);

    void requestPermissions(ComponentDescriptor descriptor, @NonNull String[] permissions, int requestCode);

}
