package io.github.zkhan93.hisab.util;

import android.content.Context;
import android.preference.PreferenceManager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import io.github.zkhan93.hisab.model.User;

/**
 * Created by n193211 on 6/30/2016.
 */
public class Util {
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
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(source.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}