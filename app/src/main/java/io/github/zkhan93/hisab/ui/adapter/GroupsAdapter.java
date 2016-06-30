package io.github.zkhan93.hisab.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
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
public class GroupsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String TAG = GroupsAdapter.class.getSimpleName();
    List<Group> groups;
    GroupItemClickClbk groupItemClickClbk;
    private User me;

    public GroupsAdapter(List<Group> groups, GroupItemClickClbk groupItemClickClbk, User me) {
        if (groups != null)
            this.groups = groups;
        else
            this.groups = new ArrayList<>();
        this.groupItemClickClbk = groupItemClickClbk;
        this.me = me;
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
            gHolder.setGroup(groups.get(position),me);
        }
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

    public void setGroups(List<Group> groups) {
        if (groups != null && groups.size() > 0) {
            this.groups.clear();
            this.groups.addAll(groups);
            notifyDataSetChanged();
        }
    }

    public void addGroup(Group group) {
        if (group != null) {
            groups.add(group);
            notifyItemInserted(groups.size());
        }
    }

    public void modifyGroup(Group group) {
        if (group != null) {
            int index = 0;
            boolean found = false;
            for (Group g : groups) {
                if (g.getId().equals(group.getId())) {
                    found = true;
                    break;
                }
                index += 1;
            }
            if (found) {
                groups.set(index, group);
                notifyItemChanged(index);
            }
        }
    }

    public void removeGroup(Group group) {
        if (group != null) {
            int index = 0;
            boolean found = false;
            for (Group g : groups) {
                if (g.getId().equals(group.getId())) {
                    found = true;
                    break;
                }
                index += 1;
            }
            if (found) {
                groups.remove(index);
                notifyItemRemoved(index);
            }
        }
    }

    public void clear() {
        groups.clear();
        notifyDataSetChanged();
    }

    interface TYPE {
        int EMPTY = 0;
        int NORMAL = 1;
    }
}
