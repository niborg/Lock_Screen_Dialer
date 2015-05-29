package com.vitaminbacon.lockscreendialer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

/**
 * This activity is solely for the purpose of fulfilling the logic that decides which lock screen
 * activity is shown.  Error handling of the instance where a lock screen type is not specified is
 * performed by instantiating an error page activity.
 */

public class LockScreenLauncherActivity extends Activity {

    private static final String TAG = "LauncherActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent;
        // Get the lock screen type from sharedPref

        SharedPreferences sharedPref = getSharedPreferences(
                getString(R.string.lock_screen_type_file_key),
                Context.MODE_PRIVATE);

        String lockScreenType = sharedPref.getString(
                getString(R.string.lock_screen_type_value_key),
                null);

        if(lockScreenType != null &&
                lockScreenType.equals(getString(R.string.lock_screen_type_value_keypad_pin))){ // Now enable the correct lock screen
            intent = new Intent (this, LockScreenKeypadPinActivity.class);
            Log.d(TAG, "Keypad PIN fragment to be implemented.");
        } //TODO: enable other lock screen types
        else { //An error of some kind
            Log.d(TAG, "No value for key " + getString(R.string.lock_screen_type_value_key));
            intent = new Intent (this, ErrorPageActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // necessary to add to android's stack of things to do
        intent.addFlags(WindowManager.LayoutParams.TYPE_SYSTEM_ERROR); //this allows the activity to be placed on top of everything -- UGLY HACK??
        if (getIntent().getExtras() != null){
            intent.putExtras(getIntent().getExtras()); //places the extras, if any, passed to the activity
        }
        else {
            Log.d(TAG, "Passed intent did not have any extras.");
        }
        startActivity(intent);
        finish();
    }


}
