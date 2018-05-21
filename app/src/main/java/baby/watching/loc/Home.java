package baby.watching.loc;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextClock;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Set;

import baby.watching.R;
import baby.watching.adapter.RecycleAdapter;
import baby.watching.model.NotificationBean;
import baby.watching.view.MyProgressView;

import static baby.watching.loc.AppConstants.animateProgress;

/**
 * Created by mohit.soni on 11/20/2017.
 */

public class Home extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = "Home";

    static ContentResolver resolver;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    private boolean granted = true;
    AppConstants constantsLocking = new AppConstants();
    NotificationListenerReceiver notificationListenerReceiver;
    TOP_CPU_LISTENER top_cpu_listener;

    TextClock clock;
    TextView date;
    RelativeLayout rl;
    MyProgressView arc_one, arc_tow, arc_three, arc_four, big_arc;

    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    RecycleAdapter recycleAdapter;

//    ProgressBar p_base;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Activity Created");
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE
        );
        request();
        serviceTask();
        register();
        setInit();
    }

    /**
     * register broadcast receiver
     */
    private void register() {
        Set<String> listnerSet = NotificationManagerCompat.getEnabledListenerPackages(this);
        boolean haveAccess = false;
        for (String sd : listnerSet) {
            if (sd.equals(AppConstants.ROOT_PACKAGE)) {
                haveAccess = true;
            }
        }
        if (!haveAccess) {
            startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
        }

        notificationListenerReceiver = new NotificationListenerReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(AppConstants.NOTIFICATION_RECEIVER);
        registerReceiver(notificationListenerReceiver, filter);

        top_cpu_listener = new TOP_CPU_LISTENER();
        IntentFilter top_filter = new IntentFilter();
        top_filter.addAction(AppConstants.TOP_CPU_DATA);
        registerReceiver(top_cpu_listener, top_filter);

//      registerReceiver(registerReceiver, new IntentFilter(AppConstants.CALL_CUSTOM));
    }

    /**
     * perform service tasks
     */
    public void serviceTask() {
        AppConstants.isServiceRunning(this.getApplicationContext(), AppConstants.CLASSES);
        for (int i = 0; i < AppConstants.SERVICE_CLASS.size(); i++) {
            boolean status = AppConstants.SERVICE_CLASS.get(AppConstants.CLASSES[i]);
            if (!status) {
                startService(new Intent(this, AppConstants.CLASSES[i]));
            }
        }
    }

    /**
     * asked for request if API > 22
     */
    private void request() {
        if (Build.VERSION.SDK_INT > 22) {
            boolean canChange = Settings.System.canWrite(this);
            if (!canChange) {
                Intent openSetting = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                openSetting.setData(Uri.parse("package:" + this.getPackageName()));
                openSetting.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(openSetting);
            }
            requestPermissions(AppConstants.PERMISSION, AppConstants.PERMISSION_REQUESTCODE);
        }
    }

    /**
     * unregister displayReceiver
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(notificationListenerReceiver);
        unregisterReceiver(top_cpu_listener);
    }

//    /**
//     * to receive that screen is rotated so increase the brightness
//     */
//    private BroadcastReceiver registerReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String val = "", type = "";
//            if (intent.getAction().equals(AppConstants.CALL_CUSTOM)) {
//                val = (String) intent.getExtras().get("val");
//                type = (String) intent.getExtras().get("type");
//            }
//            if (type.equals(AppConstants.ACCELEROMETER)) {
//                setBrightness(val);
//            }
//            if (type.equals(AppConstants.PROXIMITY)) {
//                powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
//                wakeLock = powerManager.newWakeLock(PowerManager
//                        .SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "tag");
//                wakeLock.acquire();
//                unlock();
//                wakeLock.release();
//            }
//        }
//    };


    /**
     * do nothing
     */
    @Override
    public void onBackPressed() {
        return;
    }

//    /**
//     * unlock activity
//     */
//    public void unlock() {
//        setBrightness(AppConstants.MAX_BRIGHTNESS+"");
//        AppConstants.SET_DIM(Home.this, 255);
//        finish();
//        android.os.Process.killProcess(android.os.Process.myPid());
//    }

    /**
     * set brightness
     *
     * @param vx
     */
    public void setBrightness(String vx) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = constantsLocking.getBrightness(vx);
        getWindow().setAttributes(lp);
    }

    /**
     * set initial variables
     */
    public void setInit() {
        setContentView(R.layout.locker);

        clock = (TextClock) findViewById(R.id.clock);
        date = (TextView) findViewById(R.id.date);
        rl = (RelativeLayout) findViewById(R.id.rl);

        arc_one = (MyProgressView) findViewById(R.id.arc_one);
        arc_tow = (MyProgressView) findViewById(R.id.arc_tow);
        arc_three = (MyProgressView) findViewById(R.id.arc_three);
        arc_four = (MyProgressView) findViewById(R.id.arc_four);
        big_arc = (MyProgressView) findViewById(R.id.big_arc);
//      p_base = (ProgressBar) findViewById(R.id.p_base);

        date.setTypeface(constantsLocking.getFont(this));
        clock.setTypeface(constantsLocking.getFont(this));
        clock.setFormat12Hour("KK:mm");

        arc_one.setStartingDegree(0);
        arc_one.setStrokeWidth(5);
        arc_tow.setStartingDegree(90);
        arc_tow.setStrokeWidth(5);
        arc_three.setStartingDegree(180);
        arc_three.setStrokeWidth(5);
        arc_four.setStartingDegree(270);
        arc_four.setStrokeWidth(5);
        big_arc.setStartingDegree(270);
        big_arc.setStrokeWidth(2);


        animateProgress(arc_one, 25);
        animateProgress(arc_tow, 25);
        animateProgress(arc_three, 25);
        animateProgress(arc_four, 25);
        animateProgress(big_arc, 100);

//      animateProgress(p_base, 100);

        clock.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (granted) {
//                    unlock();
                    finish();
                }
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.rview);
//      mLayoutManager = new LinearLayoutManager(this);
//      mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());


        rl.setBackground(getResources().getDrawable(R.drawable.dark));
        clock.setTextColor(getResources().getColor(R.color.white));
        date.setTextColor(getResources().getColor(R.color.white));

        date.setText(constantsLocking.getNumDate(date));
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tag = (String) date.getTag();
                if (tag.equals("num")) {
                    date.setText(constantsLocking.getTextDate(date));
                } else {
                    date.setText(constantsLocking.getNumDate(date));
                }
                NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                NotificationCompat.Builder ncomp = new NotificationCompat.Builder(Home.this);
                ncomp.setContentTitle("My Notification");
                ncomp.setContentText("Notification Listener Service Example");
                ncomp.setTicker("Notification Listener Service Example");
                ncomp.setSmallIcon(R.mipmap.app_icon);
                ncomp.setAutoCancel(true);
                nManager.notify((int)System.currentTimeMillis(),ncomp.build());
            }
        });

//        setBrightness("9");
//        resolver = getContentResolver();
//        AppConstants.SET_DIM(this, 0);
//        MonitorService.setDefault();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkNotification();
            }
        }, AppConstants.ANIMATION_SPEED - 500);

//        ImageView bar = (ImageView) findViewById(R.id.bar);
//        Animation ani = AnimationUtils.loadAnimation(this, R.anim.rotate);
//        ani.setInterpolator(new AccelerateInterpolator());
//        bar.setAnimation(ani);
    }

    private void log(String msg) {
        Log.i(TAG, msg);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case AppConstants.PERMISSION_REQUESTCODE:
                if (permissions.length > 1 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    granted = false;
                }
        }
    }

    /**
     * get event from notification listener class
     */
    class NotificationListenerReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<NotificationBean> statusBar = (ArrayList<NotificationBean>) intent
                    .getExtras().get("notification");
            recycleAdapter = new RecycleAdapter(Home.this.getApplicationContext(), statusBar);
            mRecyclerView.setAdapter(recycleAdapter);
        }
    }

    /**
     * get event from top command listener class
     */
    class TOP_CPU_LISTENER extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(AppConstants.TOP_CPU_DATA)){
//                Toast.makeText(Home.this,(String)intent
//                        .getExtras().getString("value"),Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * init notification with dummy one
     */
    public void checkNotification() {
        NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder ncomp = new NotificationCompat.Builder(Home.this);
        ncomp.setContentTitle("BabyLoc");
        ncomp.setContentText("Checking Notification");
        ncomp.setTicker("BabyLoc.....Please wait");
        ncomp.setSmallIcon(R.mipmap.app_icon);
        ncomp.setAutoCancel(true);
        nManager.notify((int) 987654321, ncomp.build());

        nManager.cancel(987654321);
    }

}
