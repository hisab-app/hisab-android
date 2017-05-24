package io.github.zkhan93.hisab.util;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.github.zkhan93.hisab.HisabApplication;
import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.User;
import io.github.zkhan93.hisab.model.callback.NotificationCountLoadClbk;
import io.github.zkhan93.hisab.model.notification.DaoSession;
import io.github.zkhan93.hisab.model.notification.LocalExpense;
import io.github.zkhan93.hisab.model.notification.LocalExpenseDao;
import io.github.zkhan93.hisab.model.notification.LocalGroup;
import io.github.zkhan93.hisab.service.SyncAndNotifyJob;
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

    public static void getNotificationMapFromDisk(Context context, final NotificationCountLoadClbk
            notificationCountLoadClbk) {
        if (context == null)
            return;
        final DaoSession daoSession = ((HisabApplication) context.getApplicationContext())
                .getDaoSession();
        if (daoSession == null)
            return;
        new AsyncTask<Void, Void, Map<String, Integer>>() {
            @Override
            protected Map<String, Integer> doInBackground(Void... voids) {
                Map<String, Integer> notificationCounts = new HashMap<>();
                for (LocalGroup group : daoSession.getLocalGroupDao().loadAll()) {
                    long expenses = daoSession.getLocalExpenseDao().queryBuilder().where
                            (LocalExpenseDao.Properties.GroupId.eq(group.getId())).count();
                    if (expenses > 0) {
                        notificationCounts.put(group.getId(), (int) expenses);
                    }
                }
                return notificationCounts;
            }

            @Override
            protected void onPostExecute(Map<String, Integer> notificationCounts) {
                notificationCountLoadClbk.onNotificationCountLoaded(notificationCounts);
            }
        }.execute();
    }

    public static void deleteNotifications(Context context, String groupId) {
        if (context == null)
            return;
        final DaoSession daoSession = ((HisabApplication) context.getApplicationContext())
                .getDaoSession();
        if (daoSession == null)
            return;
        new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... strings) {
                String groupId = strings[0];
                daoSession.getLocalExpenseDao().queryBuilder().whereOr(LocalExpenseDao.Properties
                        .GroupId.eq(groupId),LocalExpenseDao.Properties.GroupId.isNull())
                        .buildDelete().executeDeleteWithoutDetachingEntities();
//                daoSession.getLocalExpenseDao().getDatabase().rawQuery(String.format("delete from %s " +
//                        "where %s=? ", LocalExpenseDao.TABLENAME, LocalExpenseDao.Properties.GroupId
//                        .columnName), new String[]{groupId});
//                daoSession.getLocalGroupDao().deleteByKey(groupId);
                Log.d(TAG,"deleting seen notification entries from database");
                return null;
            }
        }.execute(groupId);
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
        Iterator<LocalGroup> itr=groups.iterator();
        while (itr.hasNext()) {
            LocalGroup group=itr.next();
            //fetch expenses again to get the latest data from db
            group.resetExpenses();
            //fetch only top 5 items from database
            List<LocalExpense> expenseList = daoSession.getLocalExpenseDao().queryBuilder().where
                    (LocalExpenseDao.Properties.GroupId.eq(group.getId()))
                    .limit(5).list();
            //calculate total number of expenses
            int expensesCount = (int) daoSession.getLocalExpenseDao().queryBuilder().where
                    (LocalExpenseDao.Properties.GroupId.eq(group.getId())).count();

            if (expensesCount <= 0) {
                itr.remove();
                Log.d(TAG,"removing groups having no notification");
                continue;
            }
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
                    .drawable.ic_stat_hisab)
                    .setContentTitle(title)
                    .setContentText(summary)
                    .setGroup("gsn")
                    .setContentIntent(actionIntent)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
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
        if(notificationCount>1) {
            //create summary notification is notification are grater than 1
            actionIntent = PendingIntent.getActivity(context, 0,
                    new Intent(context, MainActivity.class),
                    PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder = new NotificationCompat.Builder(context).setSmallIcon(R
                    .drawable.ic_stat_hisab)
                    .setContentTitle(String.format(Locale.ENGLISH, "%d group%s have new entries",
                            groups.size(), getPluralString(groups.size())))
                    .setContentText(String.format(Locale.ENGLISH, "%.2f worth expenses added", totalAmount))
                    .setGroup("gsn")
                    .setGroupSummary(true)
                    .setContentIntent(actionIntent)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            // type allows you to update the notification later on.
            mNotificationManager.notify(notificationCount, mBuilder.build());
        }
    }

    public static void scheduleJob(Context context) {
        //if not scheduled
        if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean
                ("job_scheduled", false)) {
            Bundle myExtrasBundle = new Bundle();
            myExtrasBundle.putString("some_key", "some_value");
            FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver
                    (context));
            Job myJob = dispatcher.newJobBuilder()
                    // the JobService that will be called
                    .setService(SyncAndNotifyJob.class)
                    // uniquely identifies the job
                    .setTag(SyncAndNotifyJob.TAG)
                    // one-off job
                    .setRecurring(false)
                    // don't persist past a device reboot
                    .setLifetime(Lifetime.FOREVER)
                    // start between 0 and 24 hours from now
                    .setTrigger(Trigger.executionWindow(0, 3600000))
                    // don't overwrite an existing job with the same tag
                    .setReplaceCurrent(true)
                    // retry with exponential backoff
                    .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                    // constraints that need to be satisfied for the job to run
                    .setConstraints(
                            // only run on an unmetered network
                            Constraint.ON_UNMETERED_NETWORK,
                            // only run when the device is charging
                            Constraint.DEVICE_CHARGING
                    )
                    .setExtras(myExtrasBundle)
                    .build();

            dispatcher.mustSchedule(myJob);
            PreferenceManager
                    .getDefaultSharedPreferences(context)
                    .edit()
                    .putBoolean("job_scheduled", true)
                    .apply();
        }
    }
}