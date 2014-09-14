package uk.co.latestarter.sunshine.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Deliver the SyncAdapterBinder to the Sync Manager
 */
public class SunshineSyncService extends Service {

//    private static final String LOG_TAG = SunshineSyncService.class.getSimpleName();
    private static final Object sSyncAdapterLock = new Object();
    private static SunshineSyncAdapter sSunshineSyncAdapter = null;

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
//            Log.v(LOG_TAG, "onCreate - SunshineSyncService");
            if (sSunshineSyncAdapter == null) {
                sSunshineSyncAdapter = new SunshineSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSunshineSyncAdapter.getSyncAdapterBinder();
    }
}
