package com.example.daniel.wil13458204_mobilecomputing;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.text.Layout;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class UserLogin extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    //region GLOBAL VARIABLES
    private EditText etUsername, etPassword;
    private TextView tvRegisterUser, tvActiveUser, tvHeaderTitle;
    private ImageView ivUserProfilePic;
    private LinearLayout layout;
    private CheckBox cbStayLoggedIn;
    private SQLiteDatabaseAdapter myDatabaseAdapter;
    private Bitmap bitProfileImage;
    private String strUsername, strPassword, strActiveUser, strActiveLastName, strActiveFirstName, strActiveUsername, strActiveProfilePicUri,
                        strPassedUsername, strPassedPassword;
    private Integer intActiveUserID, intCheckActiveID;
    private boolean blStayLoggedIn = true, blActiveUser;
    private static final String strSharedPrefsName = "MyPreferences";
    private ActiveUser au;
    private InputStream is;
    private NavigationView navigationView;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //region PRE EXISTENT CODE
        setContentView(R.layout.activity_user_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //endregion

        //region NAVIGATION DRAWER CONTROL AND LISTENER
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                //CLOSES THE KEYBOARD WHEN THE DRAWER IS OPENED
                InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //endregion

        //region CASTING
        etUsername = (EditText)findViewById(R.id.etUsername);
        etPassword = (EditText)findViewById(R.id.etPassword);
        tvRegisterUser = (TextView)findViewById(R.id.tvRegisterNewUser);
        cbStayLoggedIn = (CheckBox)findViewById(R.id.cbStayLoggedIn);
        layout = (LinearLayout)findViewById(R.id.Layout);
        //endregion

        //region INSTANTIATION
        navigationView.setCheckedItem(R.id.user_login);
        au = ActiveUser.getInstance();
        strActiveUsername = au.getActiveUsername();
        Uri imageUri = au.getProfilePicUri();
        myDatabaseAdapter = new SQLiteDatabaseAdapter(this);
        //endregion

        //region NAV HEADER VIEW SETTING
        View header = navigationView.getHeaderView(0);
        tvActiveUser = (TextView)header.findViewById(R.id.tvActiveUsername);
        tvHeaderTitle = (TextView)header.findViewById(R.id.tvHeaderTitle);
        ivUserProfilePic = (ImageView)header.findViewById(R.id.ivHeaderImageView);
        tvHeaderTitle.setText(getString(R.string.HeaderTitle));
        tvActiveUser.setText(strActiveUsername);

        try {
            is = getContentResolver().openInputStream(imageUri);
            bitProfileImage = BitmapFactory.decodeStream(is);
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
            Toast.makeText(UserLogin.this, "Image Not Fount", Toast.LENGTH_SHORT).show();
        }

        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ivUserProfilePic.setImageBitmap(bitProfileImage);
        //endregion

        layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //CLOSES THE KEYBOARD WHEN THE DRAWER IS OPENED
                InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                return false;
            }
        });

        //region CHECKS FOR PASSED INTENT RETRIEVING THE INTENT EXTRAS IS AVAILABLE
        if (getIntent().getExtras() != null) {
            strPassedUsername = getIntent().getExtras().getString("Username");
            strPassedPassword = getIntent().getExtras().getString("Password");

            //SETS EDIT TEXT VALUES IF RECEIVED EXTRAS ARE NOT NULL
            if (strPassedUsername != null && strPassedPassword != null) {
                etUsername.setText(strPassedUsername);
                etPassword.setText(strPassedPassword);
            }
        }
        //endregion

    }

    public void CheckboxControl(View view){

        if (cbStayLoggedIn.isChecked()){
            cbStayLoggedIn.setChecked(false);
            cbStayLoggedIn.toggle();
        }else {
            cbStayLoggedIn.setChecked(true);
            cbStayLoggedIn.toggle();
        }
    }

    //EXECUTES LOGIN PROCESS ON BUTTON CLICK
    public void userLogin(View view){

        //GETS THE VALUES ENTERED BY THE USER
        strUsername = etUsername.getText().toString();
        strPassword = etPassword.getText().toString();

        //GETS ACTIVE USER STATE
        SharedPreferences prefsMyPrefernces = getSharedPreferences(strSharedPrefsName, Context.MODE_PRIVATE);
        blActiveUser = prefsMyPrefernces.getBoolean("LoggedIn", false);

        //GETS AND STORES STATE OF 'STAY LOGGED IN' CHECKBOX
        if (cbStayLoggedIn.isChecked()){
            blStayLoggedIn = true;
        }
        else blStayLoggedIn = false;


        //CHECKS THERE IS NO ACTIVE USER
        if (blActiveUser != true) {

            //CHECKS THERE IS DATA ENTERED INTO THE EDIT TEXTS
            if (strUsername.length() >= 1 && strPassword.length() >= 1) {

                //CALLS DATABASE QUERY METHOD AND RETRIEVES COUNT OF MATCHING DATABASE ROWS
                int intLoginValidity = myDatabaseAdapter.validateUserLogin(strUsername, strPassword);

                //CHECKS THERE IS ONLY ONE DATABASE RESULT THAT MATCHES THE ENTERED USER CREDENTIALS
                if (intLoginValidity == 1) {

                    strActiveUser = strUsername;    //SETS THE ACTIVE USERNAME

                    //region DATABASE QUERYS TO GET ACTIVE USER DETAILS
                    intActiveUserID = myDatabaseAdapter.dbGetActiveUserID(strActiveUser, strPassword);
                    strActiveFirstName = myDatabaseAdapter.dbGetActiveUserFirstName(strActiveUser, strPassword);
                    strActiveLastName = myDatabaseAdapter.dbGetActiveUserLastName(strActiveUser, strPassword);
                    strActiveProfilePicUri = myDatabaseAdapter.dbGetActiveUserProfileUri(strActiveUser, strPassword);
                    //endregion

                    //region SETS ACTIVE USER DETAILS IN ACTIVE USER CLASS
                    ActiveUser au = ActiveUser.getInstance();       //RETRIEVES INSTANCE OF THE ACTIVE USER SINGLETON CLASS
                    au.setActiveUsername(strActiveUser);
                    au.setActiveFirstName(strActiveFirstName);
                    au.setActiveLastName(strActiveLastName);
                    au.setActiveUserID(intActiveUserID);
                    au.setProfileImageUri(strActiveProfilePicUri);
                    //endregion


                    //EXECUTED IF CHECKBOX IS TICKED KEEPING USER LOGGED IN UNTIL LOGGED OUT MANUALLY OR APP DATA CLEARED
                    if (blStayLoggedIn) {

                        //region ADD LOGGED IN USERS DETAILS TO SHARED PREFS
                        SharedPreferences prefsMyPreferences = getSharedPreferences(strSharedPrefsName, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor;
                        editor = prefsMyPreferences.edit();
                        editor.putBoolean(getString(R.string.isActiveUser), true);
                        editor.putString(getString(R.string.ActiveUsername), strActiveUser);
                        editor.putString(getString(R.string.ActiveFirstName), strActiveFirstName);
                        editor.putString(getString(R.string.ActiveLastName), strActiveLastName);
                        editor.putInt(getString(R.string.ActiveUserID), intActiveUserID);
                        editor.putString(getString(R.string.ActiveImageUri),strActiveProfilePicUri);
                        editor.commit();
                        //endregion
                    }

                    Toast.makeText(UserLogin.this, "LOGIN SUCCESSFUL", Toast.LENGTH_SHORT).show();

                    //region STARTS RANDOM RECIPE ACTIVITY CLEARING ACTIVITY STACK SO THAT BACK WILL NOT RETURN TO THI ACTIVITY
                    Intent homeIntent = new Intent(this, RandomRecipes.class);
                    homeIntent.putExtra("ActiveUsername", strActiveUser);
                    homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(homeIntent);
                    finish();
                    //endregion

                } else Toast.makeText(UserLogin.this, "No user found", Toast.LENGTH_SHORT).show();
            } else Toast.makeText(UserLogin.this, "Invalid entry!\nPlease enter username and password", Toast.LENGTH_SHORT).show();
        }else Toast.makeText(UserLogin.this, "Active user\nPlease logout before loggin in new account ", Toast.LENGTH_SHORT).show();

    }

    //STARTS NEW USER REGISTRATION ACTIVITY
    public void RegisterNewUser(View view){

        intCheckActiveID = Integer.parseInt(au.getActiveUserID());

        //CHECKS THERE IS NO ACTIVE USER
        if (intCheckActiveID == 0) {
            startActivity(new Intent(this, New_User_Registration.class));
            finish();
        }else Toast.makeText(UserLogin.this, "Active user. Please log out", Toast.LENGTH_SHORT).show();
    }

    //STARTS PASSWORD REST ACTIVITY
    public void ResetPass(View view){

        intCheckActiveID = Integer.parseInt(au.getActiveUserID());

        //CHECKS THERE IS NO ACTIVE USER
        if (intCheckActiveID == 0) {
            startActivity(new Intent(UserLogin.this, ChangeResetPassword.class));
            finish();
        }else Toast.makeText(UserLogin.this, "Active user. Please log out", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationView.setCheckedItem(R.id.user_login);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        //CLOSES NAV DRAWER IF OPEN OR CALLS SUPER IF NOT
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // INFLATES THE ACTION BAR MENU; ADDING THE ITEMS FROM THE XML IF PRESENT
        getMenuInflater().inflate(R.menu.user_login, menu);
        return true;
    }

    @Override
    //HANDLES ACTION BAR MENU ITEM CLICK
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_logout) {

            au.userLogout();    //CALLS LOGOUT METHOD OF ACTIVEUSER CLASS SETTING ALL USER DETAILS TO DEFAULT

            //region EDIT SHARED PREFERENCES DATA RESETTING TO DEFAULT SETTINGS
            SharedPreferences prefsMyPreferences = getSharedPreferences(strSharedPrefsName, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor;
            editor = prefsMyPreferences.edit();

            editor.putBoolean(getString(R.string.isActiveUser), false);
            editor.putString(getString(R.string.ActiveUsername), "No Active User");
            editor.putString(getString(R.string.ActiveFirstName), null);
            editor.putString(getString(R.string.ActiveLastName), null);
            editor.putInt(getString(R.string.ActiveUserID), 0);
            editor.putString(getString(R.string.ActiveImageUri), au.getDefaultProfilePicUri().toString());
            editor.commit();
            //endregion

            strActiveUsername = au.getActiveUsername();
            tvActiveUser.setText(strActiveUsername);
            //ivUserProfilePic.setImageBitmap();

            //region STARTS RANDOM RECIPE ACTIVITY CLOSING THE ACTIVITY STACK PREVENTING UNAUTHORISED ACCESS TO ACCOUNT VIA THE BACK BUTTON
            Intent logoutIntent = new Intent(UserLogin.this, RandomRecipes.class);
            logoutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(logoutIntent);
            finish();
            //endregion

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    //HANDLES NAVIGATION DRAWER ITEM CLICK STARTING RELEVANT ACTIVITY
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();  //GETS ID OF SELECTED DRAWER ITEM

        //EXECUTES ID SPECIFIC INTENT
        if (id == R.id.Home) {
            startActivity(new Intent(this, RandomRecipes.class));
            finish();
        } else if (id == R.id.search_recipe) {
            startActivity(new Intent(this, SearchRecipe.class));
            finish();
        } else if (id == R.id.my_favourites) {
            startActivity(new Intent(this, MyFavourites.class));
            finish();
        } else if (id == R.id.user_login) {
            startActivity(new Intent(this, UserLogin.class));
            finish();
        } else if (id == R.id.register_new) {
            startActivity(new Intent(this, New_User_Registration.class));
            finish();
        } else if (id == R.id.my_account) {
            startActivity(new Intent(this, MyAccount.class));
            finish();
        }

        //CLOSES DRAWER WHEN SELECTION MADE
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
