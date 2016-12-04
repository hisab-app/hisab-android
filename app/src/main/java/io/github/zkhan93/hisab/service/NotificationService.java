package io.github.zkhan93.hisab.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.github.zkhan93.hisab.model.callback.ExpenseChildListenerClbk;
import io.github.zkhan93.hisab.util.MyChildEventListener;

/**
 * Created by zeeshan on 11/20/2016.
 */

public class NotificationService extends Service implements FirebaseAuth.AuthStateListener {

    public static final String TAG = NotificationService.class.getSimpleName();

    private DatabaseReference dbRef, groupDbRef;
    private List<String> groupKeys;
    private ChildEventListener groupChildEventListener;
    private ExpenseChildListenerClbk expenseChildListenerClbk;
    private List<MyChildEventListener> expenseChildEventListenerList;
    private boolean isUserLoggedIn;
    private String userId;

    {
        groupKeys = new ArrayList<>();
        expenseChildEventListenerList = new ArrayList<>();
        groupChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "group added " + dataSnapshot);
                String key=dataSnapshot.getKey();
                groupKeys.add(key);
                MyChildEventListener myExpenseChildEventListener = new MyChildEventListener(key, expenseChildListenerClbk);
                dbRef.child("expenses").child(key).addChildEventListener(myExpenseChildEventListener);
                expenseChildEventListenerList.add(myExpenseChildEventListener);
                //todo: show group added notification if the owner is not current user
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "group changed " + dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "group removed " + dataSnapshot);
                String key = dataSnapshot.getKey();
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
                //todo: show notification for expense added is the user is not me
                Log.d(TAG, "expense added " + groupId + dataSnapshot);
            }

            @Override
            public void onChildChanged(String groupId, DataSnapshot dataSnapshot, String prevKey) {
                Log.d(TAG, "expense changed " + groupId + dataSnapshot);
            }

            @Override
            public void onChildRemoved(String groupId, DataSnapshot dataSnapshot) {
                Log.d(TAG, "expense removed " + groupId + dataSnapshot);
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
        isUserLoggedIn = false;
        FirebaseAuth.getInstance().addAuthStateListener(this);
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
            groupDbRef.removeEventListener(groupChildEventListener);
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
        groupDbRef.addChildEventListener(groupChildEventListener);
    }
}
