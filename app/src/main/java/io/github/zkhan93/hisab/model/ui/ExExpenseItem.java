package io.github.zkhan93.hisab.model.ui;

import android.os.Parcel;
import android.os.Parcelable;

import io.github.zkhan93.hisab.model.ExpenseItem;

/**
 * Created by zeeshan on 4/6/17.
 */

public class ExExpenseItem extends ExpenseItem implements Parcelable {

    private boolean expanded;
    private boolean imageVisible;

    public static final Creator<ExExpenseItem> CREATOR = new Creator<ExExpenseItem>() {
        @Override
        public ExExpenseItem createFromParcel(Parcel parcel) {
            return new ExExpenseItem(parcel);
        }

        @Override
        public ExExpenseItem[] newArray(int i) {
            return new ExExpenseItem[i];
        }
    };

    public ExExpenseItem(ExpenseItem expenseItem) {
        super(expenseItem.getId(), expenseItem.getItemType(), expenseItem.getGroupId(),
                expenseItem.getOwner(), expenseItem.getDescription(), expenseItem.getAmount(),
                expenseItem.getImage(), expenseItem.getCreatedOn(), expenseItem.getUpdatedOn(),
                expenseItem.getWith(),
                expenseItem.getShareType());
        this.expanded = false;
        this.imageVisible = false;
    }

    public boolean isImageVisible() {
        return imageVisible;
    }

    public void setImageVisible(boolean imageVisible) {
        this.imageVisible = imageVisible;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public ExExpenseItem(Parcel parcel) {
        super(parcel);
        boolean[] arr = new boolean[2];
        parcel.readBooleanArray(arr);
        expanded = arr[0];
        imageVisible = arr[1];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeBooleanArray(new boolean[]{expanded, imageVisible});
    }
}
