package io.github.zkhan93.hisab.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import io.github.zkhan93.hisab.service.callbacks.FetchMeClbk;
import io.github.zkhan93.hisab.ui.MainActivity;
import io.github.zkhan93.hisab.util.MyChildEventListener;

/**
 * NotificationService to show notifications when app is not active
 * Created by zeeshan on 11/20/2016.
 */

public class NotificationService extends Service implements FirebaseAuth.AuthStateListener, SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String TAG = NotificationService.class.getSimpleName();

    private DatabaseReference dbRef, groupDbRef;
    private Map<String, Long> groupLastChecked;

    private ChildEventListener groupsChildEventListener;

    private ExpenseChildListenerClbk expenseChildListenerClbk;
    private List<MyChildEventListener> expenseChildEventListenerList;
    private FetchMeClbk fetchMeClbk;

    private String userId;
    private SharedPreferences sharedPreferences;
    private User me;
    private long lastGroupsVisit;

    private ArrayMap<String, GroupNotification> groupsNotificationContent;
    private ArrayMap<String, ExpenseNotification> expensesNotificationContent;
    private PendingIntent actionIntent;
    private NotificationCompat.Builder mBuilder;

    {
        groupsNotificationContent = new ArrayMap<>();
        expensesNotificationContent = new ArrayMap<>();

        groupLastChecked = new HashMap<>();
        expenseChildEventListenerList = new ArrayList<>();
        groupsChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "group added " + dataSnapshot);
                String key = dataSnapshot.getKey();

                Group group = dataSnapshot.getValue(Group.class);
                group.setId(key);
                groupLastChecked.put(key, getLastGroupsVisit(key));
                MyChildEventListener myExpenseChildEventListener = new MyChildEventListener(key, expenseChildListenerClbk);
                dbRef.child("expenses").child(key).addChildEventListener(myExpenseChildEventListener);
                expenseChildEventListenerList.add(myExpenseChildEventListener);
                if (!group.getModerator().getId().equals(me.getId()) &&
                        lastGroupsVisit < group.getUpdatedOn()) {
                    addNotificationItem(group, ACTION.ADDED);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "group changed " + dataSnapshot);
                Group group = dataSnapshot.getValue(Group.class);
                group.setId(dataSnapshot.getKey());

                if (me != null && !group.getModerator().getId().equals(me.getId()) &&
                        lastGroupsVisit < group.getUpdatedOn()) {
                    addNotificationItem(group, ACTION.UPDATE);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "group removed " + dataSnapshot);
                Group group = dataSnapshot.getValue(Group.class);
                String key = dataSnapshot.getKey();
                group.setId(key);
                MyChildEventListener expenseChildEventListener;
                Iterator<MyChildEventListener> iterator = expenseChildEventListenerList.iterator();
                while (iterator.hasNext()) {
                    expenseChildEventListener = iterator.next();
                    if (expenseChildEventListener.getGroupId().equals(key)) {
                        dbRef.child("expenses").child(key).removeEventListener(expenseChildEventListener);
                        iterator.remove();
                        break;
                    }
                }
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
        mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R
                .drawable.ic_stat_hisab);
        fetchMeClbk = new FetchMeClbk() {
            @Override
            public void onMeFetched(User user) {
                if (user == null)
                    return;
                me = user;
                groupDbRef.addChildEventListener(groupsChildEventListener);
            }
        };

    }

    @Override
    public void onCreate() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        lastGroupsVisit = sharedPreferences.getLong("lastGroupsVisit", Calendar.getInstance()
                .getTimeInMillis());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        FirebaseAuth.getInstance().addAuthStateListener(this);
        actionIntent = PendingIntent.getActivity(this, 0, new Intent(getApplicationContext(),
                MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        super.onCreate();
    }

    /**
     * called whenever Firebase authentication state is changed i.e., whenever user logs in or
     * logs out
     */
    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser fUser = firebaseAuth.getCurrentUser();
        if (fUser != null) {
            Log.d(TAG, "user logged in");
            userId = fUser.getUid();
            dbRef = FirebaseDatabase.getInstance().getReference();
            groupDbRef = dbRef.child("groups").child(userId);
            fetchMeFromFirebase(fetchMeClbk);
        } else {
            unregisterChildEventListeners();
            Log.d(TAG, "user not logged in");
            //will wait for user to log in
        }
    }

    /**
     * UserId should be set before calling this method
     *
     * @param fetchMeClbk callback object
     */
    private void fetchMeFromFirebase(final FetchMeClbk fetchMeClbk) {
        dbRef.child("users").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            Log.e(TAG, "Service received empty dataSnapshot while fetching me");
                            return;
                        }
                        User user = dataSnapshot.getValue(User.class);
                        user.setId(dataSnapshot.getKey());
                        fetchMeClbk.onMeFetched(user);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "services failed to get user object from firebase");
                    }
                });
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
        Log.d(TAG, "service destroyed");
        unregisterChildEventListeners();
        FirebaseAuth.getInstance().removeAuthStateListener(this);
        super.onDestroy();
    }


    private void unregisterChildEventListeners() {
        if (dbRef != null) {
            groupDbRef = dbRef.child("groups").child(userId);
            groupDbRef.removeEventListener(groupsChildEventListener);
        } else
            Log.d(TAG, "database reference is null cannot unregister child listeners");
        if (expenseChildEventListenerList != null) {
            for (MyChildEventListener mel : expenseChildEventListenerList)
                dbRef.child("expenses").child(mel.getGroupId()).removeEventListener(mel);
            expenseChildEventListenerList.clear();
        }
    }

    public void addNotificationItem(ExpenseItem expenseItem, int type) {
        switch (type) {
            case ACTION.UPDATE:
                expensesNotificationContent.put(expenseItem.getId(), new ExpenseNotification(expenseItem.getUpdatedOn
                        (), expenseItem.getGroupId(), expenseItem
                        .getOwner().getName() + " updated: " + expenseItem.getDescription() + " - "+
                        expenseItem.getAmount()));

                break;
            case ACTION.ADDED:
                expensesNotificationContent.put(expenseItem.getId(), new
                        ExpenseNotification(expenseItem.getCreatedOn(), expenseItem.getGroupId(),
                        expenseItem.getOwner().getName() + " added: " + expenseItem.getDescription()
                                + " - " + expenseItem.getAmount()));
                break;
            case ACTION.DELETE:
                expensesNotificationContent.put(expenseItem.getId(), new ExpenseNotification(Calendar.getInstance()
                        .getTimeInMillis(), expenseItem.getGroupId(), expenseItem
                        .getOwner().getName() + " removed: " + expenseItem.getDescription() + " - "+
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
                return (int) (et2.getCreateOn() - et1.getCreateOn());
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
                int max_lines = 8;
                for (ExpenseNotification exn : expensesNotificationContentTmp) {
                    inboxStyle.addLine(exn.getMessage());
                    groupCount.add(exn.getGroupId());
                    max_lines--;
                    if (max_lines == 0)
                        break;
                }
                inboxStyle.setSummaryText(expensesNotificationContentTmp.size() + " updates from " +
                        "" + groupCount.size() + " groups");
            }
            mBuilder.setStyle(inboxStyle);
            mBuilder.setContentIntent(actionIntent);
            mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
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
                return (int) (gpn2.getCreatedOn() - gpn1.getCreatedOn());
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
            int max_lines = 8;
            for (GroupNotification grpn : groupsNotificationContentTmp) {
                inboxStyle.addLine(grpn.getMessage());
                max_lines--;
                if (max_lines == 0)
                    break;
            }
            mBuilder.setStyle(inboxStyle);
            mBuilder.setContentIntent(actionIntent);
            mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            // type allows you to update the notification later on.
            mNotificationManager.notify(NOTIFICATION_TYPE.GROUP, mBuilder.build());
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key == null || sharedPreferences == null)
            return;
        Log.d(TAG, "preference update " + key);
        if (key.equals("lastGroupsVisit")) {
            lastGroupsVisit = sharedPreferences.getLong(key, Calendar.getInstance().getTimeInMillis());;
            groupLastChecked.put(key, lastGroupsVisit);
        }
    }

    public long getLastGroupsVisit(String groupId) {
        if (sharedPreferences == null)
            return -1;
        return sharedPreferences.getLong(groupId, -1);
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
