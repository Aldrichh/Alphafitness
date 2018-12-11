package reboja.com.alphafitness;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.icu.text.AlphabeticIndex;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

public class LocationReceiver extends BroadcastReceiver {

    // Will hold the location of the update
    Location nLocation;
    LatLng there;

    private final static String TAG = LocationReceiver.class.getSimpleName();



    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        Location nLocation = (Location) bundle.get("New location");
        there = new LatLng(nLocation.getLatitude(), nLocation.getLongitude());
        /**
        mMap.addPolyline(new PolylineOptions().add(there).color(0));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(there));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(there.latitude, there.longitude), 12.0f));

         **/
        Log.v(TAG, "RECEIVED A NEW LOCATION");


    }


}
