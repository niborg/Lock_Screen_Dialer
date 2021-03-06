package com.vitbac.speeddiallocker.services;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneStateService extends Service {

    private final static String TAG = "PhoneStateService";
    //private final IBinder mBinder = new PhoneStateBinder();
    private static PhoneCallReceiver mReceiver;


    public PhoneStateService() {
        //Log.d(TAG, "Constructor called.");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public void onCreate() {
        //Log.d(TAG, "onCreate called");
        super.onCreate();
        IntentFilter filter = new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        mReceiver = new PhoneCallReceiver();
        registerReceiver(mReceiver, filter);
    }

    /**
     * Originally was overriden to implement the receiver.  This ends up being a bad move.
     * While a service can only have one given instance at a time, meaning onCreate is only called
     * once, subsequent calls to a service will call onStartCommand, meaning you would be registering
     * a receiver multiple times, leading to errors.
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        //Log.d(TAG, "onStartCommand called");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {

        try {
            unregisterReceiver(mReceiver);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Attempt to unregister PhoneStateReceiver failed; receiver was already unregistered", e);
            mReceiver = null;
        }
        //Log.d(TAG, "Service called onDestroy().");
        super.onDestroy();
    }

}
