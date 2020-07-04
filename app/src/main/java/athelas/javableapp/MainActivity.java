package athelas.javableapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Button buttonOn, buttonOff;
    BluetoothAdapter btAdapter;

    Intent btEnabingIntent;
    int enableRequestCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Spinner deviceSpinner = (Spinner) findViewById(R.id.deviceSpinner);
        buttonOn = (Button) findViewById(R.id.btOn);
        buttonOff = (Button) findViewById(R.id.btOff);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        btEnabingIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        enableRequestCode = 1;

        setupBluetoothOn();
        setupBluetoothOff();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == enableRequestCode) {
            if(resultCode == RESULT_OK) {
                Toast.makeText(getApplicationContext(), "Bluetooth is enabled", Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Bluetooth enabling canceled", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setupBluetoothOn() {
        buttonOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(btAdapter == null) {
                    Toast.makeText(getApplicationContext(), "Bluetooth is not supported on this device", Toast.LENGTH_LONG).show();
                } else {
                    if(!btAdapter.isEnabled()) {
                        startActivityForResult(btEnabingIntent, enableRequestCode);
                    }
                }
            }
        });
    }


    private void setupBluetoothOff() {
        buttonOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(btAdapter.isEnabled()) {
                    btAdapter.disable();
                    Toast.makeText(getApplicationContext(), "Bluetooth is disabled", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}