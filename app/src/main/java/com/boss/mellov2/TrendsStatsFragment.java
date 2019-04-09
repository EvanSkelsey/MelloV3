package com.boss.mellov2;

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
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.boss.mellov2.MainActivity.samplePeriod;
//import static com.boss.mellov2.MainActivity.prefInd;
import static com.boss.mellov2.MainActivity.graphReadings;
import static com.boss.mellov2.MainActivity.graphTimes;

public class TrendsStatsFragment extends Fragment {

    //=============================================

    //=============================================

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_trends_stats, container, false);

        final GraphView graph = (GraphView) view.findViewById(R.id.graph);
        final TextView timeSince = (TextView) view.findViewById(R.id.time_void_last);
        final TextView timeNext = (TextView) view.findViewById(R.id.time_void_next);

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
        //DataPoint[] points = new DataPoint[13];

        //LineGraphSeries<DataPoint> series =
        //series.setColor(Color.LTGRAY);
        //series.setThickness(8);
        //graph.addSeries(series);

        /////////////////////
        ScheduledThreadPoolExecutor exec1 = new ScheduledThreadPoolExecutor(1);
        exec1.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

                //update graph
                graph.removeAllSeries();

                DataPoint[] points = new DataPoint[13];
                for (int i = 0; i < 13; i++) {
                    points[i] = new DataPoint(i,graphReadings[i]);
                }

                LineGraphSeries<DataPoint> series = new LineGraphSeries<>(points);
                series.setColor(Color.LTGRAY);
                series.setThickness(8);
                graph.addSeries(series);

                //calculate time since last void
                Date date = new Date(System.currentTimeMillis());
                long lastTime = date.getTime() - prefs.getLong("last_void_time",0)-1_000*60*60*19;
                DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                final String dateFormatted = formatter.format(lastTime);

                //calculate time till next void
                long nextTime = ((90-graphReadings[12])/(graphReadings[12]-graphReadings[11]))*(graphTimes[12] - graphTimes[11])-1_000*60*60*19;
                final String dateFormatted1 = formatter.format(nextTime);

                //update GUI
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        timeSince.setText(dateFormatted);
                        if(graphReadings[12] > graphReadings [11]){//only update field if bladder is filling
                            timeNext.setText(dateFormatted1);
                        }
                    }
                });
            }
        }, 0, samplePeriod, TimeUnit.SECONDS);


        return view;
    }
}
