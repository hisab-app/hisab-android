package io.github.zkhan93.hisab.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Zeeshan Khan on 6/25/2016.
 */
public class Group implements Parcelable {
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
    public static Comparator<Group> ALPHABETICAL = new Comparator<Group>() {
        @Override
        public int compare(Group g1, Group g2) {
            if (g1.isFavorite() && g2.isFavorite())
                return 0;
            if (g1.isFavorite())
                return -1;
            else if (g2.isFavorite())
                return 1;

            return g1.getName().compareTo(g2.getName());
        }
    };
    public static Comparator<Group> REVERSE_ALPHABETICAL = new Comparator<Group>() {
        @Override
        public int compare(Group g1, Group g2) {
            if (g1.isFavorite() && g2.isFavorite())
                return 0;
            if (g1.isFavorite())
                return -1;
            else if (g2.isFavorite())
                return 1;

            return g2.getName().compareTo(g1.getName());
        }
    };
    public static Comparator<Group> CHRONOLOGICAL = new Comparator<Group>() {
        @Override
        public int compare(Group g1, Group g2) {
            if (g1.isFavorite() && g2.isFavorite())
                return 0;
            if (g1.isFavorite())
                return -1;
            else if (g2.isFavorite())
                return 1;

            return Double.compare(g1.getCreatedOn(), g2.getCreatedOn());
        }
    };
    public static Comparator<Group> REVERSE_CHRONOLOGICAL = new Comparator<Group>() {
        @Override
        public int compare(Group g1, Group g2) {
            if (g1.isFavorite() && g2.isFavorite())
                return 0;
            if (g1.isFavorite())
                return -1;
            else if (g2.isFavorite())
                return 1;

            return Double.compare(g2.getCreatedOn(), g1.getCreatedOn());
        }
    };
    String id;
    String name;
    User moderator;
    int membersCount;
    boolean favorite;
    List<String> membersIds;
    long createdOn,updatedOn;
    long lastCheckedOn;

    public Group() {
    }

    public Group(String id, String name, User moderator, List<String> membersIds, long
            createdOn,long updatedOn, int membersCount, boolean favorite) {
        this.id = id;
        this.name = name;
        this.moderator = moderator;
        this.membersIds = membersIds;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        this.membersCount = membersCount;
        this.favorite = favorite;
    }

    public Group(String name) {
        this.name = name;
    }

    public Group(Parcel parcel) {
        id = parcel.readString();
        name = parcel.readString();
        moderator = parcel.readParcelable(User.class.getClassLoader());
        membersCount = parcel.readInt();
        membersIds = new ArrayList<>();
        createdOn = parcel.readLong();
        updatedOn = parcel.readLong();
        lastCheckedOn = parcel.readLong();
        parcel.readList(membersIds, User.class.getClassLoader());
        boolean[] bools = new boolean[1];
        parcel.readBooleanArray(bools);
        favorite = bools[0];
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
        setUpdatedOn(createdOn);
    }

    public int getMembersCount() {
        return membersCount;
    }

    public void setMembersCount(int membersCount) {
        this.membersCount = membersCount;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public long getLastCheckedOn() {
        return lastCheckedOn;
    }

    public void setLastCheckedOn(long lastCheckedOn) {
        this.lastCheckedOn = lastCheckedOn;
    }

    public long getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(long updatedOn) {
        this.updatedOn = updatedOn;
    }

    @Override
    public String toString() {
        return "Group{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", moderator=" + moderator +
                ", membersCount=" + membersCount +
                ", favorite=" + favorite +
                ", membersIds=" + membersIds +
                ", createdOn=" + createdOn +
                ", updatedOn=" + updatedOn +
                ", lastCheckedOn=" + lastCheckedOn +
                '}';
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
        parcel.writeInt(membersCount);
        parcel.writeList(membersIds);
        parcel.writeLong(createdOn);
        parcel.writeLong(updatedOn);
        parcel.writeLong(lastCheckedOn);
        parcel.writeBooleanArray(new boolean[]{favorite});
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("moderator", moderator.toMap());
        map.put("membersCount", membersCount);
        map.put("favorite", false);
        map.put("createdOn", createdOn);
        map.put("updatedOn", updatedOn);
        return map;
    }

    public interface SORT_TYPE {
        int CHRONOLOGICAL = 0;
        int REVERSE_CHRONOLOGICAL = 1;
        int ALPHABETICAL = 2;
        int REVERSE_ALPHABETICAL = 3;
    }
}
