package viewstack.internal;


import android.os.Parcel;
import android.os.Parcelable;

public class ComponentDescriptor implements Parcelable {

    private final int stackId;
    private final int componentId;

    ComponentDescriptor(int stackId, int componentId) {
        this.stackId = stackId;
        this.componentId = componentId;
    }

    int getStackId() {
        return stackId;
    }

    int getComponentId() {
        return componentId;
    }

    @SuppressWarnings("WeakerAccess")
    ComponentDescriptor(Parcel in) {
        stackId = in.readInt();
        componentId = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(stackId);
        dest.writeInt(componentId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ComponentDescriptor> CREATOR = new Creator<ComponentDescriptor>() {
        @Override
        public ComponentDescriptor createFromParcel(Parcel in) {
            return new ComponentDescriptor(in);
        }

        @Override
        public ComponentDescriptor[] newArray(int size) {
            return new ComponentDescriptor[size];
        }
    };
}
