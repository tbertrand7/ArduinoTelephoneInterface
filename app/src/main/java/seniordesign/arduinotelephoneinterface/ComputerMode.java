package seniordesign.arduinotelephoneinterface;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.*;
import android.telephony.gsm.SmsMessage;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.Telephony.*;

public class ComputerMode extends AppCompatActivity {

    private TextView txtResponse;

    BroadcastReceiver smsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //---get the SMS message passed in---
            Bundle bundle = intent.getExtras();
            SmsMessage[] msgs = null;
            String str = "";
            if (bundle != null)
            {
                //---retrieve the SMS message received---
                Object[] pdus = (Object[]) bundle.get("pdus");
                msgs = new SmsMessage[pdus.length];
                for (int i=0; i<msgs.length; i++){
                    msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                    str += "SMS from " + msgs[i].getOriginatingAddress();
                    str += ": ";
                    str += msgs[i].getMessageBody().toString();
                }
                //---display the new SMS message---
                //Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
                tvAppend(txtResponse, str);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_computer_mode);

        txtResponse = (TextView) findViewById(R.id.txtArduinoResponse);

        // Register a broadcast receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.setPriority(999);
        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        //intentFilter.addDataScheme("sms");
        //intentFilter.addDataAuthority("*", "6734");
        registerReceiver(smsReceiver, intentFilter);
    }

    private void tvAppend(TextView tv, CharSequence text) {
        final TextView ftv = tv;
        final CharSequence ftext = text;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ftv.append(ftext);
            }
        });
    }
}
