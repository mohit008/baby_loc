package baby.watching.loc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DisplayReceiver extends BroadcastReceiver{
    Context context;

	@Override
	public void onReceive(Context context, Intent intent) {
        this.context = context.getApplicationContext();
		String action = intent.getAction();
		if(action.equals(Intent.ACTION_SCREEN_OFF) || action.equals(Intent.ACTION_BOOT_COMPLETED)){
            if(!AppConstants.isActivityRunning(this.context)){
                callLocking();
            }
		}
		/*if(action.equals("android.intent.action.PHONE_STATE")){
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            String number = TelephonyManager.EXTRA_INCOMING_NUMBER;
            // ringing
            if(state.equals(TelephonyManager.EXTRA_STATE_RINGING)){
                callState(number);
            }
            // picked up
            if ((state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK))){
            }
            // call ended
            if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)){
            }
            if(action.equals(Intent.ACTION_NEW_OUTGOING_CALL)){
                Toast.makeText(context,"Call",Toast.LENGTH_SHORT).show();
            }
        }*/
	}

    /**
     * call locker activity
     */
	public void callLocking(){
        Intent in = new Intent(context,Home.class);
        in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(in);
    }

    /*
     * call phone satte activity
     */
   /* public void callState(String number){
        Intent in = new Intent(context,PhoneStateMonitor.class);
        in.putExtra("number",number);
        in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(in);
    }*/
}
