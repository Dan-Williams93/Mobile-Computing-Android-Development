package com.example.daniel.wil13458204_mobilecomputing;

import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class New_User_Registration extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    //region GLOBAL VARIABLES
    private EditText etFirstName, etLastName, etUsername, etPassword, etRePassword, etSecurityAnswer;
    private TextView tvActiveUser, tvHeaderTitle;
    private Spinner spSecurityQuestions;
    private ImageView ivUserProfilePic, ivNewUserProfilePic;
    private Button btnAddData;
    private SQLiteDatabaseAdapter myDatabaseAdapter;
    private Bitmap bitProfileImage, bitBackupImage;
    private String strFirstName, strLastName, strUsername, strPassword, strRePassword, strSecQuestion, strSQAnswer, strActiveUsername,
            strDefaultProfileIm, strCurrentProfileImageUri, strSelectedImageUri;
    private static final String strSharedPrefsName = "MyPreferences";
    private ActiveUser au;
    private InputStream is;
    private NavigationView navigationView;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //region PRE EXISTENT CODE
        setContentView(R.layout.activity_new_user_registration);
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
        etFirstName = (EditText)findViewById(R.id.etFirstName);
        etLastName = (EditText)findViewById(R.id.etLastName);
        etUsername = (EditText)findViewById(R.id.etUsername);
        etPassword = (EditText)findViewById(R.id.etPassword);
        etRePassword = (EditText)findViewById(R.id.etRePassword);
        etSecurityAnswer = (EditText)findViewById(R.id.etSecurityAnswer);
        spSecurityQuestions = (Spinner)findViewById(R.id.SQSpinner);
        btnAddData = (Button)findViewById(R.id.btnAddData);
        ivNewUserProfilePic = (ImageView)findViewById(R.id.bivGetImage);
        //endregion

        //region INSTANTIATIONS
        navigationView.setCheckedItem(R.id.register_new);
        au = ActiveUser.getInstance();
        strActiveUsername = au.getActiveUsername();
        strSelectedImageUri = au.getDefaultProfilePicUri().toString();
        Uri imageUri = au.getProfilePicUri();
        myDatabaseAdapter = new SQLiteDatabaseAdapter(this);
        bitBackupImage = BitmapFactory.decodeResource(getResources(), R.drawable.carrot);
        //endregion

        //region NAV DRAW COMPONENT CASTING AND INSTANTIATION
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
            Toast.makeText(New_User_Registration.this, "Image Not Fount", Toast.LENGTH_SHORT).show();
        }

        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ivUserProfilePic.setImageBitmap(bitProfileImage);
        //endregion

    }

    public void addNewUser(View view){

        strFirstName = etFirstName.getText().toString();
        strLastName = etLastName.getText().toString();
        strUsername = etUsername.getText().toString();
        strPassword = etPassword.getText().toString();
        strRePassword = etRePassword.getText().toString();
        strSQAnswer = etSecurityAnswer.getText().toString();
        strSecQuestion = spSecurityQuestions.getSelectedItem().toString();

        //CHECKS ALL FIELDS ARE FILLED
        if (strFirstName.length() >=1 && strLastName.length() >=1 && strUsername.length() >=1 && strPassword.length() >=1 && strRePassword.length() >=1 && strSQAnswer.length() >=1) {

            //MAKES SURE PASSWORDS MATCH
            if (strPassword.equals(strRePassword)) {

                int intUsernameUses = myDatabaseAdapter.checkUsernameAvailabilty(strUsername);

                //CHECKS USERNAME ORIGINALITY
                if (intUsernameUses <= 0) {

                    Toast.makeText(New_User_Registration.this, "NO SIMILAR USERNAME'S", Toast.LENGTH_SHORT).show();

                    //REGISTER USER AND GET RETURN
                    long id = myDatabaseAdapter.registerNewUser(strFirstName, strLastName, strUsername, strPassword, strSelectedImageUri, strSecQuestion, strSQAnswer);

                    //CHECKS IF REGISTRATION WAS SUCCESSFUL
                    if (id <= 0) {
                        Toast.makeText(New_User_Registration.this, "REGISTRATION FAILED", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(New_User_Registration.this, "REGISTRATION COMPLETE", Toast.LENGTH_SHORT).show();
                        Intent loginIntent = new Intent(New_User_Registration.this, UserLogin.class);
                        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        loginIntent.putExtra("Username", strUsername);
                        loginIntent.putExtra("Password", strPassword);
                        startActivity(loginIntent);
                        finish();
                    }
                } else
                    Toast.makeText(New_User_Registration.this, "SIMILAR USERNAME FOUND", Toast.LENGTH_SHORT).show();
            }else
                Toast.makeText(New_User_Registration.this, "PASSWORDS DO NOT MATCH", Toast.LENGTH_SHORT).show();
        }else Toast.makeText(New_User_Registration.this, "Please fill all data fields", Toast.LENGTH_SHORT).show();
    }

    //region GET IMAGE USING IMPLICIT INTENT TO PHONES GALLERY OR OTHER PHOTO STORING APPLICATION

    //CREATES INTENT ALLOWING USER TO SELECT IMAGE APP
    public void GetImage(View view){
        Intent intent = new Intent();           //CREATES NEW INTENT
        intent.setType("image/*");              //SETS TYPE OF INTENT TO RETRIEVE AN IMAGE
        intent.setAction(Intent.ACTION_PICK);   //SETS ACTION TO ITEM SELECTION
        startActivityForResult(Intent.createChooser(intent, "Select Image Using:"), 1);
    }

    //GETS USER SELECTION
    public void onActivityResult(int requestCode, int resultCode, Intent data){

        if (resultCode == RESULT_OK){ //EXECUTES IF RESULT OF USER ACTION ON INTENT RETURNS OK STATUS

            if (requestCode == 1){

                Uri selectedImageUri = data.getData();     //GETS IMAGE URI FROM THE RETURNED INTENT DATA
                strSelectedImageUri = selectedImageUri.toString();  //SETS IMAGE URI TO STRING
                InputStream is;

                try {
                    is = getContentResolver().openInputStream(selectedImageUri);       //OPENS INPUT STREAM
                    Bitmap bit = BitmapFactory.decodeStream(is);    //DECODE THE INPUT STREAM AND STORES IN A BITMAP
                    ivNewUserProfilePic.setImageBitmap(bit);           //SETS PROFILE IMAGE TO THE RETURNED AND DECODED BITMAP
                    is.close();                                     //CLOSES INPUT STREAM
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    ivNewUserProfilePic.setImageBitmap(bitBackupImage);           //SETS PROFILE IMAGE TO THE DEFAULT BITMAP
                } catch (IOException e) {
                    e.printStackTrace();
                    ivNewUserProfilePic.setImageBitmap(bitBackupImage);           //SETS PROFILE IMAGE TO THE DEFAULT BITMAP
                }
            }else Toast.makeText(New_User_Registration.this, "Image Not Available", Toast.LENGTH_SHORT).show();
        }else Toast.makeText(New_User_Registration.this, "Unable to Access Image Selection", Toast.LENGTH_SHORT).show();

    }
    //endregion


    @Override
    protected void onResume() {
        super.onResume();
        navigationView.setCheckedItem(R.id.register_new);
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
        getMenuInflater().inflate(R.menu.new_user_registration, menu);
        return true;
    }

    @Override
    //HANDLES ACTION BAR MENU ITEM CLICK
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_logout) {

            au.userLogout(); //CALLS LOGOUT METHOD OF ACTIVEUSER CLASS SETTING ALL USER DETAILS TO DEFAULT

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
            Intent logoutIntent = new Intent(New_User_Registration.this, RandomRecipes.class);
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

        int id = item.getItemId(); //GETS ID OF SELECTED DRAWER ITEM

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
