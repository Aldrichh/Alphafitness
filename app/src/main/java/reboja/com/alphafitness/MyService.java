package reboja.com.alphafitness;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorEventListener2;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.sip.SipAudioCall;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 *  This class consists of the java implementation of the
 *  Remote service, which will allow the app to continue
 *  to track workouts even when in the background.
 */
public class MyService extends Service implements SensorEventListener {

    private final static String TAG = MyService.class.getSimpleName();

    // Variables for tracking time.
    int s, m, h;
    private long startTime, updateTime;



    private TextView tv;

    // Variables for the sensor.
    private SensorManager sensorManager;
    private Sensor mSensor;
    private Sensor stepDetector;


    // Placeholder for the amount of steps.
    float stepCount;
    int stepDetectCount;

    // For tracking the location and distance from the starting point.
    private LocationManager locationManager;
    private LocationListener locationListener;
    private String locationProvider;


    MyRServiceInterface.Stub mBinder;

    public MyService() {
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * onCreate Method
     */
    @Override
    public void onCreate() {
        super.onCreate();

        startTime = System.currentTimeMillis();

        // Initializing and registering the sensor.
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        sensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);


        // Step detector.
        stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        sensorManager.registerListener(this, stepDetector, SensorManager.SENSOR_DELAY_NORMAL);


        // Initializing the locationListener.
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        locationProvider = locationManager.getBestProvider(criteria, true);


        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("Location: ", location.toString());
                Intent i = new Intent("reboja.com.alphafitness.Update_Location");
                i.putExtra("New location", location);
                sendBroadcast(i);
            }


            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };



        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(locationProvider, 1000, 0, locationListener);

        // Debugging.
        Log.v(TAG, "REMOTE SERVICE IS READY!>>>>>>>>>>>");
    }




    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
        Toast.makeText(this, "Remote service stopped.", Toast.LENGTH_LONG).show();
    }


    /**
     * Used to get the Steps in the application.
     */
    private float getSteps() {

        return stepCount;
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        // Checks which sensor is being used, then act accordingly
        if (event.sensor.equals(mSensor))
        {
            float num = event.values[0];
            stepCount += num;
            Intent stepIntent = new Intent("reboja.com.alphafitness.update_steps");
            stepIntent.putExtra("New steps", stepCount);
            sendBroadcast(stepIntent);
        }
        else if (event.sensor.equals(stepDetector))
        {
            stepDetectCount++;
        }

        /**
        float num = event.values[0];
        stepCount += num;
        Intent stepIntent = new Intent("reboja.com.alphafitness.update_steps");
        stepIntent.putExtra("New steps", stepCount);
        sendBroadcast(stepIntent);
         **/
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
