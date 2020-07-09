package athelas.javableapp.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class Utils {
    private static String TAG = "Utils";

    /**
     * Display a Toast Message
     * @param contex use getApplicationContext()
     * @param msg the message to display
     */
    public static void toastMessage(Context contex, String msg) {
        Toast.makeText(contex, msg, Toast.LENGTH_LONG).show();
    }

    /**
     * Sorts the xyValues in Ascending order to prepare them for the PointsGraphSeries<DataSet>
     * @param
     * @return
     */
    public static ArrayList<XYValue> sortArrayByX(ArrayList<XYValue> array) {
        /*
        //Sorts the xyValues in Ascending order to prepare them for the PointsGraphSeries<DataSet>
         */
        int factor = Integer.parseInt(String.valueOf(Math.round(Math.pow(array.size(), 2))));
        int m = array.size() - 1;
        int count = 0;
        // Log.d(TAG, "sortArray: Sorting the XYArray.");

        while (true) {
            m--;
            if (m <= 0) {
                m = array.size() - 1;
            }
            // Log.d(TAG, "sortArray: m = " + m);
            try {
                double tempY = array.get(m - 1).getY();
                double tempX = array.get(m - 1).getX();
                if (tempX > array.get(m).getX()) {
                    array.get(m - 1).setY(array.get(m).getY());
                    array.get(m).setY(tempY);
                    array.get(m - 1).setX(array.get(m).getX());
                    array.get(m).setX(tempX);
                } else if (tempX == array.get(m).getX()) {
                    count++;
                } else if (array.get(m).getX() > array.get(m - 1).getX()) {
                    count++;
                    // Log.d(TAG, "sortArray: count = " + count);
                }
                //break when factorial is done
                if (count == factor) {
                    break;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                Log.e(TAG, "sortArray: ArrayIndexOutOfBoundsException. Need more than 1 data point to create Plot." +
                        e.getMessage());
                break;
            }
        }
        return array;
    }
}
