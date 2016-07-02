package io.github.zkhan93.hisab.model.ui;

import android.os.Parcel;

import io.github.zkhan93.hisab.model.User;

/**
 * Created by Zeeshan Khan on 7/3/2016.
 */
public class ExUser extends User {

    boolean checked;

    public ExUser(User user) {
        super(user.getName(), user.getEmail(), user.getId());
        checked = false;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    private ExUser(Parcel parcel) {
        super(parcel);
        boolean[] temp = new boolean[1];
        parcel.readBooleanArray(temp);
        checked = temp[0];
    }

    public static Creator<ExUser> CREATOR = new Creator<ExUser>() {
        @Override
        public ExUser createFromParcel(Parcel parcel) {
            return new ExUser(parcel);
        }

        @Override
        public ExUser[] newArray(int i) {
            return new ExUser[i];
        }
    };

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeBooleanArray(new boolean[]{
                checked
        });
    }
}
