package io.github.zkhan93.hisab.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.ExpenseItem;
import io.github.zkhan93.hisab.model.Group;
import io.github.zkhan93.hisab.model.User;
import io.github.zkhan93.hisab.model.callback.ExpenseChildListenerClbk;
import io.github.zkhan93.hisab.ui.MainActivity;
import io.github.zkhan93.hisab.util.MyChildEventListener;

/**
 * Created by zeeshan on 11/20/2016.
 */

public class NotificationService extends Service implements FirebaseAuth.AuthStateListener {

    public static final String TAG = NotificationService.class.getSimpleName();

    private DatabaseReference dbRef, groupDbRef;
    private Set<String> groupKeys;
    private Map<String, Long> groupLastChecked;
    private ChildEventListener groupsChildEventListener;
    private ValueEventListener lastGroupVisitListner;
    private ExpenseChildListenerClbk expenseChildListenerClbk;
    private List<MyChildEventListener> expenseChildEventListenerList;
    private boolean isUserLoggedIn;
    private String userId;
    private List<String> groupsNotificationContent;
    private List<String> expensesNotificationContent;
    private User me;
    private Set<String> dirtyGroupIds;
    private long lastGroupsVisit;
    private PendingIntent actionIntent;
    private NotificationCompat.Builder mBuilder;

    {
        groupsNotificationContent = new ArrayList<>();
        expensesNotificationContent = new ArrayList<>();
        dirtyGroupIds = new HashSet<>();
        groupKeys = new HashSet<>();
        groupLastChecked = new HashMap<>();
        expenseChildEventListenerList = new ArrayList<>();
        groupsChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "group added " + dataSnapshot);
                String key = dataSnapshot.getKey();
                groupKeys.add(key);
                Group group = dataSnapshot.getValue(Group.class);
                groupLastChecked.put(key, group.getLastCheckedOn());
                MyChildEventListener myExpenseChildEventListener = new MyChildEventListener(key, expenseChildListenerClbk);
                dbRef.child("expenses").child(key).addChildEventListener(myExpenseChildEventListener);
                expenseChildEventListenerList.add(myExpenseChildEventListener);
                if (!group.getModerator().getId().equals(me.getId()) && lastGroupsVisit < group.getUpdatedOn()) {
                    showNotification(group, ACTION.ADDED);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "group changed " + dataSnapshot);
                Group group = dataSnapshot.getValue(Group.class);
                groupLastChecked.put(dataSnapshot.getKey(), group.getLastCheckedOn());
                if (!group.getModerator().getId().equals(me.getId()) && lastGroupsVisit < group.getUpdatedOn()) {
                    showNotification(group, ACTION.UPDATE);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "group removed " + dataSnapshot);
                Group group = dataSnapshot.getValue(Group.class);
                String key = dataSnapshot.getKey();
                groupLastChecked.remove(key);
                MyChildEventListener expenseChildEventListener = null;
                Iterator<MyChildEventListener> iterator = expenseChildEventListenerList.iterator();
                while (iterator.hasNext()) {
                    expenseChildEventListener = iterator.next();
                    if (expenseChildEventListener.getGroupId().equals(key)) {
                        dbRef.child("expenses").child(key).removeEventListener(expenseChildEventListener);
                        iterator.remove();
                        break;
                    }
                }
                groupKeys.remove(key);
                if (!group.getModerator().getId().equals(me.getId()) && lastGroupsVisit < group.getUpdatedOn())
                    showNotification(group, ACTION.DELETE);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "group moved " + dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "group fetch error " + databaseError.getMessage());
            }
        };
        expenseChildListenerClbk = new ExpenseChildListenerClbk() {

            @Override
            public void onChildAdded(String groupId, DataSnapshot dataSnapshot, String prevKey) {
                ExpenseItem expense = dataSnapshot.getValue(ExpenseItem.class);
                if (!expense.getOwner().getId().equals(me.getId()) && groupLastChecked.get
                        (groupId) < expense.getCreatedOn()) {
                    Log.d(TAG, "expense added " + groupId + dataSnapshot);
                    showNotification(expense, ACTION.ADDED);
                    dirtyGroupIds.add(expense.getGroupId());
                }
            }

            @Override
            public void onChildChanged(String groupId, DataSnapshot dataSnapshot, String prevKey) {
                Log.d(TAG, "expense changed " + groupId + dataSnapshot);
                ExpenseItem expense = dataSnapshot.getValue(ExpenseItem.class);
                if (!expense.getOwner().getId().equals(me.getId()) && groupLastChecked.get
                        (groupId) < expense.getUpdatedOn()) {
                    Log.d(TAG, "expense changed " + groupId + dataSnapshot);
                    showNotification(expense, ACTION.UPDATE);
                    dirtyGroupIds.add(expense.getGroupId());
                }
            }

            @Override
            public void onChildRemoved(String groupId, DataSnapshot dataSnapshot) {
                Log.d(TAG, "expense removed " + groupId + dataSnapshot);
                ExpenseItem expense = dataSnapshot.getValue(ExpenseItem.class);
                if (!expense.getOwner().getId().equals(me.getId()) && groupLastChecked.get
                        (groupId) < expense.getUpdatedOn()) {
                    Log.d(TAG, "expense removed " + groupId + dataSnapshot);
                    showNotification(expense, ACTION.DELETE);
                    dirtyGroupIds.add(expense.getGroupId());
                }
            }

            @Override
            public void onChildMoved(String groupId, DataSnapshot dataSnapshot, String prevKey) {
                Log.d(TAG, "expense moved " + groupId + dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "expense fetch error " + databaseError.getMessage());
            }
        };
        lastGroupVisitListner = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                lastGroupsVisit = dataSnapshot.getValue(Long.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "service failed to fetch last group visit time from firebase");
            }
        };
        isUserLoggedIn = false;
        FirebaseAuth.getInstance().addAuthStateListener(this);
        mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R
                .mipmap.ic_launcher);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            //not logged in
            dbRef = null;
            isUserLoggedIn = false;
            return;
        }
        isUserLoggedIn = true;
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        me = dataSnapshot.getValue(User.class);
                        //add watcher on lastVisitOn value for me
                        dbRef.child("users").child(me.getId()).child("lastVisitOn").addValueEventListener
                                (lastGroupVisitListner);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "services failed to get user object from firebase");
                    }
                });
        actionIntent = PendingIntent.getActivity(this, 0, new Intent(getApplicationContext(),
                MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (groupDbRef != null)
            groupDbRef.removeEventListener(groupsChildEventListener);
        dbRef.child("users").child(me.getId()).child("lastVisitOn").removeEventListener(lastGroupVisitListner);
        super.onDestroy();
    }

    //auth change listener methods
    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        if (firebaseAuth.getCurrentUser() != null) {
            dbRef = FirebaseDatabase.getInstance().getReference();
            isUserLoggedIn = true;
            setChildEventListeners();
        } else {
            isUserLoggedIn = false;
        }
    }

    private void setChildEventListeners() {
        if (dbRef == null) {
            Log.d(TAG, "database reference is null not setting child listeners");
            return;
        }
        if (!isUserLoggedIn) {
            Log.d(TAG, "User is not logged in ");
        }
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        groupDbRef = dbRef.child("groups").child(userId);
        groupDbRef.addChildEventListener(groupsChildEventListener);
    }

    public void showNotification(ExpenseItem expenseItem, int type) {
        switch (type) {
            case ACTION.UPDATE:
                showNotification(NOTIFICATION_TYPE.EXPENSE, "Expenses", expenseItem
                        .getOwner().getName() + " updated: " + expenseItem.getDescription() + "@" +
                        expenseItem.getAmount());
                break;
            case ACTION.ADDED:
                showNotification(NOTIFICATION_TYPE.EXPENSE, "Expenses", expenseItem.getOwner().getName
                        () + " added: " + expenseItem.getDescription() + "@" + expenseItem.getAmount
                        ());
                break;
            case ACTION.DELETE:
                showNotification(NOTIFICATION_TYPE.EXPENSE, "Expenses", expenseItem
                        .getOwner().getName() + " removed: " + expenseItem.getDescription() + "@" +
                        expenseItem.getAmount());
                break;
            default:
                Log.d(TAG, "invalid expense update type");
        }
    }

    public void showNotification(Group group, int type) {
        switch (type) {
            case ACTION.ADDED:
                showNotification(NOTIFICATION_TYPE.GROUP, "Groups", "Included in " + group.getName
                        ());
                break;
            case ACTION.DELETE:
                showNotification(NOTIFICATION_TYPE.GROUP, "Groups", "Removed from " + group.getName());
                break;
            case ACTION.UPDATE:
                showNotification(NOTIFICATION_TYPE.GROUP, "Groups", "Renamed " + group.getName
                        ());
                break;
            default:
                Log.d(TAG, "invalid group update type");
        }
    }

    public void showNotification(int type, String title, String message) {
        mBuilder.setContentTitle(title);
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        if (type == NOTIFICATION_TYPE.EXPENSE) {
            expensesNotificationContent.add(0, message);
            if (expensesNotificationContent.size() == 1) {
                inboxStyle.setBigContentTitle(message);
                mBuilder.setContentText(message);
            } else {
                inboxStyle.setBigContentTitle(expensesNotificationContent.size() + " expenses " +
                        "update");
                mBuilder.setContentText(expensesNotificationContent.size() + " expenses update");
                for (String msg : expensesNotificationContent)
                    inboxStyle.addLine(msg);
                inboxStyle.setSummaryText(expensesNotificationContent.size() + " updates from " +
                        "" + dirtyGroupIds.size() + " groups");
            }

        } else {
            groupsNotificationContent.add(0, message);

            if (groupsNotificationContent.size() == 1) {
                inboxStyle.setBigContentTitle(message);
                mBuilder.setContentText(message);
            } else {
                inboxStyle.setBigContentTitle(groupsNotificationContent.size() + " group updates");
                mBuilder.setContentText(groupsNotificationContent.size() + " group updates");
            }

            for (String msg : groupsNotificationContent)
                inboxStyle.addLine(msg);
        }

        mBuilder.setStyle(inboxStyle);
        mBuilder.setContentIntent(actionIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // type allows you to update the notification later on.
        mNotificationManager.notify(type, mBuilder.build());
    }

    public interface NOTIFICATION_TYPE {
        int GROUP = 0;
        int EXPENSE = 1;
    }

    public interface ACTION {
        int UPDATE = 1;
        int DELETE = 2;
        int ADDED = 3;
    }
}
