package io.github.zkhan93.hisab.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import io.github.zkhan93.hisab.model.ExpenseItem;
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
    public ExpenseItemVH onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemViewType(int position) {
        int count = expenses.size();
        if (count == 0 && position == 0)
            return TYPE.EMPTY;
        return TYPE.NORMAL;
    }

    @Override
    public int getItemCount() {
        int count = expenses.size();
        return count == 0 ? 1 : count;
    }

    interface TYPE {
        int EMPTY = 0;
        int NORMAL = 1;
    }
}
