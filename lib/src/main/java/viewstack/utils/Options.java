package viewstack.utils;


public final class Options {

    private StackChangedListener stackChangedListener;

    public Options withStackChangedListener(StackChangedListener stackChangedListener) {
        this.stackChangedListener = stackChangedListener;
        return this;
    }

    public StackChangedListener getStackChangedListener() {
        return stackChangedListener;
    }
}
