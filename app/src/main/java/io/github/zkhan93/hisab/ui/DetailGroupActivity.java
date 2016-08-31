package io.github.zkhan93.hisab.ui;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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
import io.github.zkhan93.hisab.model.callback.SummaryActionItemClbk;
import io.github.zkhan93.hisab.ui.dialog.EditExpenseItemDialog;
import io.github.zkhan93.hisab.ui.dialog.EditPaidReceivedItemDialog;
import io.github.zkhan93.hisab.ui.dialog.ExpenseItemDialog;
import io.github.zkhan93.hisab.ui.dialog.PaidReceivedItemDialog;
import io.github.zkhan93.hisab.util.Util;

public class DetailGroupActivity extends AppCompatActivity implements View.OnClickListener,
        GroupRenameClbk, ExpenseItemClbk, PreferenceChangeListener, SummaryActionItemClbk,
        DialogInterface.OnClickListener {
    public static final String TAG = DetailGroupActivity.class.getSimpleName();

    @BindView(R.id.fabMenu)
    FloatingActionMenu fabMenu;
    @BindView(R.id.fabCreateShared)
    FloatingActionButton fabShareEntry;
    @BindView(R.id.fabCreatePaidReceived)
    FloatingActionButton fabGiveTakeEntryEntry;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.root_coordinate_layout)
    CoordinatorLayout rootCoordinatorLayout;

    private AlertDialog alertDialog;
    private DatabaseReference groupExpensesRef, dbGrpNameRef, dbRef, archiveRef, expensesRef;
    private FirebaseUser firebaseUser;
    private String groupId, groupName;
    private User me;
    private ArrayList<String> sharedUserIds;
    private Snackbar snackbar;

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
        archiveRef = dbRef.child("archive").child(groupId);
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
        alertDialog = new AlertDialog.Builder(this, 0).setPositiveButton("OK", this)
                .setNegativeButton("Cancel", this).create();
        snackbar = Snackbar.make(rootCoordinatorLayout, "", Snackbar.LENGTH_INDEFINITE);
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
        alertDialog.setMessage("Do you want to delete?");
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Delete", this);
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", this);
        alertDialog.setOnShowListener(
                new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTag(R.string
                                .dialog_key_expense_id,
                                expenseId);
                        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTag(R.string
                                .dialog_key_dialog_id,
                                DIALOG_IDS.EXPENSE_DELETE);
                    }
                }
        );
        alertDialog.show();
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

    @Override
    public void archiveGrp(final String groupId, final Map<String, Object> expenses) {
        alertDialog.setMessage("Do you want to archive?");
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", this);
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "No", this);
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTag(R.string.dialog_key_group_id,
                        groupId);
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTag(R.string.dialog_key_expense_map,
                        expenses);
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTag(R.string.dialog_key_dialog_id,
                        DIALOG_IDS.GROUP_ARCHIVE);
            }
        });
        alertDialog.show();
    }

    @Override
    public void moreInfo() {

    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        Button button = ((AlertDialog) dialog).getButton(which);
        int tagValue = (int) (button.getTag(R.string.dialog_key_dialog_id));//TODO:error here  Attempt to invoke virtual method 'int java.lang.Integer.intValue()' on a null object reference
        if (which == DialogInterface.BUTTON_POSITIVE) {
            switch (tagValue) {
                case DIALOG_IDS.EXPENSE_DELETE:
                    String expenseId = (String) button.getTag(R.string.dialog_key_expense_id);
                    groupExpensesRef.child(expenseId).removeValue().addOnCompleteListener(
                            new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull
                                                       Task<Void> task) {
                                    if (!task.isSuccessful()) {
                                        showSnackBar("Error occurred: " + task.getException()
                                                .getLocalizedMessage(), Snackbar
                                                .LENGTH_INDEFINITE, "OK");
                                    }
                                }
                            }

                    );
                    break;
                case DIALOG_IDS.GROUP_ARCHIVE:
                    String groupId = (String) button.getTag(R.string.dialog_key_dialog_id);
                    Map<String, Object> expensesMap = (Map<String, Object>) button.getTag
                            (R.string.dialog_key_expense_map);
                    expensesRef = dbRef.child("expenses").child(groupId);
                    archiveRef.push().setValue(expensesMap).addOnCompleteListener(this, new
                            OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                        expensesRef.setValue(null);
                                    else {
                                        showSnackBar("Couldn't archive! Try again later",
                                                Snackbar.LENGTH_INDEFINITE, "OK");
                                    }
                                }
                            });

                    break;
            }
        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
            dialog.dismiss();
        } else {
            Log.d(TAG, "invalid button");
        }
    }

    private void showSnackBar(String msg, int duration, String action) {
        if (snackbar != null) {
            if (msg != null)
                snackbar.setText(msg);
            snackbar.setDuration(duration);
            if (action != null)
                snackbar.setAction(action, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        snackbar.dismiss();
                    }
                });
            snackbar.show();
        }
    }

    interface DIALOG_IDS {
        int EXPENSE_DELETE = 1;
        int GROUP_ARCHIVE = 2;
    }

/*
    interface DIALOG_KEYS {
        int EXPENSE_ID = 1;
        int DIALOG_ID = 2;
        int GROUP_ID = 3;
        int EXPENSE_MAP = 4;
    }*/
}