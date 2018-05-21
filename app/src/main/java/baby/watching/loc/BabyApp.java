package baby.watching.loc;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.util.Log;

/**
 * Created by root on 18/12/17.
 */

public class BabyApp extends Application {
    private static final String TAG = "BabyApp";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "app create");
        try {
            PackageManager packageManager = this.getPackageManager();
            PackageInfo info = packageManager.getPackageInfo(this.getPackageName(),0);
            Log.i("Package name : ",info.packageName);
            AppConstants.ROOT_PACKAGE = info.packageName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Configuration configuration = newConfig;
    }
}
