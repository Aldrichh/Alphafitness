package reboja.com.alphafitness;


import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class RecordWorkoutPortrait extends Fragment implements OnMapReadyCallback, LocationListener, SensorEventListener {

    private final static String TAG = RecordWorkoutPortrait.class.getSimpleName();

    // Dialog for a new user
    Dialog dialog;

    // Used to establish connection to the remote service.
    MyRServiceInterface remoteService;
    RemoteConnection remoteConnection = null;

    // For tracking the current location.
    LocationManager locationManager;

    // For handling threads.
    Handler handler = new Handler();

    // This will control the states of when the button is pressed.
    boolean started;

    // The view of the map
    MapView mapView;
    String locationProvider;

    // The actual map, which may be updated numerously.
    private GoogleMap mMap;
    private Button startWorkoutButton;
    private ImageButton profileButton;

    public static final int MY_PERMISSION_REQUEST = 10;


    private long startTime, updateTime;

    private TextView  distanceText;
    private TextView timerTxt;

    // ImageView for transitioning to the profile activity.
    private ImageView profile;

    // String to hold the name of the current user.
    private String currentUser;

    private LocationReceiver locationReceiver = new LocationReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // Gets the new location from the service.
            // Gets the location and draws a polyLine bridging the two points.


            Bundle bundle = intent.getExtras();
            Location nLocation = (Location) bundle.get("New location");
            LatLng there = new LatLng(nLocation.getLatitude(), nLocation.getLongitude());
            mMap.addPolyline(new PolylineOptions().add(there).color(0));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(there));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(there.latitude, there.longitude), 12.0f));

            Log.v(TAG, "RECEIVED A NEW LOCATION");

        }
    };


    private StepReceiver stepReceiver = new StepReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            //super.onReceive(context, intent);

            Bundle bundle = intent.getExtras();
            float i = bundle.getFloat("New Steps");

            // Todo: Calculate distance and calories.
        }
    };


    static final String filter = "reboja.com.alphafitness.Update_Location";
    private MarkerOptions secondMarker;

    // For the Content Provider.
    private final static String PROVIDER = "com.wearable.alphafitness.provider";
    static final String URL = "content://" + PROVIDER + "/Workouts";
    static final Uri URI = Uri.parse(URL);

    MyContentProvider myContentProvider;


    static final String URL2 = "content://" + PROVIDER + "/Profiles";
    static final Uri URI2 = Uri.parse(URL2);

    // This will be used to keep track of who is logged in.
    private int profile_ID;

    public RecordWorkoutPortrait() {

    }

    /**
     * This will be used to update time in milliseconds.
     */

    Runnable timerThread  = new Runnable() {
        @Override
        public void run() {
            // Try this method to get the time kept at zero.
            if (!started) {
                timerTxt.setText("00:00:00");
            }
            else {
                long currentTime = System.currentTimeMillis() - startTime;
                updateTime = currentTime;
                int secs = (int) (updateTime / 1000);
                int mins = secs / 60;
                int hours = mins / 60;
                secs %= 60;
                timerTxt.setText("" + String.format("%02d", hours) + ":" + String.format("%02d", mins) + ":" + String.format("%02d", secs), TextView.BufferType.EDITABLE);
                handler.post(this);
            }
    }
};




    // The remote service for the application.
class RemoteConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            remoteService = MyRServiceInterface.Stub.asInterface((IBinder) service);
            Toast.makeText(getContext(), "Remote Service connected. ", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            remoteService = null;
            Toast.makeText(getContext(), "Remote Service disconnected. ", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_record_workout_portrait, container, false);


        dialog = new Dialog(getContext());

        // This will keep track of who is the current user logged into the system.
        final SharedPreferences preferences = getContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        currentUser = preferences.getString("UserName", null);

        /**
         *    This is responsible for launching the profile activity.
         */
        profile = (ImageView) view.findViewById(R.id.profileView);
        profile.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {

                // Call the intent to go to the profile activity.
                Intent intent = new Intent(getContext(), ProfileActivity.class);
                startActivity(intent);

            }
        });
        // ----------------------------------------------------------------------------------------- //

        timerTxt = (TextView) view.findViewById(R.id.timeTxt);
        timerThread.run();

       // myContentProvider = new MyContentProvider();


        // Getting all of the rows that

        ContentResolver cr = getContext().getContentResolver();
        Cursor cursor = cr.query(URI2, null,null, null, null);
        if (cursor.getCount() > 0) {
            Toast.makeText(getContext(), "Welcome ", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(getContext(), "No users registered, go to profile page to input info.", Toast.LENGTH_SHORT).show();
        }





        //timerTxt = (TextView) view.findViewById(R.id.textView6);
        distanceText = (TextView) view.findViewById(R.id.distanceText);

        // Initializing the map to the fragment.
        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(null);
        mapView.getMapAsync(this);





        // Initializing a profile to the application, if none, insert my info into the database.
        MyContentProvider myContentProvider = new MyContentProvider();

        // Initializing the broadcast receivers for the location and steps.
        IntentFilter intentFilter = new IntentFilter(filter);
        getContext().registerReceiver(locationReceiver, intentFilter);

        IntentFilter intentFilter2 = new IntentFilter("reboja.com.alphafitness.update_steps");
        getContext().registerReceiver(stepReceiver, intentFilter2);




        Log.v(TAG, "Map is prepping...");
        startWorkoutButton = view.findViewById(R.id.startButton);
        startWorkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If the workout has not been started.
                if (!started)
                {
                    /**
                    // Won't start program unless there is a registered user.
                    if (currentUser == null) {
                        Log.v(TAG, "No User Available");
                    }
                    else {
                    **/
                        Log.v(TAG, "Starting Workout....");
                        started = true;
                        Toast.makeText(getContext(), "Workout has begun", Toast.LENGTH_LONG).show();
                        startTime = System.currentTimeMillis();
                        startWorkoutButton.setText("Stop Workout");


                        // Setting the remote service up
                        remoteConnection = new RemoteConnection();
                        Intent intent = new Intent();

                        intent.setClassName("reboja.com.alphafitness", reboja.com.alphafitness.MyService.class.getName());
                        if (!getContext().bindService(intent, remoteConnection, Context.BIND_AUTO_CREATE)) {

                            throw new RuntimeException("Couldn't load Remote Service.");
                        }
                        Log.v(TAG, "Service has begun...");


                        // Move this later if possible.
                        //handler.post(timerThread);
                    //}

                }
                else if (started)
                {
                    // Stops the service and gets the information.
                    started = false;
                    //Toast.makeText(getContext(), "Workout has ended.", Toast.LENGTH_LONG).show();
                    startWorkoutButton.setText("Start Workout");

                    //getContext().stopService(intent);

                    Log.v(TAG, "Service has ended...");


                    ContentResolver contentResolver = getContext().getContentResolver();

                    // Content values is responsible for storing the values into the content provider.

                    // Inserting the workout data into the database.
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MyContentProvider.DURATION, String.valueOf(updateTime));
                    //contentValues.put(MyContentProvider.DISTANCE, String.valueOf(updateTime)); // Need to work on this
                    //contentValues.put(MyContentProvider.STEPS, String.valueOf());



                    // Inserting the values into the database.
                    contentResolver.insert(URI, contentValues);



                    getContext().unbindService(remoteConnection);


                    handler.removeCallbacks(timerThread);
                }

            }
        });

        return view;

    }


    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }



    @Override
    public void onDestroy() {

    getActivity().onRetainNonConfigurationInstance();

        if (mapView != null) {
            mapView.onDestroy();
        }
        getContext().unregisterReceiver(locationReceiver);
        super.onDestroy();

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (mapView != null)
        {
            mapView.onSaveInstanceState(outState);
        }
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
       super.onRequestPermissionsResult(requestCode, permissions, grantResults);
       if (requestCode == MY_PERMISSION_REQUEST) {
           if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //onMapReady(new GoogleMap());
           } else {
               Toast.makeText(getContext(), "Permissions denied", Toast.LENGTH_SHORT).show();
           }
       }
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera



        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);



        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {
            //ActivityCompat.requestPermissions(getContext(), new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, );
            Toast.makeText(getContext(), "Permissions are not given", Toast.LENGTH_LONG).show();

            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSION_REQUEST);
            //return;
        }
        mMap.setMyLocationEnabled(true);


        /**
        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        locationProvider = locationManager.getBestProvider(criteria, true);
        // Gets the last known location.
        Location location = locationManager.getLastKnownLocation(locationProvider);

        String label = "Address: ";
        List<Address> addresses;
        try {
            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());


            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            //Toast.makeText(getContext(), "Addresses found:" + addresses.size() + ".", Toast.LENGTH_LONG).show();
            if (addresses != null) {
                Address address = addresses.get(0);
                StringBuilder stringBuilder = new StringBuilder("");
                for (int i = 0; i < address.getMaxAddressLineIndex(); i++)
                {
                    stringBuilder.append(address.getAddressLine(i)).append("/");
                }
                label = label + stringBuilder.toString();

            }
        }
        catch (IOException e)
        {

        }



       // Setting the mapView when it opens to the current location.
        //LatLng here = new LatLng(location.getLatitude(), location.getLongitude());
        LatLng here = new LatLng(100, 100);
        mMap.addCircle(new CircleOptions().center(here).radius(60));
      //  mMap.addMarker(new MarkerOptions().position(here).title(label));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(here));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(here.latitude, here.longitude), 12.0f));
        // Draws the line for the traveled areas, will be drawn
        mMap.addPolyline(new PolylineOptions().add(here).color(0));
         **/

    }


    // For the location Listener.

    @Override
    public void onLocationChanged(Location location) {

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


    // For the sensors.

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


}
