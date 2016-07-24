package io.github.zkhan93.hisab.model.viewholder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.ExpenseItem;
import io.github.zkhan93.hisab.model.User;
import io.github.zkhan93.hisab.model.callback.ExpenseItemClbk;
import io.github.zkhan93.hisab.util.Util;

/**
 * Created by Zeeshan Khan on 6/26/2016.
 */
public class ExpenseItemVH extends RecyclerView.ViewHolder implements View.OnClickListener {

    public static final String TAG = ExpenseItemVH.class.getSimpleName();
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd, MMM");

    @BindView(R.id.description)
    TextView description;
    @BindView(R.id.owner)
    TextView owner;
    @BindView(R.id.edit)
    ImageButton rename;
    @BindView(R.id.delete)
    ImageButton delete;
    @BindView(R.id.image)
    CircleImageView authorImage;

    private ExpenseItemClbk expenseItemClbk;
    private ExpenseItem expense;
    private Calendar calendar;
    private Context context;

    public ExpenseItemVH(View itemView, ExpenseItemClbk
            expenseItemClbk) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.expenseItemClbk = expenseItemClbk;
        calendar = Calendar.getInstance();
        this.context = itemView.getContext();
    }

    public void setExpense(ExpenseItem expense, User me) {
        this.expense = expense;
        description.setText(expense.getDescription() + " - " + String.valueOf(expense.getAmount()));
        calendar.setTimeInMillis(expense.getCreatedOn());
        if (me.getEmail().equals(expense.getOwner().getEmail())) {
            owner.setText(String.format("added by You, on %s", DATE_FORMAT.format(calendar.getTime
                    ())));
            rename.setVisibility(View.VISIBLE);
            delete.setVisibility(View.VISIBLE);
            rename.setOnClickListener(this);
            delete.setOnClickListener(this);
        } else {
            owner.setText(String.format("added by %s on %s", expense.getOwner().getName(),
                    DATE_FORMAT.format(calendar.getTime())));
            rename.setVisibility(View.GONE);
            delete.setVisibility(View.GONE);
        }
        Log.d(TAG,"https://www.gravatar.com/avatar/" + Util.md5(expense.getOwner()
                .getEmail()));
        Glide.with(context).load("https://www.gravatar.com/avatar/" + Util.md5(expense.getOwner()
                .getEmail())).placeholder(R.drawable
                .ic_add_grey_50_24dp).centerCrop().crossFade().into(authorImage);
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
