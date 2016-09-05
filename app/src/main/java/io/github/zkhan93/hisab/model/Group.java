package io.github.zkhan93.hisab.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Zeeshan Khan on 6/25/2016.
 */
public class Group implements Parcelable {
    String id;
    String name;
    User moderator;
    List<String> membersIds;
    long createdOn;

    public Group() {
    }

    public Group(String id, String name, User moderator, List<String> membersIds, long
            createdOn) {
        this.id = id;
        this.name = name;
        this.moderator = moderator;
        this.membersIds = membersIds;
        this.createdOn = createdOn;
    }

    public Group(String name) {
        this.name = name;
    }

    public Group(Parcel parcel) {
        id = parcel.readString();
        name = parcel.readString();
        moderator = parcel.readParcelable(User.class.getClassLoader());
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

    public User getModerator() {
        return moderator;
    }

    public void setModerator(User moderator) {
        this.moderator = moderator;
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
        parcel.writeParcelable(moderator, flag);
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
                ", moderator=" + moderator +
                ", membersIds=" + membersIds +
                '}';
    }

    public static Comparator<Group> ALPHABETICAL = new Comparator<Group>() {
        @Override
        public int compare(Group g1, Group g2) {
            return g1.getName().compareTo(g2.getName());
        }
    };
    public static Comparator<Group> REVERSE_ALPHABETICAL = new Comparator<Group>() {
        @Override
        public int compare(Group g1, Group g2) {
            return g2.getName().compareTo(g1.getName());
        }
    };
    public static Comparator<Group> CHRONOLOGICAL = new Comparator<Group>() {
        @Override
        public int compare(Group g1, Group g2) {
            return Double.compare(g1.getCreatedOn(), g2.getCreatedOn());
        }
    };
    public static Comparator<Group> REVERSE_CHRONOLOGICAL = new Comparator<Group>() {
        @Override
        public int compare(Group g1, Group g2) {
            return Double.compare(g2.getCreatedOn(), g1.getCreatedOn());
        }
    };

    public interface SORT_TYPE {
        int CHRONOLOGICAL = 0;
        int REVERSE_CHRONOLOGICAL = 1;
        int ALPHABETICAL = 2;
        int REVERSE_ALPHABETICAL = 3;
    }
}
