package com.example.daniel.wil13458204_mobilecomputing;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MyFavourites extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    //region GLOBAL VARIABLES
    private TextView tvActiveUser, tvHeaderTitle, tvMessage;
    private ImageView ivUserProfilePic;
    private ListView lvFavouriteslist;
    private ProgressBar spProgSpinner;
    private String strActiveUsername, strReturnedJson, strJson, strImageUrl, strRecipeName, strSelectedRecipe, strSelectedRecipeJson;
    private Integer intActiveUserID, intSelectedRecipeID, intStatusCode;
    private Bitmap bitBackupImage, bitRecipeImage, bitProfileImage;
    private ArrayList<Integer> arFavRecipeIDs = new ArrayList<Integer>();
    private ArrayList<String> arFaveRecipeJson = new ArrayList<String>();
    private ArrayList<Bitmap> arFavRecipeImages = new ArrayList<Bitmap>();
    private ArrayList<String> arFavRecipeNames = new ArrayList<String>();
    private ActiveUser au;
    private InputStream is;
    private HttpURLConnection httpCon;
    private SQLiteDatabaseAdapter myDatabaseAdapter;
    private StringManipulation sm;
    private static final String strSharedPrefsName = "MyPreferences";
    private NavigationView navigationView;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //region PRE EXISTENT CODE
        setContentView(R.layout.activity_my_favourites);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //endregion

        //region NAVIGATION CONTROL AND LISTENER
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //endregion

        //region CASTING
        spProgSpinner = (ProgressBar)findViewById(R.id.spProgSpinner);
        tvMessage = (TextView)findViewById(R.id.tvMessage);
        lvFavouriteslist = (ListView)findViewById(R.id.lvRecipeList);
        //endregion

        //region INSTANTIATIONS
        navigationView.setCheckedItem(R.id.my_favourites);
        au = ActiveUser.getInstance();
        strActiveUsername = au.getActiveUsername();
        intActiveUserID = Integer.parseInt(au.getActiveUserID());
        Uri imageUri = au.getProfilePicUri();
        sm = new StringManipulation();
        myDatabaseAdapter = new SQLiteDatabaseAdapter(this);
        bitBackupImage = BitmapFactory.decodeResource(getResources(), R.drawable.carrot);

        //endregion

        spProgSpinner.setVisibility(View.INVISIBLE);
        tvMessage.setVisibility(View.VISIBLE);

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
            Toast.makeText(MyFavourites.this, "Image Not Fount", Toast.LENGTH_SHORT).show();
        }

        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ivUserProfilePic.setImageBitmap(bitProfileImage);
        //endregion

        new AsyncGetUserFavourites().execute(); //STARTS NEW JSON ASYNC TASK

        //region LISTVIEW CLICK EVENT
        lvFavouriteslist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                strSelectedRecipe = arFavRecipeNames.get(position);
                strSelectedRecipeJson = arFaveRecipeJson.get(position);
                intSelectedRecipeID = arFavRecipeIDs.get(position);

                Intent recipeIntent = new Intent(MyFavourites.this, GetRecipe.class);
                recipeIntent.putExtra("RecipeName", strSelectedRecipe);
                recipeIntent.putExtra("RecipeID", intSelectedRecipeID.toString());
                recipeIntent.putExtra("FromFavs", true);
                recipeIntent.putExtra("Json", strSelectedRecipeJson);
                startActivity(recipeIntent);
            }
        });
        //endregion

    }

    public Boolean CheckConnection(){
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationView.setCheckedItem(R.id.my_favourites);
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
        getMenuInflater().inflate(R.menu.my_favourites, menu);
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
            Intent logoutIntent = new Intent(MyFavourites.this, RandomRecipes.class);
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

    public class AsyncGetUserFavourites extends AsyncTask<String, String, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            spProgSpinner.setVisibility(View.VISIBLE);  //SHOWS SPINNING PROGRESS BAR
            tvMessage.setVisibility(View.INVISIBLE);    //HIDES TEXT VIEW
        }

        @Override
        protected String doInBackground(String... params) {

            //QUERY DATABASE RETRIEVING AN ARRAY LIST CONTAINING ALL USER FAVOURITES
            arFavRecipeIDs = myDatabaseAdapter.dbGetUserFavourites(intActiveUserID);

            //GET JSON FOR EACH RECIPE IN USERS FAVOURITES
            for (int i = 0; i <= arFavRecipeIDs.size()-1; i++){

                strReturnedJson = myDatabaseAdapter.dbGetRecipeDetails(arFavRecipeIDs.get(i)); //GETS JSON STRING FOR RECIPE ID AT CURRENT POSITION IN THE RECIPE ID ARRAY LIST
                arFaveRecipeJson.add(strReturnedJson); //ADDS JSON STRING TO AN ARRAY LIST
            }

            //PARSE THE JSON FOR EACH RECIPE IN THE FAVOURITES RETRIEVING THE RECIPE IMAGE AND NAME
            for (int i = 0; i <= arFavRecipeIDs.size() - 1; i++){

                strJson = arFaveRecipeJson.get(i);  //SES THE CURRENT JSON TO BE PARSED

                try {

                    //CREATE A JSON OBJECT OF THE JSON STRING
                    JSONObject joReceivedJson = new JSONObject(strJson);

                    //CREATE A JSON ARRAY FROM THE RETURNED OBJECT CONTAINING ALL RESULTS
                    JSONArray jaResults = joReceivedJson.getJSONArray("results");

                    //LOOPS THROUGH ARRAY OF RESULTS
                    for (int j = 0; j < jaResults.length(); j++){

                        //CREATES JSON OBJECT FOR THE CURRENT ARRAY POSITION
                        JSONObject joMessage = jaResults.getJSONObject(j);

                        if (joMessage != null){ //EXECUTED IF RETURNED OBJECT IS NOT NULL

                            //region RETRIEVES RECIPE IMAGE
                            try {
                                strImageUrl = joMessage.getString("thumbimg");  //RETRIEVES THE IMAGUE URL FROM THE JSON OBJECT
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            try {

                                if (strImageUrl.length() != 0){ //CHECKS THAT THE IMAGE URL IS NOT EMPTY

                                    URL u = new URL(strImageUrl); //CREATES A NEW URL WITH THE ADDRESS OF THE RECIPE IMAGE


                                    httpCon = (HttpURLConnection) u.openConnection(); //CREATES AND OPENS THE CONNECTION TO THE IMAGE URL

                                    if (CheckConnection()) {
                                        intStatusCode = httpCon.getResponseCode(); //RETRIEVES THE RESPONSE CODE FROM THE URL REQUEST
                                    }else intStatusCode = 201;

                                    InputStream is;

                                    if(intStatusCode != 200){                                   //EXECUTED IF CONNECTION NOT ESTABLISHED SUCCESSFULLY
                                        bitRecipeImage = bitBackupImage;                        //RECIPE IMAGE SET TO THE DEFAULT IMAGE
                                        arFavRecipeImages.add(bitRecipeImage);
                                    }else{
                                        if(CheckConnection()) {
                                            try {
                                                is = httpCon.getInputStream();                          //RETRIEVES THE IMAGE FROM THE URL
                                                bitRecipeImage = BitmapFactory.decodeStream(is);        //DECODES THE IMAGE AND STORED IN A BITMAP FORMAT
                                                arFavRecipeImages.add(bitRecipeImage);                  //ADDS IMAGE TO ARRAY LIST
                                                is.close();                                             //CLOSED THE INPUT STREAM
                                                httpCon.disconnect();                                   //DISCONNECTS HTTP CONNECTION

                                            }catch (IOException e){
                                                bitRecipeImage = bitBackupImage;                        //RECIPE IMAGE SET TO THE DEFAULT IMAGE
                                                arFavRecipeImages.add(bitRecipeImage);
                                            }
                                        }
                                        else{
                                            bitRecipeImage = bitBackupImage;
                                            arFavRecipeImages.add(bitRecipeImage);
                                        }
                                    }
                                }else {
                                    bitRecipeImage = bitBackupImage;                            //SETS IMAGE TO DEFAULT IN IMAGE URL IS EMPTY
                                    arFavRecipeImages.add(bitRecipeImage);                      //ADDS IMAGE TO ARRAY LIST
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            //endregion

                            //region RETRIEVES RECIPE NAME
                            try {
                                strRecipeName = joMessage.getString("name");
                            } catch (JSONException e) {
                                e.printStackTrace();
                                strRecipeName = "not found";
                            }

                            strRecipeName = sm.StringManipulations(strRecipeName);
                            arFavRecipeNames.add(strRecipeName);        //ADDS RECIPE NAME TO ARRAY LIST
                            //endregion
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            spProgSpinner.setVisibility(View.INVISIBLE);

            if (arFavRecipeNames.size() >= 1){ //EXECUTES IS THERE ARE RECIPES IN THE ARRAY LIST

                //CALLS CUSTOM ARRAY ADAPTER
                Custom_Listview_Adapter_MyFavs myAdapter = new Custom_Listview_Adapter_MyFavs(MyFavourites.this, arFavRecipeNames, arFavRecipeImages, arFavRecipeIDs);
                lvFavouriteslist.setAdapter(myAdapter);
            }else{
                tvMessage.setVisibility(View.VISIBLE);
            }



        }
    }
}
