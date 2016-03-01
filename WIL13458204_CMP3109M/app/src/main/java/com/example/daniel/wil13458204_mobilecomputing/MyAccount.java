package com.example.daniel.wil13458204_mobilecomputing;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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

public class MyAccount extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    //region GLOBAL VARIABLES
    private TextView tvActiveUser, tvHeaderTitle;
    private ImageView ivUserProfilePic;
    private Button btnChangePass, btnDeleteFavourites, btnClearAppData;
    private String strActiveUsername;
    private Integer intActiveUserID;
    private Boolean blKeyDeleted, blSingleIngDeleted, blMultiIngDeleted, blPrepTimeDeleted;
    private ActiveUser au;
    private static final String strSharedPrefsName = "MyPreferences";
    private Bitmap bitProfileImage;
    private SQLiteDatabaseAdapter myDatabaseAdapter;
    private InputStream is;
    private NavigationView navigationView;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //region PRE EXISTENT CODE
        setContentView(R.layout.activity_my_account);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //endregion

        //region CASTING
        btnChangePass = (Button) findViewById(R.id.btnChangePass);
        btnDeleteFavourites = (Button)findViewById(R.id.btnClearFavourites);
        btnClearAppData = (Button)findViewById(R.id.btnClearAppData);
        //endregion

        //region INSTANTIATIONS
        navigationView.setCheckedItem(R.id.my_account);
        au = ActiveUser.getInstance();
        strActiveUsername = au.getActiveUsername();
        intActiveUserID = Integer.parseInt(au.getActiveUserID());
        myDatabaseAdapter = new SQLiteDatabaseAdapter(this);
        Uri imageUri = au.getProfilePicUri();
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
            Toast.makeText(MyAccount.this, "Image Not Found", Toast.LENGTH_SHORT).show();
        }

        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ivUserProfilePic.setImageBitmap(bitProfileImage);
        //endregion

        if (strActiveUsername == "No Active User"){
            btnChangePass.setEnabled(false);
            //btnChangePass.setBackgroundResource(R.color.btnDisabledColor);
            btnChangePass.setBackgroundResource(R.drawable.custom_button_disabled_shape);
            btnChangePass.setTextColor(getResources().getColor(R.color.btnDisabledTextColor));

            btnDeleteFavourites.setEnabled(false);
            //btnDeleteFavourites.setBackgroundResource(R.color.btnDisabledColor);
            btnDeleteFavourites.setBackgroundResource(R.drawable.custom_button_disabled_shape);
            btnDeleteFavourites.setTextColor(getResources().getColor(R.color.btnDisabledTextColor));
        }else {
            btnChangePass.setEnabled(true);
            btnChangePass.setBackgroundResource(R.drawable.custom_button_shape);
            btnChangePass.setTextColor(getResources().getColor(R.color.colorText));

            btnDeleteFavourites.setEnabled(true);
            btnDeleteFavourites.setBackgroundResource(R.drawable.custom_button_shape);
            btnDeleteFavourites.setTextColor(getResources().getColor(R.color.colorText));

        }
    }

    //STARTS CHANGE PASSWORD ACTIVITY PASSING ACTIVE USERNAME AS EXTRA
    public void ChangePassword(View view){
        Intent passIntent = new Intent(MyAccount.this, ChangeResetPassword.class);
        passIntent.putExtra("Username", strActiveUsername);
        startActivity(passIntent);
    }

    public void ClearMyFavourites(View view){

        //CONSTRUCTS ALERT DIALOG
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to remove your favourites?\n\nThey will no longer be accessible")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        int intCount = myDatabaseAdapter.dbDeleteFavourites(intActiveUserID); //DELETES ALL FAVOURITES FOR ACTIVE USER

                        Toast.makeText(MyAccount.this, intCount + " items removed", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void ClearSearchHistory(View view){

        //CONSTRUCTS ALERT DIALOG
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to remove all search history?\n\nSuggestions will no longer appear for searches")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        //GETS PATH TO THE CREATED FILES
                        File fileDirectory = getFilesDir();

                        //GETS FILE
                        File fKeywordFile = new File(fileDirectory, "Keywords_Search_History");
                        File fSIngredientFile = new File(fileDirectory, "Single_Ingredient_Search_History");
                        File fMIngredientFile = new File(fileDirectory, "Multi_Ingredient_Search_History");
                        File fPreparationTimeFile = new File(fileDirectory, "Preparation_Time_Search_History");

                        //DELETES FILES FROM THE DEVICE
                        blKeyDeleted = fKeywordFile.delete();
                        blSingleIngDeleted = fSIngredientFile.delete();
                        blMultiIngDeleted = fMIngredientFile.delete();
                        blPrepTimeDeleted = fPreparationTimeFile.delete();

                        if (blKeyDeleted && blSingleIngDeleted && blMultiIngDeleted && blPrepTimeDeleted)
                            Toast.makeText(MyAccount.this, "All Search History Deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

    }

    public void ClearAppData(View view){

        //CONSTRUCTS ALERT DIALOG
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to clear all app data?\n\nAll user accounts, favourites and stored recipes will be lost")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        int intCount = myDatabaseAdapter.dbClearAppData(); //DELETES ALL APP DATA
                        LogOut(); //CALLS LOGOUT METHOD
                        Toast.makeText(MyAccount.this, intCount + " items removed", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();   //CLOSES ALERT DIALOG
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void LogOut(){

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
        Intent logoutIntent = new Intent(MyAccount.this, RandomRecipes.class);
        logoutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(logoutIntent);
        finish();
        //endregion
    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationView.setCheckedItem(R.id.my_account);
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
        getMenuInflater().inflate(R.menu.my_account, menu);
        return true;
    }

    @Override
    //HANDLES ACTION BAR MENU ITEM CLICK
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_logout) {

            LogOut(); //CALLS LOGOUT METHOD

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
