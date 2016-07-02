package io.github.zkhan93.hisab.model.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.User;
import io.github.zkhan93.hisab.model.callback.UserItemClickClbk;

/**
 * Created by Zeeshan Khan on 7/3/2016.
 */
public class UserVH extends RecyclerView.ViewHolder {
    @BindView(R.id.name)
    private TextView name;
    @BindView(R.id.email)
    private TextView email;
    private View itemView;

    public UserVH(View itemView) {
        super(itemView);
        this.itemView = itemView;
        ButterKnife.bind(this, itemView);
    }

    public void setUser(final User user, final UserItemClickClbk callback) {
        name.setText(user.getName());
        email.setText(user.getEmail());
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.UserCLicked(user);
            }
        });
    }
}
