package io.github.zkhan93.hisab.model.viewholder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.Group;
import io.github.zkhan93.hisab.model.User;
import io.github.zkhan93.hisab.model.callback.GroupItemClickClbk;

/**
 * Created by Zeeshan Khan on 6/26/2016.
 */
public class GroupItemVH extends RecyclerView.ViewHolder implements View.OnClickListener {

    public static final String TAG = GroupItemVH.class.getSimpleName();
    private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd, MMM yyyy");

    @BindView(R.id.name)
    TextView name;
    @BindView(R.id.moderator)
    TextView moderator;
    @BindView(R.id.time)
    TextView time;
    @BindView(R.id.divider)
    View divider;

    private View itemView;
    private GroupItemClickClbk groupItemClickClbk;
    private Group group;
    private Calendar calendar;
    private Context context;

    public GroupItemVH(View itemView, GroupItemClickClbk groupItemClickClbk) {
        super(itemView);
        context = itemView.getContext();
        ButterKnife.bind(this, itemView);
        this.itemView = itemView;
        this.groupItemClickClbk = groupItemClickClbk;
        calendar = Calendar.getInstance();
        divider.setVisibility(View.VISIBLE);
    }

    public void setGroup(Group group, User me) {
        name.setText(group.getName());
        itemView.setOnClickListener(this);

        if (me.getEmail().equals(group.getModerator().getEmail()))
            moderator.setText("Created by You");
        else
            moderator.setText("Created by " + group.getModerator().getName());
        calendar.setTimeInMillis(group.getCreatedOn());
        time.setText(DateUtils.getRelativeTimeSpanString(context, calendar.getTimeInMillis(),
                true));
        this.group = group;
    }

    @Override
    public void onClick(View view) {
        groupItemClickClbk.GroupClicked(group.getId(), group.getName());
    }

    public void hideDivider() {
        divider.setVisibility(View.GONE);
    }
}
