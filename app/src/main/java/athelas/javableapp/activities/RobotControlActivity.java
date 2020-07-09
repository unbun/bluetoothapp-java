package athelas.javableapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import athelas.javableapp.R;

public class RobotControlActivity extends AppCompatActivity {

    List<ImageButton> ctrlButtons = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robot_control);
        setTitle("athelas Robot Control");
        onCreateActSwitch();

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
            final String s = iBtn.getContentDescription().toString();
            iBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
                }
            });
        }

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