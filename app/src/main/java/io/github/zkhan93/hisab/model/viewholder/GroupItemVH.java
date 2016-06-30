package io.github.zkhan93.hisab.model.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.Group;
import io.github.zkhan93.hisab.model.User;
import io.github.zkhan93.hisab.model.callback.GroupItemClickClbk;

/**
 * Created by Zeeshan Khan on 6/26/2016.
 */
public class GroupItemVH extends RecyclerView.ViewHolder {
    @BindView(R.id.share_status)
    TextView share;
    @BindView(R.id.name)
    TextView name;
    @BindView(R.id.moderator)
    TextView moderator;
    private View itemView;
    private GroupItemClickClbk groupItemClickClbk;

    public GroupItemVH(View itemView, final GroupItemClickClbk groupItemClickClbk) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.itemView = itemView;
        this.groupItemClickClbk = groupItemClickClbk;
    }

    public void setGroup(final Group group, User me) {
        name.setText(group.getName());
        if (group.getMembersIds() == null || group.getMembersIds().size() == 0)
            share.setText("Private");
        else
            share.setText("Shared with " + String.valueOf(group.getMembersIds().size()));
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                groupItemClickClbk.GroupClicked(group.getId(), group.getName());
            }
        });

        if (me.getEmail().equals(group.getModerator().getEmail()))
            moderator.setText("Created by You");
        else
            moderator.setText("Created by " + group.getModerator().getName());
    }
}
