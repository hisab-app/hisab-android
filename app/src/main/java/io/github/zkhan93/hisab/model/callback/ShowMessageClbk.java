package io.github.zkhan93.hisab.model.callback;

import android.support.annotation.NonNull;

/**
 * Created by Zeeshan Khan on 9/4/2016.
 */
public interface ShowMessageClbk {
    void showMessage(@NonNull String msg, int how, int howLong);

    interface TYPE {
        int TOAST = 1;
        int SNACKBAR = 2;
    }
}