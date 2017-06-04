package io.github.zkhan93.hisab.model.callback;

import java.util.List;
import java.util.Map;

import io.github.zkhan93.hisab.model.ExpenseItem;
import io.github.zkhan93.hisab.model.ui.ExExpenseItem;

/**
 * Created by Zeeshan Khan on 7/10/2016.
 */
public interface SummaryActionItemClbk {
    void archiveGrp(String groupId, List<ExExpenseItem> expenses);

    void moreInfo();
}
