package baby.watching.save;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.service.notification.StatusBarNotification;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by mohit.soni on 21-Dec-17.
 */

public class AppShared {

    public static final String LOCKER_PREF = "locker_pref";
    public static final String VALUE = "value";

    public AppShared() {
        super();
    }

    public void manageNotification(Context context, String value, String op) {
        SharedPreferences settings = context.getSharedPreferences(LOCKER_PREF, Context.MODE_PRIVATE);
        Editor editor = settings.edit();
        if(op.equals("create")){
            editor.putString(VALUE, value);
        }
        if(op.equals("clear")){
            editor.clear();
        }
        editor.apply();
    }

    public String getNotification(Context context) {
        SharedPreferences settings;
        settings = context.getSharedPreferences(LOCKER_PREF, Context.MODE_PRIVATE);
        return settings.getString(VALUE, null);
    }


//    public void removeValue(Context context) {
//        SharedPreferences settings;
//        Editor editor;
//
//        settings = context.getSharedPreferences(LOCKER_PREF, Context.MODE_PRIVATE);
//        editor = settings.edit();
//
//        editor.remove(PREFS_KEY);
//        editor.commit();
//    }
}
