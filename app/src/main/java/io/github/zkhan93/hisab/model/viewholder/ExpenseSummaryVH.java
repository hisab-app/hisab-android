package io.github.zkhan93.hisab.model.viewholder;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;

/**
 * Created by Zeeshan Khan on 6/26/2016.
 */
public class ExpenseSummaryVH extends RecyclerView.ViewHolder {

    public static final String TAG = ExpenseSummaryVH.class.getSimpleName();

    @BindView(R.id.amount)
    TextView amount;
    @BindView(R.id.per_person)
    TextView individualAmount;

    public ExpenseSummaryVH(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void setSummaryExpense(float amount, int noOfMembers) {
        this.amount.setText(String.valueOf(amount));
        noOfMembers += 1;//including self
        if (noOfMembers > 0)
            individualAmount.setText(String.format("Per member(%d) %.2f",noOfMembers, amount / noOfMembers));
        else {
            individualAmount.setText("Invalid value");
            Log.e(TAG, "invalid members count encountered");
        }
    }
}
