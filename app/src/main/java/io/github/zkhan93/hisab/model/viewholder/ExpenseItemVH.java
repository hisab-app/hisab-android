package io.github.zkhan93.hisab.model.viewholder;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.ExpenseItem;
import io.github.zkhan93.hisab.model.User;
import io.github.zkhan93.hisab.model.callback.ExpenseItemClbk;

/**
 * Created by Zeeshan Khan on 6/26/2016.
 */
public class ExpenseItemVH extends RecyclerView.ViewHolder implements View.OnClickListener {

    public static final String TAG = ExpenseItemVH.class.getSimpleName();

    @BindView(R.id.description)
    TextView description;
    @BindView(R.id.amount)
    TextView amount;
    @BindView(R.id.owner)
    TextView owner;
    @BindView(R.id.edit)
    Button rename;
    @BindView(R.id.delete)
    Button delete;

    private ExpenseItemClbk expenseItemClbk;
    private ExpenseItem expense;

    public ExpenseItemVH(View itemView, ExpenseItemClbk
            expenseItemClbk) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.expenseItemClbk = expenseItemClbk;

    }

    public void setExpense(ExpenseItem expense, User me) {
        this.expense = expense;
        description.setText(expense.getDescription());
        amount.setText(String.valueOf(expense.getAmount()));
        if (me.getEmail().equals(expense.getOwner().getEmail())) {
            owner.setText("added by You");
            rename.setVisibility(View.VISIBLE);
            delete.setVisibility(View.VISIBLE);
            rename.setOnClickListener(this);
            delete.setOnClickListener(this);
        } else {
            owner.setText("added by " + expense.getOwner().getName());
            rename.setVisibility(View.GONE);
            delete.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.edit:
                update();
                break;
            case R.id.delete:
                expenseItemClbk.delete(expense.getId());
                break;
            default:
                Log.d(TAG, "click not implemented");
        }
    }

    private void update() {
        expenseItemClbk.showEditUi(expense);
    }
}
