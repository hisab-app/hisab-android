package io.github.zkhan93.hisab.model.callback;

import android.os.Parcelable;

import io.github.zkhan93.hisab.model.ExpenseItem;

/**
 * Created by Zeeshan Khan on 7/3/2016.
 */
public interface ExpenseItemActionClbk {
    void delete(String expenseId);

    void update(ExpenseItem expense);
}
