package io.github.zkhan93.hisab.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.Group;
import io.github.zkhan93.hisab.model.viewholder.GroupItemVH;

/**
 * Created by Zeeshan Khan on 6/26/2016.
 */
public class GroupsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    List<Group> groups;

    public GroupsAdapter(List<Group> groups) {
        if (groups != null)
            this.groups = groups;
        else
            this.groups = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE.EMPTY:
                return new GroupItemVH(inflater.inflate(R.layout.empty, parent, false));
            default:
                return new GroupItemVH(inflater.inflate(R.layout.group_item, parent, false));
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE.NORMAL) {
            GroupItemVH gHolder = (GroupItemVH) holder;
            gHolder.setItem(groups.get(position));
        }
    }

    @Override
    public int getItemViewType(int position) {
        int count = groups.size();
        if (count == 0 && position == 0)
            return TYPE.EMPTY;
        return TYPE.NORMAL;
    }

    @Override
    public int getItemCount() {
        int count = groups.size();
        return count == 0 ? 1 : count;
    }

    interface TYPE {
        int EMPTY = 0;
        int NORMAL = 1;
    }
}
