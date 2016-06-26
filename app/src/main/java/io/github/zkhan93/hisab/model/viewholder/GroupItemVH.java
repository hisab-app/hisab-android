package io.github.zkhan93.hisab.model.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.Group;
import io.github.zkhan93.hisab.model.callback.GroupItemClickClbk;

/**
 * Created by Zeeshan Khan on 6/26/2016.
 */
public class GroupItemVH extends RecyclerView.ViewHolder {
    @BindView(R.id.members)
    TextView members;
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

    public void setGroup(final Group group) {
        name.setText(group.getName());
        if (group.getMembersIds() != null)
            members.setText(String.valueOf(group.getMembersIds().size()));
        moderator.setText(group.getModeratorId() + " is Moderator");
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                groupItemClickClbk.GroupClicked(group.getId(),group.getName());
            }
        });
    }
}
