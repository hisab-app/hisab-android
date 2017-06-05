package io.github.zkhan93.hisab.util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by zeeshan on 6/6/17.
 */

public class RemoveExpenseImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent resultIntent = new Intent();
        resultIntent.putExtra("remove_image", true);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}
