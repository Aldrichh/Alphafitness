package reboja.com.alphafitness;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class RecordWorkoutActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_workout);


        // For handling the fragments
        Configuration config = getResources().getConfiguration();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();


        // Orientation Checking.
        if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
            RecordWorkoutPortrait recordWorkoutPortrait = new RecordWorkoutPortrait();

            // This will set the view of the fragment
            fragmentTransaction.replace(android.R.id.content, recordWorkoutPortrait);
        }
        else
        {
            // Sets the current page to the landscape layout.
            RecordWorkoutLandscape recordWorkoutLandscape = new RecordWorkoutLandscape();
            fragmentTransaction.replace(android.R.id.content, recordWorkoutLandscape);
        }

        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        getSupportFragmentManager().executePendingTransactions();
    }
}
