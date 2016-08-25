package io.github.zkhan93.hisab.ui;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.ExpenseItem;
import io.github.zkhan93.hisab.model.User;
import io.github.zkhan93.hisab.model.callback.ExpenseItemClbk;
import io.github.zkhan93.hisab.model.callback.GroupRenameClbk;
import io.github.zkhan93.hisab.ui.dialog.EditExpenseItemDialog;
import io.github.zkhan93.hisab.ui.dialog.EditPaidReceivedItemDialog;
import io.github.zkhan93.hisab.ui.dialog.ExpenseItemDialog;
import io.github.zkhan93.hisab.ui.dialog.PaidReceivedItemDialog;
import io.github.zkhan93.hisab.util.Util;

public class DetailGroupActivity extends AppCompatActivity implements View.OnClickListener,
        GroupRenameClbk, ExpenseItemClbk, PreferenceChangeListener {
    public static final String TAG = DetailGroupActivity.class.getSimpleName();

    @BindView(R.id.fabMenu)
    FloatingActionMenu fabMenu;
    @BindView(R.id.fabCreateShared)
    FloatingActionButton fabShareEntry;
    @BindView(R.id.fabCreatePaidReceived)
    FloatingActionButton fabGiveTakeEntryEntry;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private DatabaseReference groupExpensesRef, dbGrpNameRef, dbRef;
    private FirebaseUser firebaseUser;
    private String groupId, groupName;
    private User me;
    private ArrayList<String> sharedUserIds;

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
        sharedUserIds = new ArrayList<>();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        setSupportActionBar(toolbar);
        fabMenu.setClosedOnTouchOutside(true);
        fabShareEntry.setOnClickListener(this);
        fabGiveTakeEntryEntry.setOnClickListener(this);
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
            case R.id.fabCreateShared:
                showAddExpenseView();
                fabMenu.close(false);
                break;
            case R.id.fabCreatePaidReceived:
                showAddGiveTakeEntryView();
                fabMenu.close(false);
                break;
            default:
                Log.d(TAG, "click not implemented");
        }
    }

    private void showAddExpenseView() {
        DialogFragment dialog = new ExpenseItemDialog();
        dialog.show(getFragmentManager(), ExpenseItemDialog.TAG);
    }

    private void showAddGiveTakeEntryView() {
        DialogFragment dialog = new PaidReceivedItemDialog();
        Bundle bundle = new Bundle();
        bundle.putParcelable("me", me);
        bundle.putString("groupId", groupId);
        dialog.setArguments(bundle);
        dialog.show(getFragmentManager(), PaidReceivedItemDialog.TAG);
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
        attemptRename(newName);
    }

    private void attemptRename(final String newName) {
        dbRef.child("shareWith").child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dbs : dataSnapshot.getChildren()) {
                    sharedUserIds.add(dbs.getKey());
                }
                //get moderators groups name's link
                dbRef.child("groups").child(me.getId()).child(groupId).child("moderator").child
                        ("id")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                sharedUserIds.add(dataSnapshot.getValue(String.class));
                                //update all group entries
                                Map<String, Object> updateLocation = new HashMap<>();
                                for (String userId : sharedUserIds) {
                                    updateLocation.put("/groups/" + userId + "/" + groupId +
                                            "/name", newName);
                                }
                                dbRef.updateChildren(updateLocation, new DatabaseReference
                                        .CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError,
                                                           DatabaseReference
                                                                   databaseReference) {
                                        if (databaseError != null)
                                            Log.d(TAG, "Error occurred" + databaseError
                                                    .getMessage());
                                    }
                                });
                                dbGrpNameRef.setValue(newName);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.e(TAG, "error fetching shared with user ids");
                            }
                        });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "error fetching shared with user ids");
            }
        });
    }

    /**
     * Called from {@link ExpenseItemDialog} and {@link PaidReceivedItemDialog}'s positive
     * button's [@link OnClickListener]
     *
     * @param description
     * @param amount
     */
    public void createExpense(String description, float amount, int itemType, User with, int
            shareType) {
        ExpenseItem expenseItem;
        if (itemType == ExpenseItem.ITEM_TYPE.PAID_RECEIVED)
            expenseItem = new ExpenseItem(description, amount, with, shareType);
        else
            expenseItem = new ExpenseItem(description, amount);
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
        fabMenu.hideMenu(true);
        setTitle("Share with");
    }

    @Override
    public void showEditUi(ExpenseItem expense) {
        switch (expense.getItemType()) {
            case ExpenseItem.ITEM_TYPE.SHARED:
                EditExpenseItemDialog dialog = new EditExpenseItemDialog();
                Bundle bundle = new Bundle();
                bundle.putParcelable("expense", expense);
                dialog.setArguments(bundle);
                dialog.show(getFragmentManager(), EditExpenseItemDialog.TAG);
                break;
            case ExpenseItem.ITEM_TYPE.PAID_RECEIVED:
                EditPaidReceivedItemDialog pdialog = new EditPaidReceivedItemDialog();
                Bundle pbundle = new Bundle();
                pbundle.putParcelable("expense", expense);
                pbundle.putString("groupId", groupId);
                pbundle.putParcelable("me", me);
                pdialog.setArguments(pbundle);
                pdialog.show(getFragmentManager(), EditPaidReceivedItemDialog.TAG);
                break;
            default:
                Log.d(TAG, "trying to edit a invalid expense item");
        }
    }

    @Override
    public void delete(final String expenseId) {
        final AlertDialog confirmDelete = new AlertDialog.Builder(this, 0)
                .setMessage("Do you want to delete?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialogInterface, int i) {
                        groupExpensesRef.child(expenseId).removeValue().addOnCompleteListener(
                                new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull
                                                           Task<Void> task) {
                                        if (!task.isSuccessful()) {
                                            final Snackbar snackbar =
                                                    Snackbar.make(toolbar,
                                                            "Error occurred: " + task.getException()
                                                                    .getLocalizedMessage(),
                                                            Snackbar
                                                                    .LENGTH_INDEFINITE);
                                            snackbar.setAction("OK", new
                                                    View.OnClickListener() {
                                                        @Override
                                                        public void
                                                        onClick(View view) {
                                                            snackbar.dismiss();
                                                        }
                                                    });
                                            snackbar.show();
                                        }
                                    }
                                }

                        );
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }

                ).create();
        confirmDelete.show();
    }

    @Override
    public void update(ExpenseItem expense) {

        groupExpensesRef.child(expense.getId())
                .setValue(expense)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            final Snackbar snackbar = Snackbar.make(toolbar,
                                    "Error " +
                                            "Occurred:" +
                                            " " +
                                            task.getException()
                                                    .getLocalizedMessage(),
                                    Snackbar.LENGTH_SHORT);

                            snackbar.setAction("OK", new View
                                    .OnClickListener() {
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
        fabMenu.showMenu(true);
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
