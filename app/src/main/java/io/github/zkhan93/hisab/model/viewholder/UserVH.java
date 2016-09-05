package io.github.zkhan93.hisab.model.viewholder;

import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.callback.UserItemActionClickClbk;
import io.github.zkhan93.hisab.model.ui.ExUser;
import io.github.zkhan93.hisab.util.Util;

/**
 * Created by Zeeshan Khan on 7/3/2016.
 */
public class UserVH extends RecyclerView.ViewHolder implements CompoundButton
        .OnCheckedChangeListener {
    @BindView(R.id.image)
    CircleImageView image;
    @BindView(R.id.name)
    TextView name;
    @BindView(R.id.email)
    TextView email;
    @BindView(R.id.state)
    SwitchCompat state;

    private View itemView;
    private ExUser exUser;
    private UserItemActionClickClbk actionCallback;

    public UserVH(View itemView, UserItemActionClickClbk userItemActionClickClbk) {
        super(itemView);
        this.itemView = itemView;
        ButterKnife.bind(this, itemView);
        this.actionCallback = userItemActionClickClbk;
    }

    public void setUser(ExUser user) {
        name.setText(user.getName());
        email.setText(user.getEmail());
        Picasso.with(image.getContext()).load(Util.getGavatarUrl(user.getEmail(), 200))
                .placeholder(R.drawable.big_user).fit().centerCrop().into(image);
        //itemView.setOnClickListener(this);
        if (state != null) {
            state.setChecked(user.isChecked());
            state.setOnCheckedChangeListener(this);
        }
        this.exUser = user;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        exUser.setChecked(b);
        actionCallback.UserClicked(exUser);
    }
}
