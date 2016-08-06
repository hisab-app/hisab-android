package io.github.zkhan93.hisab.model.viewholder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.TextView;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.Group;
import io.github.zkhan93.hisab.model.User;
import io.github.zkhan93.hisab.model.callback.OnClickGroupItemClbk;
import io.github.zkhan93.hisab.model.callback.OnLongClickGroupItemClbk;
import io.github.zkhan93.hisab.model.ui.ExGroup;

/**
 * Created by Zeeshan Khan on 6/26/2016.
 */
public class GroupItemVH extends RecyclerView.ViewHolder implements View.OnClickListener, View
        .OnLongClickListener {

    public static final String TAG = GroupItemVH.class.getSimpleName();

    @BindView(R.id.name)
    TextView name;
    @BindView(R.id.moderator)
    TextView moderator;
    @BindView(R.id.time)
    TextView time;
    @BindView(R.id.divider)
    View divider;

    private View itemView;
    private OnClickGroupItemClbk onClickGroupItemClbk;
    private Group group;
    private Calendar calendar;
    private Context context;
    private OnLongClickGroupItemClbk onLongClickGroupItemClbk;

    public GroupItemVH(View itemView, OnClickGroupItemClbk onClickGroupItemClbk,
                       OnLongClickGroupItemClbk onLongClickGroupItemClbk) {
        super(itemView);
        context = itemView.getContext();
        ButterKnife.bind(this, itemView);
        this.itemView = itemView;
        this.onClickGroupItemClbk = onClickGroupItemClbk;
        calendar = Calendar.getInstance();
        this.onLongClickGroupItemClbk = onLongClickGroupItemClbk;
    }

    public void setGroup(ExGroup group, User me) {
        name.setText(group.getName());
        itemView.setOnClickListener(this);

        if (me.getEmail().equals(group.getModerator().getEmail())) {
            moderator.setText(context.getString(R.string.msg_grp_created_by_you));
            itemView.setLongClickable(true);
            itemView.setOnLongClickListener(this);
        } else
            moderator.setText(group.getModerator().getName());
        calendar.setTimeInMillis(group.getCreatedOn());
        time.setText(DateUtils.getRelativeTimeSpanString(context, calendar.getTimeInMillis(),
                true));
        this.group = group;
        if (group.isSelected()) {
            itemView.setSelected(true);
        } else {
            itemView.setSelected(false);
        }
        divider.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        onClickGroupItemClbk.onClick(group.getId(), group.getName());
    }


    public void hideDivider() {
        divider.setVisibility(View.GONE);
    }

    @Override
    public boolean onLongClick(View view) {
        onLongClickGroupItemClbk.onLongClick(group.getId());
        return true;
    }
}
