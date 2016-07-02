package io.github.zkhan93.hisab.model.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.callback.UserItemActionClickClbk;
import io.github.zkhan93.hisab.model.callback.UserItemUiClickClbk;
import io.github.zkhan93.hisab.model.ui.ExUser;

/**
 * Created by Zeeshan Khan on 7/3/2016.
 */
public class UserVH extends RecyclerView.ViewHolder implements View.OnClickListener {
    @BindView(R.id.name)
    TextView name;
    @BindView(R.id.email)
    TextView email;
    @BindView(R.id.state)
    ImageView state;

    private View itemView;
    private ExUser exUser;
    private UserItemActionClickClbk actionCallback;
    private UserItemUiClickClbk uiCallback;

    public UserVH(View itemView, UserItemActionClickClbk userItemActionClickClbk,
                  UserItemUiClickClbk uiCallback) {
        super(itemView);
        this.itemView = itemView;
        ButterKnife.bind(this, itemView);
        this.actionCallback = userItemActionClickClbk;
        this.uiCallback = uiCallback;
    }

    public void setUser(ExUser user) {
        name.setText(user.getName());
        email.setText(user.getEmail());
        itemView.setOnClickListener(this);
        if (user.isChecked())
            state.setVisibility(View.VISIBLE);
        else
            state.setVisibility(View.GONE);
        this.exUser = user;
    }

    @Override
    public void onClick(View view) {
        actionCallback.UserCLicked(exUser);
        exUser.setChecked(!exUser.isChecked());
        uiCallback.updateUi(exUser);
    }
}
