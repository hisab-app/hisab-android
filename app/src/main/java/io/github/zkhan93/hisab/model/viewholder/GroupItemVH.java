package io.github.zkhan93.hisab.model.viewholder;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
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
    @BindView(R.id.latest_content)
    TextView latestContent;
    @BindView(R.id.latest_name)
    TextView latestName;
    @BindView(R.id.counter)
    TextView counter;
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
    }

    public void setGroup(ExGroup group, User me, int count) {
        if (group == null)
            return;

        name.setText(group.getName());
        if (count > 0) {
            counter.setVisibility(View.VISIBLE);
            counter.setText(String.valueOf(count));
//            name.setTypeface(null, Typeface.BOLD);
        } else {
            counter.setVisibility(View.GONE);
//            name.setTypeface(null, Typeface.NORMAL);
        }
//        String tmp;
//        if (group.getMembersCount() <= 0)
//            tmp = "no members";
//        else if (group.getMembersCount() < 2)
//            tmp = group.getMembersCount() + " member";
//        else
//            tmp = group.getMembersCount() + " members";
//        latestContent.setText(tmp);
        latestName.setText(group.getLastMsgName());
        latestContent.setText(group.getLastMsgDesc());
        this.group = group;
        if (group.isSelected()) {
            itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.md_red_200));
        } else {
            itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.md_white_1000));
        }
        if (group.getModerator() == null || group.getModerator().getEmail() == null)
            return;
        Picasso.with(context).load(Util.getGavatarUrl(group.getModerator().getEmail(), 200))
                .placeholder(R.drawable.big_user).fit().centerCrop().into(image);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
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
