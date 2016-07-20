package io.github.zkhan93.hisab.model.viewholder;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.User;
import io.github.zkhan93.hisab.model.callback.ArchiveClickClbk;

/**
 * Created by Zeeshan Khan on 6/26/2016.
 */
public class ExpenseSummaryVH extends RecyclerView.ViewHolder {

    public static final String TAG = ExpenseSummaryVH.class.getSimpleName();

    @BindView(R.id.amount)
    TextView amount;
    @BindView(R.id.per_person)
    TextView individualAmount;
    @BindView(R.id.archive)
    Button archive;

    public ExpenseSummaryVH(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        archive.setVisibility(View.GONE);
    }

    public void setSummaryExpense(float amount, int noOfMembers, final ArchiveClickClbk
            archiveClickClbk, User me, User owner) {
        this.amount.setText(String.valueOf(amount));
        noOfMembers += 1;//including self
        if (noOfMembers > 0)
            individualAmount.setText(String.format("Per member(%d) %.2f", noOfMembers, amount /
                    noOfMembers));
        else {
            individualAmount.setText("Invalid value");
            Log.e(TAG, "invalid members count encountered");
        }
        if (owner == null || !owner.getId().equals(me.getId())) {
            archive.setVisibility(View.GONE);
        } else {
            archive.setVisibility(View.VISIBLE);
            archive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    archiveClickClbk.archiveGrp();
                }
            });
        }
    }
}
