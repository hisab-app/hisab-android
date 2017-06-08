package io.github.zkhan93.hisab.model.viewholder;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import io.github.zkhan93.hisab.model.callback.ExpenseItemClickClbk;
import io.github.zkhan93.hisab.model.ui.ExExpenseItem;
import io.github.zkhan93.hisab.util.Util;

/**
 * Created by Zeeshan Khan on 6/26/2016.
 */
public class ExpenseItemVH extends RecyclerView.ViewHolder implements View.OnClickListener {

    public static final String TAG = ExpenseItemVH.class.getSimpleName();
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd, MMM", Locale
            .ENGLISH);

    @BindView(R.id.name)
    TextView name;

    @BindView(R.id.description)
    TextView description;

    @BindView(R.id.details)
    TextView details;

    @BindView(R.id.amount)
    TextView amount;

    @BindView(R.id.typeCash)
    ImageView typeCash;

    @BindView(R.id.typeShare)
    ImageView typeShare;

    @BindView(R.id.hasImage)
    ImageView hasImage;

    @BindView(R.id.showImage)
    ImageView showImage;

    @Nullable
    @BindView(R.id.buttonExpand)
    ImageButton btnExpand;

    @Nullable
    @BindView(R.id.delete)
    Button btnDelete;

    @Nullable
    @BindView(R.id.edit)
    Button btnEdit;

    @Nullable
    @BindView(R.id.actions)
    View actionsContainer;

    @BindView(R.id.image)
    CircleImageView authorImage;

    private ExpenseItemClickClbk expenseItemClickClbk;
    private ExpenseItemClbk expenseItemClbk;
    private ExExpenseItem expense;
    private Context context;
    private final int mShortAnimationDuration;

    public ExpenseItemVH(View itemView, ExpenseItemClbk
            expenseItemClbk, ExpenseItemClickClbk expenseItemClickClbk) {
        super(itemView);
        this.expenseItemClickClbk = expenseItemClickClbk;
//        itemView.setOnClickListener(this);
        ButterKnife.bind(this, itemView);
        this.expenseItemClbk = expenseItemClbk;
        this.context = itemView.getContext();

        if (btnExpand != null) {
            btnExpand.setOnClickListener(this);
        }
        if (btnDelete != null)
            btnDelete.setOnClickListener(this);
        if (btnEdit != null)
            btnEdit.setOnClickListener(this);
        mShortAnimationDuration = context.getResources().getInteger(
                android.R.integer.config_shortAnimTime);
    }

    public void setExpense(ExExpenseItem expense, User me) {
        this.expense = expense;
        StringBuffer tmp = new StringBuffer();
        amount.setText(String.format(Locale.ENGLISH, "%.2f %s", expense.getAmount(), context
                .getString(R.string.rs)));
        //prefix for paid/received items
        if (expense.getItemType() == ExpenseItem.ITEM_TYPE.PAID_RECEIVED) {
            tmp.append(expense.getShareType() == ExpenseItem.SHARE_TYPE.PAID ? "Paid to " :
                    "Received from ")
                    .append(expense.getWith().getName()).append("\n");
        }
        if (expense.getDescription() != null)
            tmp.append(expense.getDescription());

        description.setText(tmp);

        if (me.getEmail().equals(expense.getOwner().getEmail())) {
            name.setText(expense.getOwner().getName());
//            name.setText("You");
        } else {
            name.setText(expense.getOwner().getName());
            if (expense.getImage() == null)
                btnExpand.setVisibility(View.GONE);
        }

        typeCash.setVisibility(expense.getItemType() == ExpenseItem.ITEM_TYPE.PAID_RECEIVED ?
                View.VISIBLE : View.GONE);
        typeShare.setVisibility(expense.getItemType() == ExpenseItem.ITEM_TYPE.SHARED ? View
                .VISIBLE : View.GONE);
        hasImage.setVisibility(expense.getImage() != null ? View.VISIBLE : View
                .GONE);


        details.setText(DateUtils.getRelativeTimeSpanString(context, expense.getCreatedOn(),
                false));
        if (authorImage != null)
            Picasso.with(context).load(Util.getGavatarUrl(expense.getOwner().getEmail(), 200))
                    .placeholder(R.drawable.big_user).fit().centerCrop().into(authorImage);

        if (expense.getImage() != null) {
            showImage.setVisibility(View.VISIBLE);
            Picasso.with(context).load(expense.getImage()).into(showImage);
        } else {
            showImage.setVisibility(View.GONE);
        }

        if (btnExpand != null) {
            actionsContainer.setVisibility(expense.isExpanded() ? View.VISIBLE : View.GONE);
            btnExpand.setImageDrawable(ContextCompat.getDrawable(context, expense
                    .isExpanded() ? R.drawable.ic_keyboard_arrow_up_grey_500_18dp : R
                    .drawable.ic_keyboard_arrow_down_grey_500_18dp));
            ViewPropertyAnimator animator = btnExpand.animate();
            animator.alpha(1f).translationYBy(expense.isExpanded() ? 10 : -10).setDuration
                    (mShortAnimationDuration);
            animator.setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    btnExpand.setTranslationY(0);
                }
            });
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.buttonExpand:
                //for other view items
                if (btnExpand != null) {
                    ViewPropertyAnimator animator = btnExpand.animate();
                    animator.translationYBy(expense.isExpanded() ? -10 : 10).alpha(0.5f).setDuration
                            (mShortAnimationDuration);
                    expense.setExpanded(!expense.isExpanded());
                    expenseItemClickClbk.onExpenseChanged(expense.getId());
                }
                break;
            case R.id.delete:
                expenseItemClbk.deleteExpense(expense.getId());
                break;
            case R.id.edit:
                update();
                break;
            default:
                Log.d(TAG, "click not implemented");
        }

    }

    private void update() {
        expenseItemClbk.showEditUi(expense);
    }

}
