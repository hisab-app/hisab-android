package io.github.zkhan93.hisab.model.callback;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

/**
 * Created by zeeshan on 12/4/2016.
 */

public interface ExpenseChildListenerClbk {
    public void onChildAdded(String groupId, DataSnapshot dataSnapshot, String prevKey);

    public void onChildChanged(String groupId, DataSnapshot dataSnapshot, String prevKey);

    public void onChildRemoved(String groupId, DataSnapshot dataSnapshot);

    public void onChildMoved(String groupId, DataSnapshot dataSnapshot, String prevKey);

    public void onCancelled(DatabaseError databaseError);
}
