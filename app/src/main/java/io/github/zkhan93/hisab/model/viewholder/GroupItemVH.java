package io.github.zkhan93.hisab.model.viewholder;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
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
    @BindView(R.id.info)
    ImageButton info;

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
        itemView.setOnClickListener(this);
        itemView.setLongClickable(true);
        itemView.setOnLongClickListener(this);
        info.setOnClickListener(this);
    }

    public void setGroup(ExGroup group, User me) {
        name.setText(group.getName());
        String tmp;
        if (group.getMembersCount() == 2)
            tmp = group.getMembersCount() + " member";
        else
            tmp = group.getMembersCount() + " members";
        moderator.setText(tmp);
        this.group = group;
        if (group.isSelected()) {
            itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.md_red_200));
        } else {
            itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.md_white_1000));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.info:
                groupItemClickClbk.onGroupInfoClicked(group);
                break;
            default:
                groupItemClickClbk.onGroupClicked(group.getId(), group.getName());
        }
    }


    public void hideDivider() {
    }

    @Override
    public boolean onLongClick(View view) {
        onLongClickGroupItemClbk.onLongClick(group.getId());
        return true;
    }
}
