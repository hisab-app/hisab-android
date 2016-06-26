package io.github.zkhan93.hisab.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zeeshan Khan on 6/25/2016.
 */
public class Group implements Parcelable {
    String id;
    String name;
    String moderatorId;
    List<String> membersIds;
    long createdOn;
    public Group() {
    }

    public Group(String id, String name, String moderatorId, List<String> membersIds, long
            createdOn) {
        this.id = id;
        this.name = name;
        this.moderatorId = moderatorId;
        this.membersIds = membersIds;
        this.createdOn = createdOn;
    }

    public Group(String name) {
        this.name = name;
    }

    public Group(Parcel parcel) {
        id = parcel.readString();
        name = parcel.readString();
        moderatorId = parcel.readString();
        membersIds = new ArrayList<>();
        parcel.readList(membersIds, User.class.getClassLoader());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModeratorId() {
        return moderatorId;
    }

    public void setModeratorId(String moderatorId) {
        this.moderatorId = moderatorId;
    }

    public List<String> getMembersIds() {
        return membersIds;
    }

    public void setMembersIds(List<String> membersIds) {
        this.membersIds = membersIds;
    }

    public long getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(long createdOn) {
        this.createdOn = createdOn;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flag) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeString(moderatorId);
        parcel.writeList(membersIds);
    }

    public static Creator<Group> CREATOR = new Creator<Group>() {
        @Override
        public Group createFromParcel(Parcel parcel) {
            return new Group(parcel);
        }

        @Override
        public Group[] newArray(int i) {
            return new Group[i];
        }
    };

    @Override
    public String toString() {
        return "Group{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", moderator=" + moderatorId +
                ", membersIds=" + membersIds +
                '}';
    }
}
