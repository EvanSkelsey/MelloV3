package com.example.mellov2;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.example.mellov2.MainActivity.samplePeriod;
import static com.example.mellov2.MainActivity.prefInd;
//import com.example.mellov2.MainActivity.prefs;

public class TrendsStatsFragment extends Fragment {

    //=============================================



    //=============================================

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_trends_stats, container, false);

        final GraphView graph = (GraphView) view.findViewById(R.id.graph);
        final TextView timeSince = (TextView) view.findViewById(R.id.time_void_last);
        TextView timeNext = (TextView) view.findViewById(R.id.time_void_next);
        TextView avgVoid = (TextView) view.findViewById(R.id.time_void_average);

        //set appearance of the graph
        GridLabelRenderer gridLabel = graph.getGridLabelRenderer();
        gridLabel.setHorizontalAxisTitle("Time");
        gridLabel.setVerticalAxisTitle("% Bladder Fullness");
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(100);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(13);

        //set initial data points
        DataPoint[] points = new DataPoint[13];
        for (int i = 0; i < points.length; i++) {
            points[i] = new DataPoint(i, 0);
        }

        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(points);

        series.setColor(Color.LTGRAY);
        series.setThickness(8);

        graph.addSeries(series);

        ScheduledThreadPoolExecutor exec1 = new ScheduledThreadPoolExecutor(1);
        exec1.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

                //update graph
                graph.removeAllSeries();

                DataPoint[] points = new DataPoint[13];
                for (int i = 0; i < 13; i++) {
                    points[12-i] = new DataPoint(12-i,prefs.getInt(Integer.toString(prefInd-i),0));
                }

                LineGraphSeries<DataPoint> series = new LineGraphSeries<>(points);
                series.setColor(Color.LTGRAY);
                series.setThickness(8);

                graph.addSeries(series);

                //calculate time since last void
                Date date = new Date(System.currentTimeMillis());
                long lastTime = prefs.getLong("last_void_time",0);
                DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                //formatter.setTimeZone(TimeZone.getTimeZone("UTC-4"));
                final String dateFormatted = formatter.format(lastTime);

                //calculate time till next void


                //calculate the average number of voids per day (estimate)


                //update GUI

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        timeSince.setText(dateFormatted);
                    }
                });


            }
        }, 1, samplePeriod, TimeUnit.SECONDS);

        return view;
    }
}
