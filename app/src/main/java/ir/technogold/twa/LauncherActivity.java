package ir.technogold.twa;

import android.content.ComponentName;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsCallback;
import androidx.browser.customtabs.CustomTabsClient;
import androidx.browser.customtabs.CustomTabsServiceConnection;
import androidx.browser.customtabs.CustomTabsSession;
import androidx.browser.trusted.TrustedWebActivityIntentBuilder;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;

public class LauncherActivity extends AppCompatActivity {

    private CustomTabsClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Thread(() -> {
            String gaid = retrieveGAID();
            if (gaid != null) {
                runOnUiThread(() ->
                        CustomTabsClient.bindCustomTabsService(
                                LauncherActivity.this,
                                "com.android.chrome",
                                getConnection("https://app.technogold.gold?gps_adid=" + gaid)
                        ));
            }
        }).start();
    }

    private String retrieveGAID() {
        try {
            AdvertisingIdClient.Info adInfo = AdvertisingIdClient.getAdvertisingIdInfo(this);
            return adInfo.getId();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private CustomTabsServiceConnection getConnection(final String url) {
        return new CustomTabsServiceConnection() {

            @Override
            public void onServiceDisconnected(@NonNull ComponentName name) {
                mClient = null;
            }

            @Override
            public void onCustomTabsServiceConnected(@NonNull ComponentName name, @NonNull CustomTabsClient client) {
                mClient = client;
                mClient.warmup(0);
                CustomTabsSession customTabsSession = mClient.newSession(new CustomTabsCallback());
                if (customTabsSession != null) {
                    TrustedWebActivityIntentBuilder intentBuilder = new TrustedWebActivityIntentBuilder(Uri.parse(url));
                    intentBuilder.build(customTabsSession).launchTrustedWebActivity(LauncherActivity.this);
                }
            }
        };
    }
}
