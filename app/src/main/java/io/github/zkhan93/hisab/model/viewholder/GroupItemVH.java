package io.github.zkhan93.hisab.model.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.Group;

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

    public GroupItemVH(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void setItem(Group group) {
        name.setText(group.getName());
        members.setText(String.valueOf(group.getMembers().size()));
        moderator.setText(group.getModerator().getName() + " is Moderator");
    }
}
