package io.github.zkhan93.hisab.util;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.lang.ref.WeakReference;

import io.github.zkhan93.hisab.model.callback.ExpenseChildListenerClbk;

/**
 * Created by zeeshan on 12/4/2016.
 */

public class MyChildEventListener implements ChildEventListener {
    private String groupId;
    private WeakReference<ExpenseChildListenerClbk> childEventListenerRef;

    public MyChildEventListener(String groupId, ExpenseChildListenerClbk childEventListener) {
        this.groupId = groupId;
        childEventListenerRef = new WeakReference<ExpenseChildListenerClbk>(childEventListener);
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        if(childEventListenerRef.get()!=null)
            childEventListenerRef.get().onChildAdded(groupId,dataSnapshot,s);
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        if(childEventListenerRef.get()!=null)
            childEventListenerRef.get().onChildChanged(groupId,dataSnapshot,s);
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        if(childEventListenerRef.get()!=null)
            childEventListenerRef.get().onChildRemoved(groupId,dataSnapshot);
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        if(childEventListenerRef.get()!=null)
            childEventListenerRef.get().onChildMoved(groupId,dataSnapshot,s);
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        if(childEventListenerRef.get()!=null)
            childEventListenerRef.get().onCancelled(databaseError);
    }
}
