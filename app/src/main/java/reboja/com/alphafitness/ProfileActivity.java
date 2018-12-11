package reboja.com.alphafitness;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.net.URI;
import java.util.Calendar;

/**
 * This class will be responsible for handling the Profile information for a given user.
 *
 */
public class ProfileActivity extends AppCompatActivity {


    private final static String TAG = ProfileActivity.class.getSimpleName();

    // For the name of the user
    private EditText userName;
    private EditText weightText, genderText;
    private Button update;

    // TextViews for the weekly and All-time data.
    private TextView wDistance, wTime, wWorkouts, wCalories;
    private TextView aDistance, aTime, aWorkouts, aCalories;

    private int id;

    private String mName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Sets the userName text to the saved user.
        userName = (EditText) findViewById(R.id.nameText);
        weightText = (EditText) findViewById(R.id.weightText);
        genderText = (EditText) findViewById(R.id.genderText);

        // TextViews for the statistics (Weekly)
        wDistance = (TextView) findViewById(R.id.distanceTV);
        wTime = (TextView) findViewById(R.id.timeTV);
        wWorkouts = (TextView) findViewById(R.id.workoutsTV);
        wCalories = (TextView) findViewById(R.id.caloriesTV);

        // TextViews for the AllTime statistics
        aDistance = (TextView) findViewById(R.id.allTimeDisTV);
        aTime = (TextView) findViewById(R.id.allTimeTV);
        aWorkouts = (TextView) findViewById(R.id.allTimeWorkoutsTV);
        aCalories = (TextView) findViewById(R.id.allTimeCalsTV);

        // This will get the user that is currently logged in.
        final SharedPreferences preferences = getSharedPreferences("MyPreferences", MODE_PRIVATE);
        mName = preferences.getString("UserName", null);

        // If a user is currently registered
        if (mName != null)
        {
            userName.setText(mName);

            ContentResolver cr = getApplicationContext().getContentResolver();
            Cursor cursor = cr.query(MyContentProvider.URI2, null, "UserName = " + mName, null,null);

            // Gets the information on the user.
            if (cursor != null && cursor.getCount() > 0) {
                genderText.setText(cursor.getString(2));
                weightText.setText(cursor.getString(3));
                id =  cursor.getInt(0);

            }
            cursor.close();
            /*
            Cursor workoutCursor = cr.query(MyContentProvider.URI, null, MyContentProvider.W_ID + " = " + id, null, null);
            if (workoutCursor != null && workoutCursor.getCount() > 0) {

            }
            */

        }
        else {
            Toast.makeText(this, "No user. Enter in fields and 'update' to add", Toast.LENGTH_SHORT).show();
            Log.v(TAG, "No Users available");
        }

        // Update button, used to add users or even update information on the user.
        update = (Button) findViewById(R.id.updateButton);
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // This checks if there is a user that is equal to the username that is inputted.
                ContentResolver cr = getApplicationContext().getContentResolver();
                Cursor cursor = cr.query(MyContentProvider.URI2, new String[] {MyContentProvider.NAME, MyContentProvider.GENDER, MyContentProvider.WEIGHT}, "UserName = " + userName.getText().toString(), null,null);

                // New user, input the data into the database.
                if (cursor.getCount() == 0) {
                    ContentValues nContent = new ContentValues();
                    nContent.put(MyContentProvider.NAME, userName.getText().toString());
                    nContent.put(MyContentProvider.WEIGHT, weightText.getText().toString());
                    nContent.put(MyContentProvider.GENDER, genderText.getText().toString());
                    nContent.put(MyContentProvider.NUM_WORKOUTS, 0);

                    getContentResolver().insert(MyContentProvider.URI2, nContent);

                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("UserName", userName.getText().toString());
                    editor.apply();

                    Log.v(TAG, "New User added to database.");
                }
                cursor.close();
            }
        });

        // When a name is inputted and the focus is changed.

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }


    /**
     * This will get the amount of workouts that a user has had within the past week.
     * @return
     */
    public int getWeeklyWorkoutCount() {
        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(MyContentProvider.URI2, new String[] {MyContentProvider.P_ID}, "UserName = " + mName, null, null);

        // Gets the currentWeek.
        int currentWeek = Calendar.WEEK_OF_YEAR;
        int userID = cursor.getInt(0);
        cursor.close();

        // Gets all records that match the user's id, as well as being the matching week of the calendar year.
        Cursor nCursor = cr.query(MyContentProvider.URI, null, MyContentProvider.W_ID + " = " + userID + " AND " +
                MyContentProvider.WEEK + " = " + currentWeek, null, null);

        return 0;
    }

    /**
     * This is the method to The amount of workouts that a user has had throughout his/her
     * career.
     *
     * Returns either the number, or 0 if none or no user.
     * @return
     */
    public int getAllTimeWorkoutCount() {
        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(MyContentProvider.URI2, new String[] {MyContentProvider.NUM_WORKOUTS}, "UserName = "+ mName, null, null);
        if (cursor != null && cursor.getCount() >0) {
            return cursor.getInt(0);
        }
        else {
            return 0;
        }
    }



}
