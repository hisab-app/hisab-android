package io.github.zkhan93.hisab.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.ui.MainActivity;
import io.github.zkhan93.hisab.util.Util;

/**
 * Created by zeeshan on 2/12/2017.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static final String TAG = MyFirebaseMessagingService.class.getSimpleName();
    PendingIntent actionIntent;
    private NotificationCompat.Builder mBuilder;
    private List<Map<String, String>> messages;
    private Set<String> groups;

    @Override
    public void onCreate() {
        super.onCreate();
        mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R
                .drawable.ic_stat_hisab);
        actionIntent = PendingIntent.getActivity(this, 0, new Intent(getApplicationContext(),
                MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        messages = new ArrayList<>();
        groups = new HashSet<>();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "fcm: " + remoteMessage.getData().toString());
        //process the message only if the user is logged in
        if (Util.isLoggedIn(getApplicationContext())) {
            messages.add(remoteMessage.getData());
            groups.add(remoteMessage.getData().get("group_id"));
            Log.d(TAG, "messages: " + messages.size());
            showNotification();
        }
    }

    private String getPluralString(Collection items) {
        if (items == null)
            return "";
        return items.size() > 1 ? "s" : "";
    }

    private void showNotification() {
        NotificationCompat.InboxStyle inboxStyle;
        inboxStyle = new NotificationCompat.InboxStyle();

        String title = String.format(Locale.ENGLISH, "%d item%s added in %d group%s", messages
                .size(), getPluralString(messages), groups.size(), getPluralString(groups));

        mBuilder.setContentTitle(title);
        inboxStyle.setBigContentTitle(title);

        mBuilder.setContentText("this is builder content");
        for (Map<String, String> data : messages)
            inboxStyle.addLine(String.format(Locale.ENGLISH, "%s: %.2f - %s", data.get
                    ("owner_name"), Float.parseFloat(data.get("amount")), data.get("desc")));

        inboxStyle.setSummaryText(title);

        mBuilder.setStyle(inboxStyle);
        mBuilder.setContentIntent(actionIntent);
        mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // type allows you to update the notification later on.
        mNotificationManager.notify(NotificationService.NOTIFICATION_TYPE.EXPENSE, mBuilder.build());
    }

}
