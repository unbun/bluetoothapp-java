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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import athelas.javableapp.utils.BluetoothConnectionService;
import athelas.javableapp.R;
import athelas.javableapp.utils.Utils;
import athelas.javableapp.utils.XYValue;

public class LiveVitalsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    public static String TAG = "LiveVitalsActivity";

    TextView tvIncomingMessages;
    StringBuilder readMessages;

    BluetoothConnectionService mBTConnection;

    private LineGraphSeries<DataPoint> vitalSeries;
    private ArrayList<XYValue> currXYValues;
    private ArrayList<XYValue> heartValues, bloodO2Values, tempValues, lungValues;
    GraphView mLinePlot;

    Spinner testSelect;
    int currTestColor;
    Map<String, Integer> testToColor;
    ArrayAdapter<CharSequence> testAdapter;

    double currX, currY;
    double startTimeMs = 0;

    final int xAxisSize = 20;

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

                    currXYValues.add(new XYValue(currX, currY));
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

        testSelect = (Spinner) findViewById(R.id.testSelection);
        testAdapter =
                ArrayAdapter.createFromResource(this, R.array.tests, android.R.layout.simple_spinner_item);
        testAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        testSelect.setAdapter(testAdapter);

        testToColor = new HashMap<>();
        testToColor.put("heart rate", Color.argb(255,233,30,90));
        testToColor.put("blood o2", Color.argb(255, 3, 169, 244));
        testToColor.put("temperature", Color.argb(255, 255, 87, 34));
        testToColor.put("lung audio", Color.argb(255, 104, 89, 84));
        currTestColor = Color.argb(100,233,30,90);

        mLinePlot = (GraphView) findViewById(R.id.vitalGraph);
        heartValues = new ArrayList<>();
        bloodO2Values = new ArrayList<>();
        tempValues = new ArrayList<>();
        lungValues = new ArrayList<>();

        currXYValues = heartValues;

        testSelect.setOnItemSelectedListener(LiveVitalsActivity.this);

        startTimeMs = System.currentTimeMillis();
        currX = 0;
        initGraph();

    }

    private void initGraph(){
        vitalSeries = new LineGraphSeries<>();

        if(currXYValues.size() != 0) {
            createScatterPlot();
        } else {
            Log.d(TAG, "onCreate: No data to plot");
        }
    }

    private void createScatterPlot() {
        Log.d(TAG, "createScatterPlog: Creating scatter plot.");
        
        heartValues = Utils.sortArrayByX(currXYValues);

        double maxX = 0;
        for(int ii = 0; ii < currXYValues.size(); ii++) {
            try {
                double x = currXYValues.get(ii).getX();
                double y = currXYValues.get(ii).getY();
                vitalSeries.appendData(new DataPoint(x,y), true, 1000);
                maxX = Math.max(x, maxX);
            } catch (IllegalArgumentException e) {
                Log.d(TAG, "createScatterPlot: IllegalArgumentException: " + e.getMessage());
            }
        }

        //set some properties
        vitalSeries.setDrawDataPoints(true);
        vitalSeries.setDataPointsRadius(5f);
        vitalSeries.setColor(currTestColor);

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
        mLinePlot.getViewport().setMinX(Math.max(maxX - xAxisSize, 0));

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

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Log.d(TAG, "onItemSelected: changing currentTest");
        String target = testAdapter.getItem(i).toString().toLowerCase();
        currTestColor = testToColor.get(target);
        tvIncomingMessages.setBackgroundColor(currTestColor);
        switch (target) {
            case "heart rate": currXYValues = heartValues;
            break;
            case "blood o2": currXYValues = bloodO2Values;
            break;
            case "temperature": currXYValues = tempValues;
            break;
            case "lung audio": currXYValues = lungValues;
            break;
        }
        initGraph();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}