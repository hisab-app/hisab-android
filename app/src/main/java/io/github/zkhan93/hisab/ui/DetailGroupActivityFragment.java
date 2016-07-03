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
import io.github.zkhan93.hisab.model.ExpenseItem;
import io.github.zkhan93.hisab.model.User;
import io.github.zkhan93.hisab.model.callback.ExpenseItemActionClbk;
import io.github.zkhan93.hisab.model.callback.ExpenseItemUiClbk;
import io.github.zkhan93.hisab.model.callback.GroupRenameClbk;
import io.github.zkhan93.hisab.ui.adapter.ExpensesAdapter;
import io.github.zkhan93.hisab.ui.dialog.EditExpenseItemDialog;
import io.github.zkhan93.hisab.ui.dialog.RenameGroupDialog;
import io.github.zkhan93.hisab.util.Util;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailGroupActivityFragment extends Fragment implements GroupRenameClbk,
        ValueEventListener, ExpenseItemUiClbk {
    public static final String TAG = DetailGroupActivityFragment.class.getSimpleName();
    //member views
    @BindView(R.id.expenses)
    RecyclerView expensesList;
    String groupId;
    //other members
    ExpensesAdapter expensesAdapter;
    private User me;
    private DatabaseReference groupNameRef;

    public DetailGroupActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            Bundle bundle = getArguments();
            if (bundle != null) {
                groupId = bundle.getString("groupId");
            }
        } else {
            groupId = savedInstanceState.getString("groupId");
        }
        me = Util.getUser(getContext());
        groupNameRef = FirebaseDatabase.getInstance().getReference("groups/" + me.getId() + "/" +
                groupId).child("name");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail_group, container, false);
        ButterKnife.bind(this, rootView);
        expensesList.setLayoutManager(new LinearLayoutManager(getActivity()));

        expensesAdapter = new ExpensesAdapter(me, groupId,this);
        expensesList.setAdapter(expensesAdapter);
        setHasOptionsMenu(true);
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
    }

    @Override
    public void onStart() {
        super.onStart();
        expensesAdapter.registerChildEventListener();
        groupNameRef.addValueEventListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        expensesAdapter.unregisterChildEventListener();
        expensesAdapter.clear();
        groupNameRef.removeEventListener(this);
    }

    @Override
    public void renameTo(String newName) {
        if (newName == null || newName.isEmpty()) {
            Log.e(TAG, "invalid new name, cannot rename groups");
            return;
        }
        if (me == null || me.getId() == null || me.getId().isEmpty()) {
            Log.e(TAG, "user id is not valid cannot rename group");
            return;
        }
        if (groupId == null || groupId.isEmpty()) {
            Log.e(TAG, "groupId is not valid cannot rename group");
            return;
        }
        groupNameRef.setValue(newName);
    }

    private void showRenameUi() {
        RenameGroupDialog renameDialog = new RenameGroupDialog();
        renameDialog.addGroupRenameCallback(this);
        renameDialog.show(getActivity().getFragmentManager(), RenameGroupDialog.TAG);
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        getActivity().setTitle(dataSnapshot.getValue().toString());
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.d(TAG, "name fetching operation cancelled");
    }

    @Override
    public void showEditUi(ExpenseItem expense, ExpenseItemActionClbk actionClbk) {
        EditExpenseItemDialog dialog = new EditExpenseItemDialog();
        dialog.setExpense(expense);
        dialog.setActionClbk(actionClbk);
        dialog.show(getActivity().getFragmentManager(), EditExpenseItemDialog.TAG);
    }
}
