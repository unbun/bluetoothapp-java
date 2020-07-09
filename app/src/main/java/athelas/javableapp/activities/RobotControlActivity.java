package athelas.javableapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import athelas.javableapp.utils.*;
import athelas.javableapp.R;

public class RobotControlActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static String TAG = "RobotControlActivity";

    BluetoothConnectionService mBTConnection;
    List<ImageButton> ctrlButtons = new ArrayList<>();

    EditText etBase, etTilt, etElbow, etWrist;

    Spinner sTargets;
    ArrayAdapter<CharSequence> targetAdapter;


    /*
        Command String: sss:sss[:][ddd]

        Move Joints: MOV:<jjj>[:param]
            jjj: J00-J06, ALL, NaN
            :param: w=[angular speed]
        Stop Joints: STP:<jjj>
            jjj: J00-J06, ALL, NaN
        Retract: RTC:<jjj>
            jjj: J00-J06, ALL, NaN
        Track Body Part: TRK:<target>
            target: HEAD, CHST, HAND
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robot_control);
        mBTConnection = MainActivity.getBluetoothConnection();

        setTitle("athelas Robot Control");
        onCreateActSwitch();

        etBase = (EditText) findViewById(R.id.editBaseSpeed);
        etTilt = (EditText) findViewById(R.id.editTiltSpeed);
        etElbow = (EditText) findViewById(R.id.editElbowSpeed);
        etWrist = (EditText) findViewById(R.id.editWristSpeed);

        ctrlButtons.add((ImageButton) findViewById(R.id.ctrlBtn_j0Pos));
        ctrlButtons.add((ImageButton) findViewById(R.id.ctrlBtn_j0Pos));
        ctrlButtons.add((ImageButton) findViewById(R.id.ctrlBtn_j0Neg));
        ctrlButtons.add((ImageButton) findViewById(R.id.ctrlBtn_j1Pos));
        ctrlButtons.add((ImageButton) findViewById(R.id.ctrlBtn_j1Neg));
        ctrlButtons.add((ImageButton) findViewById(R.id.ctrlBtn_j2Pos));
        ctrlButtons.add((ImageButton) findViewById(R.id.ctrlBtn_j2Neg));
        ctrlButtons.add((ImageButton) findViewById(R.id.ctrlBtn_j3Pos));
        ctrlButtons.add((ImageButton) findViewById(R.id.ctrlBtn_j3Neg));

        for(final ImageButton iBtn : ctrlButtons) {
            final String desc = iBtn.getContentDescription().toString();

            iBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String jointName = ctrlBtnOnClick(desc);
                    if(!jointName.isEmpty()) {
                        Utils.toastMessage(getApplicationContext(), "Moving " + jointName);
                    } else {
                        Log.d(TAG, "setOnClickListener: btn description has bad joint label "+ desc);
                    }
                }
            });

        }

        Button btnRetract = (Button) findViewById(R.id.retractBtn);
        btnRetract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeCommand("RTC:ALL");
                Utils.toastMessage(getApplicationContext(),"Retracting");
            }
        });

        Button btnStop = (Button) findViewById(R.id.stopBtn);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeCommand("STP:ALL");
                Utils.toastMessage(getApplicationContext(), "Stopping");
            }
        });

        sTargets = (Spinner) findViewById(R.id.targetSpinner);
        sTargets.setOnItemSelectedListener(RobotControlActivity.this);

        targetAdapter =
                ArrayAdapter.createFromResource(this, R.array.targets, android.R.layout.simple_spinner_item);
        targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sTargets.setAdapter(targetAdapter);

    }

    private String ctrlBtnOnClick(String desc) {
        int jointIdx = desc.indexOf('J');

        final String jointName = desc.substring(jointIdx, jointIdx + 2);

        double jointSpeed = (desc.startsWith("+") ? 1.0 : -1.0);
        try{
            if(jointName.endsWith("0")) {
                jointSpeed *= Double.parseDouble(etBase.getText().toString());
            } else if(jointName.endsWith("1")) {
                jointSpeed *= Double.parseDouble(etTilt.getText().toString());
            } else if(jointName.endsWith("2")) {
                jointSpeed *= Double.parseDouble(etElbow.getText().toString());
            } else if(jointName.endsWith("3")) {
                jointSpeed *= Double.parseDouble(etWrist.getText().toString());
            } else {
                jointSpeed = 0.0;
            }
            Log.d(TAG, "ctrlBtnOnClick: joint and joint speed msg created");
        } catch (NumberFormatException nfe) {
            Log.d(TAG, "ctrlBtnOnClick: canceled bc no valid joint speed");
            Utils.toastMessage(getApplicationContext(), "Specify Joint Speed (deg/sec)");
            return "";
        }

        writeCommand(jointName, jointSpeed);
        return jointName;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Log.d(TAG, "onItemSelected: activating preset robot movement");
        String target = targetAdapter.getItem(i).toString();
        String cmdString = "TRK:";
        switch (target.toLowerCase()){
            case "forehead" : cmdString += "HEAD";
            break;
            case "chest" : cmdString += "CHST";
            break;
            case "hand" : cmdString += "HAND";
            break;
            case "none (stop)" : cmdString = "STP:ALL";
            break;
            default: cmdString = "RTC:ALL";
        }
        writeCommand(cmdString);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        return;
    }

    private void writeCommand(String cmdString) {
        Log.d(TAG, "writeCommand: writing to bt connection");
        byte[] bytes = cmdString.getBytes(Charset.defaultCharset());
        mBTConnection.write(bytes);
    }

    private void writeCommand(String jointName, double jointSpeed) {
        writeCommand("MOV:" + jointName + ":w=" + jointSpeed);
    }

    ///////////////////////////////////////////////////////////////
    ///// Activity Switching //////////////////////////////////////
    ///////////////////////////////////////////////////////////////

    public ImageButton toVitalsBtn;

    public void onCreateActSwitch() {
        Log.d(TAG, "onCreateActSwitch: returning from robot ctr activity");
        toVitalsBtn = (ImageButton) findViewById(R.id.rbtToVitalsBtn);
        toVitalsBtn.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent resultIntent = new Intent();
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }


}