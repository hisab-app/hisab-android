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
import io.github.zkhan93.hisab.model.ExpenseItem;
import io.github.zkhan93.hisab.model.User;
import io.github.zkhan93.hisab.model.callback.ExpenseItemActionClbk;
import io.github.zkhan93.hisab.model.callback.ExpenseItemUiClbk;
import io.github.zkhan93.hisab.model.viewholder.EmptyVH;
import io.github.zkhan93.hisab.model.viewholder.ExpenseItemVH;
import io.github.zkhan93.hisab.model.viewholder.ExpenseSummaryVH;

/**
 * Created by Zeeshan Khan on 6/26/2016.
 */
public class ExpensesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements
        ChildEventListener, ExpenseItemActionClbk {

    public static final String TAG = ExpensesAdapter.class.getSimpleName();

    private List<ExpenseItem> expenses;
    private User me;
    private DatabaseReference dbRef;
    private ExpenseItemUiClbk uiCallback;

    public ExpensesAdapter(User me, String groupId, ExpenseItemUiClbk uiCallback) {
        expenses = new ArrayList<>();
        this.me = me;
        dbRef = FirebaseDatabase.getInstance().getReference("expenses/" + groupId);
        this.uiCallback = uiCallback;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE.EMPTY:
                return new EmptyVH(inflater.inflate(R.layout.empty, parent, false));
            case TYPE.SUMMARY:
                return new ExpenseSummaryVH(inflater.inflate(R.layout.expense_summary_item,
                        parent, false));
            default:
                return new ExpenseItemVH(inflater.inflate(R.layout.expense_item, parent, false),
                        this, uiCallback);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case TYPE.NORMAL:
                ((ExpenseItemVH) holder).setExpense(expenses.get(position), me);
                break;
            case TYPE.SUMMARY:
                ((ExpenseSummaryVH) holder).setSummaryExpense(getTotalAmount());
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        int count = expenses.size();
        if (count == 0)
            return TYPE.EMPTY;
        else if (position < count)
            return TYPE.NORMAL;
        else
            return TYPE.SUMMARY;
    }

    @Override
    public int getItemCount() {
        int size = expenses.size();
        return size == 0 ? 1 : size + 1;
    }


    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        ExpenseItem expense = dataSnapshot.getValue(ExpenseItem.class);
        expense.setId(dataSnapshot.getKey());
        expenses.add(expense);
        notifyItemInserted(expenses.size());
        notifyItemChanged(expenses.size());
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        ExpenseItem expense = dataSnapshot.getValue(ExpenseItem.class);
        expense.setId(dataSnapshot.getKey());
        int index = findExpenseIndex(dataSnapshot.getKey());
        if (index != -1) {
            expenses.set(index, expense);
            notifyItemChanged(index);
            notifyItemChanged(expenses.size());
        }
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        ExpenseItem expense = dataSnapshot.getValue(ExpenseItem.class);
        expense.setId(dataSnapshot.getKey());
        int index = findExpenseIndex(dataSnapshot.getKey());
        if (index != -1) {
            expenses.remove(index);
            notifyItemRemoved(index);
            notifyItemChanged(expenses.size());
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

    public int findExpenseIndex(String id) {
        int index = -1;
        int len = expenses.size();
        for (int i = 0; i < len; i++) {
            if (expenses.get(i).getId().equals(id)) {
                index = i;
                break;
            }
        }
        return index;
    }

    public void clear() {
        expenses.clear();
        notifyDataSetChanged();
    }

    public void registerChildEventListener() {
        dbRef.addChildEventListener(this);
    }

    public void unregisterChildEventListener() {
        dbRef.removeEventListener(this);
    }

    private float getTotalAmount() {
        float res = 0;
        for (ExpenseItem ex : expenses) {
            if (ex != null)
                res += ex.getAmount();
        }
        return res;
    }

    @Override
    public void delete(String expenseId) {
        if(dbRef!=null)
        dbRef.child(expenseId).removeValue();
    }

    @Override
    public void update(ExpenseItem expense) {
        if(dbRef!=null)
        dbRef.child(expense.getId()).setValue(expense);
    }

    interface TYPE {
        int EMPTY = 0;
        int NORMAL = 1;
        int SUMMARY = 2;
    }
}
