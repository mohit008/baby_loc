/*
package baby.watching.loc;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

*/
/**
 * Created by mohit.soni on 19/12/17.
 *//*


public class PhoneStateMonitor extends Activity {

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phone_state);
        TextView tvCallerName = (TextView) findViewById(R.id.tvCallerName);
        ImageView ivEndCall = (ImageView) findViewById(R.id.ivEndCall);
        ImageView ivCall = (ImageView) findViewById(R.id.ivCall);
        tvCallerName.setTypeface(new AppConstants().getFont(PhoneStateMonitor.this));

        intent = getIntent();
        if (intent != null) {
            AppConstants.SET_DIM(PhoneStateMonitor.this, 255);
            tvCallerName.setText(intent.getExtras().getString("number"));
        }
        ivCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

}
*/
