package io.github.zkhan93.hisab.ui;

import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.ExpenseItem;
import io.github.zkhan93.hisab.model.Group;
import io.github.zkhan93.hisab.model.User;
import io.github.zkhan93.hisab.model.callback.GroupItemClickClbk;
import io.github.zkhan93.hisab.model.callback.ShowMessageClbk;
import io.github.zkhan93.hisab.model.callback.SummaryActionItemClbk;
import io.github.zkhan93.hisab.ui.dialog.ConfirmDialog;
import io.github.zkhan93.hisab.ui.dialog.CreateGroupDialog;
import io.github.zkhan93.hisab.ui.dialog.GroupDetailDialog;
import io.github.zkhan93.hisab.util.Util;

import static io.github.zkhan93.hisab.ui.GroupsFragment.GRP_FRAGMENT_PERMISSIONS_REQUEST_READ_CONTACTS;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        OnCompleteListener<Void>, PreferenceChangeListener, DialogInterface.OnClickListener,
        SummaryActionItemClbk, ShowMessageClbk, GroupItemClickClbk, FirebaseAuth.AuthStateListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    @Nullable
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @Nullable
    @BindView(R.id.toolbar_expenses)
    Toolbar toolbar_expenses;
    @BindView(R.id.root_coordinate_layout)
    CoordinatorLayout rootCoordinatorLayout;
    @BindView(R.id.fragmentContainer)
    FrameLayout fragmentContainer;
    @Nullable
    @BindView(R.id.select_group_msg)
    View selectGroupMsg;
    @Nullable
    @BindView(R.id.expenses_container)
    View expensesContainer;

    private Snackbar snackbar;
    private Snackbar.Callback snackCallback;
    private boolean isTwoPaneMode;
    private DatabaseReference dbRef;
    private DatabaseReference groupExpensesRef, archiveRef, expensesRef;
    private FirebaseUser firebaseUser;
    private FirebaseAuth firebaseAuth;
    private User me;
    /**
     * to hold expenses until confirmed by User and then write it to firebase
     */
    private List<ExpenseItem> expenses;
    private String toDeleteExpenseId, activeGroupId, activeGroupName;

    {
        snackCallback = new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                super.onDismissed(snackbar, event);
                snackbarDismissed();
            }

            @Override
            public void onShown(Snackbar snackbar) {
                super.onShown(snackbar);
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        dbRef = FirebaseDatabase.getInstance().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.addAuthStateListener(this);
        me = Util.getUser(getApplicationContext());
        dbRef = FirebaseDatabase.getInstance().getReference();
        if (savedInstanceState != null) {
            expenses = savedInstanceState.getParcelableArrayList("expenses");
            toDeleteExpenseId = savedInstanceState.getString("toDeleteExpenseId");
            activeGroupId = savedInstanceState.getString("activeGroupId");
            activeGroupName = savedInstanceState.getString("activeGroupName");
        }
        //to start into group when we have intent with group Id and name set in it
        Intent intent = getIntent();
        int notificationId = -1;
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                activeGroupId = bundle.getString("groupId");
                activeGroupName = bundle.getString("groupName");
                notificationId = bundle.getInt("notificationId", -1);

            }
        }
        Log.d(TAG, String.format("notification clicked: %d %s : %s", notificationId, activeGroupId,
                activeGroupName));
//        //dismiss notifications if any
        if (notificationId != -1) {
            //we came here from an notification intent need to cancel that notification
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(notificationId);
        }
        snackbarDismissed(); //this will create a new snackbar and register callback with it
        isTwoPaneMode = findViewById(R.id.secFragmentContainer) != null;
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean
                ("isTwoPaneMode", isTwoPaneMode).apply();

        if (isTwoPaneMode && selectGroupMsg != null) {
            ImageView image = ButterKnife.findById(selectGroupMsg, R.id.image);
            TextView msg = ButterKnife.findById(selectGroupMsg, R.id.msg);
            if (image != null)
                image.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R
                        .drawable.big_item));
            if (msg != null) msg.setText(getString(R.string.msg_select_group));
        }

        if (activeGroupId != null && activeGroupName != null)
            onGroupClicked(activeGroupId, activeGroupName);
        else {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(GroupsFragment.TAG);
            if (fragment == null)
                fragment = new GroupsFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, fragment,
                    GroupsFragment.TAG).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_groups, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
//            case R.id.action_loadnotifications:
//                Util.showNotification(getApplicationContext());
//                return true;
            case R.id.action_logout:
                logout();
                return true;
            case android.R.id.home:
                if (!isTwoPaneMode) {
                    Fragment fragment = getSupportFragmentManager().findFragmentByTag
                            (GroupsFragment.TAG);
                    if (fragment == null)
                        fragment = new GroupsFragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id
                            .fragmentContainer, fragment, GroupsFragment.TAG).commit();
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    activeGroupId = null;
                    activeGroupName = null;
                    toDeleteExpenseId = null;
                    expenses = null;
                    setTitle(getString(R.string.title_activity_groups));
                } else {
                    //Home button won't be there for clicking if this is a two pane mode
                }
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
//        Util.scheduleJob(getApplicationContext());
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            default:
                Log.d(TAG, "click not implemented");
        }
    }

    /**
     * {@link CreateGroupDialog} calls this method
     *
     * @param groupName
     */
    public void createGroup(String groupName) {
        Group group = new Group();
        group.setName(groupName);
        group.setMembersCount(1);
        group.setCreatedOn(java.util.Calendar.getInstance().getTimeInMillis());
        group.setModerator(me);
        Log.d(TAG, group.toString() + "");
        dbRef.child("groups/" + me.getId()).push().setValue(group).addOnCompleteListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("expenses", (ArrayList) expenses);
        outState.putString("toDeleteExpenseId", toDeleteExpenseId);
        outState.putString("activeGroupId", activeGroupId);
        outState.putString("activeGroupName", activeGroupName);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case GRP_FRAGMENT_PERMISSIONS_REQUEST_READ_CONTACTS: {
                Fragment grpFragment = getSupportFragmentManager().findFragmentByTag(GroupsFragment.TAG);
                if (grpFragment == null || !(grpFragment instanceof GroupsFragment))
                    return;
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ((GroupsFragment) grpFragment).showCreateGroupDialog();
                } else {
                    ((GroupsFragment) grpFragment).showCreateGroupDialog();
                }
                return;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void logout() {
        firebaseAuth.signOut();
        Util.clearPreferences(getApplicationContext());
        //startActivity(LoginActivity) and finish() on this activity is called in Auth
        // listener method below
    }

    private void snackbarDismissed() {
        snackbar = Snackbar.make(rootCoordinatorLayout, "", Snackbar.LENGTH_INDEFINITE);
        snackbar.setCallback(snackCallback);
    }

    private void showSnackBar(String msg, int duration, String action) {
        if (snackbar == null) {
            snackbarDismissed();
        }
        if (msg != null)
            snackbar.setText(msg);
        snackbar.setDuration(duration);
        if (action != null)
            snackbar.setAction(action, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                 just let the snackbar hide itself don't call dismiss
                }
            });
        snackbar.show();
    }

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
                            showMessage(msg, ShowMessageClbk.TYPE.SNACKBAR,
                                    Snackbar.LENGTH_INDEFINITE);
                        }
                    }
                });
    }

    /**
     * {@link android.content.DialogInterface.OnClickListener} implementation
     */
    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        AlertDialog dialog = ((AlertDialog) dialogInterface);
        if (which == DialogInterface.BUTTON_POSITIVE) {
            Button positiveBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            int type = ConfirmDialog.TYPE.INVALID;
            Object typeObj = positiveBtn.getTag();
            if (typeObj != null)
                type = (int) typeObj;
            switch (type) {
                case ConfirmDialog.TYPE.EXPENSE_DELETE:
                    groupExpensesRef.child(toDeleteExpenseId).removeValue().addOnCompleteListener(
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
                case ConfirmDialog.TYPE.GROUP_ARCHIVE:
                    Map<String, Object> expensesMap = new HashMap<>();
                    for (ExpenseItem e : expenses) {
                        expensesMap.put(e.getId(), e.toMap());
                    }
                    expensesRef = dbRef.child("expenses").child(activeGroupId);
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
                case ConfirmDialog.TYPE.GROUP_DELETE:
                    ((GroupsFragment) getSupportFragmentManager().findFragmentByTag(GroupsFragment.TAG)).deleteSelectedGroupConfirmed();
                    ;
                    break;
                default:
                    Log.d(TAG, "invalid tag value in positive button");
            }
        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
            dialog.dismiss();
        } else {
            Log.d(TAG, "action on this button is not defined yet");
        }
    }

    /**
     * {@link SummaryActionItemClbk} implementation
     */
    @Override
    public void archiveGrp(final String groupId, final List<ExpenseItem> expenses) {
        Bundle bundle = new Bundle();
        bundle.putInt("type", ConfirmDialog.TYPE.GROUP_ARCHIVE);
        bundle.putString("msg", "Do you really want to archive?");//TODO: String resource
        bundle.putString("positiveBtnTxt", "Yes");//TODO: String resource
        bundle.putString("negativeBtnTxt", "No");//TODO: String resource
        this.expenses = expenses;
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setArguments(bundle);
        confirmDialog.show(getFragmentManager(), ConfirmDialog.TAG);

    }

    @Override
    public void moreInfo() {

    }

    /**
     * {@link GroupItemClickClbk} implementation
     */
    @Override
    public void onGroupClicked(String groupId, String groupName) {
        //updating member variables
        activeGroupId = groupId;
        activeGroupName = groupName;
        setTitle(activeGroupName);
        groupExpensesRef = dbRef.child("expenses").child(groupId);
        archiveRef = dbRef.child("archive").child(groupId);

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(ExpensesFragment.TAG);
        if (fragment == null) {
            fragment = new ExpensesFragment();
            Bundle bundle = new Bundle();
            bundle.putString("groupId", groupId);
            bundle.putString("groupName", groupName);
            fragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(isTwoPaneMode ? R.id
                            .secFragmentContainer : R.id.fragmentContainer, fragment,
                    ExpensesFragment.TAG).commit();
        } else {
            Log.d(TAG, "changing group Id" + groupId);
            getSupportFragmentManager().beginTransaction().replace(isTwoPaneMode ? R.id
                            .secFragmentContainer : R.id.fragmentContainer, fragment,
                    ExpensesFragment.TAG).commit();
            getSupportFragmentManager().executePendingTransactions();
            setTitle(activeGroupName);
            ((ExpensesFragment) fragment).changeGroup(groupId);
        }

        if (!isTwoPaneMode) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } else {
            selectGroupMsg.setVisibility(View.GONE);
            expensesContainer.setVisibility(View.VISIBLE);
        }
    }

    public void onGroupInfoClicked(Group group) {
        if (group == null)
            return;
        GroupDetailDialog groupDetailDialog = new GroupDetailDialog();
        Bundle bundle = new Bundle();
        bundle.putParcelable("me", me);
        bundle.putParcelable("group", group);
        groupDetailDialog.setArguments(bundle);
        groupDetailDialog.show(getFragmentManager(), GroupDetailDialog.TAG);
    }

    /**
     * {@link android.content.SharedPreferences.OnSharedPreferenceChangeListener} implementation
     */
    @Override
    public void preferenceChange(PreferenceChangeEvent preferenceChangeEvent) {
        String keyChanged = preferenceChangeEvent.getKey();
        if (keyChanged.equals("name") || keyChanged.equals("email") || keyChanged.equals
                ("user_id")) {
            me = Util.getUser(getApplicationContext());
        }
    }

    /**
     * {@link ShowMessageClbk} implementation
     */

    @Override
    public void showMessage(String msg, int how, int duration) {
        if (how == TYPE.SNACKBAR)
            showSnackBar(msg, duration, "Ok");
        else
            Toast.makeText(this, msg, duration).show();
    }

    /**
     * {@link OnCompleteListener} implementation
     */
    @Override
    public void onComplete(@NonNull Task<Void> task) {
        if (!task.isSuccessful()) {
            String error = "";
            if (task.getException() != null)
                error = task.getException().getLocalizedMessage();
            else
                error = "Unknown error";
            showSnackBar("Unable to create group " + error, Snackbar.LENGTH_INDEFINITE, "Ok");
        }
    }

    public void setToDeleteExpenseId(String expenseId) {
        this.toDeleteExpenseId = expenseId;
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        if (firebaseAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, EntryActivity.class));
        }
    }
}
