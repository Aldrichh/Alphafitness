package reboja.com.alphafitness;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.lang.reflect.Array;
import java.util.ArrayList;


/**
 * This fragment will contain the profile information for the user.
 *
 * Author: Aldrich Reboja
 * CS 175
 */
public class RecordWorkoutLandscape extends Fragment  {


    private LineChart chart;


    public RecordWorkoutLandscape() {
        // Required empty public constructor
    }


    // Website to help with charts: https://www.youtube.com/watch?v=DD1CxoVONFE


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_record_workout_landscape, container, false);

        /**
      // chart = (LineChart) v.findViewById(R.id.chart);

      // chart.setOnChartGestureListener(RecordWorkoutLandscape.this);
       //chart.setOnChartValueSelectedListener(RecordWorkoutLandscape.this);


      // chart.setDragEnabled(true);
      // chart.setScaleEnabled(false);

       // ArrayList that will hold all of the values in the chart
        ArrayList<Entry>  yValues = new ArrayList<>();


        yValues.add(new Entry(0,60f));
        yValues.add(new Entry(1,60f));
        yValues.add(new Entry(2,60f));
        yValues.add(new Entry(3,60f));
        yValues.add(new Entry(4,60f));
        yValues.add(new Entry(5,60f));
        yValues.add(new Entry(6,60f));
        LineDataSet set1 = new LineDataSet(yValues, "Data Set 1");

        set1.setFillAlpha(110);

        set1.setColor(Color.RED);
        set1.setLineWidth(3f);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        LineData data = new LineData(dataSets);

        chart.setData(data);

         **/
        return v;
    }

}
