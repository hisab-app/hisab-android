package io.github.zkhan93.hisab.model.ui;

import android.os.Parcel;

import io.github.zkhan93.hisab.model.Group;

/**
 * Created by Zeeshan Khan on 8/6/2016.
 */
public class ExGroup extends Group {
    private boolean selected;

    private ExGroup(Parcel parcel) {
        super(parcel);
        boolean[] temp = new boolean[1];
        parcel.readBooleanArray(temp);
        selected = temp[0];
    }

    public ExGroup(Group group) {
        super(group.getId(), group.getName(), group.getModerator(), group.getMembersIds(), group
                .getCreatedOn());
        selected = false;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public static Creator<ExGroup> CREATOR = new Creator<ExGroup>() {
        @Override
        public ExGroup createFromParcel(Parcel parcel) {
            return new ExGroup(parcel);
        }

        @Override
        public ExGroup[] newArray(int i) {
            return new ExGroup[i];
        }
    };

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeBooleanArray(new boolean[]{
                selected
        });
    }
}
