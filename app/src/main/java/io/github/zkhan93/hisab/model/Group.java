package io.github.zkhan93.hisab.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by Zeeshan Khan on 6/25/2016.
 */
public class Group implements Parcelable {
    String id;
    String name;
    User moderator;
    List<User> members;

    public Group() {
    }

    public Group(List<User> members, String name, String id, User moderator) {
        this.members = members;
        this.name = name;
        this.id = id;
        this.moderator = moderator;
    }

    public Group(Parcel parcel){

    }

    public List<User> getMembers() {
        return members;
    }

    public void setMembers(List<User> members) {
        this.members = members;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getModerator() {
        return moderator;
    }

    public void setModerator(User moderator) {
        this.moderator = moderator;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flag) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeParcelable(moderator,flag);
        parcel.writeTypedList(members);
    }

    public static Creator<Group> CREATOR=new Creator<Group>() {
        @Override
        public Group createFromParcel(Parcel parcel) {
            return new Group(parcel);
        }

        @Override
        public Group[] newArray(int i) {
            return new Group[i];
        }
    };
}
