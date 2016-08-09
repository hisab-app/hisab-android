package io.github.zkhan93.hisab.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.User;
import io.github.zkhan93.hisab.model.callback.UserItemActionClickClbk;
import io.github.zkhan93.hisab.model.ui.ExUser;
import io.github.zkhan93.hisab.model.viewholder.EmptyVH;
import io.github.zkhan93.hisab.model.viewholder.UserVH;

/**
 * Created by Zeeshan Khan on 7/3/2016.
 */
public class UsersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements
        ChildEventListener {

    public static final String TAG = UsersAdapter.class.getSimpleName();

    private List<ExUser> users;
    private User moderator;
    private DatabaseReference dbRef, shareRef;
    private UserItemActionClickClbk actionCallback;
    private User me;
    private ChildEventListener shareChildListeners;
    private ValueEventListener moderatorListener;


    {
        shareChildListeners = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                User user = dataSnapshot.getValue(User.class);
                ExUser exUser = new ExUser(user);
                exUser.setChecked(true);
                int index = findUserIndex(exUser.getId());
                if (index != -1) {
                    users.set(index, exUser);
                    notifyItemChanged(index);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                User user = dataSnapshot.getValue(User.class);
                ExUser exUser = new ExUser(user);
                exUser.setChecked(true);
                int index = findUserIndex(exUser.getId());
                if (index != -1) {
                    users.set(index, exUser);
                    notifyItemChanged(index);
                }

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                int index = findUserIndex(user.getId());
                if (index != -1) {
                    users.set(index, new ExUser(user));
                    notifyItemChanged(index);
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "share child moved");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "share child cancelled");
            }
        };
        moderatorListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                moderator = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    public UsersAdapter(UserItemActionClickClbk actionCallback, User me, String groupId) {
        users = new ArrayList<>();
        this.actionCallback = actionCallback;
        dbRef = FirebaseDatabase.getInstance().getReference();
        shareRef = dbRef.child("shareWith").child(groupId);
        dbRef.child("groups").child(me.getId()).child(groupId).child("moderator")
                .addListenerForSingleValueEvent(moderatorListener);
        dbRef = dbRef.child("users");
        this.me = me;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE.EMPTY)
            return new EmptyVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.empty,
                    parent, false));
        else
            return new UserVH(LayoutInflater.from(parent.getContext()).inflate(R.layout
                    .user_item, parent, false), actionCallback);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE.NORMAL) {
            ((UserVH) holder).setUser(users.get(position));
        }
        if (holder instanceof EmptyVH)
            ((EmptyVH) holder).setType(EmptyVH.TYPE.USERS);
    }

    @Override
    public int getItemCount() {
        if (users == null || users.size() == 0)
            return 1;
        else
            return users.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (users == null || users.size() == 0)
            return VIEW_TYPE.EMPTY;
        else
            return VIEW_TYPE.NORMAL;
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        User user = dataSnapshot.getValue(User.class);
        if (user != null && user.getId() != null && !user.getId().isEmpty() && !user.getId()
                .equals(me.getId()) && !moderator.getId().equals(user.getId())) {
            this.users.add(new ExUser(user));
            notifyItemChanged(users.size() - 1);
        }
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        User user = dataSnapshot.getValue(User.class);
        if (user == null || user.getId().equals(me.getId()))
            return;
        int index = findUserIndex(user.getId());
        if (index != -1) {
            users.set(index, new ExUser(user));
            notifyItemChanged(index);
        }
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        User user = dataSnapshot.getValue(User.class);
        if (user == null || user.getId().equals(me.getId()))
            return;
        int index = findUserIndex(user.getId());
        if (index != -1) {
            users.remove(index);
            notifyItemRemoved(index);
        }
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        Log.d(TAG, dataSnapshot.toString() + " moved");
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.d(TAG, " cancelled");
    }

    private int findUserIndex(String id) {
        int index = -1;

        if (users != null) {
            int len = users.size();
            for (int i = 0; i < len; i++) {
                if (users.get(i).getId().equals(id)) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }

    public void clear() {
        users.clear();
        notifyDataSetChanged();
    }

    public void registerEventListener() {
        dbRef.addChildEventListener(this);
        shareRef.addChildEventListener(shareChildListeners);
    }

    public void unregisterEventListener() {
        dbRef.removeEventListener(this);
        shareRef.removeEventListener(shareChildListeners);
    }

    private interface VIEW_TYPE {
        int NORMAL = 0;
        int EMPTY = 1;
    }
}
