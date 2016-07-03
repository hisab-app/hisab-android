package io.github.zkhan93.hisab.model.callback;

import io.github.zkhan93.hisab.model.ExpenseItem;

/**
 * Created by Zeeshan Khan on 7/4/2016.
 */
public interface ExpenseItemUiClbk {
    void showEditUi(ExpenseItem expense,ExpenseItemActionClbk actionClbk);
}
