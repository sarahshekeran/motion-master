package com.sarahchristina.motion;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import java.util.concurrent.TimeUnit;
import android.os.CountDownTimer;
import android.view.View;
import android.view.View.OnClickListener;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import java.util.HashMap;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
@SuppressLint("NewApi")
public class motion extends Activity implements SensorEventListener {

    Context context = this;
    private float lastX, lastY, lastZ;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private float deltaXMax = 0;
    private float deltaYMax = 0;
    private float deltaZMax = 0;

    private float deltaX = 0;
    private float deltaY = 0;
    private float deltaZ = 0;

    private float vibrateThreshold = 0;
    int vibrateCounter = 0;

    private TextView currentX, currentY, currentZ, maxX, maxY, maxZ, result;
    private Button btnstart, GraphActivityButton;

    public Vibrator v;

    final CounterClass timer = new CounterClass(15000,50);
    float res[]= new float[300];
    int i=0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeViews();
        //BEGIN SPEED MODULE
        LocationManager locationManager = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                location.getLatitude();
                Toast.makeText(context, "Current speed:" + location.getSpeed(), Toast.LENGTH_SHORT).show();
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
            public void onProviderEnabled(String provider) {
            }
            public void onProviderDisabled(String provider) {
            }
        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
                0, locationListener);

        //END SPEED MODULE




        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // success! we have an accelerometer

            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            vibrateThreshold = accelerometer.getMaximumRange() / 2;
        } else {
            // fai! we dont have an accelerometer!
        }

        //initialize vibration
        v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        btnstart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                timer.start();
            }
        });

        GraphActivityButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent INT=new Intent(motion.this,GraphActivity.class);
                startActivity(INT);

            }
        });

    }



    public void initializeViews() {
        setContentView(R.layout.activity_main);
        currentX = (TextView) findViewById(R.id.currentX);
        currentY = (TextView) findViewById(R.id.currentY);
        currentZ = (TextView) findViewById(R.id.currentZ);

        maxX = (TextView) findViewById(R.id.maxX);
        maxY = (TextView) findViewById(R.id.maxY);
        maxZ = (TextView) findViewById(R.id.maxZ);
        btnstart = (Button) findViewById(R.id.start);
        GraphActivityButton = (Button) findViewById(R.id.GraphActivityButton);
        result = (TextView) findViewById(R.id.result);



    }



    //onResume() register the accelerometer for listening the events
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    //onPause() unregister the accelerometer for stop listening the events
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        // clean current values
        displayCleanValues();
        // display the current x,y,z accelerometer values
        displayCurrentValues();
        // display the max x,y,z accelerometer values
        displayMaxValues();

        // get the change of the x,y,z values of the accelerometer
        deltaX = Math.abs(lastX - event.values[0]);
        deltaY = Math.abs(lastY - event.values[1]);
        deltaZ = Math.abs(lastZ - event.values[2]);

        // if the change is below 2, it is just plain noise
        if (deltaX < 1)
            deltaX = 0;
        if (deltaY < 1)
            deltaY = 0;
        if (deltaZ < 1)
            deltaZ = 0;

        // set the last know values of x,y,z
        lastX = event.values[0];
        lastY = event.values[1];
        lastZ = event.values[2];

        vibrate();

    }

    // if the change in the accelerometer value is big enough, then vibrate!
    // our threshold is MaxValue/2
    public void vibrate() {
        if ((deltaX > vibrateThreshold) || (deltaY > vibrateThreshold) || (deltaZ > vibrateThreshold)) {
            v.vibrate(50);
            vibrateCounter++;
        }
    }

    public void displayCleanValues() {
        currentX.setText("0.0");
        currentY.setText("0.0");
        currentZ.setText("0.0");
    }

    // display the current x,y,z accelerometer values
    public void displayCurrentValues() {
        currentX.setText(Float.toString(deltaX));
        currentY.setText(Float.toString(deltaY));
        currentZ.setText(Float.toString(deltaZ));
    }

    // display the max x,y,z accelerometer values
    public void displayMaxValues() {
        if (deltaX > deltaXMax) {
            deltaXMax = deltaX;
            maxX.setText(Float.toString(deltaXMax));
        }
        if (deltaY > deltaYMax) {
            deltaYMax = deltaY;
            maxY.setText(Float.toString(deltaYMax));
        }
        if (deltaZ > deltaZMax) {
            deltaZMax = deltaZ;
            maxZ.setText(Float.toString(deltaZMax));
        }
    }




    public class CounterClass extends CountDownTimer {
        public CounterClass(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            float finalres[][] =new float[200][2];
            float resultvalues[][] =new float[50][2];
            int l = 0;
            int k = 0;
            float sum = 0;
            float totaltime=0;


            //Function to find the average

            for(int j=0;j<res.length;j++)
            {
                sum=sum+res[j];
            }
            sum=sum/res.length;

            for(int j=0;j<res.length;j++)
            {
                res[j]=Math.abs(res[j]-sum);
            }

            //Function to find the peaks

            if(k<finalres.length && res[0]>res[1])
            {
                finalres[k][0]=res[0];
                finalres[k][1] =0;
                k++;
            }
            for(int j=1;j<res.length-1;j++)
            {
                if(k<finalres.length && res[j]>res[j-1] && res[j]>res[j+1])
                {
                    finalres[k][0]=res[j];
                    finalres[k][1] =j;
                    k++;
                }

            }
            if(k<finalres.length && res[res.length-1]>res[res.length-2])
            {
                finalres[k][0]=res[res.length-1];
                finalres[k][1] =res.length-1;
                k++;
            }

            if(k<finalres.length && res[res.length-1]>res[res.length-2])
            {
                finalres[k][0]=res[res.length-1];
                finalres[k][1] =res.length-1;
                k++;
            }

            //Function to find the 50 maximum peak values

            float max = sum +1;
            float large = 0;
            int pos = 0;
            for(int i = 0; i < resultvalues.length;i++)
            {   large = 0;
                for(int j = 0; j < finalres.length;j++)
                {
                    if(finalres[j][0]<max && finalres[j][0]>large)
                    {
                        large = finalres[j][0];
                        pos = j;
                    }
                }
                if(l<resultvalues.length)
                {
                    resultvalues[l][0]=finalres[pos][0];
                    resultvalues[l][1]=finalres[pos][1];
                    l++;
                    max = finalres[pos][0];
                }
            }

            //Function to measure time between peaks

            for(int j=1;j<resultvalues.length;j++)
            {
                totaltime=totaltime+((50)*Math.abs(resultvalues[j][1]-resultvalues[j-1][1]));
            }
            totaltime=totaltime/((finalres.length-1)*1000);

            if(totaltime<0.25 && vibrateCounter>10)
                result.setText("JOGGING");
            else
                result.setText("WALKING");

        }





        @TargetApi(Build.VERSION_CODES.GINGERBREAD)
        @SuppressLint("NewApi")
        public void onTick(long millisUntilFinished) {
            long millis = millisUntilFinished;
            String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                    TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                    TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
            System.out.println(hms);
            result.setText(hms);
            res[i]=lastY;
            i++;

        }

    }

}