package baby.watching.loc;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import baby.watching.model.NotificationBean;

/**
 * Created by mohit on 28-Mar-17.
 */
public class MonitorService extends NotificationListenerService implements SensorEventListener {

    private static final String TAG = "[MonitorService]";

    Set<Integer> acc_values = new HashSet<>();
    ArrayList<Integer> pro_values = new ArrayList<>();
    String pattern = "50";
    public static int delta = 0, x = 0, y = 0, z = 10, unlock_count = 0;
    private static long timer_count = 5000;

    //-- sensor
    SensorManager sensorManager;
    Context context;
    DisplayReceiver displayReceiver;
    Timer timer;
    TimerTask timeTask;


    //-- cpu
    HashMap<String,String> master = new HashMap<>();
    String[] titles = {"PID","PR","CPU%","S","#THR","VSS","RSS","PCY","UID","Name",
            "User %","System %","IOW %","IRQ %","Nice","Sys","Idle","IOW","IRQ","SIRQ"};
    JSONArray rootArray = new JSONArray();
    JSONObject root = new JSONObject();

    @Override
    public void onCreate() {
        //disable key guard
        KeyguardManager kManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock key = kManager.newKeyguardLock(KEYGUARD_SERVICE);
        key.disableKeyguard();

        // call display reciever with filter
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_BOOT_COMPLETED);


        // register sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            if (sensorManager.getDefaultSensor(Sensor.TYPE_ALL) != null) {
                sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
                sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY), SensorManager.SENSOR_DELAY_NORMAL);
            }
        }

        // register display receiver
        displayReceiver = new DisplayReceiver();
        registerReceiver(displayReceiver, filter);

        context = this.getApplicationContext();
        getDescriptionContent();

        startTimerforTopCPU();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service destroyed");
        sensorManager.unregisterListener(this);
        unregisterReceiver(displayReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * check for sensor value if greater then 6
     *
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
//        Sensor s = event.sensor;
//
//        // if accelerometer
//        if (s.getType() == Sensor.TYPE_ACCELEROMETER) {
//            x = (int) event.values[0];
//            y = (int) event.values[1];
//            z = (int) event.values[2];
//            if (y >= AppConstants.MINI_BRIGHTNESS) {
//                acc_values.add(y);
//                broadcastIntent(Integer.toString(y), AppConstants.ACCELEROMETER);
//                Log.i(TAG, "type : " + AppConstants.ACCELEROMETER + " : " + y + "");
//            }
//
//        }
//        // if proximity
//        if (s.getType() == Sensor.TYPE_PROXIMITY) {
//            delta = (int) event.values[0];
//            pattern = pattern + Integer.toString(delta);
//            pro_values.add(delta);
//            // check if pattern matches
//            if (Pattern.matches("05", pattern)) {
//                // check if phone is stable
//                if (isStable()) {
//                    Log.i(TAG, "x : " + x + ", " + " y : " + y + ", " + " z : " + z);
//                    unlock_count = unlock_count + 1;
//                    startTimerforProximity();
//                    broadcastIntent(Integer.toString(delta), AppConstants.PROXIMITY);
//                    Log.i(TAG, "type : " + AppConstants.PROXIMITY + " : " + delta + "");
//                }
//                pro_values.clear();
//                pattern = "";
//            }
//
//            // skip for first deploy
//            if (pattern.length() == 3) {
//                pattern = "";
//            }
//            delta = 0;
//        }
    }

    /**
     * sent brightness msg to activity
     *
     * @param val
     * @param type
     */
    public void broadcastIntent(String val, String type) {
        if (AppConstants.isActivityRunning(context.getApplicationContext())) {
            sendBroadcast(new Intent()
                    .setAction(AppConstants.CALL_CUSTOM)
                    .putExtra("val", val)
                    .putExtra("type", type));
        }
    }

    /**
     * check if device is in stable position
     *
     * @return
     */
    public boolean isStable() {
        return x >= -1 && x <= 1 && y >= -1 && y <= 1 && z == 10;
    }

    /**
     * det default value of variables
     */
    public static void setDefault() {
        x = y = delta = 0;
        z = 10;
    }

    /**
     * start timer_count to check for off signal
     */
    public void startTimerforProximity() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (unlock_count == 2) {
                    Intent in = new Intent(context, Home.class);
                    in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(in);
                }
                unlock_count = 0;
            }
        }, timer_count);
    }

    /**
     * start timer for top command
     */
    public void startTimerforTopCPU() {
        timer = new Timer();
        timeTask = new TimerTask() {
            @Override
            public void run() {
                collectCPUData();
            }
        };
        try {
            timer.schedule(timeTask, 3000, 5000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.i(TAG, "========== NotificationPosted :: ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());
        sendNotificationDetail();
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i(TAG, "========== NotificationRemoved :: ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());
        sendNotificationDetail();
    }

    /**
     * send notification detail to activity class
     */
    public void sendNotificationDetail() {
        ArrayList<NotificationBean> notificationBeans =new ArrayList<>();
        for (StatusBarNotification sbn : MonitorService.this.getActiveNotifications()) {
            NotificationBean bean = new NotificationBean();
            bean.setPackageName(sbn.getPackageName());
            bean.setId(sbn.getPackageName());
            bean.setTag(sbn.getPackageName());
            bean.setPostTime(sbn.getPackageName());
            bean.setAppName(getAppName(sbn.getPackageName()));
            bean.setIcon(sbn.getNotification().icon);
            bean.setCount(1);
            notificationBeans.add(bean);
        }
        checkCount(notificationBeans);
        // sent notification detail to activity
//        Log.i("NASDNCAINEICEsdfVNEWVW",notificationBeans.toString());
        Intent intent = new  Intent(AppConstants.NOTIFICATION_RECEIVER);
        intent.putExtra("notification", notificationBeans);
        sendBroadcast(intent);
    }


    /*public void createJson() {
        AppConstants.NOTIFICATION_JSON = "";
        AppShared locShared = new AppShared();
        StatusBarNotification[] statusBar = MonitorService.this.getActiveNotifications();
        try{
            JSONArray jsonArray = new JSONArray();
            JSONObject root = new JSONObject();
            for(StatusBarNotification sb: statusBar){
                JSONObject pkgOj = new JSONObject();
                pkgOj.put("pkg",sb.getPackageName());
                pkgOj.put("id",sb.getId());
                pkgOj.put("tag",sb.getTag());
//                pkgOj.put("key",sb.getKey());
//                pkgOj.put("groupKey",sb.getGroupKey());
//                pkgOj.put("overrideGroupKey",sb.getOverrideGroupKey());
//                pkgOj.put("initialPid",sb.getInitialPid());
                pkgOj.put("postTime",sb.getPostTime());
                pkgOj.put("name",getAppName(sb.getPackageName()));
                jsonArray.put(pkgOj);
            }
            root.put("notification",jsonArray);
            locShared.manageNotification(this,root.toString(),"clear");
            locShared.manageNotification(this,root.toString(),"create");
            AppConstants.NOTIFICATION_JSON = root.toString();
        }catch (JSONException e){
            e.printStackTrace();
        }
    }*/

    /*private void log(String msg) {
        Log.i(TAG, msg);
    }*/

    /**
     * get application name from package
     * @param pkg
     * @return
     */
    public String getAppName(String pkg){
        try {
            PackageManager manager = context.getPackageManager();
            ApplicationInfo applicationInfo = manager
                    .getApplicationInfo(pkg,PackageManager.GET_META_DATA);
            return (String) manager.getApplicationLabel(applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * check count of notification
     * @param notificationBeans
     */
    public void checkCount(ArrayList<NotificationBean> notificationBeans){
        for(int i=0; i<notificationBeans.size();i++){
            NotificationBean bean = notificationBeans.get(i);
            for(int j = notificationBeans.size() - 1; j > i;  j --){
                if(bean.getPackageName().equals(notificationBeans.get(j).getPackageName())){
                    bean.setCount(bean.getCount() + 1);
                    notificationBeans.remove(j);
                }
            }
        }
    }

    /**
     * collectCPUData 'top' data of process
     */
    public void collectCPUData() {
        String output = null;
        try {
            Process process = Runtime.getRuntime().exec("top -m 10 -n 1");
            InputStream in = process.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));

            StringBuilder builder = new StringBuilder();
            while (bufferedReader.readLine() != null) {
                builder.append(bufferedReader.readLine() + "\n");
            }
            output = builder.toString();
            Log.i("Exec_Service", output);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] str = output.split("\n");
        ArrayList<String[]> gts = new ArrayList<>();
        for(String s : str){
            if(s.isEmpty()){
                continue;
            }else{
                if(s.contains(",")){
                    // head
                    String[] head = s.split(",");
                    gts.add(head);
                }else{
                    if(!s.equals("null")){
                        String[] a = s.split(" " );
                        for (int i = 0; i < a.length; i++) {
                            if (a[i].equals("")) {
                                a[i] = null;
                            }
                        }
                        gts.add(a);
                    }
                }
            }
        }
        createJson(gts);

    }

    /**
     * create json of raw data
     * @param arrayList
     */
    public void createJson(ArrayList<String[]> arrayList) {
        try {

            //--head
            String[] head = arrayList.get(0);
            JSONArray headArray = new JSONArray();
            JSONObject headObject = new JSONObject();
            for (int i = 0; i < head.length; i++) {
                JSONObject headObj = new JSONObject();
                headObj.put("title", head[i]);
                headArray.put(headObj);
            }
            headObject.put("head",headArray);
            rootArray.put(headObject);

            //--content
            JSONArray dataArray = new JSONArray();
            JSONObject ContentObject = new JSONObject();
            arrayList.remove(null);
            for (int i = 1; i < arrayList.size(); i++) {
                String[] ar = getString(arrayList.get(i));
                JSONObject pkgOj = new JSONObject();
                pkgOj.put("PID", ar[0]);
                pkgOj.put("PR", ar[1]);
                pkgOj.put("CPU%", ar[2]);
                pkgOj.put("S", ar[3]);
                pkgOj.put("#THR", ar[4]);
                pkgOj.put("VSS", ar[5]);
                pkgOj.put("RSS", ar[6]);
                pkgOj.put("PCY", ar[7]);
                pkgOj.put("UID", ar[8]);
                pkgOj.put("Name", ar[9]);
                dataArray.put(pkgOj);
            }
            ContentObject.put("content",dataArray);
            rootArray.put(ContentObject);

            //--description
            JSONObject obj_Detail = new JSONObject();
            JSONArray object_DetailArray = new JSONArray();

            for(int i=0;i<= master.size()-1;i++){
                JSONObject objectDetail = new JSONObject();
                objectDetail.put("definition", master.get(titles[i]));
                objectDetail.put("title", titles[i]);
                object_DetailArray.put(objectDetail);
            }
            obj_Detail.put("description",object_DetailArray);
            rootArray.put(obj_Detail);

            root.put("root", rootArray);
            Log.i("JSON", root.toString());

            broadcastCPU(root.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * update array
     * @param strings
     * @return
     */
    public String[] getString(String[] strings){
        ArrayList<String> arrayList = new ArrayList<>();
        for (String str : strings) {
            if (str != null) {
                arrayList.add(str);
            }
        }
        if(arrayList.size() == 9){
            arrayList.add(7," ");
        }
        return arrayList.toArray(new String[arrayList.size()]);
    }


    /**
     * sent brightness msg to activity
     * @param value
     */
    public void broadcastCPU(String value) {
        sendBroadcast(new Intent()
                .setAction(AppConstants.TOP_CPU_DATA)
                .putExtra("value", value));
    }

    public void getDescriptionContent(){
        master.put("PID", "Process ID");
        master.put("PR", "Priority of the process ");
        master.put("CPU%", "CPU Usage");
        master.put("S", "State (or possibly status) R=Running, S=Sleeping");
        master.put("#THR", "Number of threads");
        master.put("VSS", "Virtual Set Size : Indicates how much virtual memory is associated with the process.");
        master.put("RSS", "Resident Set Size : Indicates how many physical pages are associated with the process.");
        master.put("PCY", "Policy -- Determines how an app should be treated by Android's memory. FG : Foreground, BG : Background");
        master.put("UID", "Name of the user that started the task");
        master.put("Name", "Name of Process");
        master.put("User %", "Percentage of the CPU for user processes");
        master.put("System %", "Percentage of the CPU for system processes");
        master.put("IOW %", "Percentage of the CPU on Input Output Wait");
        master.put("IRQ %", "Percentage of the CPU time spent servicing/handling hardware Interrupt Request");
        master.put("Nice", "Percentage of CPU time spent on low priority processes and been niced.");
        master.put("Sys", "CPU for system processes. The  time  the  CPU  has  spent  running  the  kernel  and  its processes.");
        master.put("Idle", "CPU time spent idle");
        master.put("IOW", "Input Output Wait");
        master.put("IRQ", "CPU time spent servicing/handling hardware Interrupt Requests");
        master.put("SIRQ", "CPU time spent servicing/handling software Interrupt Requests");
    }


}
