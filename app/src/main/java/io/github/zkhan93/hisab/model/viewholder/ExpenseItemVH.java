package io.github.zkhan93.hisab.model.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.ExpenseItem;
import io.github.zkhan93.hisab.model.User;

/**
 * Created by Zeeshan Khan on 6/26/2016.
 */
public class ExpenseItemVH extends RecyclerView.ViewHolder {
    @BindView(R.id.description)
    TextView description;
    @BindView(R.id.amount)
    TextView amount;
    @BindView(R.id.owner)
    TextView owner;

    public ExpenseItemVH(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void setExpense(ExpenseItem expense, User me) {
        description.setText(expense.getDescription());
        amount.setText(String.valueOf(expense.getAmount()));
        if (me.getEmail().equals(expense.getOwner().getEmail()))
            owner.setText("added by You");
        else
            owner.setText("added by " + expense.getOwner().getName());
    }
}
