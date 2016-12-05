package seniordesign.arduinotelephoneinterface;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.*;
import android.telephony.gsm.SmsMessage;
import android.widget.EditText;
import android.widget.TextView;
import android.support.v4.app.ActivityCompat;
import android.Manifest;
import android.content.pm.PackageManager;

import java.io.*;

import com.opencsv.*;

public class ComputerMode extends AppCompatActivity {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private TextView txtResponse;
    private EditText phoneNumber;

    BroadcastReceiver smsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //---get the SMS message passed in---
            Bundle bundle = intent.getExtras();
            SmsMessage[] msgs = null;
            if (bundle != null) {
                //---retrieve the SMS message received---
                Object[] pdus = (Object[]) bundle.get("pdus");
                msgs = new SmsMessage[pdus.length];
                for (int i = 0; i < msgs.length; i++) {
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    if (msgs[i].getOriginatingAddress().contains(phoneNumber.getText())) {
                        String msg = msgs[i].getMessageBody().toString();
                        String[] data = msg.split(",");

                        writeToCSV(data);

                        StringBuilder dataStr = new StringBuilder();
                        for (int j = 0; j < data.length; j++) {
                            dataStr.append(data[j] + "|");
                        }
                        dataStr.append("\n");
                        tvAppend(txtResponse, dataStr.toString());
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_computer_mode);

        txtResponse = (TextView) findViewById(R.id.txtArduinoResponse);
        phoneNumber = (EditText) findViewById(R.id.recPhoneNum);

        // Register a broadcast receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.setPriority(999);
        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(smsReceiver, intentFilter);

        //Request permissions for file writing
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private void writeToCSV(String[] data)
    {
        try {
            String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
            String fileName = "PhaseAngleData.csv";
            String filePath = baseDir + File.separator + fileName;
            File f = new File(filePath );
            CSVWriter writer;
            // File exist
            if(f.exists() && !f.isDirectory()){
                FileWriter mFileWriter = new FileWriter(filePath , true);
                writer = new CSVWriter(mFileWriter);
            }
            else {
                writer = new CSVWriter(new FileWriter(filePath));
            }

            writer.writeNext(data);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
