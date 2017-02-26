package io.github.zkhan93.hisab.ui.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.Group;
import io.github.zkhan93.hisab.model.User;
import io.github.zkhan93.hisab.model.callback.ContextActionBarClbk;
import io.github.zkhan93.hisab.model.callback.GroupItemClickClbk;
import io.github.zkhan93.hisab.model.callback.GrpSelectModeClbk;
import io.github.zkhan93.hisab.model.callback.NotificationCountLoadClbk;
import io.github.zkhan93.hisab.model.callback.OnLongClickGroupItemClbk;
import io.github.zkhan93.hisab.model.events.ExpenseAddedEvent;
import io.github.zkhan93.hisab.model.ui.ExGroup;
import io.github.zkhan93.hisab.model.viewholder.EmptyVH;
import io.github.zkhan93.hisab.model.viewholder.GroupItemVH;
import io.github.zkhan93.hisab.model.viewholder.HeaderVH;

/**
 * Created by Zeeshan Khan on 6/26/2016.
 */
public class GroupsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements
        ChildEventListener, GrpSelectModeClbk, GroupItemClickClbk, OnLongClickGroupItemClbk,
        ActionMode.Callback, NotificationCountLoadClbk {
    public static final String TAG = GroupsAdapter.class.getSimpleName();
    GroupItemClickClbk groupItemClickClbk;
    boolean selectionMode;
    private Map<String, Integer> newItemCount;
    private List<ExGroup> groups;
    private User me;
    private DatabaseReference grpDbRef, dbRef;
    private ContextActionBarClbk contextActionBarClbk;
    private int selectedGroupsCount = 0;
    private int favCount = 0;

    {
        selectionMode = false;
        newItemCount = new HashMap<>();
    }

    public GroupsAdapter(GroupItemClickClbk groupItemClickClbk, User me, ContextActionBarClbk
            contextActionBarClbk) {
        groups = new ArrayList<>();

        this.groupItemClickClbk = groupItemClickClbk;
        this.me = me;
        dbRef = FirebaseDatabase.getInstance().getReference("");
        grpDbRef = dbRef.child("groups/" + me.getId());
        this.contextActionBarClbk = contextActionBarClbk;
    }

    private void startMultiSelectMode() {
        //TODO:initialize traking array and start cab
        contextActionBarClbk.showCAB();
        selectionMode = true;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE.EMPTY:
                return new EmptyVH(inflater.inflate(R.layout.empty, parent, false));
            case TYPE.HEADER_FAV:
            case TYPE.HEADER_OTHER:
                return new HeaderVH(inflater.inflate(R.layout.group_category_header, parent, false));
            default:
                return new GroupItemVH(inflater.inflate(R.layout.group_item, parent, false),
                        this, this);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case TYPE.NORMAL:
                ExGroup group = groups.get(getActualItemPosition(position));
                int newCount = 0;
                if (newItemCount.containsKey(group.getId()))
                    newCount = newItemCount.get(group.getId());
                ((GroupItemVH) holder).setGroup(group, me, newCount);
                break;
            case TYPE.HEADER_FAV:
                ((HeaderVH) holder).setType(HeaderVH.TYPE.FAVORITE);
                break;
            case TYPE.HEADER_OTHER:
                ((HeaderVH) holder).setType(HeaderVH.TYPE.OTHER);
                break;
            case TYPE.EMPTY:
                ((EmptyVH) holder).setType(EmptyVH.TYPE.GROUP);
                break;
            default:
                Log.d(TAG, "invalid item type encountered");
        }
    }

    @Override
    public int getItemViewType(int position) {
        int totalCount = groups.size();
        //empty case handled i.e., no items at all
        if (totalCount == 0)
            return TYPE.EMPTY;

        int favCount = 0;
        for (Group group : groups)
            if (group.isFavorite())
                favCount++;

        if (favCount > 0) {
            //there are some favorite items
            if (position == 0)
                return TYPE.HEADER_FAV;
            else if (position == favCount + 1)
                return TYPE.HEADER_OTHER;
            else
                return TYPE.NORMAL;
        }
        //else there are no favorite items so
        return TYPE.NORMAL;
    }

    @Override
    public int getItemCount() {
        int totalCount = groups.size();
        if (totalCount == 0)
            return 1;//empty view
        if (favCount > 0)
            totalCount += 2;//fav and other headers
        return totalCount;
    }

    private int getActualItemPosition(int position) {
        int totalCount = groups.size();
        if (totalCount == 0)
            return -1;
        if (favCount > 0) {
            if (position <= favCount) {
                return position - 1;
            } else {
                return position - 2;
            }
        }
        return position;
    }

    /**
     * @param position position of item in data set
     * @return the position of item in list UI
     */
    private int getItemPositing(int position) {
        if (favCount > 0) {
            if (position < favCount)
                return position + 1;
            else
                return position + 2;
        }
        return position;
    }

    /**
     * find index of group whose id is passed in local data set
     *
     * @param id
     * @return
     */
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
        Log.d(TAG, "adding child " + dataSnapshot.toString());
        Group group = dataSnapshot.getValue(Group.class);
        group.setId(dataSnapshot.getKey());
        int size = groups.size(); // size before adding new group to groups arrayList
        if (group.isFavorite()) {
            groups.add(favCount, new ExGroup(group));
            favCount += 1;
            if (favCount == 1) {
                notifyDataSetChanged();
                //calling notifyItemInserted here is causing inconsistency in items
            } else {
                notifyItemInserted(getItemPositing(favCount - 1));//insert at last of favorites
            }
        } else {
            groups.add(new ExGroup(group));
            Log.d(TAG, "adding to position" + groups.size());
            notifyItemInserted(getItemPositing(groups.size()));
//            notifyDataSetChanged();
        }
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        ExGroup group = new ExGroup(dataSnapshot.getValue(Group.class));
        group.setId(dataSnapshot.getKey());
        int index = findGroupIndex(dataSnapshot.getKey());
        if (index != -1) {
            Group oldGroup = groups.get(index);
            if (oldGroup.isFavorite()) {
                if (group.isFavorite()) {
                    //new group stays at index, no movement
                    groups.set(index, group);
                    Log.d(TAG, "changed " + index + ":" + getItemPositing(index));
                    notifyItemChanged(getItemPositing(index));
                } else {
                    //Todo: fix when last item is removed from favorities
                    //item moves to end of list
                    int oldPos = getItemPositing(index);
                    favCount -= 1;
                    groups.remove(index); //remove from fav list
                    groups.add(group); // add to last
                    if (favCount <= 0) {
                        notifyItemMoved(1, getItemPositing(groups.size()) + 1);
                        notifyItemRemoved(0);
                        notifyItemRemoved(0);
                        Log.d(TAG, "removed headers");
                    } else {
                        int newPos = getItemPositing(groups.size() - 1);
                        Log.d(TAG, "moved " + index + ":" + (oldPos - 1) + " " + (groups.size() - 1) + ":" + getItemPositing(groups
                                .size() - 1) + " -> " + getItemCount());
                        notifyItemMoved(oldPos, newPos);
                    }

                }
            } else {
                if (group.isFavorite()) {
                    //move to end of favorites list
                    // this section is working fine
                    int oldPos = getItemPositing(index);
                    groups.remove(index);
                    favCount += 1;
                    groups.add(favCount - 1, group);
                    int newPos = getItemPositing(favCount - 1);
                    Log.d(TAG, "moved " + index + ":" + oldPos + " " + (favCount - 1) + ":" + newPos + " -> " + getItemCount());
                    if (favCount == 1) {
                        notifyItemMoved(oldPos, 0);
                        notifyItemInserted(0);
                        notifyItemInserted(2);
                    } else
                        notifyItemMoved(oldPos, newPos);
                } else {
                    //new group stays at index, no movement
                    groups.set(index, group);
                    Log.d(TAG, "changed " + index + ":" + getItemPositing(index));
                    notifyItemChanged(getItemPositing(index));
                }
            }
        }
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        Log.d(TAG, dataSnapshot.toString());
        Group group = dataSnapshot.getValue(Group.class);
        group.setId(dataSnapshot.getKey());
        int index = findGroupIndex(dataSnapshot.getKey());
        if (group.isFavorite()) {
            favCount--;
            Log.d(TAG, "was a fav one");
        }
        if (favCount <= 0) {
            notifyItemInserted(0);
            notifyItemInserted(1);
        }
        if (index != -1) {
            groups.remove(index);
            notifyItemRemoved(getItemPositing(index));
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
        favCount = 0;
        groups.clear();
        notifyDataSetChanged();
    }

    public void registerChildEventListener() {
        Log.d(TAG, "registering " + groups.size());
        grpDbRef.orderByChild("name").addChildEventListener(this);
        Log.d(TAG, "registered " + groups.size());
        EventBus.getDefault().register(this);
        notifyDataSetChanged();
    }

    public void unregisterChildEventListener() {
        Log.d(TAG, "unregister " + groups.size());
        grpDbRef.orderByChild("name").removeEventListener(this);
        clear();
        EventBus.getDefault().unregister(this);
        Log.d(TAG, "unregistered " + groups.size());
    }

    @Subscribe
    public void OnMessageEvent(ExpenseAddedEvent expenseEvent) {
        String groupId = expenseEvent.getExpense().getGroupId();
        int count = 1;
        if (newItemCount.containsKey(groupId))
            count += newItemCount.get(groupId);
        newItemCount.put(groupId, count);

        for (int i = 0; i < groups.size(); i++) {
            ExGroup group = groups.get(i);
            if (group.getId().equals(groupId)) {
                notifyItemChanged(getItemPositing(i));
                break;
            }
        }
    }

    @Override
    public void onNotificationCountLoaded(Map<String, Integer> notificationCountMap) {
        if (notificationCountMap == null)
            return;
        newItemCount = notificationCountMap;
        notifyDataSetChanged();

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

    @Override
    public void on() {

    }

    @Override
    public void off() {

    }

    @Override
    public void onGroupClicked(String groupId, String groupName) {
        if (selectionMode) {
            int index = findGroupIndex(groupId);
            ExGroup group = groups.get(index);
            if (group.isSelected()) {
                selectedGroupsCount -= 1;
            } else {
                selectedGroupsCount += 1;
            }
            group.setSelected(!group.isSelected());
            notifyItemChanged(getItemPositing(index));
            contextActionBarClbk.setCount(selectedGroupsCount);
        } else
            groupItemClickClbk.onGroupClicked(groupId, groupName);
    }

    @Override
    public void onGroupInfoClicked(Group group) {
        groupItemClickClbk.onGroupInfoClicked(group);
    }

    @Override
    public void onLongClick(String groupId) {
        if (!selectionMode) {
            startMultiSelectMode();
            //TODO:remove long click listener from each group item
            onGroupClicked(groupId, null);
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        actionMode.getMenuInflater().inflate(R.menu.menu_groups_context, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        //for references to batch update
        Map<String, Object> refs = new HashMap<>();
        switch (menuItem.getItemId()) {
            case R.id.action_delete:
                //delete the selected item from firebase database. changes will be reflected automatically.
                for (ExGroup group : groups) {
                    if (group.isSelected()) {
                        refs.put("groups/" + me.getId() + "/" + group.getId(),
                                null);//my group list
                        refs.put("shareWith/" + group.getId() + "/" + me.getId(), null);
                        //TODO: reduce group membersCount
                    }
                }
                dbRef.updateChildren(refs).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "group deleted from user");
                        } else {
                            Log.d(TAG, "error" + task.getException().getLocalizedMessage());
                        }
                    }
                });
                actionMode.finish();
                return true;
            case R.id.action_favorite:
                for (ExGroup group : groups) {
                    if (group.isSelected()) {
                        refs.put("groups/" + me.getId() + "/" + group.getId() + "/favorite",
                                !group.isFavorite());//toggle the favorite value
                    }
                }
                dbRef.updateChildren(refs).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "group favorite updated");
                        } else {
                            Log.d(TAG, "error" + task.getException().getLocalizedMessage());
                        }
                    }
                });
                actionMode.finish();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
//        actionMode = null;
//        actionMode.finish();
        selectionMode = false;
        selectedGroupsCount = 0;
        int i = 0;
        //clear all selection
        for (ExGroup group : groups) {
            if (group.isSelected()) {
                group.setSelected(false);
                notifyItemChanged(getItemPositing(i));
            }
            i++;
        }
        //TODO:add on long click listeners to items
    }

    interface TYPE {
        int EMPTY = 0;
        int NORMAL = 1;
        int HEADER_FAV = 2;
        int HEADER_OTHER = 3;
    }

}
