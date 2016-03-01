package com.example.daniel.wil13458204_mobilecomputing;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.Layout;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ChangeResetPassword extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    //region GLOBAL VARIABLES
    private SQLiteDatabaseAdapter myDatabaseAdapter;
    private String strUsername, strSecurityQuestion, strSecurityAnswer, strNewPassword, strReNewPassword, strActiveUsername, strPassedUsername;
    private Integer intUserID;
    private TextView tvSecurityQuestion, tvSecurityAnswer, tvNewPass, tvReNewPass, tvActiveUser, tvHeaderTitle;
    private EditText etUsername, etSecurityAnswer, etNewPassword, etReNewPassword;
    private ImageView ivUserProfilePic;
    private Button btnGetSQ, btnChangePass;
    private LinearLayout layPassChangeLayout;
    private Bitmap bitProfileImage;
    private ActiveUser au;
    private InputStream is;
    private static final String strSharedPrefsName = "MyPreferences";
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //region PRE-EXISTENT CODE
        setContentView(R.layout.activity_change_reset_password);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //endregion

        //region NAVIGATION DRAWER CONTROL AND LISTENER
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);              //INSTANTIATES NAVIGATION DRAWER LAYOUT
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

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);       //INSTANTIATES NAVIGATION VIEW
        navigationView.setNavigationItemSelectedListener(this);                             //INITIALISES ON CLICK LISTENER FOR MENU ITEMS
        //endregion

        //region COMPONENT CASTING
        tvSecurityQuestion = (TextView)findViewById(R.id.tvSecurityQuestion);
        tvSecurityAnswer = (TextView)findViewById(R.id.tvSecurityAnswer);
        tvNewPass = (TextView)findViewById(R.id.tvNewPass);
        tvReNewPass = (TextView)findViewById(R.id.tvReNewPass);

        btnGetSQ = (Button)findViewById(R.id.btnGetSQ);
        btnChangePass = (Button)findViewById(R.id.btnChangePass);

        etUsername = (EditText)findViewById(R.id.etUsername);
        etSecurityAnswer = (EditText)findViewById(R.id.etSecurityAnswer);
        etNewPassword = (EditText)findViewById(R.id.etNewPass);
        etReNewPassword = (EditText)findViewById(R.id.etReNewPass);
        etSecurityAnswer = (EditText)findViewById(R.id.etSecurityAnswer);
        etNewPassword = (EditText)findViewById(R.id.etNewPass);
        etReNewPassword = (EditText)findViewById(R.id.etReNewPass);

        layPassChangeLayout = (LinearLayout)findViewById(R.id.layPasschangeLinear);
        //endregion

        //region INSTANTIATIONS
        myDatabaseAdapter = new SQLiteDatabaseAdapter(this);
        au = ActiveUser.getInstance();
        strActiveUsername = au.getActiveUsername();
        Uri imageUri = au.getProfilePicUri();
        //endregion

        //region NAV DRAW HEADER VIEW SETTING SETTING ACTIVE USER DETAILS
        //region NAV DRAW COMPONENT CASTING AND INSTANTIATION
        View header = navigationView.getHeaderView(0);
        tvActiveUser = (TextView)header.findViewById(R.id.tvActiveUsername);
        tvHeaderTitle = (TextView)header.findViewById(R.id.tvHeaderTitle);
        ivUserProfilePic = (ImageView)header.findViewById(R.id.ivHeaderImageView);
        tvHeaderTitle.setText(getString(R.string.HeaderTitle));
        tvActiveUser.setText(strActiveUsername);
        //endregion

        //region READ IN USER PROFILE IMAGE FROM imageURI
        try {
            is = getContentResolver().openInputStream(imageUri);
            bitProfileImage = BitmapFactory.decodeStream(is);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(ChangeResetPassword.this, "Image Not Fount", Toast.LENGTH_SHORT).show();
        }
        //endregion

        //region CLOSE INPUT STREAM
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //endregion

        ivUserProfilePic.setImageBitmap(bitProfileImage);
        //endregion

        //CHECKING INTENT FOR EXTRAS TO DETERMINE THE INTENT INITIALISER (EITHER MY ACCOUNT CHANGE PASSWORD OR LOGIN FORGOTTEN PASSWORD)
        if (getIntent().getExtras() != null){
            strPassedUsername = getIntent().getExtras().getString("Username", null);    //GETS USERNAME PASSED THROUGH INTENT
            etUsername.setText(strPassedUsername);
            etUsername.setEnabled(false);
            btnGetSQ.setEnabled(false);
            btnGetSQ.setBackgroundResource(R.color.btnDisabledColor);
            btnGetSQ.setTextColor(getResources().getColor(R.color.btnDisabledTextColor));
            GetSecureQuestion();     //RETURNS USER SECURITY QUESTION
        }
    }

    //RETRIEVES SECURITY QUESTION FROM DATABASE FOR GIVEN USERNAME
    public void GetSecureQuestion(){

        strUsername = etUsername.getText().toString();  //GETS USERNAME FROM USER INPUT

        if (strUsername.length() >= 1){                 //CHECKS THAT A USERNAME HAS BEEN INSERTED INTO THE FIELD

            strSecurityQuestion = myDatabaseAdapter.dbGetSecurityQuestion(strUsername);     //RETRIEVES SECURITY QUESTION FROM DATABASE
            intUserID = myDatabaseAdapter.dbGetActiveUserIDFromUsername(strUsername);       //RETRIEVES USER ID FROM DATABASE

            if (strSecurityQuestion != null && intUserID != 0){         //CHECKS THAT THERE IS A VALID USER WITHIN THE DATABASE

                tvSecurityQuestion.setText(strSecurityQuestion);        //SETS SECURITY QUESTION TO TEXT VIEW

                //region SETTING COMPONENT VISIBILITY
                tvSecurityQuestion.setVisibility(View.VISIBLE);
                tvSecurityAnswer.setVisibility(View.VISIBLE);
                tvNewPass.setVisibility(View.VISIBLE);
                tvReNewPass.setVisibility(View.VISIBLE);
                etSecurityAnswer.setVisibility(View.VISIBLE);
                etNewPassword.setVisibility(View.VISIBLE);
                etReNewPassword.setVisibility(View.VISIBLE);
                btnChangePass.setVisibility(View.VISIBLE);
                layPassChangeLayout.setVisibility(View.VISIBLE);
                //endregion

            }else Toast.makeText(ChangeResetPassword.this, "No User Found!", Toast.LENGTH_SHORT).show();
        }else Toast.makeText(ChangeResetPassword.this, "Please Enter Your Username", Toast.LENGTH_SHORT).show();
    }

    public void SecureQuestionRecovery(View view){
        GetSecureQuestion();
    }

    //COMPLETES PASSWORD CHANGE PROCESS WHEN BUTTON CLICKED
    public void ChangePassword(View view){

        //region GETTING USER ENTRY
        strSecurityAnswer = etSecurityAnswer.getText().toString();
        strNewPassword = etNewPassword.getText().toString();
        strReNewPassword = etReNewPassword.getText().toString();
        //endregion

        //region PASSWORD CHANGE PROCESS
        if (strSecurityAnswer.length() >= 1 && strNewPassword.length() >= 1 && strReNewPassword.length() >= 1) {    //CHECKS ALL FIELDS HAVE AN ENTRY

            int intAuthCount = myDatabaseAdapter.dbAuthenticatePassChange(intUserID, strSecurityQuestion, strSecurityAnswer);   //QUERIES DATABASE TO CHECK THAT THER IS AN ENTRY THAT MATHCED THE USERS ENTRY

            if (intAuthCount == 1){     //CHECKS COUNT OF RETURNED DATABASE MATCHES

                if (strNewPassword.equals(strReNewPassword)){   //CHECK PASSWORD INPUT MATCH

                    myDatabaseAdapter.dbChangePassword(intUserID, strNewPassword);  //DATABASE QUERY TO SET THE DATABASE FOR GIVEN USER ID

                    Toast.makeText(ChangeResetPassword.this, "Password successfully changed", Toast.LENGTH_SHORT).show();

                    //STARTS RANDOM RECIPE ACTIVITY CLOSING THE ACTIVITY STACK PREVENTING UNAUTHORISED PASSWORD CHANGES VIA THE BACK BUTTON
                    Intent passChange = new Intent(ChangeResetPassword.this, RandomRecipes.class);
                    passChange.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    passChange.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(passChange);
                    finish();

                }else Toast.makeText(ChangeResetPassword.this, "Passwords Do Not Match", Toast.LENGTH_SHORT).show();
            }else Toast.makeText(ChangeResetPassword.this, "Unauthorised password change", Toast.LENGTH_SHORT).show();
        } else Toast.makeText(ChangeResetPassword.this, "Please enter your security answer and new password", Toast.LENGTH_SHORT).show();
        //endregion
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
        getMenuInflater().inflate(R.menu.change_reset_password, menu);
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

            strActiveUsername = au.getActiveUsername();     //GETS ACTIVE USERNAME FROM ACTIVEUSER CLASS
            tvActiveUser.setText(strActiveUsername);
            //ivUserProfilePic.setImageBitmap();

            //region STARTS RANDOM RECIPE ACTIVITY CLOSING THE ACTIVITY STACK PREVENTING UNAUTHORISED ACCESS TO ACCOUNT VIA THE BACK BUTTON
            Intent logoutIntent = new Intent(ChangeResetPassword.this, RandomRecipes.class);
            logoutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(logoutIntent);
            finish();
            //endregion

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

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
