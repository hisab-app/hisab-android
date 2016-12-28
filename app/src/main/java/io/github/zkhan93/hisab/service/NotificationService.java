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
import java.util.Calendar;
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
import io.github.zkhan93.hisab.model.notification.ExpenseNotification;
import io.github.zkhan93.hisab.model.notification.GroupNotification;
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
    private List<GroupNotification> groupsNotificationContent;
    private List<ExpenseNotification> expensesNotificationContent;
    private User me;
    private long lastGroupsVisit;
    private PendingIntent actionIntent;
    private NotificationCompat.Builder mBuilder;

    {
        groupsNotificationContent = new ArrayList<>();
        expensesNotificationContent = new ArrayList<>();
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
                if (me != null && !group.getModerator().getId().equals(me.getId()) &&
                        lastGroupsVisit < group
                                .getUpdatedOn()) {
                    addNotificationItem(group, ACTION.ADDED);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "group changed " + dataSnapshot);
                Group group = dataSnapshot.getValue(Group.class);
                groupLastChecked.put(dataSnapshot.getKey(), group.getLastCheckedOn());
                if (me != null && !group.getModerator().getId().equals(me.getId()) && lastGroupsVisit < group.getUpdatedOn()) {
                    addNotificationItem(group, ACTION.UPDATE);
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
                if (me != null && !group.getModerator().getId().equals(me.getId()) &&
                        lastGroupsVisit < group.getUpdatedOn())
                    addNotificationItem(group, ACTION.DELETE);
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
                if (me != null && !expense.getOwner().getId().equals(me.getId()) &&
                        groupLastChecked.get
                                (groupId) < expense.getCreatedOn()) {
                    Log.d(TAG, "expense added " + groupId + dataSnapshot);
                    addNotificationItem(expense, ACTION.ADDED);

                }
            }

            @Override
            public void onChildChanged(String groupId, DataSnapshot dataSnapshot, String prevKey) {
                Log.d(TAG, "expense changed " + groupId + dataSnapshot);
                ExpenseItem expense = dataSnapshot.getValue(ExpenseItem.class);
                if (me != null && !expense.getOwner().getId().equals(me.getId()) && groupLastChecked.get
                        (groupId) < expense.getUpdatedOn()) {
                    Log.d(TAG, "expense changed " + groupId + dataSnapshot);
                    addNotificationItem(expense, ACTION.UPDATE);

                }
            }

            @Override
            public void onChildRemoved(String groupId, DataSnapshot dataSnapshot) {
                Log.d(TAG, "expense removed " + groupId + dataSnapshot);
                ExpenseItem expense = dataSnapshot.getValue(ExpenseItem.class);
                if (me != null && !expense.getOwner().getId().equals(me.getId()) && groupLastChecked.get
                        (groupId) < expense.getUpdatedOn()) {
                    Log.d(TAG, "expense removed " + groupId + dataSnapshot);
                    addNotificationItem(expense, ACTION.DELETE);

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
                .drawable.ic_stat_hisab);
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
        init();
    }

    private void init() {
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
        unregisterChildEventListeners();
        super.onDestroy();
    }

    //auth change listener methods
    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        if (firebaseAuth.getCurrentUser() != null) {
            init();
            dbRef = FirebaseDatabase.getInstance().getReference();
            isUserLoggedIn = true;
            registerChildEventListeners();
        } else {
            isUserLoggedIn = false;
            unregisterChildEventListeners();
        }
    }

    private void registerChildEventListeners() {
        if (dbRef == null) {
            Log.d(TAG, "database reference is null not registerring child listeners");
            return;
        }
        if (!isUserLoggedIn) {
            Log.d(TAG, "User is not logged in ");
        }
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        groupDbRef = dbRef.child("groups").child(userId);
        groupDbRef.addChildEventListener(groupsChildEventListener);
    }

    private void unregisterChildEventListeners() {
        if (dbRef != null) {
            Log.d(TAG, "database reference is null cannot unregister child listeners");
            groupDbRef = dbRef.child("groups").child(userId);
            groupDbRef.removeEventListener(groupsChildEventListener);
            dbRef.child("users").child(me.getId()).child("lastVisitOn").removeEventListener(lastGroupVisitListner);
        }
        if (expenseChildEventListenerList != null)
            for (MyChildEventListener mel : expenseChildEventListenerList)
                dbRef.child("expenses").child(mel.getGroupId()).removeEventListener(mel);
        expenseChildEventListenerList.clear();
    }

    public void addNotificationItem(ExpenseItem expenseItem, int type) {
        switch (type) {
            case ACTION.UPDATE:
                expensesNotificationContent.add(new ExpenseNotification(expenseItem.getUpdatedOn
                        (), expenseItem.getGroupId(), expenseItem
                        .getOwner().getName() + " updated: " + expenseItem.getDescription() + "@" +
                        expenseItem.getAmount()));

                break;
            case ACTION.ADDED:
                expensesNotificationContent.add(new
                        ExpenseNotification(expenseItem.getCreatedOn(), expenseItem.getGroupId(),
                        expenseItem.getOwner().getName() + " added: " + expenseItem.getDescription()
                                + "@" + expenseItem.getAmount()));
                break;
            case ACTION.DELETE:
                expensesNotificationContent.add(new ExpenseNotification(Calendar.getInstance()
                        .getTimeInMillis(), expenseItem.getGroupId(), expenseItem
                        .getOwner().getName() + " removed: " + expenseItem.getDescription() + "@" +
                        expenseItem.getAmount()));
                break;
            default:
                Log.d(TAG, "invalid expense update type");
        }
        showNotification();
    }

    public void addNotificationItem(Group group, int type) {
        switch (type) {
            case ACTION.ADDED:
                groupsNotificationContent.add(new GroupNotification(group.getUpdatedOn(), "Included " +
                        "in " + group.getName
                        (), group.getId()));
                break;
            case ACTION.DELETE:
                groupsNotificationContent.add(new GroupNotification(Calendar.getInstance()
                        .getTimeInMillis(), "Removed from " + group
                        .getName(), group.getId()));
                break;
            case ACTION.UPDATE:
                groupsNotificationContent.add(new GroupNotification(group.getUpdatedOn(), "Renamed " + group.getName
                        (), group.getId()));
                break;
            default:
                Log.d(TAG, "invalid group update type");
        }
        showNotification();
    }

    public void showNotification() {
        NotificationCompat.InboxStyle inboxStyle;
        //clean expense notification content list
        Iterator<ExpenseNotification> exIterator = expensesNotificationContent.listIterator();
        while (exIterator.hasNext()) {
            ExpenseNotification exn = exIterator.next();
            if (groupLastChecked.containsKey(exn.getGroupId()) &&
                    exn.getCreateOn() <= groupLastChecked.get(exn.getGroupId()))
                exIterator.remove();
        }
        if (expensesNotificationContent.size() > 0) {
            //show expense notification
            mBuilder.setContentTitle("Expenses");
            inboxStyle = new NotificationCompat.InboxStyle();
            if (expensesNotificationContent.size() == 1) {
                inboxStyle.setBigContentTitle(expensesNotificationContent.get(0).getMessage());
                mBuilder.setContentText(expensesNotificationContent.get(0).getMessage());
            } else {
                inboxStyle.setBigContentTitle(expensesNotificationContent.size() + " expenses " +
                        "update");
                mBuilder.setContentText(expensesNotificationContent.size() + " expenses update");
                Set<String> groupCount = new HashSet<>();
                for (ExpenseNotification exn : expensesNotificationContent) {
                    inboxStyle.addLine(exn.getMessage());
                    groupCount.add(exn.getGroupId());
                }
                inboxStyle.setSummaryText(expensesNotificationContent.size() + " updates from " +
                        "" + groupCount.size() + " groups");
            }
            mBuilder.setStyle(inboxStyle);
            mBuilder.setContentIntent(actionIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            // type allows you to update the notification later on.
            mNotificationManager.notify(NOTIFICATION_TYPE.EXPENSE, mBuilder.build());
        }

        //clean group notification content list
        Iterator<GroupNotification> grpIterator = groupsNotificationContent.listIterator();
        while (grpIterator.hasNext()) {
            GroupNotification grpn = grpIterator.next();
            if (grpn.getCreatedOn() <= lastGroupsVisit) {
                grpIterator.remove();
            }
        }
        if (groupsNotificationContent.size() > 0) {
            //show group notification
            mBuilder.setContentTitle("Groups");
            inboxStyle = new NotificationCompat.InboxStyle();

            if (groupsNotificationContent.size() == 1) {
                inboxStyle.setBigContentTitle(groupsNotificationContent.get(0).getMessage());
                mBuilder.setContentText(groupsNotificationContent.get(0).getMessage());
            } else {
                inboxStyle.setBigContentTitle(groupsNotificationContent.size() + " group updates");
                mBuilder.setContentText(groupsNotificationContent.size() + " group updates");
            }

            for (GroupNotification grpn : groupsNotificationContent)
                inboxStyle.addLine(grpn.getMessage());
            mBuilder.setStyle(inboxStyle);
            mBuilder.setContentIntent(actionIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            // type allows you to update the notification later on.
            mNotificationManager.notify(NOTIFICATION_TYPE.GROUP, mBuilder.build());
        }


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
