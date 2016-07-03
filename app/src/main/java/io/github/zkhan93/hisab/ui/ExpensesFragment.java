package io.github.zkhan93.hisab.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.User;
import io.github.zkhan93.hisab.ui.adapter.ExpensesAdapter;
import io.github.zkhan93.hisab.ui.dialog.RenameGroupDialog;
import io.github.zkhan93.hisab.util.Util;

/**
 * A placeholder fragment containing a simple view.
 */
public class ExpensesFragment extends Fragment implements
        ValueEventListener {
    public static final String TAG = ExpensesFragment.class.getSimpleName();
    //member views
    @BindView(R.id.expenses)
    RecyclerView expensesList;
    String groupId, groupName;
    //other members
    ExpensesAdapter expensesAdapter;
    private User me;
    private DatabaseReference groupNameRef;
    private DatabaseReference dbRef;

    public ExpensesFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            Bundle bundle = getArguments();
            if (bundle != null) {
                groupId = bundle.getString("groupId");
                groupName = bundle.getString("groupName");
            }
        } else {
            groupId = savedInstanceState.getString("groupId");
            groupName = savedInstanceState.getString("groupName");
        }
        me = Util.getUser(getContext());
        groupNameRef = FirebaseDatabase.getInstance().getReference("groups/" + me.getId() + "/" +
                groupId).child("name");
        dbRef = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail_group, container, false);
        ButterKnife.bind(this, rootView);
        expensesList.setLayoutManager(new LinearLayoutManager(getActivity()));

        expensesAdapter = new ExpensesAdapter(me, groupId, (DetailGroupActivity) getActivity());
        expensesList.setAdapter(expensesAdapter);
        setHasOptionsMenu(true);
        getActivity().setTitle(groupName);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detail_group, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_share:
                ((DetailGroupActivity) getActivity()).showShareGroupUi();
                return true;
            case R.id.action_rename:
                showRenameUi();
            default:
                return false;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("groupId", groupId);
        outState.putString("groupName", groupName);
    }

    @Override
    public void onStart() {
        super.onStart();
        expensesAdapter.registerEventListener();
        groupNameRef.addValueEventListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        expensesAdapter.unregisterEventListener();
        expensesAdapter.clear();
        groupNameRef.removeEventListener(this);
    }


    private void showRenameUi() {
        RenameGroupDialog renameDialog = new RenameGroupDialog();
        Bundle bundle = new Bundle();
        bundle.putString("name", groupName);
        renameDialog.setArguments(bundle);
        renameDialog.show(getActivity().getFragmentManager(), RenameGroupDialog.TAG);
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        groupName = dataSnapshot.getValue().toString();
        if (isVisible())
            getActivity().setTitle(groupName);
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.d(TAG, "name fetching operation cancelled");
    }


}
