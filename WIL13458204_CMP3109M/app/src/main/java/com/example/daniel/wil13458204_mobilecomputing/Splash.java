package com.example.daniel.wil13458204_mobilecomputing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;

public class Splash extends Activity {

    //region GLOBAL VARIABLES
    private static final String strSharedPrefsName = "MyPreferences";
    private boolean blUserLoggedIn = false;
    private String strUsername, strActiveUsername, strActiveFirstName, strActiveLastName, strProfileImageUri;
    private Integer intActiveUserID;
    private ProgressBar pbSpinnerProgress;
    private ActiveUser au = ActiveUser.getInstance();
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //RETRIEVES SHARED PREFERENCE FILE AND RETRIEVES THE ACTIVE USER VALUE
        SharedPreferences prefsMyPreferences = getSharedPreferences(strSharedPrefsName, Context.MODE_PRIVATE);
        blUserLoggedIn = prefsMyPreferences.getBoolean(getString(R.string.isActiveUser), false);

        //EXECUTED IF THERE IS AN ACTIVE USER LOGGED INTO THE APPLICATION
        if(blUserLoggedIn) {

            //region RETRIEVES THE LOGGED IN USERS DETAILS FRM SHARED PREFERENCES
            strActiveUsername = prefsMyPreferences.getString(getString(R.string.ActiveUsername), "No Active User");
            strActiveFirstName = prefsMyPreferences.getString(getString(R.string.ActiveFirstName), null);
            strActiveLastName = prefsMyPreferences.getString(getString(R.string.ActiveLastName), null);
            intActiveUserID = prefsMyPreferences.getInt(getString(R.string.ActiveUserID), 0);
            strProfileImageUri = prefsMyPreferences.getString(getString(R.string.ActiveImageUri), null);
            //endregion

            //region SETS THE VALUES OF THE ACTIVE USER WITHIN THE SINGLETON ACTIVE USER CLASS
            au.setActiveUsername(strActiveUsername);
            au.setActiveFirstName(strActiveFirstName);
            au.setActiveLastName(strActiveLastName);
            au.setActiveUserID(intActiveUserID);
            au.setProfileImageUri(strProfileImageUri);
            //endregion
        }

        //CREATES A HANDLER TO HANDLE A DELAY FUNCTION
        Handler intentHandler = new Handler();
        intentHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //STARTS THE RANDOM RECIPE ACTIVITY AFTER THE DELAY
                startActivity(new Intent(Splash.this, RandomRecipes.class));
                finish();   //ENDS THE ACTIVITY SO IT WILL NOT APPEAR ON THE ACTIVITY BACK STACK
            }
        },2000);    //SETS A DELAY OF 2 SECONDS
    }
}
