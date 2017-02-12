package io.github.zkhan93.hisab.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Zeeshan Khan on 6/25/2016.
 */
@IgnoreExtraProperties
public class ExpenseItem implements Parcelable {
    public static final Creator<ExpenseItem> CREATOR = new Creator<ExpenseItem>() {
        @Override
        public ExpenseItem createFromParcel(Parcel parcel) {
            return new ExpenseItem(parcel);
        }

        @Override
        public ExpenseItem[] newArray(int i) {
            return new ExpenseItem[i];
        }
    };
    public static Comparator<ExpenseItem> BY_TIME_DESC = new Comparator<ExpenseItem>() {
        @Override
        public int compare(ExpenseItem ei1, ExpenseItem ei2) {
            return (int) (ei1.getCreatedOn() - ei2.getCreatedOn());
        }
    };
    @Exclude
    String id;
    String groupId;
    User owner;
    /**
     * values from {@link ITEM_TYPE}
     * represents the the type of expense item this is
     */
    int itemType;
    String description;
    float amount;
    long createdOn, updatedOn;
    User with;
    /**
     * paid or received
     */
    int shareType;

    public ExpenseItem() {
    }

    public ExpenseItem(String id, int itemType, String groupId, User owner, String description,
                       float amount, long createdOn, long updatedOn, User with, int shareType) {
        this(description, amount, with, shareType);
        this.id = id;
        this.groupId = groupId;
        this.owner = owner;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        this.itemType = itemType;
    }

    public ExpenseItem(String description, float amount, User with, int shareType) {
        this(description, amount);
        this.itemType = ITEM_TYPE.PAID_RECEIVED;
        this.with = with;
        this.shareType = shareType;
    }

    public ExpenseItem(String description, float amount) {
        this.description = description;
        this.amount = amount;
        this.itemType = ITEM_TYPE.SHARED;
    }

    public ExpenseItem(Parcel parcel) {
        id = parcel.readString();
        itemType = parcel.readInt();
        groupId = parcel.readString();
        owner = parcel.readParcelable(User.class.getClassLoader());
        description = parcel.readString();
        amount = parcel.readFloat();
        createdOn = parcel.readLong();
        updatedOn = parcel.readLong();
        with = parcel.readParcelable(User.class.getClassLoader());
        shareType = parcel.readInt();
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public long getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(long createdOn) {
        this.createdOn = createdOn;
        setUpdatedOn(createdOn);
    }

    public int getShareType() {
        return shareType;
    }

    public void setShareType(int shareType) {
        this.shareType = shareType;
    }

    public User getWith() {
        return with;
    }

    public void setWith(User with) {
        this.with = with;
    }

    public long getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(long updatedOn) {
        this.updatedOn = updatedOn;
    }

    @Override
    public String toString() {
        return "ExpenseItem{" +
                "id='" + id + '\'' +
                ", groupId='" + groupId + '\'' +
                ", owner=" + owner +
                ", itemType=" + itemType +
                ", description='" + description + '\'' +
                ", amount=" + amount +
                ", createdOn=" + createdOn +
                ", updatedOn=" + updatedOn +
                ", with=" + with +
                ", shareType=" + shareType +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeInt(itemType);
        parcel.writeString(groupId);
        parcel.writeParcelable(owner, i);
        parcel.writeString(description);
        parcel.writeFloat(amount);
        parcel.writeLong(createdOn);
        parcel.writeLong(updatedOn);
        parcel.writeParcelable(with, i);
        parcel.writeInt(shareType);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("itemType", itemType);
        map.put("owner", owner.toMap());
        map.put("description", description);
        map.put("amount", (double) amount);
        map.put("createdOn", createdOn);
        map.put("updatedOn", updatedOn);
        if (itemType == ITEM_TYPE.PAID_RECEIVED) {
            map.put("with", with);
            map.put("shareType", shareType);
        }
        return map;
    }

    /**
     * SHARED = 0;
     * PAID_RECEIVED = 1;
     */
    public interface ITEM_TYPE {
        int SHARED = 0;
        int PAID_RECEIVED = 1;
    }

    public interface SHARE_TYPE {
        int PAID = 0;
        int RECEIVED = 1;
    }
}
