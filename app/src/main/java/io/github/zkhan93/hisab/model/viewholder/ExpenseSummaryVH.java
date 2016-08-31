package io.github.zkhan93.hisab.model.viewholder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.User;
import io.github.zkhan93.hisab.model.callback.SummaryActionItemClbk;

/**
 * Created by Zeeshan Khan on 6/26/2016.
 */
public class ExpenseSummaryVH extends RecyclerView.ViewHolder implements View.OnClickListener{

    public static final String TAG = ExpenseSummaryVH.class.getSimpleName();

    @BindView(R.id.description)
    TextView description;
    @BindView(R.id.per_person)
    TextView individualAmount;
    @BindView(R.id.archive)
    ImageButton archive;
    private Context context;
    private SummaryActionItemClbk summaryActionItemClbk;

    public ExpenseSummaryVH(View itemView, SummaryActionItemClbk summaryActionItemClbk) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        archive.setVisibility(View.GONE);
        this.context = itemView.getContext();
        this.summaryActionItemClbk = summaryActionItemClbk;
    }

    public void setSummaryExpense(float amount, float myExpenses, int noOfMembers, User me, User
            owner) {
        this.description.setText("Total expenses " + String.valueOf(amount));
        noOfMembers += 1;//including self
        if (noOfMembers == 1) {
            individualAmount.setText(context.getString(R.string.msg_share_to_split));
        } else if (noOfMembers > 1) {
            float genShare = amount / noOfMembers;
            float myShare = genShare - myExpenses;
            String msg = null;
            String rs = context.getString(R.string.rs);
            msg = context.getString(myShare < 0 ? R.string.msg_summary_collect : R.string
                            .msg_summary_give,
                    Math.abs(myShare), rs);

            if (myShare == 0) {
                msg = context.getString(R.string.msg_your_clear);
            }
            individualAmount.setText(String.format(msg, noOfMembers, genShare, myExpenses,
                    myShare));
        } else {
            individualAmount.setText("Invalid value");
            Log.e(TAG, "invalid members count encountered");
        }
        if (owner == null || !owner.getId().equals(me.getId())) {
            archive.setVisibility(View.GONE);
        } else {
            archive.setVisibility(View.VISIBLE);
            archive.setOnClickListener(this);
//            new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    summaryActionItemClbk.archiveGrp();
//                }
//            });
        }
    }

    @Override
    public void onClick(View view) {
        //the values will be handeled by intermediate Expense Adapter
        summaryActionItemClbk.archiveGrp(null,null);
    }
}
