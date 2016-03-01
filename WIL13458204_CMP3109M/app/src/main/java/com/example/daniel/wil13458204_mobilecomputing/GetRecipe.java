package com.example.daniel.wil13458204_mobilecomputing;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
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

//###########################################################################################################
//# CONNECTION LISTENER, IF CONNECTION LOST ON CREATION SHOW WARNING ALERT, CHECK IF RECIPE IS IN DATABASE
//#     IF SO SHOW,
//# CONNECTION LISTENER, IF CONNECTION ON CREATION SHOW RESULTS
//#
//# CHECK FOR INTENT STARTING ACTIVITY AND IF INTENT COMES FROM FAVOURITES SET THE STRJSON THROUGH INTENT
//#     AND USE IN
//############################################################################################################

public class GetRecipe extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    //region GLOBAL VARIABLES
    private String strJson, strYourServiceUrl, strRecipeName, strRecipeDiscription, strPrepTime, strCookTime, strTotalTime, strRecipeID,
            strMakes, strIngredients, strStepNumber, strInstruction, strImageUrl, strActiveUserID, strActiveUsername, strCalories, strTotalFat,
            strSaturat, strCholesterol, strSodium, strTotalCarb, strDietryFiber, strProtein, strVitaminA, strVitaminC, strCalcium, strIron, strNutrition;
    private Integer intRecipeID, intStatusCode;
    private ArrayList<String> arRecipeInstructions = new ArrayList<String>();
    private ArrayList<String> arRecipeIngredients = new ArrayList<String>();
    private Bitmap bitRecipeImage, bitProfileImage, bitBackupImage;
    private InputStream is;
    private Boolean blIsConnected = false, blFromFavs;
    private TextView tvRecipeName, tvRecipeDescription, tvPrepTime, tvCookTme, tvTotalTime, tvMakes, tvIngredients, tvInstructions, tvActiveUser, tvHeaderTitle, tvNurtition;
    private ImageView ivRecipeImage, ivUserProfilePic;
    private StringManipulation sm = new StringManipulation();
    private ActiveUser au;
    private SQLiteDatabaseAdapter myDatabaseAdapter;
    private static final String strSharedPrefsName = "MyPreferences";
    private HttpURLConnection httpCon;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //region PRE-EXISTENT CODE
        setContentView(R.layout.activity_get_recipe);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //endregion

        tvRecipeName = (TextView)findViewById(R.id.tvRecipeName);

        //region INSTANTIATIONS
        myDatabaseAdapter = new SQLiteDatabaseAdapter(this);
        au = ActiveUser.getInstance();
        strActiveUsername = au.getActiveUsername();
        bitBackupImage = BitmapFactory.decodeResource(getResources(), R.drawable.carrot);
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
            Toast.makeText(GetRecipe.this, "Image Not Found", Toast.LENGTH_SHORT).show();
        }

        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ivUserProfilePic.setImageBitmap(bitProfileImage);
        //endregion

        blIsConnected = CheckConnection();

        //CHECKING INTENT FOR EXTRAS TO DETERMINE THE INTENT INITIALISER (EITHER MY FAVOURITES, SEARCH OR RANDOM RECIPE)
        if (getIntent().getExtras() != null){

            //region GET INTENT EXTRAS
            strRecipeName = getIntent().getStringExtra("RecipeName");
            strRecipeID = getIntent().getStringExtra("RecipeID");
            blFromFavs = getIntent().getExtras().getBoolean("FromFavs");
            //endregion

            strRecipeName = sm.StringManipulations(strRecipeName);
            tvRecipeName.setText(strRecipeName);

            if (blFromFavs){    //EXECUTED IF FAVOURITES INITIATED THE INTENT
                //GETS THE STORED JSON STRING FOR THE RECIPE AND STARTS A NEW JSON PARSE ASYNC TASK
                strJson = getIntent().getExtras().getString("Json", null);
                new AsyncTaskGetRecipe().execute();
            }
            else {
                if(CheckConnection()) { //CHECKS NETWORK CONNECTION
                    //SETS API QUERY AND STARTS A NEW ASYNC JSON PARSE TASK
                    strYourServiceUrl = "http://api.campbellskitchen.com/brandservice.svc/api/recipeextended/" + strRecipeID + "?format=json&app_id=287ff47e&app_key=a37085350d61741dca2ce6a8ab070c72";
                    new AsyncTaskGetRecipe().execute();
                }else {
                    NoConnectionDialog(); //SHOWS NO CONNECTION DIALOG
                }
            }
        }
    }

    public Boolean CheckConnection(){
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);      //CREATE A NEW CONNECTION MANAGER
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();  //GETS THE INFORMATION OF THE ACTIVE NETWORK

        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();    //RETURNS TRUE IF THERE IS A CONNECTION OR A CONNECTING PROCESS
    }

    public void NoConnectionDialog(){
        //CREATES AND SHOWS AN ALERT DIALOG
        AlertDialog alertNoActiveUser = new AlertDialog.Builder(this).create();
        alertNoActiveUser.setTitle("LIMITED USE!\nNo Connection");
        alertNoActiveUser.setMessage("With no Internet connection you are limited to viewing recipes in your favourites.\n\nPlease connect to the Internet for a full feature set");

        alertNoActiveUser.setButton(AlertDialog.BUTTON_NEUTRAL, "Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();      //CLOSES ALERT DIALOG
            }
        });
        alertNoActiveUser.show();
    }

    public void SaveToFavourites(View view){

        ActiveUser au = ActiveUser.getInstance();
        Integer intActiveUserID = Integer.parseInt(au.getActiveUserID());
        Integer intRecipeID = Integer.parseInt(strRecipeID);

        //CHECKS THERE IS AN ACTIVE USER (ALL USERS HAVE A USER ID > 0)
        if (intActiveUserID != 0){

            //PERFORMS DATABASE QUERY THAT CHECKS USER DOES NOT ALREADY HAVE RECIPE IN FAVOURITES
            int intDuplicateInFavs = myDatabaseAdapter.dbCheckFavourites(intActiveUserID, intRecipeID);

            //EXECUTED IF RECIPE NOT FOUND WITHIN THE USERS FAVOURITES
            if (intDuplicateInFavs < 1){

                //PERFORMS DATABASE QUERY THAT CHECKS IF THE RECIPE JSON IS ALREADY STORED WITHIN THE DATABASE
                int intDuplicateRecipe = myDatabaseAdapter.dbCheckDuplicateRecipe(intRecipeID);

                //CHECKS THERE IS NO DUPLICATES
                if (intDuplicateRecipe < 1){

                    //ADDS RECIPE JSON TO RECIPE TABLE OF DATABASE
                    myDatabaseAdapter.dbAddToRecipes(intRecipeID, strRecipeName, strJson);

                }else Toast.makeText(GetRecipe.this, "RECIPE JSON ALREADY STORED", Toast.LENGTH_SHORT).show();

                //ADDS RECIPE TO THE ACTIVE USERS FAVOURITES IN DATABASE
                myDatabaseAdapter.dbAddToFavourites(intActiveUserID, intRecipeID);

                Toast.makeText(GetRecipe.this, "Recipe Successfully added to favourites", Toast.LENGTH_SHORT).show();

            }else Toast.makeText(GetRecipe.this, "Recipe already in favourites", Toast.LENGTH_SHORT).show();

        }else {
            //region ALERT DIALOG DETAILING NO ACTIVE USER AND INABILITY TO SAVE RECIPES TO FAVOURITES
            AlertDialog alertNoActiveUser = new AlertDialog.Builder(this).create();
            alertNoActiveUser.setTitle("Alert! No active user!");
            alertNoActiveUser.setMessage("There is no active user signed in.\n\nTo add to favourites please login\nto a registered Dan's Kitchen account");
            alertNoActiveUser.setButton(AlertDialog.BUTTON_NEUTRAL, "Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alertNoActiveUser.show();
            //endregion
        }
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
        getMenuInflater().inflate(R.menu.get_recipe, menu);
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

            strActiveUsername = au.getActiveUsername(); //GETS ACTIVE USERNAME FROM ACTIVEUSER CLASS
            tvActiveUser.setText(strActiveUsername);
            //ivUserProfilePic.setImageBitmap();

            //region STARTS RANDOM RECIPE ACTIVITY CLOSING THE ACTIVITY STACK PREVENTING UNAUTHORISED ACCESS TO ACCOUNT VIA THE BACK BUTTON
            Intent logoutIntent = new Intent(GetRecipe.this, RandomRecipes.class);
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

    public class AsyncTaskGetRecipe extends AsyncTask<String, String, String> {

        protected void JsonParsing(){

            try {

                //CREATES A NEW JSON OBJECT FROM THE PARSED JSON STRING
                JSONObject jsonObject = new JSONObject(strJson);

                //GETS THE JSON ARRAY TITLED "RESULTS" FROM THE CREATED JSON OBJECT
                JSONArray jsonArray = jsonObject.getJSONArray("results");

                //LOOPS THROUGH THE JSON ARRAY ADDING THE RECIPE DETAILS TO THE SPECIFIED VARIABLES
                for (int i = 0; i < jsonArray.length(); i++) {

                    //CREATES A JSON OBJECT FOR THE ITEM AT THE CURRENT POSITION
                    JSONObject json_message = jsonArray.getJSONObject(i);

                    if (json_message != null) { //CHECKS THAT THE RETURNED JSON OBJECT HOLDS DATA

                        //region RETRIEVES RECIPE IMAGE
                        try {
                            strImageUrl = (json_message.getString("mobilehdimg"));  //RETRIEVES THE IMAGE URL FROM THE JSON OBJECT
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        if (strImageUrl.length() != 0) {    //CHECKS THAT THE IMAGE URL IS NOT EMPTY

                            URL u = new URL(strImageUrl);   //CREATES A NEW URL WITH THE ADDRESS OF THE RECIPE IMAGE


                            if (CheckConnection()) {
                                try {
                                    httpCon = (HttpURLConnection) u.openConnection(); //CREATES AND OPENS THE CONNECTION TO THE IMAGE URL
                                    intStatusCode = httpCon.getResponseCode();     //RETRIEVES THE RESPONSE CODE FROM THE URL REQUEST
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    intStatusCode = 201;
                                }
                            }else intStatusCode = 201;

                            InputStream is;

                            if (intStatusCode != 200) {                             //EXECUTED IF CONNECTION NOT ESTABLISHED SUCCESSFULLY
                                bitRecipeImage = bitBackupImage;                    //RECIPE IMAGE SET TO THE DEFAULT IMAGE
                            } else {
                                if (CheckConnection()) {
                                    try {
                                        is = httpCon.getInputStream();                      //RETRIEVES THE IMAGE FROM THE URL
                                        bitRecipeImage = BitmapFactory.decodeStream(is);    //DECODES THE IMAGE AND STORED IN A BITMAP FORMAT
                                        is.close();                                         //CLOSED THE INPUT STREAM
                                        httpCon.disconnect();                               //DISCONNECTS HTTP CONNECTION
                                    }catch (IOException e){
                                        e.printStackTrace();
                                        bitRecipeImage = bitBackupImage;
                                    }
                                }
                                else{
                                    bitRecipeImage = bitBackupImage;
                                }
                            }
                        } else {
                            bitRecipeImage = bitBackupImage;                        //SETS IMAGE TO DEFAULT IF IMAGE URL IS EMPTY
                        }
                        //endregion

                        //region RETRIEVES RECIPE DESCRIPTION
                        try {
                            strRecipeDiscription = "Description:\n\n" + (json_message.getString("description"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            strRecipeDiscription = "Description:\n\n not found";
                        }

                        strRecipeDiscription = sm.StringManipulations(strRecipeDiscription);    //MANIPULATES STRING REMOVING UNWANTED INCLUSIONS
                        //endregion

                        //region RETRIEVES RECIPE TIMINGS AND SERVINGS
                        JSONObject glanceObject = json_message.getJSONObject("glance");
                        strPrepTime = "Prep: " + glanceObject.getString("prep");
                        strPrepTime = sm.TimingManipulation(strPrepTime);

                        //TRIES TO RETRIEVE A COOK TIME FOR THE RECIPE
                        try {
                            strCookTime = "Cook: " + glanceObject.getString("cook");
                            strCookTime = sm.TimingManipulation(strCookTime);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        if (strCookTime == null) { //EXECUTED IF NO COOK TIME IS FOUND
                            //TRIES TO RETRIEVE A BAKE TIME
                            try {
                                strCookTime = "Bake: " + glanceObject.getString("bake");
                                strCookTime = sm.TimingManipulation(strCookTime);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            if (strCookTime == null) { //EXECUTED IF NO COOK OR BAKE TIME FOUND
                                strCookTime = "Cook: N/A";
                            }
                        }

                        //RETRIEVES THE TOTAL RECIPE CONSTRUCTION TIME
                        try {
                            strTotalTime = "Total: " + glanceObject.getString("totaltime");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            strTotalTime = "Total: 0";
                        }

                        strTotalTime = sm.TimingManipulation(strTotalTime);

                        //RETRIEVES THE NUMBER OF SERVINGS THE RECIPE MAKES
                        try {
                            strMakes = "Servings: " + glanceObject.getString("make");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            strMakes = "Servinge: not found";
                        }
                        //endregion

                        //region RETRIEVES RECIPE INGREDIENTS
                        //GETS AND CREATES A JSON ARRAY WITH THE JSON TITLE "ingredients"
                        JSONArray ingredientsArray = json_message.getJSONArray("ingredients");

                        //LOOPS THROUGH THE INGREDIENTS ARRAY
                        for (int ing = 0; ing < ingredientsArray.length(); ing++) {

                            //CREATES A JSON OBJECT FOR THE INGREDIENT AT THE CURRENT ARRAY POSITION
                            JSONObject ingredientObject = ingredientsArray.getJSONObject(ing);

                            try {
                                strIngredients = ingredientObject.getString("ingredient");
                            } catch (JSONException e) {
                                e.printStackTrace();
                                strIngredients = "not found";
                            }

                            strIngredients = sm.StringManipulations(strIngredients);

                            arRecipeIngredients.add(strIngredients);    //ADDS THE INGREDIENT TO THE INGREDIENTS ARRAY LIST
                        }
                        //endregion

                        //region RETRIEVES RECIPE DIRECTIONS
                        //GETS AND CREATES A JSON ARRAY WITH THE JSON TITLE "steps"
                        JSONArray instructionsArray = json_message.getJSONArray("steps");

                        //LOOPS THROUGH THE DIRECTIONS ARRAY
                        for (int ins = 0; ins < instructionsArray.length(); ins++) {

                            //CREATES A JSON OBJECT FOR THE DIRECTION AT THE CURRENT ARRAY POSITION
                            JSONObject instructionsObject = instructionsArray.getJSONObject(ins);

                            try {
                                strStepNumber = instructionsObject.getString("number"); //RETRIEVES THE STEP NUMBER
                            } catch (JSONException e) {
                                e.printStackTrace();
                                strStepNumber = "n";
                            }

                            try {
                                strInstruction = instructionsObject.getString("step");  //RETRIEVES THE INSTRUCTION
                            } catch (JSONException e) {
                                e.printStackTrace();
                                strInstruction = "not found";
                            }

                            strInstruction = sm.StringManipulations(strInstruction);
                            strInstruction = strStepNumber + ": " + strInstruction; //CONSTRUCTS THE DIRECTION STRING

                            arRecipeInstructions.add(strInstruction);   //ADDS THE INSTRUCTION STRING TO THE ARRAY LIST
                        }
                        //endregion

                        //region RETRIEVES NUTRITIONAL DATA
                        JSONObject nutritionObject = json_message.getJSONObject("nutrition");

                        try {
                            strCalories = "Calories:  " + nutritionObject.getString("calories");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            strCalories = "Calories: not found";
                        }

                        try {
                            strTotalFat = "Fat:  " + nutritionObject.getString("totalfat");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            strTotalFat = "Fat: not found";
                        }

                        try {
                            strSaturat = "Saturated Fat:  " + nutritionObject.getString("saturatedfat");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            strSaturat = "Saturated Fat: not found";
                        }

                        try {
                            strCholesterol = "Cholesterol:  " + nutritionObject.getString("cholesterol");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            strCholesterol = "Cholesterol: not found";
                        }

                        try {
                            strSodium = "Sodium:  " + nutritionObject.getString("sodium");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            strSodium = "Sodium: not found";
                        }

                        try {
                            strTotalCarb = "Carbohydrate:  " + nutritionObject.getString("totalcarbohydrate");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            strTotalCarb = "Carbohydrate: not found";
                        }

                        try {
                            strDietryFiber = "Fiber:  " + nutritionObject.getString("dietaryfiber");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            strDietryFiber = "Fiber: not found";
                        }

                        try {
                            strProtein = "Protein:  " + nutritionObject.getString("protein");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            strProtein = "Protein: not found";
                        }

                        try {
                            strVitaminA = "Vitamin A:  " + nutritionObject.getString("vitamina");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            strVitaminA = "Vitamin A: not found";
                        }

                        try {
                            strVitaminC = "Vitamin C:  " + nutritionObject.getString("vitaminc");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            strVitaminC = "Vitamin C: not found";
                        }

                        try {
                            strCalcium = "Calcium:  " + nutritionObject.getString("calcium");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            strCalcium = "Calcium: not found";
                        }

                        try {
                            strIron = "Iron:  " + nutritionObject.getString("iron");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            strIron = "Iron: not found";
                        }
                        //endregion
                    }
                }

                strNutrition = "Nutrition:\n\n" + strCalories + "\n" + strTotalFat + "\n" + strSaturat + "\n" + strCholesterol + "\n" + strSodium + "\n" + strTotalCarb + "\n" +
                        strDietryFiber + "\n" + strProtein + "\n" + strVitaminA + "\n" + strVitaminC + "\n" + strCalcium + "\n" + strIron;

                //region CREATES INGREDIENTS STRING TO DISPLAY
                strIngredients = "Ingredients:\n\n";

                //LOOPS THROUGH RECIPE INGREDIENTS ARRAY LIST AND ADDS THE INGREDIENT TO THE STRING
                for (int i = 0; i < arRecipeIngredients.size(); i++) {
                    strIngredients += arRecipeIngredients.get(i).toString() + "\n\n";
                }

                strIngredients = strIngredients.trim(); //REMOVES THE END LINE BREAK FROM THE STRING
                //endregion

                //region CREATES INSTRUCTIONS STRING TO DISPLAY
                strInstruction = "Instructions:\n\n";

                //LOOPS THROUGH RECIPE INSTRUCTIONS ARRA LIST AND ADDS THE INSTRUCTIONS TO TH STRING
                for (int i = 0; i < arRecipeInstructions.size(); i++) {
                    strInstruction += arRecipeInstructions.get(i).toString() + "\n\n";
                }

                strInstruction = strInstruction.trim(); //REMOVES THE END LINE BREAK FROM THE STRING
                //endregion

            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            if (!blFromFavs) {  //EXECUTED IF INTENT WAS NOT MADE FROM MY_FAVOURITES ACTIVITY

                //region JSON RETRIEVAL AND PARSING FROM REST API
                try {

                    //CREATES NEW INSTANCE OF HttpConnect CLASS
                    httpConnect jParser = new httpConnect();

                    if (CheckConnection()) {
                        //GET JSON STRING FROM PROVIDED URL
                        strJson = jParser.getJSONFromUrl(strYourServiceUrl);

                        if (strJson != null) {
                            //CALLS JSON RETRIEVAL METHOD TO PARSE JSON FROM REST API
                            JsonParsing();
                        }
                    }else{
                        //ERROR DIALOG
                        NoConnectionDialog();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                //endregion
            }
            else{
                try {

                    //CALLS JSON RETRIEVAL METHOD TO PARSE JSON FROM DATABASE STORED JSON STRING
                    JsonParsing();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            //region COMPONENT INSTANTIATION
            tvRecipeDescription = (TextView)findViewById(R.id.tvRecipeDecription);
            tvPrepTime = (TextView)findViewById(R.id.tvPrepTime);
            tvCookTme = (TextView)findViewById(R.id.tvCookTime);
            tvTotalTime = (TextView)findViewById(R.id.tvTotalTime);
            tvMakes = (TextView)findViewById(R.id.tvMakes);
            tvIngredients = (TextView)findViewById(R.id.tvIngredients);
            tvInstructions = (TextView)findViewById(R.id.tvInstructions);
            tvNurtition = (TextView)findViewById(R.id.tvNutrition);
            ivRecipeImage = (ImageView)findViewById(R.id.ivRecipeImage);
            //endregion

            //region SETTING COMPONENT DATA
            tvRecipeDescription.setText(strRecipeDiscription);
            tvPrepTime.setText(strPrepTime);
            tvCookTme.setText(strCookTime);
            tvTotalTime.setText(strTotalTime);
            tvMakes.setText(strMakes);
            tvIngredients.setText(strIngredients);
            tvInstructions.setText(strInstruction);
            tvNurtition.setText(strNutrition);
            ivRecipeImage.setImageBitmap(bitRecipeImage);
            //endregion
        }
    }
}
