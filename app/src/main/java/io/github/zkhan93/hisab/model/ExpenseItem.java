package io.github.zkhan93.hisab.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Zeeshan Khan on 6/25/2016.
 */
public class ExpenseItem implements Parcelable {
    String id;
    String groupId;
    String authorId;
    String description;
    float amount;
    long createdOn;

    public ExpenseItem() {
    }

    public ExpenseItem(String id, String groupId, String authorId, String description, float
            amount, long
                               createdOn) {
        this.id = id;
        this.groupId = groupId;
        this.authorId = authorId;
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
        authorId = parcel.readString();
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

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
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
                ", authorId='" + authorId + '\'' +
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
        parcel.writeString(authorId);
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
