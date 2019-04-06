package com.example.mellov2;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;
import android.content.Intent;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import android.graphics.drawable.ClipDrawable;
import android.widget.EditText;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {

    //=============================================

    //Bluetooth Initialization
    String address = "98:D3:51:FD:86:53";
    static BluetoothAdapter myBluetooth = null;
    static BluetoothSocket btSocket = null;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    //define the upper and lower bounds of our calibrated system
    public static int lowBound = 0;
    public static int upBound = 264;

    //for notifications
    private static String notification_title = "Bladder Fullness Status";
    private String notification_content;
    private final String CHANNEL_ID = "mello_notifications";
    private final int NOTIFICATION_ID = 001;

    //define arrays used in calibration
    public static int[][] lowRead = new int[4][5];
    public static int[][] highRead = new int[4][5];
    public static int[][] refRead = new int[4][5]; //these will store the results used in the actual calculations
        //row 1 -> LED
        //row 2 -> Sensor
        //row 3 -> full reading (min value)
        //row 4 -> empty reading (max value)

    //read in value from device
    private int percentBladderFullness = 0;
    public static int samplePeriod = 2; //sampling period in seconds
    //public final SharedPreferences pctRecord = this.getSharedPreferences("com.example.mellov2.percents", Context.MODE_PRIVATE); //pctRecord will contain all the percents captured as data
    //public final SharedPreferences pctTimes = this.getSharedPreferences("com.example.mellov2.pct_times",Context.MODE_PRIVATE); //pctTimes will capture the times at which percentages were captured
    public static int prefInd = 0;

    //for the drop animation
    private ClipDrawable mImageDrawable;
    private int fromLevel;
    private int toLevel;
    public static final int MAX_LEVEL = 10000;  //******to be updated with maximum ADC value
    public static final int LEVEL_DIFF = 200;   //minimum ADC value
    public static final int DELAY = 0;

    //to move progress upwards
    private Handler mUpHandler = new Handler();
    private Runnable animateUpImage = new Runnable(){
        @Override
        public void run(){
            doTheUpAnimation(fromLevel, toLevel);
        }
    };

    //to move progress downwards
    private Handler mDownHandler = new Handler();
    private Runnable animateDownImage = new Runnable(){
        @Override
        public void run(){
            doTheDownAnimation(fromLevel, toLevel);
        }
    };
    //=============================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Navigation Bar
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        if (savedInstanceState == null) {
            navigation.setSelectedItemId(R.id.navigation_current_status);
        }
        navigation.setSelectedItemId(R.id.navigation_current_status);

        //check bluetooth status, if not enabled prompt the user to toggle status
        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        if (!myBluetooth.isEnabled()) {
            //prompt user to turn bluetooth on and connect with device
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon, 1);
        }

        //connect to bluetooth device
        try {
            if (btSocket == null) {
                myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                btSocket.connect();//start connection
            }
        } catch (IOException e) {
            msg("Bluetooth connection failed.  Please restart application.");
        }

        //set up status bar
        ImageView img = (ImageView) findViewById(R.id.imageView1);
        mImageDrawable = (ClipDrawable) img.getDrawable();
        mImageDrawable.setLevel(0);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        final TextView PercentFullness = (TextView) findViewById(R.id.fullness_pct);

        ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
        exec.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {

                //get preferences
                boolean switch50Value = prefs.getBoolean("switch_status_50", true);
                boolean switch80Value = prefs.getBoolean("switch_status_80", true);
                boolean switch100Value = prefs.getBoolean("switch_status_100", true);

                //get current time
                Date date = new Date(System.currentTimeMillis());

                //calculate percentBladderFullness -- *****to implement taking ADC value*****
                percentBladderFullness = (percentBladderFullness+10)%100; //bladder fullness in percent

                //store percentage in historical array
                prefs.edit().putInt(Integer.toString(prefInd),percentBladderFullness).apply();
                prefs.edit().putLong(Integer.toString(prefInd).concat("T"),date.getTime()).apply();
                prefInd++;
                //if a bladder voiding has occurred, note the time
                int avgBefore = (prefs.getInt(Integer.toString(prefInd-5),0)+prefs.getInt(Integer.toString(prefInd-4),0)+prefs.getInt(Integer.toString(prefInd-3),0))/3;
                int avgAfter = (prefs.getInt(Integer.toString(prefInd-2),0)+prefs.getInt(Integer.toString(prefInd-1),0)+prefs.getInt(Integer.toString(prefInd),0))/3;
                //if(avgAfter< 30 && avgBefore > 30){
                if(prefs.getInt(Integer.toString(prefInd-1),0) > 30 && prefs.getInt(Integer.toString(prefInd),0) < 30){
                    prefs.edit().putLong("last_void_time",prefs.getLong(Integer.toString(prefInd).concat("T"),0)).apply(); //save last void time to preferences
                }

                //update percentage in UI
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        PercentFullness.setText(Integer.toString(percentBladderFullness).concat("%")); //update percent field
                    }
                });

                //round to nearest 10
                percentBladderFullness = 10*(Math.round(percentBladderFullness/10));

                //format bladder fullness into position notation
                int temp_level = (percentBladderFullness*MAX_LEVEL)/100;

                if(toLevel == temp_level || temp_level>MAX_LEVEL){
                    return;//if there is no change, do nothing
                }
                toLevel = temp_level;
                if(toLevel>fromLevel){
                    mDownHandler.removeCallbacks(animateDownImage); //cancel any previous processes
                    MainActivity.this.fromLevel = toLevel;
                    mUpHandler.post(animateUpImage);
                } else {
                    mUpHandler.removeCallbacks(animateUpImage); //cancel any previous processes
                    MainActivity.this.fromLevel = toLevel;
                    mDownHandler.post(animateDownImage);
                }

                if(switch100Value && percentBladderFullness > 90){
                    notification_content = "Your bladder is at 100% fullness";
                    displayNotification();
                } else if(switch80Value && percentBladderFullness > 70){
                    notification_content = "Your bladder is at 80% fullness";
                    displayNotification();
                } else if(switch50Value && percentBladderFullness > 40){
                    notification_content = "Your bladder is at 50% fullness";
                    displayNotification();
                }
            }
        }, 0, samplePeriod, TimeUnit.SECONDS);
    }


    public TrendsStatsFragment trendsStatsFragment = new TrendsStatsFragment();
    public CalibrationFragment calibrationFragment = new CalibrationFragment();
    public SettingsFragment settingsFragment = new SettingsFragment();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

            switch (item.getItemId()) {
                case R.id.navigation_current_status:
                    if (trendsStatsFragment.isAdded()) {
                        ft.hide(trendsStatsFragment);
                    }
                    if (calibrationFragment.isAdded()) {
                        ft.hide(calibrationFragment);
                    }
                    if (settingsFragment.isAdded()) {
                        ft.hide(settingsFragment);
                    }
                    ft.commit();
                    return true;
                case R.id.navigation_trends_stats:
                    if (!trendsStatsFragment.isAdded()) {
                        ft.add(R.id.main_container, trendsStatsFragment);
                    } else {
                        if (calibrationFragment.isAdded()) {
                            ft.hide(calibrationFragment);
                        }
                        if (settingsFragment.isAdded()) {
                            ft.hide(settingsFragment);
                        }
                        ft.show(trendsStatsFragment);
                    }
                    ft.commit();
                    return true;
                case R.id.navigation_calibration:
                    if (!calibrationFragment.isAdded()) {
                        ft.add(R.id.main_container, calibrationFragment);
                    } else {
                        if (trendsStatsFragment.isAdded()) {
                            ft.hide(trendsStatsFragment);
                        }
                        if (settingsFragment.isAdded()) {
                            ft.hide(settingsFragment);
                        }
                        ft.remove(calibrationFragment);
                        calibrationFragment = new CalibrationFragment();
                        ft.add(R.id.main_container, calibrationFragment);
                    }
                    ft.commit();
                    return true;
                case R.id.navigation_settings:
                    if (!settingsFragment.isAdded()) {
                        ft.add(R.id.main_container, settingsFragment);
                    } else {
                        if (trendsStatsFragment.isAdded()) {
                            ft.hide(trendsStatsFragment);
                        }
                        if (calibrationFragment.isAdded()) {
                            ft.hide(calibrationFragment);
                        }
                        ft.show(settingsFragment);
                    }
                    ft.commit();
                    return true;
            }
            return false;
        }
    };

    public static int takeMeasure(int LED, int Sens){
        //store initial buffer value
        int curr = Integer.parseInt(recVolt().replace("\n","").replace("\r","").replace(" ","")); //clean up received bit

        //send command to micro
        if(Sens == 1){
            pulseLED(Integer.toString(LED).concat("A"));
        }else if(Sens == 2){
            pulseLED(Integer.toString(LED).concat("B"));
        }else if(Sens == 3){
            pulseLED(Integer.toString(LED).concat("C"));
        }else if(Sens == 4){
            pulseLED(Integer.toString(LED).concat("D"));
        }else{
            //msg("Error Code : 1AA43");
        }

        //wait until the value in the buffer changes
        while(curr == Integer.parseInt(recVolt().replace("\n","").replace("\r","").replace(" ",""))){
            try{
                TimeUnit.MILLISECONDS.sleep(1);
            }catch(InterruptedException e){
                //wait
            }
        }

        //return integer stored in the buffer
        return Integer.parseInt(recVolt().replace("\n","").replace("\r","").replace(" ",""));
    }


    public static void pulseLED(String LEDnum) { //method to pulse a specific LED to observe its effect
        if (btSocket != null) {
            try {
                switch (LEDnum) {
                    case "1A": //pulse LED 1, get reading from sensor 1/A
                        //btSocket.getOutputStream().write("1".getBytes());
                        btSocket.getOutputStream().write((byte)0x11); //00010001
                        break;
                    case "1B": //pulse LED 1, get reading from sensor 2/B
                        btSocket.getOutputStream().write((byte)0x12); //00010010
                        break;
                    case "1C": //pulse LED 1, get reading from sensor 3/C
                        btSocket.getOutputStream().write((byte)0x13); //00010011
                        break;
                    case "1D": //pulse LED 1, get reading from sensor 4/D
                        btSocket.getOutputStream().write((byte)0x14); //00010100
                        break;
                    case "2A": //pulse LED 2, get reading from sensor 1/A
                        btSocket.getOutputStream().write((byte)0x21); //00100001
                        break;
                    case "2B": //pulse LED 2, get reading from sensor 2/B
                        btSocket.getOutputStream().write((byte)0x22); //00100010
                        break;
                    case "2C": //pulse LED 2, get reading from sensor 3/C
                        btSocket.getOutputStream().write((byte)0x23); //00100011
                        break;
                    case "2D": //pulse LED 2, get reading from sensor 4/D
                        btSocket.getOutputStream().write((byte)0x24); //00100100
                        break;
                    case "3A": //pulse LED 3, get reading from sensor 1/A
                        btSocket.getOutputStream().write((byte)0x31); //00110001
                        break;
                    case "3B":  //pulse LED 3, get reading from sensor 2/B
                        btSocket.getOutputStream().write((byte)0x32); //00110010
                        break;
                    case "3C":  //pulse LED 3, get reading from sensor 3/C
                        btSocket.getOutputStream().write((byte)0x33); //00110011
                        break;
                    case "3D":  //pulse LED 3, get reading from sensor 4/D
                        btSocket.getOutputStream().write((byte)0x34); //00110100
                        break;
                    case "4A":  //pulse LED 4, get reading from sensor 1/A
                        btSocket.getOutputStream().write((byte)0x41); //01000001
                        break;
                    case "4B": //pulse LED 4, get reading from sensor 2/B
                        btSocket.getOutputStream().write((byte)0x42); //01000010
                        break;
                    case "4C": //pulse LED 4, get reading from sensor 3/C
                        btSocket.getOutputStream().write((byte)0x43); //01000011
                        break;
                    case "4D": //pulse LED 4, get reading from sensor 4/D
                        btSocket.getOutputStream().write((byte)0x44); //01000100
                        break;
                    case "5A": //pulse LED 5, get reading from sensor 1/A
                        btSocket.getOutputStream().write((byte)0x51); //01010001
                        break;
                    case "5B": //pulse LED 5, get reading from sensor 2/B
                        btSocket.getOutputStream().write((byte)0x52); //01010010
                        break;
                    case "5C": //pulse LED 5, get reading from sensor 3/C
                        btSocket.getOutputStream().write((byte)0x53); //01010011
                        break;
                    case "5D": //pulse LED 5, get reading from sensor 4/D
                        btSocket.getOutputStream().write((byte)0x54); //01010100
                        break;
                    default:
                        btSocket.getOutputStream().write(0xFF); //11111111
                        break;
                }
            } catch (IOException e) {
                //msg("Error");
            }
        }
    }

    public static String recVolt() {
        byte[] buffer = null;
        buffer = new byte[7];
        try {
            int len = btSocket.getInputStream().read(buffer,0,3);
            String text = new String(buffer, 0, len);
            return text;
        } catch (IOException e) {
            //something happened
            //msg("error");
            return "error";
        }
    }

    private void doTheUpAnimation(int fromLevel, int toLevel){
        mImageDrawable.setLevel(toLevel);
        //TextView PercentFullness = (TextView) findViewById(R.id.fullness_pct);
        //PercentFullness.setText(percentBladderFullness); //update percent field
        //if(mLevel<=toLevel){
        //    mUpHandler.postDelayed(animateUpImage, DELAY);
        //}else{
        //    mUpHandler.removeCallbacks(animateUpImage);
         //   MainActivity.this.fromLevel = toLevel;
        //}
    }

    private void doTheDownAnimation(int fromLevel, int toLevel){
        mImageDrawable.setLevel(toLevel);
        //if (mLevel >= toLevel){
        //    mDownHandler.postDelayed(animateDownImage,DELAY);
        //}else{
        //    mDownHandler.removeCallbacks(animateDownImage);
        //    MainActivity.this.fromLevel = toLevel;
        //}
    }

    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    public void displayNotification() {
        createNotificationChannel();

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);

        notificationBuilder.setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.dashboard_icon_current)
                .setContentTitle(notification_title)
                .setContentText(notification_content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Mello Notifications";
            String description = "Bladder Fullness Notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, name, importance);

            notificationChannel.setDescription(description);

            NotificationManager notificationManager = (NotificationManager) this.getSystemService(this.NOTIFICATION_SERVICE);

            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}