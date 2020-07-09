package athelas.javableapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import athelas.javableapp.BluetoothConnectionService;
import athelas.javableapp.R;

public class RobotControlActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

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
                        Toast.makeText(getApplicationContext(),
                                "Moving " + jointName, Toast.LENGTH_LONG).show();
                    }
                }
            });

        }

        Button btnRetract = (Button) findViewById(R.id.retractBtn);
        btnRetract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeCommand("RTC:ALL");
                Toast.makeText(getApplicationContext(),
                        "Retracting", Toast.LENGTH_LONG).show();
            }
        });

        Button btnStop = (Button) findViewById(R.id.stopBtn);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeCommand("STOP:ALL");
                Toast.makeText(getApplicationContext(),
                        "Stopping", Toast.LENGTH_LONG).show();
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
        } catch (NumberFormatException nfe) {
            Toast.makeText(getApplicationContext(),
                    "Specify Joint Speed (deg/sec)", Toast.LENGTH_LONG).show();
            return "";
        }

        writeCommand(jointName, jointSpeed);
        return jointName;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String target = targetAdapter.getItem(i).toString();
        String cmdString = "TRK:";
        switch (target.toLowerCase()){
            case "forehead" : cmdString += "HEAD";
            break;
            case "chest" : cmdString += "CHST";
            break;
            case "hand" : cmdString += "HAND";
            break;
            case "none (stop)" : cmdString = "STOP:ALL";
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