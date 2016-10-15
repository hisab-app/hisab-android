package io.github.zkhan93.hisab.model.viewholder;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.Group;
import io.github.zkhan93.hisab.model.User;
import io.github.zkhan93.hisab.model.callback.GroupItemClickClbk;
import io.github.zkhan93.hisab.model.callback.OnLongClickGroupItemClbk;
import io.github.zkhan93.hisab.model.ui.ExGroup;
import io.github.zkhan93.hisab.util.Util;

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
    @BindView(R.id.image)
    ImageView image;

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
        if (group.getMembersCount() <= 0)
            tmp = "no members";
        else if (group.getMembersCount() < 2)
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
        Picasso.with(context).load(Util.getGavatarUrl(group.getModerator().getEmail(), 200))
                .placeholder(R.drawable.big_user).fit().centerCrop().into(image);
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
    
    @Override
    public boolean onLongClick(View view) {
        onLongClickGroupItemClbk.onLongClick(group.getId());
        return true;
    }
}
