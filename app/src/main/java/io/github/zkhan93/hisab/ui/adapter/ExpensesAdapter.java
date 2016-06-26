package io.github.zkhan93.hisab.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.ExpenseItem;
import io.github.zkhan93.hisab.model.Group;
import io.github.zkhan93.hisab.model.viewholder.EmptyVH;
import io.github.zkhan93.hisab.model.viewholder.ExpenseItemVH;

/**
 * Created by Zeeshan Khan on 6/26/2016.
 */
public class ExpensesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    List<ExpenseItem> expenses;

    public ExpensesAdapter(List<ExpenseItem> expenses) {
        if (expenses != null)
            this.expenses = expenses;
        else
            this.expenses = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE.EMPTY:
                return new EmptyVH(inflater.inflate(R.layout.empty, parent, false));
            default:
                return new ExpenseItemVH(inflater.inflate(R.layout.expense_item, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE.NORMAL) {
            ((ExpenseItemVH) holder).setExpense(expenses.get(position));
        }
    }

    @Override
    public int getItemViewType(int position) {
        int count = expenses.size();
        if (count == 0)
            return TYPE.EMPTY;
        return TYPE.NORMAL;
    }

    @Override
    public int getItemCount() {
        int count = expenses.size();
        return count == 0 ? 1 : count;
    }

    public void setExpenses(List<ExpenseItem> expenses) {
        if (expenses != null && expenses.size() > 0) {
            this.expenses = expenses;
            notifyDataSetChanged();
        }
    }

    public void addExpense(ExpenseItem expense) {
        if (expense != null) {
            expenses.add(expense);
            notifyItemInserted(expenses.size());
        }
    }

    public void modifyExpense(ExpenseItem expense) {
        if (expense != null) {
            int index = 0;
            boolean found = false;
            for (ExpenseItem e : expenses) {
                if (e.getId().equals(expense.getId())) {
                    found = true;
                    break;
                }
                index += 1;
            }
            if (found) {
                expenses.set(index, expense);
                notifyItemChanged(index);
            }
        }
    }

    public void removeExpense(ExpenseItem expense) {
        if (expense != null) {
            int index = 0;
            boolean found = false;
            for (ExpenseItem e : expenses) {
                if (e.getId().equals(expense.getId())) {
                    found = true;
                    break;
                }
                index += 1;
            }
            if (found) {
                expenses.remove(index);
                notifyItemRemoved(index);
            }
        }
    }

    interface TYPE {
        int EMPTY = 0;
        int NORMAL = 1;
    }
}
