package io.github.zkhan93.hisab.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.ExpenseItem;
import io.github.zkhan93.hisab.model.User;
import io.github.zkhan93.hisab.ui.adapter.ExpensesAdapter;
import io.github.zkhan93.hisab.util.Util;

/**
 * Created by zeeshan on 4/1/2017.
 */

public class WidgetProvider extends AppWidgetProvider {
    private static List<ExpenseItem> expenses;
    private static User me;

    static void updateAppWidget(Context context, final AppWidgetManager appWidgetManager, final
    int appWidgetId) {
        me = Util.getUser(context);
        String groupName = WidgetConfigActivity.loadGroupNamePref(context, appWidgetId);
        String groupId = WidgetConfigActivity.loadGroupIdPref(context, appWidgetId);
        // Construct the RemoteViews object
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
        views.setTextViewText(R.id.name, context.getString(R.string.widget_group_name, groupName));
        FirebaseDatabase.getInstance().getReference().child("expenses").child(groupId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        float finalValue = 0;
                        if (expenses == null)
                            expenses = new ArrayList<ExpenseItem>();
                        expenses.clear();
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            ExpenseItem ex = ds.getValue(ExpenseItem.class);
                            ex.setId(ds.getKey());
                            expenses.add(ex);
                        }
                        views.setTextViewText(R.id.name, String.valueOf(getMyExpensesSum
                                () + getPaidReceived()));
                        appWidgetManager.updateAppWidget(appWidgetId, views);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        // Instruct the widget manager to update the widget
    }

    private static float getTotalAmount() {
        float res = 0;
        for (ExpenseItem ex : expenses) {
            if (ex != null && ex.getItemType() == ExpenseItem.ITEM_TYPE.SHARED)
                res += ex.getAmount();
        }
        return res;
    }

    private static  float getMyExpensesSum() {
        float res = 0;
        for (ExpenseItem ex : expenses) {
            if (ex != null && ex.getOwner().getId().equals(me.getId()) && ex.getItemType() ==
                    ExpenseItem.ITEM_TYPE.SHARED)
                res += ex.getAmount();
        }
        return res;
    }

    private static float getPaidReceived() {
        float res = 0;
        for (ExpenseItem ex : expenses) {
            if (ex != null && ex.getItemType() == ExpenseItem.ITEM_TYPE.PAID_RECEIVED) {
                if (ex.getOwner().getId().equals(me.getId()))
                    res += ex.getShareType() == ExpenseItem.SHARE_TYPE.PAID ? ex.getAmount() : -ex
                            .getAmount();
                else if (ex.getWith().getId().equals(me.getId()))
                    res += ex.getShareType() == ExpenseItem.SHARE_TYPE.PAID ? -ex.getAmount() : ex
                            .getAmount();
            }
        }
        return res;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            WidgetConfigActivity.deleteTitlePref(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}
