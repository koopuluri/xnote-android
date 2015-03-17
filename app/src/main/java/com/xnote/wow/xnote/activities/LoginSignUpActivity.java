package com.xnote.wow.xnote.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.parse.ParseAnonymousUtils;
import com.parse.ParseUser;
import com.xnote.wow.xnote.Controller;
import com.xnote.wow.xnote.R;
import com.xnote.wow.xnote.Util;
import com.xnote.wow.xnote.XnoteApplication;
import com.xnote.wow.xnote.fragments.LoginFragment;
import com.xnote.wow.xnote.fragments.SignUpFragment;
import com.xnote.wow.xnote.fragments.WelcomeFragment;

/**
 * Created by Vignesh Prasad on 03/02/2015
 * The main activity that controls all login and registration activity
 * It opens up fragments based on whether the user has used the application before
 * and depending on whether the user log in information is stored
 */
public class LoginSignUpActivity extends Activity {
    public final String TAG = "LoginSignUpActivity";
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!XnoteApplication.LOG_IN) {
            ParseUser.logInInBackground("vig9295@gmail.com", "greatbro123");
            Controller.launchMainActivity(this);
        }

        setContentView(R.layout.activity_loginsignup);
        //Activity variable is used so that inner classes can refer to current activity
        final Activity activity = this;

        // Creating SharedPreferences to check if the app is being used the first time
        // Also checks if the user has chosen to continue without registering
        // http://stackoverflow.com/a/13237848
        final String PREFS_NAME = "MyPrefsFile";
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        //Fragment manager stuff to call the necessary fragments when needed
        FragmentManager fm = getFragmentManager();
        android.app.FragmentTransaction fragmentTransaction = fm.beginTransaction();

        if (settings.getBoolean("my_first_time", true)) {
            // the app is being launched for first time show the registration information
            // record the fact that the app has been started at least once
            // used apply rather than commit so it runs in the background
            // Note my first time is updated in hte fragment
            Fragment fragment = new WelcomeFragment();
            fragmentTransaction.add(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            settings.edit().putBoolean("chosen_to_signup", false).apply();
            Log.d(TAG, "using the application first time");
        } else {
            //Check if the user has logged in before and the details are on the cache
            Log.d(TAG, "not first time");
            ParseUser currentUser = ParseUser.getCurrentUser();

            if(settings.getBoolean("chosen_to_signup", true)) {
                //Anonymous user has indicated that he wants to sign up
                Fragment fragment = new SignUpFragment();
                fragmentTransaction.add(R.id.fragment_container, fragment);
                fragmentTransaction.commit();
                settings.edit().putBoolean("chosen_to_signup", false).apply();
                Log.d(TAG, "Anonymous user has chosen to signup");
            } else if ((currentUser != null) && (!ParseAnonymousUtils.isLinked(ParseUser.getCurrentUser()))) {
                //If login details are in the cache and user is not anonymous
                Util.IS_ANON = false;
                Controller.launchMainActivity(activity);
                Log.d(TAG, "A user is logged in and cached");
            } else {
                //Check if the user has indicated anonymous user preference before
                if ((currentUser != null) && (ParseAnonymousUtils.isLinked(ParseUser.getCurrentUser()))) {
                    Util.IS_ANON = true;
                    ParseUser.enableAutomaticUser();
                    ParseUser.getCurrentUser().saveInBackground();
                    Controller.launchMainActivity(activity);
                    Log.d(TAG, "User has chosen to be anonymous user");
                } else {
                    // If user is not anonymous then the user must be asked to login
                    settings.edit().putBoolean("chosen_to_signup", false).apply();
                    LoginFragment fragment = new LoginFragment();
                    fragmentTransaction.add(R.id.fragment_container, fragment);
                    fragmentTransaction.commit();
                    Log.d(TAG, "User has logged out and needs to be asked to login");
                }
            }
        }
    }
}