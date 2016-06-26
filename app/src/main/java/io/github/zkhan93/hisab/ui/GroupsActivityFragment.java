package io.github.zkhan93.hisab.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.Group;
import io.github.zkhan93.hisab.model.callback.GroupItemClickClbk;
import io.github.zkhan93.hisab.ui.adapter.GroupsAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class GroupsActivityFragment extends Fragment implements GroupItemClickClbk,
        ChildEventListener {
    public static final String TAG = GroupsActivityFragment.class.getSimpleName();

    //member views
    @BindView(R.id.groups)
    RecyclerView groupList;
    //other members
    private ArrayList<Group> groups;
    private DatabaseReference dbRef;
    private GroupsAdapter groupsAdapter;

    public GroupsActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbRef = FirebaseDatabase.getInstance().getReference("groups");
        dbRef.addChildEventListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_groups, container, false);
        ButterKnife.bind(this, rootView);

        groupList.setLayoutManager(new LinearLayoutManager(getActivity()));
        if (savedInstanceState == null) {
            groups = new ArrayList<>();
        } else {
            groups = savedInstanceState.getParcelableArrayList("groups");
        }
        groupsAdapter = new GroupsAdapter(groups, this);
        groupList.setAdapter(groupsAdapter);
        return rootView;
    }

    @Override
    public void GroupClicked(String groupId, String groupName) {
        Intent intent = new Intent(getActivity(), DetailGroupActivity.class);
        intent.putExtra("groupId", groupId);
        intent.putExtra("groupName", groupName);
        startActivity(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("groups", groups);
    }

    private void updateAdapterData() {
        if (groupsAdapter != null) {
            groupsAdapter.setGroups(this.groups);
            Log.d(TAG, "" + this.groups);
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        dbRef.removeEventListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        dbRef.addChildEventListener(this);
    }
    //Firebase data listeners

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        Group group = dataSnapshot.getValue(Group.class);
        group.setId(dataSnapshot.getKey());
        groupsAdapter.addGroup(group);
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        Group group = dataSnapshot.getValue(Group.class);
        group.setId(dataSnapshot.getKey());
        groupsAdapter.modifyGroup(group);
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        Log.d(TAG, dataSnapshot.toString());
        Group group = dataSnapshot.getValue(Group.class);
        group.setId(dataSnapshot.getKey());
        groupsAdapter.removeGroup(group);
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        Log.d(TAG, "onChildMoved");
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.d(TAG, "onCancelled");
    }
}
