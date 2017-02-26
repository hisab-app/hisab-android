package io.github.zkhan93.hisab.util;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import io.github.zkhan93.hisab.HisabApplication;
import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.User;
import io.github.zkhan93.hisab.model.notification.DaoSession;
import io.github.zkhan93.hisab.model.notification.LocalExpense;
import io.github.zkhan93.hisab.model.notification.LocalExpenseDao;
import io.github.zkhan93.hisab.model.notification.LocalGroup;
import io.github.zkhan93.hisab.ui.MainActivity;

/**
 * Created by n193211 on 6/30/2016.
 */
public class Util {
    public static final String TAG = Util.class.getSimpleName();

    public static String encodedEmail(String email) {
        return email.replace('.', ',');
    }

    public static String decodedEmail(String email) {
        if (email == null)
            return "";
        return email.replace(',', '.');

    }

    public static String getUserId(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString("user_id", null);
    }

    public static String getUserEmail(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString("email", null);
    }

    public static String getUserName(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString("name", null);
    }

    public static User getUser(Context context) {
        String name, email, userId;
        name = getUserName(context);
        userId = getUserId(context);
        email = getUserEmail(context);
        User user = new User(name, email, userId);
        return user;
    }

    public static void clearPreferences(Context context) {
        if (context != null)
            PreferenceManager.getDefaultSharedPreferences(context).edit().clear().apply();
    }

    public static String md5(String source) {
        try {
            MessageDigest md =
                    MessageDigest.getInstance("MD5");
            return hex(md.digest(source.getBytes("CP1252")));
        } catch (NoSuchAlgorithmException e) {
        } catch (UnsupportedEncodingException e) {
        }
        return null;
    }

    public static String hex(byte[] array) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < array.length; ++i) {
            sb.append(Integer.toHexString((array[i]
                    & 0xFF) | 0x100).substring(1, 3));
        }
        return sb.toString();
    }

    public static String getGavatarUrl(String email, int size) {
        return "https://www.gravatar.com/avatar/" + Util.md5(email) + "?s=" + size;
    }

    public static boolean isLoggedIn(Context context) {
        if (context != null)
            return PreferenceManager.getDefaultSharedPreferences(context).contains("logged_in");
        else
            return false;
    }

    public static String getPluralString(Collection items) {
        if (items == null)
            return "";
        return items.size() > 1 ? "s" : "";
    }

    public static String getPluralString(long items) {
        if (items < 2)
            return "";
        return "s";
    }

    public static void showNotification(Context context) {
        DaoSession daoSession = ((HisabApplication) context.getApplicationContext()).getDaoSession();
        NotificationCompat.Builder mBuilder;
        PendingIntent actionIntent;
        int notificationCount = 0;
        List<LocalGroup> groups = daoSession.getLocalGroupDao().loadAll();
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        float totalAmount = 0;

        for (LocalGroup group : groups) {
            //reset groupexpenses so that it fetches again and get us the latest data from db
            group.resetExpenses();
            //fetch only top 5 items from database
            List<LocalExpense> expenseList = daoSession.getLocalExpenseDao().queryBuilder().where
                    (LocalExpenseDao.Properties.GroupId.eq(group.getId()))
                    .limit(5).list();
            //calculate totla number of expenses
            int expensesCount = (int) daoSession.getLocalExpenseDao().queryBuilder().where
                    (LocalExpenseDao.Properties.GroupId.eq(group.getId())).count();
            //get the total amount added under that group
            Cursor cursor = daoSession.getDatabase().rawQuery("select sum(amount) " +
                    "from " + LocalExpenseDao.TABLENAME + " where " + LocalExpenseDao.Properties
                    .GroupId.columnName + "=?", new String[]{group.getId()});
            cursor.moveToFirst();
            float amount = cursor.getFloat(0);
            cursor.close();

            totalAmount += amount;

            String title = String.format(Locale.ENGLISH, "%d update%s in %s", expensesCount, getPluralString(expensesCount), group.getName());
            String summary = String.format(Locale.ENGLISH, "+%.2f INR", amount);
            //build intent for this notification
            Bundle bundle = new Bundle();
            bundle.putString("groupId", group.getId());
            bundle.putString("groupName", group.getName());
            bundle.putInt("notificationId", notificationCount);
            Log.d(TAG, bundle.toString());
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtras(bundle);
            intent.setAction(Long.toString(System.currentTimeMillis()));
            actionIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent
                    .FLAG_ONE_SHOT);

            mBuilder = new NotificationCompat.Builder(context).setSmallIcon(R
                    .drawable.ic_stat_hisab);
            mBuilder.setContentTitle(title);
            mBuilder.setContentText(summary);
            mBuilder.setGroup("gsn");
            mBuilder.setContentIntent(actionIntent);
            mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            // type allows you to update the notification later on.
            NotificationCompat.InboxStyle inbox = new NotificationCompat.InboxStyle();
            inbox.setBigContentTitle(title);
            inbox.setSummaryText(summary);
            if (expensesCount > expenseList.size())
                inbox.setSummaryText(String.format(Locale.ENGLISH, "+%d more",
                        expensesCount - expenseList.size()));
            for (LocalExpense expense : expenseList) {
                inbox.addLine(String.format(Locale.ENGLISH, "%s: %s - %.2f", expense.getOwnerName()
                        , expense.getDesc(), expense.getAmount()));
            }
            mBuilder.setStyle(inbox);
            mNotificationManager.notify(notificationCount++, mBuilder.build());

        }
        actionIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class),
                PendingIntent.FLAG_ONE_SHOT);
        mBuilder = new NotificationCompat.Builder(context).setSmallIcon(R
                .drawable.ic_stat_hisab);
        mBuilder.setContentTitle(String.format(Locale.ENGLISH, " %d group%s have new entries",
                groups.size(), getPluralString(groups.size())));
        mBuilder.setContentText(String.format(Locale.ENGLISH, "%.2f worth expenses added", totalAmount));
        mBuilder.setGroup("gsn");
        mBuilder.setGroupSummary(true);
        mBuilder.setContentIntent(actionIntent);
        mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        // type allows you to update the notification later on.
        mNotificationManager.notify(notificationCount, mBuilder.build());
    }
}