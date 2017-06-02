package io.github.zkhan93.hisab.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.Group;
import io.github.zkhan93.hisab.model.callback.GroupItemClickClbk;
import io.github.zkhan93.hisab.ui.adapter.GroupsAdapter;
import io.github.zkhan93.hisab.util.Util;

/**
 * Created by zeeshan on 4/2/2017.
 */

public class WidgetConfigActivity extends Activity {
    public static final String TAG = WidgetConfigActivity.class.getSimpleName();
    private static final String PREFS_NAME = "com.example.zeeshan.test.Widget";
    private static final String PREF_PREFIX_KEY = "appwidget_";
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @BindView(R.id.groups)
    RecyclerView groupsView;

    View.OnClickListener mOnClickListener;
    private GroupItemClickClbk groupItemClickClbk;
    private GroupsAdapter groupsAdapter;

    {
        mOnClickListener = new View.OnClickListener() {
            public void onClick(View v) {

            }
        };
        groupItemClickClbk = new GroupItemClickClbk() {
            @Override
            public void onGroupClicked(String groupId, String groupName) {
                Log.d(TAG, "group clicked" + groupName);
                final Context context = WidgetConfigActivity.this;

                // When the button is clicked, store the string locally
                saveTitlePref(context, mAppWidgetId, groupName,groupId);

                // It is the responsibility of the configuration activity to update the app widget
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                WidgetProvider.updateAppWidget(context, appWidgetManager, mAppWidgetId);

                // Make sure we pass back the original appWidgetId
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
            }

        };
    }

    public WidgetConfigActivity() {
        super();
    }

    // Write the prefix to the SharedPreferences object for this widget
    static void saveTitlePref(Context context, int appWidgetId, String groupName, String groupId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId + "group_name", groupName);
        prefs.putString(PREF_PREFIX_KEY + appWidgetId + "group_id", groupId);
        prefs.apply();
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static String loadGroupNamePref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String groupName = prefs.getString(PREF_PREFIX_KEY + appWidgetId+ "group_name", null);
        if (groupName != null) {
            return groupName;
        } else {
            return "No Group saved";
        }
    }
    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static String loadGroupIdPref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String groupId = prefs.getString(PREF_PREFIX_KEY + appWidgetId+ "group_id", null);
        if (groupId != null) {
            return groupId;
        } else {
            return "nogroupid";
        }
    }

    static void deleteTitlePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.widget_configure);
        ButterKnife.bind(this);
        groupsAdapter = new GroupsAdapter(groupItemClickClbk, Util.getUser(getApplicationContext()), null);
        groupsView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        groupsView.setAdapter(groupsAdapter);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        groupsAdapter.registerChildEventListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        groupsAdapter.unregisterChildEventListener();
    }
}
