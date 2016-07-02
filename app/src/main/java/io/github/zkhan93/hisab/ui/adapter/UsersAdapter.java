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

import java.util.ArrayList;
import java.util.List;

import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.User;
import io.github.zkhan93.hisab.model.callback.UserItemActionClickClbk;
import io.github.zkhan93.hisab.model.callback.UserItemUiClickClbk;
import io.github.zkhan93.hisab.model.ui.ExUser;
import io.github.zkhan93.hisab.model.viewholder.EmptyVH;
import io.github.zkhan93.hisab.model.viewholder.UserVH;

/**
 * Created by Zeeshan Khan on 7/3/2016.
 */
public class UsersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements
        ChildEventListener, UserItemUiClickClbk {

    public static final String TAG = UsersAdapter.class.getSimpleName();

    private List<ExUser> users;
    private DatabaseReference dbRef;
    private UserItemActionClickClbk actionCallback;


    public UsersAdapter(UserItemActionClickClbk actionCallback) {
        users = new ArrayList<>();
        this.actionCallback = actionCallback;
        dbRef = FirebaseDatabase.getInstance().getReference("users");
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE.EMPTY)
            return new EmptyVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.empty,
                    parent, false));
        else
            return new UserVH(LayoutInflater.from(parent.getContext()).inflate(R.layout
                    .user_item, parent, false),actionCallback,this);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE.NORMAL) {
            ((UserVH) holder).setUser(users.get(position));
        }
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
        if (user != null && user.getId() != null && !user.getId().isEmpty()) {
            this.users.add(new ExUser(user));
            notifyItemChanged(users.size() - 1);
        }
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        User user = dataSnapshot.getValue(User.class);
        int index = findUserIndex(user.getId());
        if (index != -1) {
            users.set(index, new ExUser(user));
            notifyItemChanged(index);
        }
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        User user = dataSnapshot.getValue(User.class);
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

    public void registerChildListener() {
        dbRef.addChildEventListener(this);
    }

    public void unregisterChildListener() {
        dbRef.removeEventListener(this);
    }

    @Override
    public void updateUi(ExUser user) {
        int index = findUserIndex(user.getId());
        if (index != -1) {
            notifyItemChanged(index);
        }
    }

    private interface VIEW_TYPE {
        int NORMAL = 0;
        int EMPTY = 1;
    }


}
