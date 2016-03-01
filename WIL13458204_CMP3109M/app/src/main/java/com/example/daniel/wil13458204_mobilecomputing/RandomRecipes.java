package com.example.daniel.wil13458204_mobilecomputing;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;

//########################################################################################################
//# CONNECTION LISTENER, IF CONNECTION LOST DISABLE LOAD MORE BUTTON AND ONCLICK LISTENER FOR LISTVIEW,
//#     DISPLAY WARNING ALERT STATING ONLY RECIPES STORED IN FAVS WILL BE ACCESSIBLE
//# CONNECTION LISTENER, IF CONNECTION MADE CHECK LOAD MORE BUTTON COUNT IF 5 LEAVE DISABLED ELSE ENABLE,
//#     AND ENABLE THE LISTVIEW ONCLICK LISTENER.
//########################################################################################################

public class RandomRecipes extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    //region GLOBAL VARIABLES
    private TextView tvActiveUser, tvHeaderTitle;
    private ImageView ivUserProfilePic;
    private ProgressBar progbarLoading;
    private Button btnLoadMore;
    private ArrayList<String> recipes = new ArrayList<String>();
    private ArrayList<Integer> prepTimeArray = new ArrayList<Integer>();
    private ArrayList<Integer> recipeID = new ArrayList<Integer>();
    private ArrayList<Bitmap> recipeImages = new ArrayList<Bitmap>();
    //ArrayList<String> recipeImageUrl = new ArrayList<String>();
    private Integer intRandSelectTime, intRandNumber, intPageCount = 0, intPageStart, intPageLimit, intSelectedRecipeID, intStatusCode;
    private String strJsonTest, strYourServiceUrl, strJson, strSelectedRecipe, strImageUrl, strActiveUsername, strRecipeName;
    private Boolean blIsConnected = false;
    private Bitmap bitRecipeImage, bitProfileImage, bitBackUpImage;
    private static final String strSharedPrefsName = "MyPreferences";
    private StringManipulation sm = new StringManipulation();
    private ActiveUser au;
    private InputStream is;
    private HttpURLConnection httpCon;
    private NavigationView navigationView;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //region PRE EXISTENT CODE
        setContentView(R.layout.activity_random_recipes);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //endregion

        //region NAVIGATION DRAWER CONTROL AND LISTENER
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //endregion

        //region CASTING
        progbarLoading = (ProgressBar) findViewById(R.id.progbarLoading);
        btnLoadMore = (Button) findViewById(R.id.btnLoadMore);
        bitBackUpImage = BitmapFactory.decodeResource(getResources(), R.drawable.carrot);
        //endregion

        //region INSTANTIATION
        navigationView.setCheckedItem(R.id.Home);
        au = ActiveUser.getInstance();
        strActiveUsername = au.getActiveUsername();
        String strUserID = au.getActiveUserID();
        Uri imageUri = au.getProfilePicUri();

        intPageLimit = 20;
        prepTimeArray.add(40);
        prepTimeArray.add(50);
        prepTimeArray.add(60);
        prepTimeArray.add(70);
        prepTimeArray.add(80);

        blIsConnected = CheckConnection();
        //endregion

        //region SET NAVIGATION HEADER VIEWS
        View header = navigationView.getHeaderView(0);
        tvActiveUser = (TextView) header.findViewById(R.id.tvActiveUsername);
        tvHeaderTitle = (TextView) header.findViewById(R.id.tvHeaderTitle);
        ivUserProfilePic = (ImageView) header.findViewById(R.id.ivHeaderImageView);
        tvHeaderTitle.setText(getString(R.string.HeaderTitle));
        tvActiveUser.setText(strActiveUsername);

        try {
            is = getContentResolver().openInputStream(imageUri);
            bitProfileImage = BitmapFactory.decodeStream(is);
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
            Toast.makeText(RandomRecipes.this, "Image Not Fount", Toast.LENGTH_SHORT).show();
        }

        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ivUserProfilePic.setImageBitmap(bitProfileImage);
        //endregion

        //region LISTVIEW ON CLICK LISTENER AND EVENT HANDLER
        ListView recipeList = (ListView) findViewById(R.id.recipeList);
        recipeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //region ONCLICK CODE
                if (CheckConnection()) {
                    blIsConnected = true;
                    intSelectedRecipeID = recipeID.get(position);
                    strSelectedRecipe = recipes.get(position);

                    Intent getRecipeIntent = new Intent(RandomRecipes.this, GetRecipe.class);
                    getRecipeIntent.putExtra("RecipeID", intSelectedRecipeID.toString());
                    getRecipeIntent.putExtra("RecipeName", strSelectedRecipe);
                    startActivity(getRecipeIntent);
                }else{
                    NoConnectionDialog();
                    blIsConnected = false;
                }
                //endregion
            }
        });
        //endregion

        if (blIsConnected) {

            //region VARIABLE INSTANTIATION
            intPageCount = 1;
            intPageStart = 1;

            //SELECT A RANDOM PREPARATION TIME AND REMOVE SELECTED FROM ARRAY
            Random r = new Random();
            intRandNumber = r.nextInt(4 - 0) + 0; //RANDOM NUMBER BETWEEN 0 AND 4
            intRandSelectTime = prepTimeArray.get(intRandNumber);
            prepTimeArray.remove(prepTimeArray.get(intRandNumber));

            //INSTANTIATION OF THE API REQUEST URL
            strYourServiceUrl = "http://api.campbellskitchen.com/brandservice.svc/api/search?preptime=" + intRandSelectTime.toString() + "&start=" + intPageStart + "&total=" + intPageLimit +
                    "&format=json&app_id=287ff47e&app_key=a37085350d61741dca2ce6a8ab070c72";
            //endregion

            new AsyncTaskParseJson().execute(); //EXECUTION OF A NEW JSON PARSE ASYNC TASK

        } else {
            NoConnectionDialog(); //SHOWS NO CONNECTION DIALOG

            //GET LAST RECEIVED JSON STRING FROM SHARED PREFERENCSE AND EXECUTE ASYC TASK
            SharedPreferences myPreferences = getSharedPreferences(strSharedPrefsName, Context.MODE_PRIVATE);
            strJson = myPreferences.getString("LastReturedJson", null);

            new AsyncTaskParseJson().execute();
        }
    }

    public Boolean CheckConnection(){
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE); //CREATE A NEW CONNECTION MANAGER
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();  //GETS THE INFORMATION OF THE ACTIVE NETWORK

        return activeNetwork != null && activeNetwork.isConnectedOrConnecting(); //RETURNS TRUE IS THERE IS A CONNECTION OT A CONNECTING PROCESS
    }

    public void NoConnectionDialog(){
        //CREATES AND SHOWS AN ALERT DIALOG
        AlertDialog alertNoActiveUser = new AlertDialog.Builder(this).create();
        alertNoActiveUser.setTitle("LIMITED USE!\nNo Connection");
        alertNoActiveUser.setMessage("With no Internet connection you are limited to viewing recipes in your favourites.\n\nPlease connect to the Internet for a full feature set");

        alertNoActiveUser.setButton(AlertDialog.BUTTON_NEUTRAL, "Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();    //CLOSES ALERT DIALOG
            }
        });
        alertNoActiveUser.show();
    }

    public void LoadMore_Recipes(View view) {

        if (CheckConnection()) {    //CHECKS FOR AN ACTIVE CONNECTION

            //CLEARS THE DEFAULT LAST RETURNED JSON RECIPES IF APP STARTS WITH NO CONNECTION
            if (intPageCount < 1){
                recipes.clear();
                recipeImages.clear();
                recipeID.clear();
            }

            //region VARIABLE INSTANTIATION
            blIsConnected = true;
            strYourServiceUrl = "";
            intPageCount++;
            intPageStart = 1;

            //CALCULATES NEW RANDOM NUMBER AND SELECTS THE RELATED PREPERATION TIME FROM THE ARRAY
            Random r = new Random();
            intRandNumber = r.nextInt(prepTimeArray.size() - 0) + 0;
            intRandSelectTime = prepTimeArray.get(intRandNumber);
            prepTimeArray.remove(prepTimeArray.get(intRandNumber));

            //CREATES NEW STRING FOR THE API QUERY
            strYourServiceUrl = "http://api.campbellskitchen.com/brandservice.svc/api/search?preptime=" + intRandSelectTime.toString() + "&total=" + intPageLimit +
                    "&format=json&app_id=287ff47e&app_key=a37085350d61741dca2ce6a8ab070c72";
            //endregion

            new AsyncTaskParseJson().execute(); //EXECUTES A NEW JSON PARSE ASYNC TASK

            //DEACTIVATES BUTTON IF ALL RECIPE PAGES HAVE BEEN LOADED
            if (intPageCount == 5) {
                btnLoadMore.setEnabled(false);
                //btnLoadMore.setBackgroundResource(R.color.btnDisabledColor);
                btnLoadMore.setBackgroundResource(R.drawable.custom_button_disabled_shape);
                btnLoadMore.setTextColor(getResources().getColor(R.color.btnDisabledTextColor));
            }

        }else {
            NoConnectionDialog();   //SHOWS NO CONNECTION DIALoG
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationView.setCheckedItem(R.id.Home);
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
        getMenuInflater().inflate(R.menu.random_recipes, menu);
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
            Intent logoutIntent = new Intent(RandomRecipes.this, RandomRecipes.class);
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
        } else if (id == R.id.search_recipe) {
            startActivity(new Intent(this, SearchRecipe.class));
        } else if (id == R.id.my_favourites) {
            startActivity(new Intent(this, MyFavourites.class));
        } else if (id == R.id.user_login) {
            startActivity(new Intent(this, UserLogin.class));
        } else if (id == R.id.register_new) {
            startActivity(new Intent(this, New_User_Registration.class));
        } else if (id == R.id.my_account) {
            startActivity(new Intent(this, MyAccount.class));
        }

        //CLOSES DRAWER WHEN SELECTION MADE
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    public class AsyncTaskParseJson extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            progbarLoading.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... arg0) {

            if(blIsConnected) { //EXECUTED IF THERE IS AN ACTIVE CONNECTION

                //region RETRIEVE AND PARSE JSON
                try {

                    //CREATES NEW INSTANCE OF HttpConnect CLASS
                    httpConnect jParser = new httpConnect();

                    //GET JSON STRING FROM PROVIDED URL
                    strJson = jParser.getJSONFromUrl(strYourServiceUrl);

                    //CREATES A NEW JSON OBJECT FROM THE PARSED JSON STRING
                    JSONObject jsonObject = new JSONObject(strJson);

                    //GETS THE JSON ARRAY TITLED "RESULTS" FROM THE CREATED JSON OBJECT
                    JSONArray jsonArray = jsonObject.getJSONArray("recipes");

                    //LOOPS THROUGH THE JSON ARRAY ADDING THE RECIPE DETAILS TO THE SPECIFIED VARIABLES
                    for (int i = 0; i < jsonArray.length(); i++) {

                        //BREAKS OUT OF LOOP IF CONNECTION IS LOST WHILST PROCESSING THE RETURNED JSON
                        if (!CheckConnection())
                            break;

                        //CREATES A JSON OBJECT FOR THE ITEM AT THE CURRENT POSITION
                        JSONObject json_message = jsonArray.getJSONObject(i);

                        if (json_message != null) {  //CHECKS THAT THE RETURNED JSON OBJECT HOLDS DATA

                            //region RETRIEVES RECIPE NAME
                            try {
                                strRecipeName = json_message.getString("name");
                            } catch (JSONException e) {
                                e.printStackTrace();
                                strRecipeName = "not found";
                            }

                            strRecipeName = sm.StringManipulations(strRecipeName);  //MAIPULATES RETURNED STRING REMOVING UNWANTD ELEMENTS
                            recipes.add(strRecipeName);                             //ADDS RECIPE NAME TO ARRAY
                            //endregion

                            //region RETRIEVES RECIPE ID
                            try {
                                recipeID.add(json_message.getInt("recipe_id"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            //endregion

                            //region RETRIEVES RECIPE IMAGE
                            try {
                                strImageUrl = (json_message.getString("recipelink")); //RETRIEVES THE IMAGE URL FROM THE JSON OBJECT
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            if (strImageUrl.length() != 0) {    //EXECUTED IF IMAGE URL IS NOT EMPTY

                                URL u = new URL(strImageUrl);   //CREATES A NEW URL WITH THE ADDRESS OF THE RECIPE IMAGE

                                if (CheckConnection()) {
                                    try {
                                        httpCon = (HttpURLConnection) u.openConnection();   //CREATES AND OPENS THE CONNECTION TO THE IMAGE URL
                                        intStatusCode = httpCon.getResponseCode();          //RETRIEVES THE RESPONSE CODE FROM THE URL REQUEST
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        intStatusCode = 201;
                                    }
                                 }else intStatusCode = 201;

                                InputStream is;

                                if (intStatusCode != 200) {                             //EXECUTED IF CONNECTION NOT ESTABLISHED SUCCESSFULLY
                                    recipeImages.add(bitBackUpImage);                   //RECIPE IMAGE SET TO THE DEFAULT IMAGE
                                } else {
                                    if(CheckConnection()) {
                                        try {
                                            is = httpCon.getInputStream();                      //RETRIEVES THE IMAGE FROM THE URL
                                            bitRecipeImage = BitmapFactory.decodeStream(is);    //DECODES THE IMAGE AND STORED IN A BITMAP FORMAT
                                            recipeImages.add(bitRecipeImage);                   //ADDS RECIPE IMAGE TO ARRAY
                                            is.close();                                         //CLOSED THE INPUT STREAM
                                            httpCon.disconnect();                               //DISCONNECTS HTTP CONNECTION
                                        }catch (IOException e){
                                            e.printStackTrace();
                                            recipeImages.add(bitBackUpImage);
                                        }
                                    }
                                    else {
                                        recipeImages.add(bitBackUpImage);
                                    }
                                }
                            }else{
                                recipeImages.add(bitBackUpImage);                       //ADDS DEFAULT IMAGE TO ARRAY
                            }
                            //endregion
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                //endregion

            }else if(!blIsConnected){

                //region PARSES STORED JSON FILE
                try {

                    //CREATES A NEW JSON OBJECT FROM THE PARSED JSON STRING
                    JSONObject jsonObject = new JSONObject(strJson);

                    //GETS THE JSON ARRAY TITLED "RESULTS" FROM THE CREATED JSON OBJECT
                    JSONArray jsonArray = jsonObject.getJSONArray("recipes");

                    //LOOPS THROUGH THE JSON ARRAY ADDING THE RECIPE DETAILS TO THE SPECIFIED VARIABLES
                    for (int i = 0; i < jsonArray.length(); i++) {


                        //CREATES A JSON OBJECT FOR THE ITEM AT THE CURRENT POSITION
                        JSONObject json_message = jsonArray.getJSONObject(i);

                        if (json_message != null) {  //CHECKS THAT THE RETURNED JSON OBJECT HOLDS DATA

                            //region RETRIEVES RECIPE NAME
                            try {
                                strRecipeName = json_message.getString("name");
                            } catch (JSONException e) {
                                e.printStackTrace();
                                strRecipeName = "not found";
                            }

                            strRecipeName = sm.StringManipulations(strRecipeName);  //MAIPULATES RETURNED STRING REMOVING UNWANTD ELEMENTS
                            recipes.add(strRecipeName);                             //ADDS RECIPE NAME TO ARRAY
                            //endregion

                            //region RETRIEVES RECIPE ID
                            try {
                                recipeID.add(json_message.getInt("recipe_id"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            //endregion

                            //region RETRIEVES RECIPE IMAGE
                            try {
                                strImageUrl = (json_message.getString("recipelink")); //RETRIEVES THE IMAGE URL FROM THE JSON OBJECT
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            if (strImageUrl.length() != 0) {    //EXECUTED IF IMAGE URL IS NOT EMPTY

                                URL u = new URL(strImageUrl);   //CREATES A NEW URL WITH THE ADDRESS OF THE RECIPE IMAGE

                                if (CheckConnection()) {
                                    httpCon = (HttpURLConnection) u.openConnection(); //CREATES AND OPENS THE CONNECTION TO THE IMAGE URL
                                    intStatusCode = httpCon.getResponseCode();  //RETRIEVES THE RESPONSE CODE FROM THE URL REQUEST
                                }else intStatusCode = 201;

                                InputStream is;

                                if (intStatusCode != 200) {                             //EXECUTED IF CONNECTION NOT ESTABLISHED SUCCESSFULLY
                                    recipeImages.add(bitBackUpImage);                   //RECIPE IMAGE SET TO THE DEFAULT IMAGE
                                } else {
                                    if(CheckConnection()) {
                                        is = httpCon.getInputStream();                      //RETRIEVES THE IMAGE FROM THE URL
                                        bitRecipeImage = BitmapFactory.decodeStream(is);    //DECODES THE IMAGE AND STORED IN A BITMAP FORMAT
                                        recipeImages.add(bitRecipeImage);                   //ADDS RECIPE IMAGE TO ARRAY
                                        is.close();                                         //CLOSED THE INPUT STREAM
                                        httpCon.disconnect();                               //DISCONNECTS HTTP CONNECTION
                                    }
                                    else {
                                        recipeImages.add(bitBackUpImage);
                                    }
                                }
                            }else{
                                recipeImages.add(bitBackUpImage);                       //ADDS DEFAULT IMAGE TO ARRAY
                            }
                            //endregion
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                //endregion
            }
            return null;
        }

        @Override
        // below method will run when service HTTP request is complete, will then bind tweet text in arrayList to ListView
        protected void onPostExecute(String strFromDoInBg) {
            progbarLoading.setVisibility(View.INVISIBLE);

            //region PASS DATA TO CUSTOM LIST VIEW ADAPTER
            ListView list = (ListView) findViewById(R.id.recipeList);
            Custom_ListView_Adapter custom_listView_adapter = new Custom_ListView_Adapter(RandomRecipes.this, recipes, recipeImages);
            list.setAdapter(custom_listView_adapter);
            //endregion

            //region SAVES LAST RETURNED JSON TO SHARD PREFERENCES
            if (strJson != null){
                SharedPreferences myPreferences = getSharedPreferences(strSharedPrefsName, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor;
                editor = myPreferences.edit();

                editor.putString("LastReturedJson", strJson);
                editor.commit();
            }
            //endregion
        }
    }
}

