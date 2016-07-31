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
import java.util.Collections;
import java.util.List;

import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.Group;
import io.github.zkhan93.hisab.model.User;
import io.github.zkhan93.hisab.model.callback.GroupItemClickClbk;
import io.github.zkhan93.hisab.model.viewholder.EmptyVH;
import io.github.zkhan93.hisab.model.viewholder.GroupItemVH;

/**
 * Created by Zeeshan Khan on 6/26/2016.
 */
public class GroupsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements
        ChildEventListener {
    public static final String TAG = GroupsAdapter.class.getSimpleName();
    List<Group> groups;
    GroupItemClickClbk groupItemClickClbk;
    private User me;
    private DatabaseReference dbRef;

    public GroupsAdapter(GroupItemClickClbk groupItemClickClbk, User me) {
        groups = new ArrayList<>();
        this.groupItemClickClbk = groupItemClickClbk;
        this.me = me;
        dbRef = FirebaseDatabase.getInstance().getReference("groups/" + me.getId());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE.EMPTY:
                return new EmptyVH(inflater.inflate(R.layout.empty, parent, false));
            default:
                return new GroupItemVH(inflater.inflate(R.layout.group_item, parent, false),
                        groupItemClickClbk);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE.NORMAL) {
            GroupItemVH gHolder = (GroupItemVH) holder;
            gHolder.setGroup(groups.get(position), me);
        }
        if (holder instanceof EmptyVH)
            ((EmptyVH) holder).setType(EmptyVH.TYPE.GROUP);
    }

    @Override
    public int getItemViewType(int position) {
        int count = groups.size();
        if (count == 0)
            return TYPE.EMPTY;
        return TYPE.NORMAL;
    }

    @Override
    public int getItemCount() {
        int count = groups.size();
        return count == 0 ? 1 : count;
    }

    public int findGroupIndex(String id) {
        int index = -1;
        int len = groups.size();
        for (int i = 0; i < len; i++) {
            if (groups.get(i).getId().equals(id)) {
                index = i;
                break;
            }
        }
        return index;
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        Group group = dataSnapshot.getValue(Group.class);
        group.setId(dataSnapshot.getKey());
        groups.add(group);
        notifyItemInserted(groups.size());
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        Group group = dataSnapshot.getValue(Group.class);
        group.setId(dataSnapshot.getKey());
        int index = findGroupIndex(dataSnapshot.getKey());
        if (index != -1) {
            groups.set(index, group);
            notifyItemChanged(index);
        }
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        Log.d(TAG, dataSnapshot.toString());
        Group group = dataSnapshot.getValue(Group.class);
        group.setId(dataSnapshot.getKey());
        int index = findGroupIndex(dataSnapshot.getKey());
        if (index != -1) {
            groups.remove(index);
            notifyItemRemoved(index);
        }
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        Log.d(TAG, "onChildMoved");
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.d(TAG, "onCancelled");
    }

    public void clear() {
        groups.clear();
        notifyDataSetChanged();
    }

    public void registerChildEventListener() {
        dbRef.orderByChild("name").addChildEventListener(this);
    }

    public void unregisterChildEventListener() {
        dbRef.removeEventListener(this);
    }

    public void sort(int type) {
        switch (type) {
            case Group.SORT_TYPE.ALPHABETICAL:
                Collections.sort(groups, Group.ALPHABETICAL);
                notifyDataSetChanged();
                break;
            case Group.SORT_TYPE.REVERSE_ALPHABETICAL:
                Collections.sort(groups, Group.REVERSE_ALPHABETICAL);
                notifyDataSetChanged();
                break;
            case Group.SORT_TYPE.CHRONOLOGICAL:
                Collections.sort(groups, Group.CHRONOLOGICAL);
                notifyDataSetChanged();
                break;
            case Group.SORT_TYPE.REVERSE_CHRONOLOGICAL:
                Collections.sort(groups, Group.REVERSE_CHRONOLOGICAL);
                notifyDataSetChanged();
                break;
            default:
                Collections.sort(groups, Group.ALPHABETICAL);
                notifyDataSetChanged();
                break;
        }
    }

    interface TYPE {
        int EMPTY = 0;
        int NORMAL = 1;
    }

}
