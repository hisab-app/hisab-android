package io.github.zkhan93.hisab.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.Group;
import io.github.zkhan93.hisab.model.User;
import io.github.zkhan93.hisab.model.callback.ContextActionBarClbk;
import io.github.zkhan93.hisab.model.callback.OnClickGroupItemClbk;
import io.github.zkhan93.hisab.ui.adapter.GroupsAdapter;
import io.github.zkhan93.hisab.util.Util;

/**
 * A placeholder fragment containing a simple view.
 */
public class GroupsFragment extends Fragment implements OnClickGroupItemClbk,
        PreferenceChangeListener, ContextActionBarClbk {
    public static final String TAG = GroupsFragment.class.getSimpleName();

    //member views
    @BindView(R.id.groups)
    RecyclerView groupList;
    //other members
    private GroupsAdapter groupsAdapter;
    private User me;
    private ActionMode actionMode;

    public GroupsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        me = Util.getUser(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_groups, container, false);
        ButterKnife.bind(this, rootView);

        groupList.setLayoutManager(new LinearLayoutManager(getActivity()));
        groupsAdapter = new GroupsAdapter(this, me, this);
        groupList.setAdapter(groupsAdapter);

        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onClick(String groupId, String groupName) {
        Intent intent = new Intent(getActivity(), DetailGroupActivity.class);
        intent.putExtra("groupId", groupId);
        intent.putExtra("groupName", groupName);
        startActivity(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();
        groupsAdapter.registerChildEventListener();
    }

    @Override
    public void onStop() {
        super.onStop();
        groupsAdapter.unregisterChildEventListener();
        groupsAdapter.clear();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_groups_frag, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_sort:
                toggleSortType();
                groupsAdapter.sort(sortType);
                Toast.makeText(getContext(), getCurrentSortTypeString(), Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private int sortType = Group.SORT_TYPE.ALPHABETICAL;

    private void toggleSortType() {
        sortType += 1;
        sortType %= 4;
    }

    private String getCurrentSortTypeString() {
        switch (sortType) {
            case Group.SORT_TYPE.ALPHABETICAL:
                return "Sorted alphabetically";
            case Group.SORT_TYPE.REVERSE_ALPHABETICAL:
                return "Sorted reversed alphabetically";
            case Group.SORT_TYPE.CHRONOLOGICAL:
                return "Sorted chronologically";
            case Group.SORT_TYPE.REVERSE_CHRONOLOGICAL:
                return "Sorted reversed chronologically";
        }
        return "";
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent preferenceChangeEvent) {
        String keyChanged = preferenceChangeEvent.getKey();
        if (keyChanged.equals("name") || keyChanged.equals("email") || keyChanged.equals
                ("user_id")) {
            me = Util.getUser(getActivity());
        }
    }


    @Override
    public void showCAB() {
        if (actionMode == null)
            actionMode = getActivity().startActionMode(groupsAdapter);
    }

    @Override
    public void setCount(int count) {
        actionMode.setTitle(count + " Selected");
    }
}
