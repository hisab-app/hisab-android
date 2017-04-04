package io.github.zkhan93.hisab.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
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
import io.github.zkhan93.hisab.util.Util;

/**
 * Created by zeeshan on 4/1/2017.
 */

public class WidgetProvider extends AppWidgetProvider {
    public static final String TAG = WidgetProvider.class.getSimpleName();
    public static final String ACTION_UPDATE = "update";
    private static List<ExpenseItem> expenses;
    private static User me;

    static void updateAppWidget(final Context context, final AppWidgetManager appWidgetManager,
                                final
                                int appWidgetId) {
        Log.d(TAG, "got update for " + appWidgetId);
        me = Util.getUser(context);
        String groupName = WidgetConfigActivity.loadGroupNamePref(context, appWidgetId);
        final String groupId = WidgetConfigActivity.loadGroupIdPref(context, appWidgetId);
        // Construct the RemoteViews object
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
        views.setTextViewText(R.id.name, context.getString(R.string.widget_group_name, groupName));
        final FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
        firebaseDatabase.goOnline();
        firebaseDatabase.getReference().child("shareWith").child(groupId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //count members
                        final long noOfMembers = dataSnapshot.getChildrenCount() + 1;
                        firebaseDatabase.getReference().child("expenses").child
                                (groupId)
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
                                        float amount = getTotalAmount();
                                        float myExpenses = getMyExpensesSum() + getPaidReceived();
                                        float genShare = amount / noOfMembers;
                                        float myShare = genShare - myExpenses;
                                        String msg = null;
                                        String rs = context.getString(R.string.rs);
                                        msg = context.getString(myShare < 0 ? R.string.msg_summary_collect : R.string
                                                        .msg_summary_give,
                                                Math.abs(myShare), rs);

                                        if (myShare == 0) {
                                            msg = context.getString(R.string.msg_your_clear);
                                        }
                                        views.setTextViewText(R.id.summary, msg);
                                        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                                        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
                                                new int[]{appWidgetId});
                                        PendingIntent pIntent = PendingIntent.getBroadcast(context,
                                                appWidgetId, intent, PendingIntent
                                                        .FLAG_UPDATE_CURRENT);
                                        views.setOnClickPendingIntent(R.id.widget, pIntent);
                                        appWidgetManager.updateAppWidget(appWidgetId, views);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
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

    private static float getMyExpensesSum() {
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
