package io.github.zkhan93.hisab.model.viewholder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.User;
import io.github.zkhan93.hisab.model.callback.SummaryActionItemClbk;

/**
 * Created by Zeeshan Khan on 6/26/2016.
 */
public class ExpenseSummaryVH extends RecyclerView.ViewHolder {

    public static final String TAG = ExpenseSummaryVH.class.getSimpleName();

    @BindView(R.id.todayAmount)
    TextView today;

    @BindView(R.id.month)
    TextView month;

    @BindView(R.id.monthAmount)
    TextView monthAmount;

    @BindView(R.id.totalAmount)
    TextView total;

    @BindView(R.id.members)
    TextView members;

    @BindView(R.id.per_person)
    TextView individualAmount;


    private Context context;
    private SummaryActionItemClbk summaryActionItemClbk;
    private String currSymbol;

    public ExpenseSummaryVH(View itemView, SummaryActionItemClbk summaryActionItemClbk) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.context = itemView.getContext();
        this.summaryActionItemClbk = summaryActionItemClbk;
        currSymbol = context.getString(R.string.rs);
    }

    public void setSummaryExpense(float totlaAmount, float myExpenses, int noOfMembers, User me, User
            owner, float monthAmt, float todayAmount) {
        total.setText(context.getString(R.string
                .msg_total_expenses, totlaAmount));
        noOfMembers += 1;//including self
        if (noOfMembers > 1)
            members.setText(context.getString(R.string.n_members, noOfMembers));
        else
            members.setText(context.getString(R.string.no_member));

        total.setText(context.getString(R.string.amount, totlaAmount, currSymbol));

        month.setText(String.format(Locale.US, "%tB", Calendar.getInstance()));

        monthAmount.setText(context.getString(R.string.amount, monthAmt, currSymbol));

        today.setText(context.getString(R.string.amount, todayAmount, currSymbol));

        if (noOfMembers == 1) {
            individualAmount.setText(context.getString(R.string.msg_share_to_split));
        } else if (noOfMembers > 1) {
            float genShare = totlaAmount / noOfMembers;
            float myShare = genShare - myExpenses;
            String msg = null;

            msg = context.getString(myShare < 0 ? R.string.msg_summary_collect : R.string
                            .msg_summary_give,
                    Math.abs(myShare), currSymbol);

            if (myShare == 0) {
                msg = context.getString(R.string.msg_your_clear);
            }
            individualAmount.setText(String.format(msg, noOfMembers, genShare, myExpenses,
                    myShare));
        } else {
            individualAmount.setText(context.getString(R.string.msg_invalid_value));
            Log.e(TAG, "invalid members count encountered");
        }

    }

//    @Override
//    public void onClick(View view) {
//        //the values will be handeled by intermediate Expense Adapter
//        summaryActionItemClbk.archiveGrp(null, null);
//    }
}
