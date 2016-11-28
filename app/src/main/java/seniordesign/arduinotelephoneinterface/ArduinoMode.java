package seniordesign.arduinotelephoneinterface;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.Manifest;
import android.content.pm.PackageManager;
import android.widget.Toast;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.UnsupportedEncodingException;
import java.text.*;
import java.util.*;

public class ArduinoMode extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    public final String ACTION_USB_PERMISSION = "seniordesign.arduinotelephoneinterface.USB_PERMISSION";
    private Button connectButton, stopButton;
    private TextView txtResponse;
    private EditText phoneNum;
    private UsbManager usbManager;
    private UsbDevice device;
    private UsbSerialDevice serialPort;
    private UsbDeviceConnection connection;
    private StringBuffer msgData = new StringBuffer();

    private boolean isFirst = true;

    UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() { //Defining a Callback which triggers whenever data is read.
        @Override
        public void onReceivedData(byte[] arg0) {
            String data = null;
            try {
                data = new String(arg0, "UTF-8");
                createMsg(data.trim());
                tvAppend(txtResponse, data);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    };

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { //Broadcast Receiver to automatically start and stop the Serial connection.
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) {
                    connection = usbManager.openDevice(device);
                    serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                    if (serialPort != null) {
                        if (serialPort.open()) { //Set Serial Connection Parameters.
                            serialPort.setBaudRate(9600);
                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                            serialPort.read(mCallback);
                            Toast.makeText(getApplication(),"Serial Connection Opened!", Toast.LENGTH_SHORT).show();

                        } else {
                            Log.d("SERIAL", "PORT NOT OPEN");
                        }
                    } else {
                        Log.d("SERIAL", "PORT IS NULL");
                    }
                } else {
                    Log.d("SERIAL", "PERM NOT GRANTED");
                }
//            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
//                connect(connectButton);
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                onClickStop(stopButton);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arduino_mode);
        usbManager = (UsbManager) getSystemService(this.USB_SERVICE);
        connectButton = (Button) findViewById(R.id.btnConnect);
        stopButton = (Button) findViewById(R.id.btnDisconnect);
        txtResponse = (TextView) findViewById(R.id.txtArduinoResponse);
        phoneNum = (EditText) findViewById(R.id.phoneNum);
        //sendButton = (Button) findViewById(R.id.buttonSend);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(broadcastReceiver, filter);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
        }

        isFirst = true;
    }

    /* Connect to Arduino Device */
    public void connect(View view) {
        if (phoneNum.getText().toString().equals("")) {
            Toast.makeText(getApplication(), "Please enter a phone number", Toast.LENGTH_SHORT).show();
        } else {
            phoneNum.setEnabled(false);
            HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
            if (!usbDevices.isEmpty()) {
                boolean keep = true;
                for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                    device = entry.getValue();
                    int deviceVID = device.getVendorId();
                    if (deviceVID == 0x2341)//Arduino Vendor ID
                    {
                        PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                        usbManager.requestPermission(device, pi);
                        keep = false;
                    } else {
                        connection = null;
                        device = null;
                    }

                    if (!keep)
                        break;
                }
            }
        }
    }

    private void createMsg(String data)
    {
        if (data != null && !data.equals("")) {
            msgData.append(data);

            if (msgData.toString().contains("LEAD") || msgData.toString().contains("LAG")) {
                msgData.append(",");
                msgData.append((new SimpleDateFormat("hh:mm:ss aa").format(Calendar.getInstance().getTime())));

                if (isFirst) {
                    msgData = new StringBuffer();
                    isFirst = false;
                }
                else {
                    try {
                        sendSMS(msgData.toString());
                        msgData = new StringBuffer();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void onClickSend(View view) {
        //String string = editText.getText().toString();
        //serialPort.write(string.getBytes());
        //tvAppend(txtResponse, "\nData Sent : " + string + "\n");

    }

    public void onClickStop(View view) {
        serialPort.close();
        phoneNum.setEnabled(true);
        Toast.makeText(getApplication(),"Serial Connection Closed!", Toast.LENGTH_SHORT).show();
    }

    @SuppressWarnings("deprecation")
    private void sendSMS(String msg)
    {
        final String fmsg = msg;

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED && msg != null) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phoneNum.getText().toString(), null, fmsg, null, null);
                    //Toast.makeText(getApplicationContext(), "SMS sent", Toast.LENGTH_SHORT).show();
                }
            });
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
