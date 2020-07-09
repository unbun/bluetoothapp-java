package athelas.javableapp.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;

import athelas.javableapp.utils.BluetoothConnectionService;
import athelas.javableapp.R;
import athelas.javableapp.utils.Utils;
import athelas.javableapp.utils.XYValue;

public class LiveVitalsActivity extends AppCompatActivity {
    public static String TAG = "LiveVitalsActivity";

    TextView tvIncomingMessages;
    StringBuilder readMessages;

    BluetoothConnectionService mBTConnection;

    private LineGraphSeries<DataPoint> vitalSeries;
    private ArrayList<XYValue> xyValueList;
    GraphView mLinePlot;

    double currX, currY;
    double startTimeMs = 0;
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
                try {
                    currY = Double.parseDouble(readMessages.toString().replaceAll("\\D+", ""));
                    currX = (System.currentTimeMillis() - startTimeMs) / 1000.0f;

                    xyValueList.add(new XYValue(currX, currY));
                    Log.d(TAG, "mReadReciever: plotting data to graph (" + currY + ", " + currX + ")");

                    initGraph();

                    readMessages = new StringBuilder();
                } catch (NumberFormatException e) {
                    Log.d(TAG, "mReadReciever: bad data given for vitals graph: " + readMessages.toString());
                }
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

        mLinePlot = (GraphView) findViewById(R.id.vitalGraph);
        xyValueList = new ArrayList<>();

        startTimeMs = System.currentTimeMillis();
        currX = 0;

        initGraph();

    }

    private void initGraph(){
        vitalSeries = new LineGraphSeries<>();

        if(xyValueList.size() != 0) {
            createScatterPlot();
        } else {
            Log.d(TAG, "onCreate: No data to plot");
        }
    }

    private void createScatterPlot() {
        Log.d(TAG, "createScatterPlog: Creating scatter plot.");
        
        xyValueList = Utils.sortArrayByX(xyValueList);

        double maxX = 0;
        for(int ii = 0; ii < xyValueList.size(); ii++) {
            try {
                double x = xyValueList.get(ii).getX();
                double y = xyValueList.get(ii).getY();
                vitalSeries.appendData(new DataPoint(x,y), true, 1000);
                maxX = Math.max(x, maxX);
            } catch (IllegalArgumentException e) {
                Log.d(TAG, "createScatterPlot: IllegalArgumentException: " + e.getMessage());
            }
        }

        //set some properties
        vitalSeries.setDrawDataPoints(true);
        vitalSeries.setDataPointsRadius(5f);
        vitalSeries.setColor(Color.BLUE);

        //set Scrollable and Scaleable
        mLinePlot.getViewport().setScalable(true);
        mLinePlot.getViewport().setScalableY(true);
        mLinePlot.getViewport().setScrollable(true);
        mLinePlot.getViewport().setScrollableY(true);

        //set manual x bounds
        mLinePlot.getViewport().setYAxisBoundsManual(true);
        mLinePlot.getViewport().setMaxY(100);
        mLinePlot.getViewport().setMinY(0);

        //set manual y bounds
        mLinePlot.getViewport().setXAxisBoundsManual(true);
        mLinePlot.getViewport().setMaxX(maxX + 1);
        mLinePlot.getViewport().setMinX(Math.max(maxX - 30, 0));

        mLinePlot.addSeries(vitalSeries);

        mLinePlot.addSeries(vitalSeries);
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