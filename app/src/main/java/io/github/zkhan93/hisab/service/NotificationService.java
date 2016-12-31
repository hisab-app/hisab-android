package io.github.zkhan93.hisab.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.ArrayMap;
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
import java.util.Collections;
import java.util.Comparator;
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
    //    private List<GroupNotification> groupsNotificationContent;
    private ArrayMap<String, GroupNotification> groupsNotificationContent;
    //    private List<ExpenseNotification> expensesNotificationContent;
    private ArrayMap<String, ExpenseNotification> expensesNotificationContent;
    private User me;
    private long lastGroupsVisit;
    private PendingIntent actionIntent;
    private NotificationCompat.Builder mBuilder;

    {
        groupsNotificationContent = new ArrayMap<>();
        expensesNotificationContent = new ArrayMap<>();
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
                group.setId(key);
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
                group.setId(dataSnapshot.getKey());
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
                group.setId(key);
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
                Log.d(TAG, "expense added " + groupId + dataSnapshot);
                ExpenseItem expense = dataSnapshot.getValue(ExpenseItem.class);
                expense.setId(dataSnapshot.getKey());
                if (me != null && !expense.getOwner().getId().equals(me.getId()) &&
                        groupLastChecked.get
                                (groupId) < expense.getCreatedOn()) {
                    addNotificationItem(expense, ACTION.ADDED);

                }
            }

            @Override
            public void onChildChanged(String groupId, DataSnapshot dataSnapshot, String prevKey) {
                Log.d(TAG, "expense changed " + groupId + dataSnapshot);
                ExpenseItem expense = dataSnapshot.getValue(ExpenseItem.class);
                expense.setId(dataSnapshot.getKey());
                if (me != null && !expense.getOwner().getId().equals(me.getId()) && groupLastChecked.get
                        (groupId) < expense.getUpdatedOn()) {
                    addNotificationItem(expense, ACTION.UPDATE);

                }
            }

            @Override
            public void onChildRemoved(String groupId, DataSnapshot dataSnapshot) {
                Log.d(TAG, "expense removed " + groupId + dataSnapshot);
                ExpenseItem expense = dataSnapshot.getValue(ExpenseItem.class);
                expense.setId(dataSnapshot.getKey());
                if (me != null && !expense.getOwner().getId().equals(me.getId()) && groupLastChecked.get
                        (groupId) < expense.getUpdatedOn()) {
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
                Log.d(TAG, "lastGroupVisit updated :" + lastGroupsVisit);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "service failed to fetch last group visit time from firebase");
            }
        };
        mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R
                .drawable.ic_stat_hisab);
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate service");
        SharedPreferences spf = PreferenceManager.getDefaultSharedPreferences(getApplicationContext
                ());
        lastGroupsVisit = spf.getLong("service_lastGroupsVisit", Calendar.getInstance()
                .getTimeInMillis());
        isUserLoggedIn = spf.getBoolean("service_isUserLoggedIn", false);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            isUserLoggedIn = false;
        } else {
            isUserLoggedIn = true;
            init();
        }
        FirebaseAuth.getInstance().addAuthStateListener(this);
        super.onCreate();
    }

    private void init() {
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        me = dataSnapshot.getValue(User.class);
                        me.setId(dataSnapshot.getKey());
                        Log.d(TAG, "me updated");
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
        //save lastGroupsVisit
        Log.d(TAG, "service destroyed values saved");
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                .putLong("service_lastGroupsVisit", lastGroupsVisit)
                .putBoolean("service_isUserLoggedIn", isUserLoggedIn).apply();
        unregisterChildEventListeners();
        super.onDestroy();
    }

    //auth change listener methods
    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        if (firebaseAuth.getCurrentUser() != null) {
            isUserLoggedIn = true;
            init();
            registerChildEventListeners();
        } else {
            unregisterChildEventListeners();
            isUserLoggedIn = false;
            dbRef = null;
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
            groupDbRef = dbRef.child("groups").child(userId);
            groupDbRef.removeEventListener(groupsChildEventListener);
            dbRef.child("users").child(me.getId()).child("lastVisitOn").removeEventListener(lastGroupVisitListner);
        } else
            Log.d(TAG, "database reference is null cannot unregister child listeners");
        if (expenseChildEventListenerList != null)
            for (MyChildEventListener mel : expenseChildEventListenerList)
                dbRef.child("expenses").child(mel.getGroupId()).removeEventListener(mel);
        expenseChildEventListenerList.clear();
    }

    public void addNotificationItem(ExpenseItem expenseItem, int type) {
        switch (type) {
            case ACTION.UPDATE:
                expensesNotificationContent.put(expenseItem.getId(), new ExpenseNotification(expenseItem.getUpdatedOn
                        (), expenseItem.getGroupId(), expenseItem
                        .getOwner().getName() + " updated: " + expenseItem.getDescription() + "@" +
                        expenseItem.getAmount()));

                break;
            case ACTION.ADDED:
                expensesNotificationContent.put(expenseItem.getId(), new
                        ExpenseNotification(expenseItem.getCreatedOn(), expenseItem.getGroupId(),
                        expenseItem.getOwner().getName() + " added: " + expenseItem.getDescription()
                                + "@" + expenseItem.getAmount()));
                break;
            case ACTION.DELETE:
                expensesNotificationContent.put(expenseItem.getId(), new ExpenseNotification(Calendar.getInstance()
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
                groupsNotificationContent.put(group.getId(), new GroupNotification(group.getUpdatedOn(), "Included " +
                        "in " + group.getName
                        (), group.getId()));
                break;
            case ACTION.DELETE:
                groupsNotificationContent.put(group.getId(), new GroupNotification(Calendar.getInstance()
                        .getTimeInMillis(), "Removed from " + group
                        .getName(), group.getId()));
                break;
            case ACTION.UPDATE:
                groupsNotificationContent.put(group.getId(), new GroupNotification(group.getUpdatedOn(), "Renamed " + group.getName
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
        List<ExpenseNotification> expensesNotificationContentTmp = new ArrayList<>();
        expensesNotificationContentTmp.addAll(expensesNotificationContent.values());
        Collections.sort(expensesNotificationContentTmp, new Comparator<ExpenseNotification>() {
            @Override
            public int compare(ExpenseNotification et1, ExpenseNotification et2) {
                return (int) (et1.getCreateOn() - et2.getCreateOn());
            }
        });
        Iterator<ExpenseNotification> exIterator = expensesNotificationContentTmp.listIterator();
        while (exIterator.hasNext()) {
            ExpenseNotification exn = exIterator.next();
            if (groupLastChecked.containsKey(exn.getGroupId()) &&
                    exn.getCreateOn() <= groupLastChecked.get(exn.getGroupId()))
                exIterator.remove();
        }
        if (expensesNotificationContentTmp.size() > 0) {
            //show expense notification
            mBuilder.setContentTitle("Expenses");
            inboxStyle = new NotificationCompat.InboxStyle();
            if (expensesNotificationContentTmp.size() == 1) {
                inboxStyle.setBigContentTitle(expensesNotificationContentTmp.get(0).getMessage());
                mBuilder.setContentText(expensesNotificationContentTmp.get(0).getMessage());
            } else {
                inboxStyle.setBigContentTitle(expensesNotificationContentTmp.size() + " expenses " +
                        "update");
                mBuilder.setContentText(expensesNotificationContentTmp.size() + " expenses update");
                Set<String> groupCount = new HashSet<>();
                for (ExpenseNotification exn : expensesNotificationContentTmp) {
                    inboxStyle.addLine(exn.getMessage());
                    groupCount.add(exn.getGroupId());
                }
                inboxStyle.setSummaryText(expensesNotificationContentTmp.size() + " updates from " +
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
        List<GroupNotification> groupsNotificationContentTmp = new ArrayList<>();
        groupsNotificationContentTmp.addAll(groupsNotificationContent.values());
        Collections.sort(groupsNotificationContentTmp, new Comparator<GroupNotification>() {
            @Override
            public int compare(GroupNotification gpn1, GroupNotification gpn2) {
                return (int) (gpn1.getCreatedOn() - gpn2.getCreatedOn());
            }
        });
        Iterator<GroupNotification> grpIterator = groupsNotificationContentTmp.listIterator();
        while (grpIterator.hasNext()) {
            GroupNotification grpn = grpIterator.next();
            if (grpn.getCreatedOn() <= lastGroupsVisit) {
                grpIterator.remove();
            }
        }
        if (groupsNotificationContentTmp.size() > 0) {
            //show group notification
            mBuilder.setContentTitle("Groups");
            inboxStyle = new NotificationCompat.InboxStyle();

            if (groupsNotificationContentTmp.size() == 1) {
                inboxStyle.setBigContentTitle(groupsNotificationContentTmp.get(0).getMessage());
                mBuilder.setContentText(groupsNotificationContentTmp.get(0).getMessage());
            } else {
                inboxStyle.setBigContentTitle(groupsNotificationContentTmp.size() + " group updates");
                mBuilder.setContentText(groupsNotificationContentTmp.size() + " group updates");
            }

            for (GroupNotification grpn : groupsNotificationContentTmp)
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
