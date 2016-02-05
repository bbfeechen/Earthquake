package andrew.cmu.edu.earthquake;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class EarthquakeAlarmReceiver extends BroadcastReceiver {
    private static final String TAG = EarthquakeAlarmReceiver.class.getSimpleName();

    public static final String ACTION_REFRESH_EARTHQUAKE_ALARM =
            "andrew.cmu.edu.earthquake.ACTION_REFRESH_EARTHQUAKE_ALARM";

    public EarthquakeAlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");

        Intent startIntent = new Intent(context, EarthquakeUpdateService.class);
        context.startService(startIntent);
    }
}
