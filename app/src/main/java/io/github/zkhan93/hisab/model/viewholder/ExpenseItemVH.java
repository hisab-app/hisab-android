package io.github.zkhan93.hisab.model.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.ExpenseItem;

/**
 * Created by Zeeshan Khan on 6/26/2016.
 */
public class ExpenseItemVH extends RecyclerView.ViewHolder {
    @BindView(R.id.description)
    TextView description;
    @BindView(R.id.amount)
    TextView amount;
    @BindView(R.id.author)
    TextView author;

    public ExpenseItemVH(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void setExpense(ExpenseItem expense) {
        description.setText(expense.getDescription());
        amount.setText(String.valueOf(expense.getAmount()));
        author.setText(expense.getAuthorId());
    }
}
