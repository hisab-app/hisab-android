package io.github.zkhan93.hisab.ui.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;

/**
 * Created by Zeeshan Khan on 9/1/2016.
 */
public class ConfirmDialog extends DialogFragment {
    public static final String TAG = ConfirmDialog.class.getSimpleName();

    private String msg, positiveBtnTxt, negativeBtnTxt;
    private int type;

    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        if (bundle == null)
            bundle = getArguments();
        msg = bundle.getString("msg");
        positiveBtnTxt = bundle.getString("positiveBtnTxt");
        negativeBtnTxt = bundle.getString("negativeBtnTxt");
        type = bundle.getInt("type");
        AlertDialog dialog = new AlertDialog.Builder(getActivity(), 0).setMessage(msg)
                .setPositiveButton
                        (positiveBtnTxt, (DialogInterface.OnClickListener) getActivity())
                .setNegativeButton(negativeBtnTxt, (DialogInterface.OnClickListener) getActivity
                        ()).create();
        dialog.setOnShowListener(getOnShowListener(type));
        return dialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("msg", msg);
        outState.putString("positiveBtnTxt", positiveBtnTxt);
        outState.putString("negativeBtnTxt", negativeBtnTxt);
        outState.putInt("type", type);
    }

    private DialogInterface.OnShowListener getOnShowListener(int type) {
        switch (type) {
            case TYPE.EXPENSE_DELETE:
                return new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        ((AlertDialog) dialogInterface).getButton(DialogInterface
                                .BUTTON_POSITIVE).setTag(TYPE.EXPENSE_DELETE);
                        Log.d(TAG,"set delete tag");
                    }
                };

            case TYPE.GROUP_ARCHIVE:
                return new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        ((AlertDialog) dialogInterface).getButton(DialogInterface
                                .BUTTON_POSITIVE).setTag(TYPE.GROUP_ARCHIVE);
                        Log.d(TAG,"set archive tag");
                    }
                };

            default:
                return null;
        }
    }

    public interface TYPE {
        int EXPENSE_DELETE = 1;
        int GROUP_ARCHIVE = 2;
        int INVALID = 3;
    }

}
