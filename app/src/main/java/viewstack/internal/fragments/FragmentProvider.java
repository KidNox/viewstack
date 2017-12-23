package viewstack.internal.fragments;


import android.app.Activity;
import android.support.v4.app.FragmentActivity;

public final class FragmentProvider {

    private static final String TAG = FragmentDelegate.class.getSimpleName();

    public static FragmentDelegate getOrCreateInterface(FragmentCallbacks callbacks, Activity activity) {
        boolean isSupport = false;
        try {
            isSupport = activity instanceof FragmentActivity;
        } catch (Exception ignored) {
            //class not found exception
        }
        if (isSupport) {
            return getOrCreateSupportFragment(callbacks, activity);
        } else {
            return getOrCreateFragment(callbacks, activity);
        }
    }

    private static FragmentDelegate getOrCreateFragment(FragmentCallbacks callbacks, Activity activity) {
        StateHolderFragment fragment = (StateHolderFragment) activity.getFragmentManager().findFragmentByTag(TAG);
        if (fragment == null) {
            fragment = new StateHolderFragment();
            activity.getFragmentManager().beginTransaction().add(fragment, TAG).commit();
        }
        fragment.setCallbacks(callbacks);
        return fragment;
    }

    private static FragmentDelegate getOrCreateSupportFragment(FragmentCallbacks callbacks, Activity supportActivity) {
        FragmentActivity activity = (FragmentActivity) supportActivity;
        StateHolderSupportFragment fragment = (StateHolderSupportFragment) activity.getSupportFragmentManager().findFragmentByTag(TAG);
        if (fragment == null) {
            fragment = new StateHolderSupportFragment();
            activity.getSupportFragmentManager().beginTransaction().add(fragment, TAG).commitNow();
        }
        fragment.setCallbacks(callbacks);
        return fragment;
    }

}
