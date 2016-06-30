package io.github.zkhan93.hisab.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Zeeshan Khan on 6/25/2016.
 */
public class ExpenseItem implements Parcelable {
    String id;
    String groupId;
    User owner;
    String description;
    float amount;
    long createdOn;

    public ExpenseItem() {
    }

    public ExpenseItem(String id, String groupId, User owner, String description, float
            amount, long
                               createdOn) {
        this.id = id;
        this.groupId = groupId;
        this.owner = owner;
        this.description = description;
        this.amount = amount;
        this.createdOn = createdOn;
    }

    public ExpenseItem(String description, float amount) {
        this.description = description;
        this.amount = amount;
    }

    public ExpenseItem(Parcel parcel) {
        id = parcel.readString();
        groupId = parcel.readString();
        owner = parcel.readParcelable(User.class.getClassLoader());
        description = parcel.readString();
        amount = parcel.readFloat();
        createdOn = parcel.readLong();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
    }

    @Override
    public String toString() {
        return "ExpenseItem{" +
                "id='" + id + '\'' +
                ", groupId='" + groupId + '\'' +
                ", owner='" + owner + '\'' +
                ", description='" + description + '\'' +
                ", amount=" + amount +
                ", createdOn=" + createdOn +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(groupId);
        parcel.writeParcelable(owner,i);
        parcel.writeString(description);
        parcel.writeFloat(amount);
        parcel.writeLong(createdOn);
    }

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
}
