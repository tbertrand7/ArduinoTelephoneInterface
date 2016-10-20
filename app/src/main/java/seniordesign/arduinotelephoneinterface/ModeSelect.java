package seniordesign.arduinotelephoneinterface;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class ModeSelect extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode_select);
    }

    /** Launches activity to collect and send Arduino data */
    public void launchArduinoMode(View view)
    {
        Intent intent = new Intent(this, ArduinoMode.class);
        startActivity(intent);
    }

    /** Launches activity to receive Arduino data and send to computer */
    public void launchComputerMode(View view)
    {
        Intent intent = new Intent(this, ComputerMode.class);
        startActivity(intent);
    }
}
