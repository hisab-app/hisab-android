package io.github.zkhan93.hisab.service;


import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import io.github.zkhan93.hisab.HisabApplication;
import io.github.zkhan93.hisab.model.ExpenseItem;
import io.github.zkhan93.hisab.model.Group;
import io.github.zkhan93.hisab.model.User;
import io.github.zkhan93.hisab.model.notification.DaoSession;
import io.github.zkhan93.hisab.model.notification.LocalExpense;
import io.github.zkhan93.hisab.model.notification.LocalGroup;
import io.github.zkhan93.hisab.util.Util;

/**
 * Created by zeeshan on 4/4/2017.
 */

public class SyncAndNotifyJob extends JobService {
    public static final String TAG = SyncAndNotifyJob.class.getSimpleName();
    int groupsCnt = 0;
    List<ExpenseItem> expenseItems = new ArrayList<>();
    List<Group> groups = new ArrayList<>();
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    @Override
    public boolean onStartJob(JobParameters job) {
        Log.d(TAG, "job ran scheduling another");
        User me = Util.getUser(getApplicationContext());
        databaseReference.child("groups").child(me.getId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //all groups
                        groups.clear();
                        for (DataSnapshot groupDs : dataSnapshot.getChildren()) {
                            final Group group = groupDs.getValue(Group.class);
                            group.setId(groupDs.getKey());
                            groups.add(group);
                            inc();
                            databaseReference.child("expenses").child(group.getId())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            //all expenese for group.getId()
                                            for (DataSnapshot expenseDs : dataSnapshot.getChildren()) {
                                                ExpenseItem expenseItem = expenseDs.getValue
                                                        (ExpenseItem.class);
                                                expenseItem.setId(expenseDs.getKey());
                                                if (expenseItem.getUpdatedOn() >= group.getLastCheckedOn()) {
                                                    expenseItems.add(expenseItem);
                                                }
                                            }
                                            dec();
                                            if (groupsCnt == 0) {
                                                //all groups items loaded
                                                showNotifications();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        Util.scheduleJob(getApplicationContext());
        return false;
    }

    private void inc() {
        groupsCnt += 1;
    }

    private void dec() {
        groupsCnt -= 1;
    }

    private void showNotifications() {
        DaoSession daoSession = ((HisabApplication) getApplication()).getDaoSession();
        for (Group group : groups) {
            LocalGroup localGroup = new LocalGroup();
            localGroup.setId(group.getId());
            localGroup.setName(group.getName());
            daoSession.getLocalGroupDao().insertOrReplace(localGroup);
        }
        for (ExpenseItem expenseItem : expenseItems) {
            LocalExpense localExpense = new LocalExpense();
            localExpense.setId(expenseItem.getId());
            localExpense.setDesc(expenseItem.getDescription());
            localExpense.setType(expenseItem.getItemType());
            localExpense.setOwnerName(expenseItem.getOwner().getName());
            localExpense.setOwnerId(expenseItem.getOwner().getId());
            localExpense.setTimestamp(expenseItem.getUpdatedOn());
            localExpense.setAmount(expenseItem.getAmount());
            daoSession.getLocalExpenseDao().insertOrReplace(localExpense);
        }
        Util.showNotification(getApplicationContext());
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return true;
    }
}
