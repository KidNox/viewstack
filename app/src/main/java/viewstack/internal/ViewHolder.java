package viewstack.internal;


import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.view.View;


public abstract class ViewHolder extends ContextHolder {

    private View view;

    private SparseArray<Parcelable> viewHierarchyState;

    @Nullable
    public final View getView() {
        return view;
    }

    void setView(View view) {
        this.view = view;
        if(viewHierarchyState != null) {
            view.restoreHierarchyState(viewHierarchyState);
        }
    }

    void removeViewReference() {
        onDestroyView();
        view = null;
    }

    public void onDestroyView() { }

    void prepareViewState() {
        if(view == null || !saveViewState()) return;

        viewHierarchyState = new SparseArray<>();
        view.saveHierarchyState(viewHierarchyState);
    }

    final void setViewState(SparseArray<Parcelable> viewHierarchyState) {
        this.viewHierarchyState = viewHierarchyState;
    }

    final SparseArray<Parcelable> getViewState() {
        return viewHierarchyState;
    }

    protected boolean saveViewState() {
        return true;
    }

}
