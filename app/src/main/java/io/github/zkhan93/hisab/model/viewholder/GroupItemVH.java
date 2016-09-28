package io.github.zkhan93.hisab.model.viewholder;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.Group;
import io.github.zkhan93.hisab.model.User;
import io.github.zkhan93.hisab.model.callback.GroupItemClickClbk;
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
//    @BindView(R.id.time)
//    TextView time;
//    @BindView(R.id.divider)
//    View divider;

    private View itemView;
    private GroupItemClickClbk groupItemClickClbk;
    private Group group;
    private Calendar calendar;
    private Context context;
    private OnLongClickGroupItemClbk onLongClickGroupItemClbk;

    public GroupItemVH(View itemView, GroupItemClickClbk groupItemClickClbk,
                       OnLongClickGroupItemClbk onLongClickGroupItemClbk) {
        super(itemView);
        context = itemView.getContext();
        ButterKnife.bind(this, itemView);
        this.itemView = itemView;
        this.groupItemClickClbk = groupItemClickClbk;
        calendar = Calendar.getInstance();
        this.onLongClickGroupItemClbk = onLongClickGroupItemClbk;
    }

    public void setGroup(ExGroup group, User me) {
        name.setText(group.getName());
        itemView.setOnClickListener(this);

//        if (me.getEmail().equals(group.getModerator().getEmail())) {

        itemView.setLongClickable(true);
        itemView.setOnLongClickListener(this);
//        } else
//            moderator.setText(group.getModerator().getName());

        String tmp = context.getString(R.string.msg_grp_created_by_you);
        Log.d(TAG, "group:" + group.toString());
        if (group.getMembersCount() > 2)
            tmp += " and " + (group.getMembersCount() - 1) + " others";
        else if (group.getMembersCount() > 1)
            tmp += " and " + (group.getMembersCount() - 1) + " other";
        moderator.setText(tmp);
//        calendar.setTimeInMillis(group.getCreatedOn());
//        time.setText(DateUtils.getRelativeTimeSpanString(context, calendar.getTimeInMillis(),
//                true));
        this.group = group;
        if (group.isSelected()) {
            itemView.setBackground(ContextCompat.getDrawable(context, R.drawable
                    .selected_list_item_background));
        } else {
//            itemView.setBackground(ContextCompat.getDrawable(context, R.drawable
//                    .list_item_background));
        }
//        divider.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        groupItemClickClbk.onGroupClicked(group.getId(), group.getName());
    }


    public void hideDivider() {
//        divider.setVisibility(View.GONE);
    }

    @Override
    public boolean onLongClick(View view) {
        onLongClickGroupItemClbk.onLongClick(group.getId());
        return true;
    }
}
