package io.github.zkhan93.hisab.service;

import android.app.PendingIntent;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.greenrobot.eventbus.EventBus;

import java.util.Map;

import io.github.zkhan93.hisab.HisabApplication;
import io.github.zkhan93.hisab.model.events.ExpenseAddedEvent;
import io.github.zkhan93.hisab.model.notification.DaoSession;
import io.github.zkhan93.hisab.model.notification.LocalExpense;
import io.github.zkhan93.hisab.model.notification.LocalGroup;
import io.github.zkhan93.hisab.model.notification.LocalGroupDao;
import io.github.zkhan93.hisab.util.Util;

/**
 * Created by zeeshan on 2/12/2017.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static final String TAG = MyFirebaseMessagingService.class.getSimpleName();
    PendingIntent actionIntent;
    DaoSession daoSession;

    @Override
    public void onCreate() {
        super.onCreate();
        daoSession = ((HisabApplication) getApplication()).getDaoSession();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> tmpData = remoteMessage.getData();
        Log.d(TAG, "fcm: " + tmpData.toString());
        //process the message only if the user is logged in
        if (Util.isLoggedIn(getApplicationContext())) {
            LocalGroupDao groupNotificationDao = daoSession.getLocalGroupDao();
            LocalGroup group = groupNotificationDao.load(tmpData.get("group_id"));
            if (group == null) {
                group = new LocalGroup(tmpData.get("group_id"), tmpData.get("group_name"));
                groupNotificationDao.insert(group);
            }
            LocalExpense expense = new LocalExpense();
            expense.setId(tmpData.get("expense_id"));
            expense.setDesc(tmpData.get("desc"));
            expense.setType(Integer.parseInt(tmpData.get("type")));
            expense.setOwnerName(tmpData.get("owner_name"));
            expense.setOwnerId(tmpData.get("owner_id"));
            expense.setTimestamp(Long.parseLong(tmpData.get("timestamp")));
            expense.setAmount(Float.parseFloat(tmpData.get("amount")));
            expense.setGroup(group);
            daoSession.getLocalExpenseDao().insertOrReplace(expense);

            //notifiy active listeners(GroupsAdapter) about this expense being added
            EventBus.getDefault().post(new ExpenseAddedEvent(expense));

            Util.showNotification(getApplicationContext());
        }
    }

}
