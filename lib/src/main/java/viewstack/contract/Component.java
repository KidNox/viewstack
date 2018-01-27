package viewstack.contract;


public interface Component {

    void onCreate();

    void onAttach();

    void onDetach(boolean removedFromStack);

    void onDestroy(boolean removedFromStack);

}
