package viewstack.utils;


import viewstack.ViewComponent;

public interface StackChangedListener {

    void onComponentAdded(ViewComponent component);

    void onComponentRemoved(ViewComponent component);

}
