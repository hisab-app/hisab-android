package io.github.zkhan93.hisab.model.events;

import io.github.zkhan93.hisab.model.notification.LocalExpense;

/**
 * Created by zeeshan on 2/26/2017.
 */

public class ExpenseAddedEvent {
    private LocalExpense expense;

    public ExpenseAddedEvent(LocalExpense expense) {
        this.expense = expense;
    }

    public LocalExpense getExpense() {
        return expense;
    }

    public void setExpense(LocalExpense expense) {
        this.expense = expense;
    }

    @Override
    public String toString() {
        return "ExpenseAddedEvent{" +
                "expense=" + expense +
                '}';
    }
}
