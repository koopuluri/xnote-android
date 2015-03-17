package com.xnote.wow.xnote.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parse.ParseAnonymousUtils;
import com.parse.ParseUser;
import com.xnote.wow.xnote.Controller;
import com.xnote.wow.xnote.DB;
import com.xnote.wow.xnote.R;

import java.util.ArrayList;

/**
 *  Created by Vignesh Prasad on 03/02/2015
 */
public class SettingsFragment extends ListFragment {

    public static final String TAG = "Settings Fragment";
    ArrayList<String> mOptionsList;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Settings fragment loaded");

        //OptionsList stores the options available to the user
        //Currently only logout or login
        mOptionsList = new ArrayList<String>();
        if (ParseAnonymousUtils.isLinked(ParseUser.getCurrentUser())) {
            mOptionsList.add("Sign Up");
        } else {
            mOptionsList.add("Logout");
        }
        final ArrayAdapter mAdapter = new ArrayAdapter(this.getActivity(),
                android.R.layout.simple_list_item_1, mOptionsList);
        setListAdapter(mAdapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // when an option is selected from the Article List:
        String ch = (String) getListAdapter().getItem(position);
        if (ch.equalsIgnoreCase("Logout")) {
            //User is logged out if they choose logout
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.are_you_sure_logout);
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    new LogoutTask(getActivity()).execute();
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else if (ch.equalsIgnoreCase("Sign Up")) {
            Controller.launchSignUpFromAnonymousUser(this.getActivity());
        }
    }



    private class LogoutTask extends AsyncTask<Void, Void, Void> {
        private static final String TAG = "SettingsFragment.LogoutTask";
        Activity parentActivity;

        public LogoutTask(Activity activity) {
            parentActivity = activity;
        }

        @Override
        public void onPreExecute() {
            // TODO: start spinner.
        }

        @Override
        public Void doInBackground(Void... params) {
            ParseUser.logOut();
            // DB.clearLocalDatabase(); TODO: holy fuck this call does not do what you expect it to do!
            Log.d(TAG, "ParseUser.logged out and cleared local datastore.");
            return null;
        }

        @Override
        public void onPostExecute(Void _) {
            super.onPostExecute(_);
            Controller.launchLoginSignUpActivity(parentActivity);
            parentActivity.finish();
            // TODO: end spinner.
        }
    }
}