package io.github.zkhan93.hisab.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.User;
import io.github.zkhan93.hisab.model.callback.UserItemClickClbk;
import io.github.zkhan93.hisab.ui.adapter.UsersAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class ShareActivityFragment extends Fragment implements UserItemClickClbk {
    public static final String TAG = ShareActivityFragment.class.getSimpleName();

    @BindView(R.id.users)
    RecyclerView usersListView;

    private UsersAdapter usersAdapter;

    public ShareActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_share, container, false);
        ButterKnife.bind(this, rootView);
        usersListView.setLayoutManager(new LinearLayoutManager(getContext()));
        usersAdapter = new UsersAdapter(this);
        usersListView.setAdapter(usersAdapter);
        return rootView;
    }

    @Override
    public void UserCLicked(User user) {
        //TODO: add this user to me's fried list
        Log.d(TAG, "adding " + user.getName() + " as afriend ");
    }
}
