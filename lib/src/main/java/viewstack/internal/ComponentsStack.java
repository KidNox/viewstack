package viewstack.internal;


import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.view.View;
import android.view.ViewGroup;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import viewstack.ViewComponent;
import viewstack.utils.ComponentFinder;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class ComponentsStack {

    private final int stackId;

    private final LinkedList<ViewComponent> stack = new LinkedList<>();

    ComponentsStack(int stackId) {
        this.stackId = stackId;
    }

    void add(ViewComponent component) {
        stack.add(component);
    }

    ViewComponent getTop() {
        return stack.peekLast();
    }

    ViewComponent removeTop() {
        return stack.pollLast();
    }

    LinkedList<ViewComponent> clear() {
        LinkedList<ViewComponent> list = new LinkedList<>(stack);
        stack.clear();
        return list;
    }

    @SuppressWarnings("unchecked")
    public <T extends ViewComponent> T findUnique(Class<T> clazz) {
        ViewComponent result = null;
        for (ViewComponent component : stack) {
            if (component.getClass().equals(clazz)) {
                if (result != null) {
                    throw new IllegalStateException("find more than one instance of class " + clazz + " in stack " + stack);
                }
                result = component;
            }
        }
        return (T) result;
    }

    LinkedList<ViewComponent> removeAllFrom(ViewComponent fromExclusive) {
        LinkedList<ViewComponent> sublist = new LinkedList<>();
        boolean found = false;
        Iterator<ViewComponent> iterator = stack.iterator();
        while (iterator.hasNext()) {
            ViewComponent component = iterator.next();
            if (found) {
                iterator.remove();
                sublist.add(component);
            } else {
                if (component == fromExclusive) {
                    found = true;
                }
            }
        }
        return sublist;
    }

    @Nullable
    public ViewComponent find(ComponentFinder finder) {
        for (ViewComponent component : stack) {
            if (finder.isExpected(component)) return component;
        }
        return null;
    }

    public boolean contains(ViewComponent component) {
        return stack.contains(component);
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public int size() {
        return stack.size();
    }

    List<ViewComponent> internalList() {
        return stack;
    }

    static int idOf(ViewGroup container) {
        if (container.getId() == View.NO_ID) throw new IllegalArgumentException("illegal view id for ViewStack container: " + container);
        return container.getId();
    }

    int getId() {
        return stackId;
    }

    @Override
    public String toString() {
        return "Stack{id=" + stackId + ", components=" + stack + '}';
    }
}
