package athelas.javableapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class RobotControlActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robot_control);
        setTitle("athelas Robot Control");
        onCreateActSwitch();


    }

    ///////////////////////////////////////////////////////////////
    ///// Activity Switching //////////////////////////////////////
    ///////////////////////////////////////////////////////////////

    public Button toVitalsBtn;

    public void onCreateActSwitch() {
        toVitalsBtn = (Button) findViewById(R.id.rbtToVitalsBtn);
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