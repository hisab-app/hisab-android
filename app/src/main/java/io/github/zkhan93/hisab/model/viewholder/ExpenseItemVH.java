package io.github.zkhan93.hisab.model.viewholder;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.drawable.Drawable;
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

    @Nullable
    @BindView(R.id.image)
    CircleImageView authorImage;


    private ExpenseItemClbk expenseItemClbk;
    private ExpenseItem expense;
    private Context context;
    private Drawable tdrawable;

    public ExpenseItemVH(View itemView, ExpenseItemClbk
            expenseItemClbk) {
        super(itemView);
//        itemView.setOnClickListener(this);
        ButterKnife.bind(this, itemView);
        this.expenseItemClbk = expenseItemClbk;
        this.context = itemView.getContext();

        if (btnExpand != null) {
            btnExpand.setOnClickListener(this);
            tdrawable = btnExpand.getDrawable();
        }
        if (btnDelete != null)
            btnDelete.setOnClickListener(this);
        if (btnEdit != null)
            btnEdit.setOnClickListener(this);
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

        if (me.getEmail().equals(expense.getOwner().getEmail())) {
            name.setText(expense.getOwner().getName());
//            name.setText("You");
        } else {
            name.setText(expense.getOwner().getName());
        }

        typeCash.setVisibility(expense.getItemType() == ExpenseItem.ITEM_TYPE.PAID_RECEIVED ? View.VISIBLE : View.GONE);
        typeShare.setVisibility(expense.getItemType() == ExpenseItem.ITEM_TYPE.SHARED ? View.VISIBLE : View.GONE);

        hasImage.setVisibility(expense.getDescription().length() % 2 == 0 ? View.VISIBLE : View.GONE);

        details.setText(DateUtils.getRelativeTimeSpanString(context, expense.getCreatedOn(),
                false));
        if (authorImage != null)
            Picasso.with(context).load(Util.getGavatarUrl(expense.getOwner().getEmail(), 200))
                    .placeholder(R.drawable.big_user).fit().centerCrop().into(authorImage);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.buttonExpand:
                final boolean expanded = actionsContainer.getVisibility() == View.VISIBLE;
                if (btnExpand != null) {
                    final int mShortAnimationDuration = context.getResources().getInteger(
                            android.R.integer.config_shortAnimTime) / 2;
                    btnExpand.setAlpha(1f);
                    final ViewPropertyAnimator animator = btnExpand.animate();
                    animator.alpha(0f).setDuration(mShortAnimationDuration).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            actionsContainer.setVisibility(expanded ? View.GONE : View.VISIBLE);
                            btnExpand.setImageDrawable(
                                    ContextCompat.getDrawable(context, expanded ? R.drawable.ic_keyboard_arrow_down_grey_500_18dp : R.drawable.ic_keyboard_arrow_up_grey_500_18dp)
                            );
                            animator.alpha(1f).setDuration(mShortAnimationDuration);
                        }
                    });

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
