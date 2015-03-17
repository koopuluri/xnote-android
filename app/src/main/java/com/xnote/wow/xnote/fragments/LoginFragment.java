package com.xnote.wow.xnote.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.xnote.wow.xnote.Controller;
import com.xnote.wow.xnote.R;
import com.xnote.wow.xnote.TextValidator;
import com.xnote.wow.xnote.Util;


/**
 * Created by Vignesh Prasad on 03/02/2015
 * Fragment that allows the user to login
 * Note that in the login class the email field
 * is always referred to as username.
 */
public class LoginFragment extends Fragment {

    public final String TAG = "Login Fragment";
    Button mDoneButton;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private TextView mSignUpTextView;
    private TextView mForgotPasswordTextView;


    //The boolean variables keep track of preliminary validation done without using Parse
    //This allows us to make Parse calls only when necessary and also gives us freedom to define our own
    //constraints for the text field.
    public Boolean usernameIsValid = false;
    public Boolean passwordIsValid = false;

    //The invalid details flag is used so that the errors can be removed from both fields when there is an
    //edit made on any one field after an invalid credentials error is thrown by Parse. This allows us to
    //maintain security by not letting the user know in which of the fields the error is in.
    public Boolean invalidDetailsFlag = false;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        final Fragment thisFragment = this;
        usernameEditText = (EditText) view.findViewById(R.id.email);
        usernameEditText.addTextChangedListener(new TextValidator(usernameEditText) {
            //Override the validator of the abstract class
            @Override
            public void validate(TextView textView, String text) {
                usernameIsValid = true;
                if (text.equals("")) {
                    usernameIsValid = false;
                    textView.setError("Please enter a valid email ID");
                }
                if (invalidDetailsFlag) {
                    invalidDetailsFlag = false;
                    passwordEditText.setError(null);
                }
            }
        });

        passwordEditText = (EditText) view.findViewById(R.id.password);
        passwordEditText.addTextChangedListener(new TextValidator(passwordEditText) {
            //Override the validator of the abstract class
            @Override
            public void validate(TextView textView, String text) {
                passwordIsValid = true;
                if (text.equals("")) {
                    passwordIsValid = false;
                    textView.setError("Please enter a valid password");
                }
                if(invalidDetailsFlag) {
                    invalidDetailsFlag = false;
                    usernameEditText.setError(null);
                }
            }
        });

        mDoneButton = (Button) view.findViewById(R.id.done_button);
        mDoneButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Done button logs in the user if the details are correct
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                if(usernameIsValid && passwordIsValid) {
                    ParseUser.logInInBackground(username, password, new LogInCallback() {
                        @Override
                        public void done(ParseUser parseUser, ParseException e) {
                            if (parseUser != null) {
                                new LoginTask(getActivity()).execute();
                            } else {
                                usernameEditText.setError("Invalid username or password");
                                passwordEditText.setError("Invalid username or password");
                                invalidDetailsFlag = true;
                            }
                        }
                    });
                }
            }
        });

        mSignUpTextView = (TextView)view.findViewById(R.id.signup_textview);
        mSignUpTextView.setOnClickListener(new View.OnClickListener() {
            //Opens up the signup fragment to signup a new user
            public void onClick(View v) {
                FragmentManager fm = getActivity().getFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                Fragment fragment = new SignUpFragment();
                fragmentTransaction.remove(thisFragment);
                fragmentTransaction.add(R.id.fragment_container, fragment);
                fragmentTransaction.commit();

            }
        });

        mForgotPasswordTextView = (TextView)view.findViewById(R.id.forgot_password);
        mForgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            //Opens up the signup fragment to signup a new user
            public void onClick(View v) {
                FragmentManager fm = getActivity().getFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                Fragment fragment = new ForgotPasswordFragment();
                fragmentTransaction.remove(thisFragment);
                fragmentTransaction.add(R.id.fragment_container, fragment);
                fragmentTransaction.commit();
            }
        });
        return view;
    }


    private class LoginTask extends AsyncTask<Void, Void, Void> {
        private static final String TAG = "LoginFragment.LoginTask";
        Activity parentActivity;

        public LoginTask(Activity activity) {
            parentActivity = activity;
        }

        @Override
        public void onPreExecute() {
            // TODO: start spinner.
        }

        @Override
        public Void doInBackground(Void... params) {
            Util.IS_ANON = false;
            //DB.sync();
            Log.d(TAG, "doInBackground() DB.synced!");
            return null;
        }

        @Override
        public void onPostExecute(Void _) {
            super.onPostExecute(_);
            Controller.launchMainActivity(parentActivity);
            parentActivity.finish();
            // TODO: end spinner.
        }
    }
}