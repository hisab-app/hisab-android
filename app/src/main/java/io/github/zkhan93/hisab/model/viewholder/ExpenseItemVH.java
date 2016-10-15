package io.github.zkhan93.hisab.model.viewholder;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Locale;

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
public class ExpenseItemVH extends RecyclerView.ViewHolder implements View.OnClickListener,
        PopupMenu.OnMenuItemClickListener {

    public static final String TAG = ExpenseItemVH.class.getSimpleName();
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd, MMM", Locale
            .ENGLISH);

    @BindView(R.id.description)
    TextView description;
    @BindView(R.id.details)
    TextView details;
    @BindView(R.id.amount)
    TextView amount;
    @BindView(R.id.actions)
    ImageButton actions;
    @BindView(R.id.image)
    CircleImageView authorImage;


    private ExpenseItemClbk expenseItemClbk;
    private ExpenseItem expense;
    private Context context;
    private PopupMenu popup;

    public ExpenseItemVH(View itemView, ExpenseItemClbk
            expenseItemClbk) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.expenseItemClbk = expenseItemClbk;
        this.context = itemView.getContext();
        popup = new PopupMenu(context, actions, Gravity.RIGHT, 0, R.style.ItemActionPopup);
        popup.getMenuInflater().inflate(R.menu.menu_expense_item_actions, popup.getMenu());
        popup.setOnMenuItemClickListener(this);
        actions.setOnClickListener(this);
    }

    public void setExpense(ExpenseItem expense, User me) {
        this.expense = expense;
        StringBuffer tmp = new StringBuffer();
        amount.setText(String.format(Locale.ENGLISH, "%.2f %s", expense.getAmount(), context
                .getString(R.string.rs)));
        //prefix for paid/received items
        if (expense.getItemType() == ExpenseItem.ITEM_TYPE.PAID_RECEIVED) {
            tmp.append(expense.getShareType() == ExpenseItem.SHARE_TYPE.PAID ? "Paid to " : "Received from ")
                    .append(expense.getWith().getName()).append("\n");
        }
        if (expense.getDescription() != null)
            tmp.append(expense.getDescription());

        description.setText(tmp);
        tmp.setLength(0);
        if (me.getEmail().equals(expense.getOwner().getEmail())) {
            tmp.append("You");
            actions.setVisibility(View.VISIBLE);
        } else {
            tmp.append(expense.getOwner().getName());
            actions.setVisibility(View.GONE);
        }
        tmp.append(" | ");
        tmp.append(expense.getItemType() == ExpenseItem.ITEM_TYPE.SHARED ? "Shared" :
                "Paid/Received");
        tmp.append(" | ");
        tmp.append(DateUtils.getRelativeTimeSpanString(context, expense.getCreatedOn(),
                true));
        details.setText(tmp);
        Picasso.with(context).load(Util.getGavatarUrl(expense.getOwner().getEmail(), 200))
                .placeholder(R.drawable.big_user).fit().centerCrop().into(authorImage);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.edit:
                update();
                break;
            case R.id.delete:
                expenseItemClbk.deleteExpense(expense.getId());
                break;
            case R.id.actions:
                popup.show();
                break;
            default:
                Log.d(TAG, "click not implemented");
        }
    }

    private void update() {
        expenseItemClbk.showEditUi(expense);
    }

    public void hideDivider() {

    }

    /**
     * This method will be invoked when a menu item is clicked if the item
     * itself did not already handle the event.
     *
     * @param item the menu item that was clicked
     * @return {@code true} if the event was handled, {@code false}
     * otherwise
     */
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                update();
                return true;
            case R.id.delete:
                expenseItemClbk.deleteExpense(expense.getId());
                return true;
        }
        return false;
    }
}
