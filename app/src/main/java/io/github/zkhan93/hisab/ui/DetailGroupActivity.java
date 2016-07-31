package io.github.zkhan93.hisab.ui;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.ExpenseItem;
import io.github.zkhan93.hisab.model.User;
import io.github.zkhan93.hisab.model.callback.ExpenseItemClbk;
import io.github.zkhan93.hisab.model.callback.GroupRenameClbk;
import io.github.zkhan93.hisab.ui.dialog.CreateExpenseItemDialog;
import io.github.zkhan93.hisab.ui.dialog.EditExpenseItemDialog;
import io.github.zkhan93.hisab.util.Util;

public class DetailGroupActivity extends AppCompatActivity implements View.OnClickListener,
        GroupRenameClbk, ExpenseItemClbk, PreferenceChangeListener {
    public static final String TAG = DetailGroupActivity.class.getSimpleName();

    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private DatabaseReference groupExpensesRef, dbGrpNameRef, dbRef;
    private FirebaseUser firebaseUser;
    private String groupId, groupName;
    private User me;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        me = Util.getUser(getApplicationContext());
        setContentView(R.layout.activity_detail_group);
        ButterKnife.bind(this);
        groupId = getIntent().getStringExtra("groupId");
        groupName = getIntent().getStringExtra("groupName");
        dbRef = FirebaseDatabase.getInstance().getReference();
        groupExpensesRef = dbRef.child("expenses").child(groupId);
        dbGrpNameRef = dbRef.child("groups").child(me.getId()).child(groupId).child("name");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        setSupportActionBar(toolbar);
        fab.setOnClickListener(this);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (savedInstanceState == null) {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag
                    (ExpensesFragment.TAG);
            if (fragment == null) {
                fragment = new ExpensesFragment();
                fragment.setArguments(getIntent().getExtras());
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment, fragment,
                    ExpensesFragment.TAG).commit();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                showAddExpenseView();
                break;
            default:
                Log.d(TAG, "click not implemented");
        }
    }

    private void showAddExpenseView() {
        DialogFragment dialog = new CreateExpenseItemDialog();
        dialog.show(getFragmentManager(), "dialog");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

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
        dbGrpNameRef.setValue(newName);
    }

    public void createExpense(String description, float amount) {
        ExpenseItem expenseItem = new ExpenseItem(description, amount);
        expenseItem.setCreatedOn(Calendar.getInstance().getTimeInMillis());
        expenseItem.setOwner(me);
//        expenseItem.setGroupId(groupId); no need to set this as data is already under the group
// id branch
        groupExpensesRef.push().setValue(expenseItem).addOnCompleteListener(this, new
                OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "";
                        if (!task.isSuccessful()) {
                            msg = "Error occurred: " + task.getException().getLocalizedMessage();
                            final Snackbar snackbar = Snackbar.make(toolbar, msg, Snackbar
                                    .LENGTH_INDEFINITE);
                            snackbar.setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    snackbar.dismiss();
                                }
                            });
                            snackbar.show();
                        }

                    }
                });

    }

    public void showShareGroupUi() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(ShareFragment
                .TAG);
        if (fragment == null) {
            fragment = new ShareFragment();
            Bundle bundle = new Bundle();
            bundle.putString("groupId", groupId);
            bundle.putParcelable("me", me);
            fragment.setArguments(bundle);
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, fragment,
                ShareFragment.TAG).addToBackStack(ExpensesFragment.TAG).commit();
        fab.hide();
        setTitle("Share with");
    }

    @Override
    public void showEditUi(ExpenseItem expense) {
        EditExpenseItemDialog dialog = new EditExpenseItemDialog();
        Bundle bundle = new Bundle();
        bundle.putParcelable("expense", expense);
        dialog.setArguments(bundle);
        dialog.show(getFragmentManager(), EditExpenseItemDialog.TAG);
    }

    @Override
    public void delete(String expenseId) {
        groupExpensesRef.child(expenseId).removeValue();
    }

    @Override
    public void update(ExpenseItem expense) {
        groupExpensesRef.child(expense.getId()).setValue(expense).addOnCompleteListener(this, new
                OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            final Snackbar snackbar = Snackbar.make(toolbar, "Error Occurred: " +
                                    task.getException()
                                    .getLocalizedMessage(), Snackbar.LENGTH_SHORT);
                            snackbar.setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    snackbar.dismiss();
                                }
                            });
                            snackbar.show();
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        fab.show();
        super.onBackPressed();
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent preferenceChangeEvent) {
        String keyChanged = preferenceChangeEvent.getKey();
        if (keyChanged.equals("name") || keyChanged.equals("email") || keyChanged.equals
                ("user_id")) {
            me = Util.getUser(getApplicationContext());
        }
    }
}
