package andrew.cmu.edu.earthquake;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.LiveFolders;
import android.support.v7.app.AppCompatActivity;

/**
 * Author  : KAILIANG CHEN
 * Version : 0.1
 * Date    : 1/23/16
 */
public class EarthquakeLiveFoldersActivity extends AppCompatActivity {
    public static class EarthquakeLiveFolderActivity extends AppCompatActivity {
        private static Intent createLiveFolderIntent(Context context) {
            Intent intent = new Intent();
            intent.setData(EarthquakeProvider.LIVE_FOLDER_URI);
            intent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_BASE_INTENT,
                    new Intent(Intent.ACTION_VIEW, EarthquakeProvider.CONTENT_URI));
            intent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_DISPLAY_MODE,
                    LiveFolders.DISPLAY_MODE_LIST);
            intent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_ICON,
                    Intent.ShortcutIconResource.fromContext(context, R.drawable.app_icon));
            intent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_NAME, "Earthquakes");
            return intent;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            String action = getIntent().getAction();
            if (LiveFolders.ACTION_CREATE_LIVE_FOLDER.equals(action)) {
                setResult(RESULT_OK, createLiveFolderIntent(this));
            } else {
                setResult(RESULT_CANCELED);
            }
            finish();
        }
    }
}
