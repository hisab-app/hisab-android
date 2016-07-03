package io.github.zkhan93.hisab.model.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;

/**
 * Created by Zeeshan Khan on 6/26/2016.
 */
public class ExpenseSummaryVH extends RecyclerView.ViewHolder {

    @BindView(R.id.amount)
    TextView amount;
    @BindView(R.id.per_person)
    TextView individualAmount;

    public ExpenseSummaryVH(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void setSummaryExpense(float amount) {
        this.amount.setText(String.valueOf(amount));
        individualAmount.setText(String.format("Per person %.2f", amount / 3));
    }
}
