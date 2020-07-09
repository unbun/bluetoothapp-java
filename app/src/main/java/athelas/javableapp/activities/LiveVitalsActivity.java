package athelas.javableapp.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;

import athelas.javableapp.BluetoothConnectionService;
import athelas.javableapp.R;

public class LiveVitalsActivity extends AppCompatActivity {

    TextView tvIncomingMessages;
    StringBuilder readMessages;

    BluetoothConnectionService mBTConnection;

    private BroadcastReceiver mReadReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("data");

            // asynchronized problems require some kind of "end of file" message
            // to make sure the whole message gets through and is displayed properly
            boolean completedMessage = false;
            readMessages.append(text);
            if(readMessages.toString().endsWith("EOF")){
                readMessages.setLength(readMessages.length() - 3);
                completedMessage = true;
            }

            tvIncomingMessages.setText(readMessages);

            if(completedMessage) {
                readMessages = new StringBuilder();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_vitals);
        onCreateActSwitch();
        setTitle("athelas Vital Data");
        mBTConnection = MainActivity.getBluetoothConnection();

        readMessages = new StringBuilder();

        tvIncomingMessages = (TextView) findViewById(R.id.incomingMessages);

        //Use local broadcast manager to recieve incoming messages
        LocalBroadcastManager.getInstance(this).registerReceiver(mReadReciever, new IntentFilter("incomingMessage"));

    }




    ///////////////////////////////////////////////////////////////
    ///// Activity Switching //////////////////////////////////////
    ///////////////////////////////////////////////////////////////

    public ImageButton toConnectBtn;
    public ImageButton toRobotCtrlBtn;

    public void onCreateActSwitch() {
        toConnectBtn = (ImageButton) findViewById(R.id.toConnectBtn);
        toConnectBtn.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent resultIntent = new Intent();
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });

        toRobotCtrlBtn  = (ImageButton) findViewById(R.id.toRobotBtn);
        toRobotCtrlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loadNewACt = new Intent(LiveVitalsActivity.this, RobotControlActivity.class);
                startActivityForResult(loadNewACt, 2);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            // do anything from the LiveVitalActivity
        } else if(resultCode == RESULT_CANCELED) {
            Toast.makeText(getApplicationContext(), "Robot Controlling Failed", Toast.LENGTH_LONG).show();
        }
    }
}