package io.github.zkhan93.hisab.model.callback;

import io.github.zkhan93.hisab.model.ExpenseItem;

/**
 * Created by Zeeshan Khan on 7/3/2016.
 */
public interface ExpenseItemClbk {
    void deleteExpense(String expenseId);

    void showEditUi(ExpenseItem expense);

    void update(ExpenseItem expense);
}
