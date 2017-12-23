package viewstack.internal.fragments;


import android.os.Bundle;
import android.util.SparseArray;

import viewstack.internal.ComponentDescriptor;


class ActivityRequestHelper {

    private SparseArray<ComponentDescriptor> activityRequests;
    private SparseArray<ComponentDescriptor> permissionRequests;

    SparseArray<ComponentDescriptor> activityRequests() {
        if (activityRequests == null) {
            activityRequests = new SparseArray<>();
        }
        return activityRequests;
    }

    SparseArray<ComponentDescriptor> permissionRequests() {
        if (permissionRequests == null) {
            permissionRequests = new SparseArray<>();
        }
        return permissionRequests;
    }

    ComponentDescriptor removeActivityRequest(int requestCode) {
        if (activityRequests == null) return null;
        ComponentDescriptor result = activityRequests.get(requestCode);
        activityRequests.remove(requestCode);
        return result;
    }

    ComponentDescriptor removePermissionRequest(int requestCode) {
        if (permissionRequests == null) return null;
        ComponentDescriptor result = permissionRequests.get(requestCode);
        permissionRequests.remove(requestCode);
        return result;
    }

    void saveState(Bundle outState) {
        if (activityRequests != null) {
            outState.putSparseParcelableArray("activityRequests", activityRequests);
        }
        if (permissionRequests != null) {
            outState.putSparseParcelableArray("permissionRequests", permissionRequests);
        }
    }

    void restoreState(Bundle state) {
        activityRequests = state.getSparseParcelableArray("activityRequests");
        permissionRequests = state.getSparseParcelableArray("permissionRequests");
    }

}
