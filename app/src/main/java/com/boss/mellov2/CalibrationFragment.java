package com.boss.mellov2;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import static com.boss.mellov2.MainActivity.highRead;
import static com.boss.mellov2.MainActivity.lowRead;

public class CalibrationFragment extends Fragment {

    //=============================================

    //=============================================

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_calibration, container, false);

        final ImageButton startCalibrateButton = (ImageButton) view.findViewById(R.id.calibration_button_start);

        final ImageView calibrationNextBackground = (ImageView) view.findViewById(R.id.calibration_next_background);
        final ImageButton yesCalibrateButton = (ImageButton) view.findViewById(R.id.calibration_button_yes);
        final ImageButton noCalibrateButton = (ImageButton) view.findViewById(R.id.calibration_button_no);

        final ImageView calibrationNextNoBackground = (ImageView) view.findViewById(R.id.calibration_next_no_background);
        final ImageButton okCalibrateButton = (ImageButton) view.findViewById(R.id.calibration_button_ok);

        final ImageView calibrationNextYesBackground = (ImageView) view.findViewById(R.id.calibration_next_yes_background);
        final ImageButton doneCalibrateButton = (ImageButton) view.findViewById(R.id.calibration_button_done);

        final ImageView calibrationNextYesCompleteBackground = (ImageView) view.findViewById(R.id.calibration_next_yes_complete_background);

        final ImageView calibrationPleaseWait = (ImageView) view.findViewById(R.id.calibration_please_wait);

        calibrationNextBackground.setVisibility(View.INVISIBLE);
        yesCalibrateButton.setVisibility(View.INVISIBLE);
        noCalibrateButton.setVisibility(View.INVISIBLE);
        calibrationNextNoBackground.setVisibility(View.INVISIBLE);
        calibrationNextYesBackground.setVisibility(View.INVISIBLE);
        calibrationNextYesCompleteBackground.setVisibility(View.INVISIBLE);
        doneCalibrateButton.setVisibility(View.INVISIBLE);
        okCalibrateButton.setVisibility(View.INVISIBLE);
        calibrationPleaseWait.setVisibility(View.INVISIBLE);

        //start button appears
        startCalibrateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View w) {
                startCalibrateButton.setVisibility(View.INVISIBLE);

                calibrationNextBackground.setVisibility(View.VISIBLE);
                yesCalibrateButton.setVisibility(View.VISIBLE);
                noCalibrateButton.setVisibility(View.VISIBLE);

                yesCalibrateButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

//                        getActivity().runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                calibrationPleaseWait.setVisibility(View.VISIBLE);
//                            }
//                        });
                        calibrationNextBackground.setVisibility(View.INVISIBLE);
                        yesCalibrateButton.setVisibility(View.INVISIBLE);
                        noCalibrateButton.setVisibility(View.INVISIBLE);



                        highRead = MainActivity.takeMeasure();
//                        Toast.makeText(getActivity().getApplicationContext(), Integer.toString(highRead), Toast.LENGTH_LONG).show();

                        calibrationNextYesBackground.setVisibility(View.VISIBLE);
                        doneCalibrateButton.setVisibility(View.VISIBLE);

                        doneCalibrateButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                calibrationNextYesBackground.setVisibility(View.INVISIBLE);
                                doneCalibrateButton.setVisibility(View.INVISIBLE);
//                                calibrationPleaseWait.setVisibility(View.INVISIBLE);


                                lowRead = MainActivity.takeMeasure();
//                                Toast.makeText(getActivity().getApplicationContext(), Integer.toString(lowRead), Toast.LENGTH_LONG).show();

                                calibrationNextYesCompleteBackground.setVisibility(View.VISIBLE);
//                                okCalibrateButton.setVisibility(View.VISIBLE);
//
//                                okCalibrateButton.setOnClickListener(new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View w) {
//
//                                    }
//                                });
                            }
                        });
                    }
                });

                noCalibrateButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        calibrationNextBackground.setVisibility(View.INVISIBLE);
                        yesCalibrateButton.setVisibility(View.INVISIBLE);
                        noCalibrateButton.setVisibility(View.INVISIBLE);

                        calibrationNextNoBackground.setVisibility(View.VISIBLE);
//                        okCalibrateButton.setVisibility(View.VISIBLE);
//
//                        okCalibrateButton.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View w) {
//                                calibrationNextNoBackground.setVisibility(View.INVISIBLE);
//                                okCalibrateButton.setVisibility(View.INVISIBLE);
//
//                                //want it to restart at the beginning of oncreateview
//                            }
//                        });
                    }
                });
            }
        });
        return view;
    }
}

