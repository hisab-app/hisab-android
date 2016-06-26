package io.github.zkhan93.hisab.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.Group;
import io.github.zkhan93.hisab.model.User;
import io.github.zkhan93.hisab.model.callback.GroupItemClickClbk;
import io.github.zkhan93.hisab.ui.adapter.GroupsAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class GroupsActivityFragment extends Fragment implements GroupItemClickClbk{
    public static final String TAG = GroupsActivityFragment.class.getSimpleName();

    //member views
    @BindView(R.id.groups)
    RecyclerView groupList;
    //other members
    private GroupsAdapter groupsAdapter;

    public GroupsActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_groups, container, false);
        ButterKnife.bind(this, rootView);

        groupList.setLayoutManager(new LinearLayoutManager(getActivity()));
        //TODO: remove dummy data
        List<Group> groups = new ArrayList<>();
        Group group;
        List<User> members = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            group = new Group();
            group.setName("Group " + i);
            if (i % 2 == 0) {
                User user = new User();
                user.setName("User " + i);
                members.add(user);
            }
            group.setMembers(members);
            group.setModerator(members.get(members.size() - 1));
            groups.add(group);
        }
        groupsAdapter = new GroupsAdapter(groups);
        groupList.setAdapter(groupsAdapter);
        return rootView;
    }

    @Override
    public void GroupClicked(Group group) {
        Intent intent=new Intent(getActivity(),DetailGroupActivity.class);
        intent.putExtra("Group",group);
        startActivity(intent);
    }
}
