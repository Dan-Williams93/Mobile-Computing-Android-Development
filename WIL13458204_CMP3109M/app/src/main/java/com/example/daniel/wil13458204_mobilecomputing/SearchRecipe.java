package com.example.daniel.wil13458204_mobilecomputing;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Collections;

//##############################################################################################################
//# CONNECTION LISTENER, IF CONNECTION LOST DISABLE ONCLICK LISTENER FOR LISTVIEW AND SEARCH BUTTON,
//#     DISPLAY WARNING ALERT STATING ONLY RECIPES STORED IN FAVS WILL BE ACCESSIBLE
//# CONNECTION LISTENER, IF CONNECTION MADE ENABLE SEARCH BUTTON AND ENABLE THE LISTVIEW ONCLICK LISTENER.
//##############################################################################################################

public class SearchRecipe extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    //region GLOBAL VARIABLES
    private TextView SearchCridentialMessage1, SearchCridentialMessage2, tvActiveUser, tvHeaderTitle;
    private ImageView ivUserProfilePic;
    private ListView recipeList;
    private EditText searchInput;
    private Spinner spSearchSpinner;
    private Button btnSearch;
    private ProgressBar progbarLoading;
    private String strSearchInput, strSearchString, strJson, strYourServiceUrl, strSpinnerSelection, strSelectedRecipe, strImageUrl, strRecipeName, strActiveUsername, strFile;
    private Bitmap bitRecipeImage, bitBackUpImage, bitProfileImage;
    private ArrayList<String> recipes = new ArrayList<String>();
    private ArrayList<Integer> recipeID = new ArrayList<Integer>();
    private ArrayList<Bitmap> recipeImages = new ArrayList<Bitmap>();
    private ArrayList<String> arHistory = new ArrayList<String>();
    private Integer intSearchNum, intSelectedRecipeID, intstatusCode;
    private StringManipulation sm = new StringManipulation();
    private static final String strSharedPrefsName = "MyPreferences";
    private ActiveUser au;
    private InputStream is;
    private HttpURLConnection httpCon;
    private PopupWindow popupWindow;
    private PopupWindow puSearchHistory;
    private NavigationView navigationView;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //region PRE EXISTENT CODE
        setContentView(R.layout.activity_search_recipe);
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
        SearchCridentialMessage1 = (TextView)findViewById(R.id.tvSearchMessage1);
        SearchCridentialMessage2 = (TextView)findViewById(R.id.tvSearchMessage2);
        recipeList = (ListView)findViewById(R.id.lstRecipes);
        searchInput = (EditText) findViewById(R.id.etSearchInput);
        btnSearch = (Button)findViewById(R.id.btnSearch);
        progbarLoading = (ProgressBar)findViewById(R.id.probarSpinner);
        spSearchSpinner = (Spinner)findViewById(R.id.SearchSpinner);
        //endregion

        //region INSTANTIATION
        navigationView.setCheckedItem(R.id.search_recipe);
        au = ActiveUser.getInstance();
        strActiveUsername = au.getActiveUsername();
        Uri imageUri = au.getProfilePicUri();
        SearchCridentialMessage1.setText("No Search Credentials Entered");
        SearchCridentialMessage2.setText("Please Select the Search Type and Input Credentials");
        SearchCridentialMessage1.setVisibility(View.VISIBLE);
        SearchCridentialMessage2.setVisibility(View.VISIBLE);
        recipeList.setVisibility(View.INVISIBLE);
        bitBackUpImage = BitmapFactory.decodeResource(getResources(), R.drawable.carrot);
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
            Toast.makeText(SearchRecipe.this, "Image Not Fount", Toast.LENGTH_SHORT).show();
        }

        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ivUserProfilePic.setImageBitmap(bitProfileImage);
        //endregion

        //EXECUTED IF SPINNER IS TAPPED
        spSearchSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                searchInput.clearFocus();   //RELEASES THE FOCUS FROM THE SEARCH EDIT TEXT ALLOWING SEARCH HISTORY TO BE RESET
                return false;
            }
        });

        //HANDLES TEXT CHANGE EVENT OF SEARCH EDIT TEXT
        searchInput.addTextChangedListener(new TextWatcher() {

            @Override
            //UNUSED
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                //EXECUTED IF CHARACTER ENTERED IS THE FIRST IN THE EDIT TEXT
                if (start == 0 && before == 0) {
                    //EXECUTED IF THERE IS AN ACTIVE SEARCH HISTORY POPUP
                    if (puSearchHistory != null) {
                        //EXECUTED IF THERE IS CURRENTLY NO SHOWING SEARCH HISTORY POPUP
                        if (!puSearchHistory.isShowing()) {
                            GetSearchHistory(); //CREATES AND SHOWS A NEW SEARCH HISTORY POPUP IF THERE IS NOT ONE ALREADY SHOWING
                        }
                    }
                }

                //EXECUTED IF THE EDIT TEXT BECOMES EMPTY
                if (start == 0 && before == 1 && count == 0) {
                    if (puSearchHistory != null) {
                        puSearchHistory.dismiss();
                    }
                }
            }

            @Override
            //UNUSED
            public void afterTextChanged(Editable s) {
            }
        });

        //EXECUTED WHEN FOCUS OF SEARCH EDIT TEXT CHANGES
        searchInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                //EXECUTED IS THE EDIT TEXT LOSES THE FOCUS
                if (!hasFocus) {
                    //EXECUTED IF THERE IS AN ACTIVE SEARCH HISTORY POPUP
                    if (puSearchHistory != null) {
                        puSearchHistory.dismiss();  //CLOSES ACTIVE SEARCH HISTORY POPUP
                    }
                    HideKeyboard();         //CLOSES THE VIRTUAL KEYBOARD
                } else {
                    GetSearchHistory();     //CREATES AND SHOWS NEW SEARCH HISTORY POPUP
                }

            }
        });

            //region LIST VIEW CLICK HANDLER
        recipeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (CheckConnection()) {    //EXECUTED IS THERE IS AN ACTIVE NETWORK CONNECTION
                    intSelectedRecipeID = recipeID.get(position);       //GETS RECIPE ID OF SELECTED RECIPE
                    strSelectedRecipe = recipes.get(position);          //GETS RECIPE NAME OF SELECTED RECIPE

                    //CREATES NEW INTENT PASSING THE RECIPE ID AND NAME OF THE SELECTED RECIPE
                    Intent getRecipeIntent = new Intent(SearchRecipe.this, GetRecipe.class);
                    getRecipeIntent.putExtra("RecipeID", intSelectedRecipeID.toString());
                    getRecipeIntent.putExtra("RecipeName", strSelectedRecipe);
                    startActivity(getRecipeIntent);
                } else {
                    NoConnectionDialog();   //SHOWS NO CONNECTION DIALOG
                }
            }
        });
        //endregion

    }

    //CHECKS AND RETURNS WHETHER THERE IS AN ACTIVE METWORK CONNECTION OR NOT
    public Boolean CheckConnection(){
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    //SHOWS NO CONNECTION DIALOG
    public void NoConnectionDialog(){
        AlertDialog alertNoActiveUser = new AlertDialog.Builder(this).create();
        alertNoActiveUser.setTitle("LIMITED USE!\nNo Connection");
        alertNoActiveUser.setMessage("With no Internet connection you are limited to viewing recipes in your favourites.\n\nPlease connect to the Internet for a full feature set");

        alertNoActiveUser.setButton(AlertDialog.BUTTON_NEUTRAL, "Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();   //CLOSES THE DIALOG
            }
        });
        alertNoActiveUser.show();
    }

    //SETS THE SEARCH HISTORY VALUES AND POPUP VISIBILITY
    public void GetSearchHistory(){

        strSpinnerSelection = spSearchSpinner.getSelectedItem().toString(); //GETS SPINNER SELECTION

        //EXECUTES CODE DEPENDENT ON THE SPINNER ITEM SELECTION
        switch (strSpinnerSelection)
        {
            case "Keyword":
                strFile = "Keywords_Search_History";        //SETS FILE TO RETRIEVE
                puSearchHistory = HistoryPopup(GetHistoryFiles(strFile));   //RETRIEVES AND CREATED A CUSTOM POPUP CONTAING THE SEARCH HISTORY OF THE GIVEN SEARCH TYPE
                if (arHistory.size() != 0) {                                //CHECKS THAT THERE ARE PREVIOUS SEARCHES PRESENT WITHIN THE HISTORY FILE
                    puSearchHistory.showAsDropDown(searchInput, 0, 0);      //DISPLAYS POPUP AS A DROP DOWN LIST UNDERNEATH THE EDIT TEXT IF THERE ARE HISTORIC SEARCHES
                }
                break;

            case "Single Ingredient":
                strFile = "Single_Ingredient_Search_History";
                puSearchHistory = HistoryPopup(GetHistoryFiles(strFile));
                if (arHistory.size() != 0) {
                    puSearchHistory.showAsDropDown(searchInput, 0, 0);
                }
                break;

            case "Multi Ingredient":
                strFile = "Multi_Ingredient_Search_History";
                puSearchHistory = HistoryPopup(GetHistoryFiles(strFile));
                if (arHistory.size() != 0) {
                    puSearchHistory.showAsDropDown(searchInput, 0, 0);
                }
                break;

            case "Preparation Time (Minutes)":
                strFile = "Preparation_Time_Search_History";
                puSearchHistory = HistoryPopup(GetHistoryFiles(strFile));
                if (arHistory.size() != 0) {
                    puSearchHistory.showAsDropDown(searchInput, 0, 0);
                }
                break;
        }
    }

    //RETRIEVES AND MANIPULATES THE SPECIFIED SEARCH HISTORY FILE
    public ArrayList<String> GetHistoryFiles(String file){

        int c;
        String strTemp = "";
        arHistory.clear();  //CLEAR THE HISTORY ARRAY LIST REMOVING ALL PRESENT SEARCHES
//        ArrayList<String> arHistory = new ArrayList<String>();

        try {

            FileInputStream fileInputStream = openFileInput(file);  //RETRIEVES THE SPECIFIED FILE

            while ((c = fileInputStream.read()) != -1){
                strTemp = strTemp + Character.toString((char) c);   //ADDS THE CHARACTERS TO THE TEMPORARY STRING
            }

            fileInputStream.close();        //CLOSES THE INPUT STREAM
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //PUT EACH SEARCH INTO THE HISTORY ARRAY LIST IN REVERSE ORDER
        if (strTemp != "") {    //EXECUTES IF THE RETURNED STRING IS NOT EMPTY
            arHistory.clear();

            String[] arSplitString = strTemp.split("\r\n"); //CREATES AN ARRAY CONTAINING EACH OF THE SEARCH VALUES IN THE FILE

            //LOOPS THROUGH THE ARRAY AND ADDS THE VALUES TO AN THE ARRAY LIST
            for (int i = 0; i < arSplitString.length; i++){
                arHistory.add(arSplitString[i]);
            }

            //REVERSES THE ARRAY LIST SO THAT THE LAST SEARCHED ITEM IS FIRST
            Collections.reverse(arHistory);
        }

        //RETURNS THE CONSTRUCTED ARRAY LIST
        return arHistory;
    }

    //CREATES A NEW CUSTOM POPUP WINDOW
    public PopupWindow HistoryPopup(final ArrayList<String> arSearchHistory) {

        popupWindow = new PopupWindow(this);    //INITIALISES THE NEW POPUP WINDOW

        //PROGRAMMATICALLY CREATES A LIST VIEW THAT WILL BE DISPLAYED WITHIN THE POPUP WINDOW
        final ListView lvHistoryList = new ListView(this);
        lvHistoryList.setBackgroundColor(Color.WHITE);

        //CONSTRUCTS THE LIST VIEW PASSING THE COMPILED ARRAY LIST OF HISTORIC SEARCHES SO TO SHOW THE RESULTS
        ArrayAdapter<String> adHistoryLisAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arSearchHistory);
        lvHistoryList.setAdapter(adHistoryLisAdapter);

        //SETS THE ONCLICK LISTENER FOR THE SELECTION OF THE LIST ITEMS
        lvHistoryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                searchInput.setText(arSearchHistory.get(position)); //SETS THE SEARCH EDIT TEXT TO THE SELECTED ITEM
                popupWindow.dismiss();  //CLOSES THE POPUP WINDOW
            }
        });

        popupWindow.setWidth(searchInput.getWidth());   //SETS THE WIDTH OF THE POPUP WINDOW TO THE SAME AS THE EDIT TEXT WIDTH
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());     //CONVERTS A SET DIP VALUE TO A PI VALUE BASED ON THE DEVICE SCREEN BEING USED
        popupWindow.setHeight(px);

        popupWindow.setContentView(lvHistoryList);      //MAKES THE LIST VIEW THE ACTIVE VIEW OF THE POPUP WINDOW

        return popupWindow;                             //RETURNS THE CONSTRUCTED POPUP WINDOW TO THE UI

    }

    //CLOSES VIRTUAL KEYBOARD
    public void HideKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
    }

    //CALLS SPECIFIED SEARCH METHODS WHEN SEARCH IS CLICKED
    public void SearchRecipe(View view){

        HideKeyboard();     //HIDES KEYBOARD

        //GETS THE USERS SELECTION OF SPINNER ITEM
        strSpinnerSelection = spSearchSpinner.getSelectedItem().toString();

        //SELECTS AN CALLS CORRECT SEARCH METHOD DEPENDENT ON SPINNER SELECTION
        switch (strSpinnerSelection)
        {
            case "Keyword":
                KeywordSearch();
                break;
            case "Single Ingredient":
                SingleIngredientSearch();
                break;
            case "Multi Ingredient":
                MultiIngredientSearch();
                break;
            case "Preparation Time (Minutes)":
                PreparationTimeSearch();
                break;
        }
    }

    //SEARCHES API BY KEYWORD
    public void KeywordSearch(){

        if (CheckConnection()) {    //CHECKS FOR ACTIVE CONNECTION

            strSearchInput = String.valueOf(searchInput.getText()); //GETS THE USERS SEARCH QUERY INPUT

            if (strSearchInput.length() != 0) { //CHECKS THAT THERE HAS BEEN AN INPUT

                String[] strKeywordsArray = strSearchInput.split(" ");  //SPLIT THE ENTRY TO COLLECT ALL WORDS ENTERED
                strSearchString = "";

                if (strKeywordsArray.length == 1) {     //CHECKS THERE HAS ONLY BEEN A SINGLE WORD ENTERED

                    //ADDS THE WORD TO THE SEARCH STRING
                    for (int i = 0; i < strKeywordsArray.length; i++) {
                        strSearchString += strKeywordsArray[i];
                    }

                    //region ADDS KEYWORD TO FILE
                    String strFileName = "Keywords_Search_History";     //SETS FILE NAME
                    FileOutputStream fileOutputStream;
                    File file = getFileStreamPath(strFileName);         //GETS THE PATH TO THE SPECIFIED FILE

                    if (file == null || !file.exists()){    //EXECUTED IF THE FILE NAME DOES NOT EXIST
                        try {
                            fileOutputStream = openFileOutput(strFileName, MODE_PRIVATE);   //CREATES FILE
                            fileOutputStream.write(strSearchString.getBytes());             //ADDS THE SEARCH QUERY
                            fileOutputStream.write("\r\n".getBytes());                      //ADDS LINE BREAK
                            fileOutputStream.close();                                       //CLOSES THE OUTPUT STREAM
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }else if (file.exists()){
                        try {
                            fileOutputStream = openFileOutput(strFileName, Context.MODE_APPEND);    //RETRIEVES THE FILE FOR EDITING
                            fileOutputStream.write(strSearchString.getBytes());
                            fileOutputStream.write("\r\n".getBytes());
                            fileOutputStream.close();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    //endregion

                    Toast.makeText(SearchRecipe.this, "" + strSearchString, Toast.LENGTH_LONG).show();

                    //region CLEARS ARRAY LISTS THAT MAY CONTAIN PAST SEARCH RESULTS
                    recipes.clear();
                    recipeImages.clear();
                    recipeID.clear();
                    //endregion

                    //CREATES THE API QUERY STRING
                    strYourServiceUrl = "http://api.campbellskitchen.com/brandservice.svc/api/search?keywords=" + strSearchString + "&format=json&app_id=287ff47e&app_key=a37085350d61741dca2ce6a8ab070c72&total=200";

                    new AsyncTaskSearchRecipe().execute();

                    //btnSearch.setBackgroundResource(R.color.btnDisabledColor);
                    btnSearch.setBackgroundResource(R.drawable.custom_button_disabled_shape);
                    btnSearch.setTextColor(getResources().getColor(R.color.btnDisabledTextColor));
                    btnSearch.setEnabled(false);    //DISABLES THE SEARCH BUTTON SO THAT ONLY ONE SEARCH CAN TAKE PLACE AT A TIME


                } else {    //HANDLES INVALID SEARCH ENTRY EVENT

                    SearchCridentialMessage1.setText("Invalid Input!");
                    SearchCridentialMessage2.setText("Please Insert a Single Keyword");
                    SearchCridentialMessage1.setVisibility(View.VISIBLE);
                    SearchCridentialMessage2.setVisibility(View.VISIBLE);
                    recipeList.setVisibility(View.INVISIBLE);
                }
            }
        }else{
            NoConnectionDialog();   //DISPLAYS NO CONNECTION DIALOG
        }
    }

    //SEARCHES API BY SINGLE INGREDIENT
    public void SingleIngredientSearch() {

        if(CheckConnection()) { //CHECKS FOR ACTIVE CONNECTION

            strSearchInput = String.valueOf(searchInput.getText()); //GETS USER SEARCH ENTRY

            if (strSearchInput.length() != 0) { //CHECKS THERE HAS BEEN AN ENTRY

                String[] strIngredientsArray = strSearchInput.split(" ");   //SPLITS THE ENTRY TO GET ALL WORDS ENTERED
                strSearchString = "";

                if (strIngredientsArray.length == 1) {      //CHECKS THERE HAS ONLY BEEN ONE WORD ENTERED

                    //ADDS TH SEARCH WORD TO THE SEARCH STRING
                    for (int i = 0; i < strIngredientsArray.length; i++) {
                        strSearchString += strIngredientsArray[i];
                    }

                    //region ADDS INGREDIENT TO FILE
                    String strFileName = "Single_Ingredient_Search_History";        //SETS THE FIL NAME
                    FileOutputStream fileOutputStream;
                    File file = getFileStreamPath(strFileName);                     //GETS THE PATH TO THE SPECIFIED FILE

                    if (file == null || !file.exists()){              //EXECUTED IF THE FILE DOES NOT EXISTS ALREADY
                        try {
                            fileOutputStream = openFileOutput(strFileName, MODE_PRIVATE);   //CREATES THE FILE
                            fileOutputStream.write(strSearchString.getBytes());
                            fileOutputStream.write("\r\n".getBytes());
                            fileOutputStream.close();                                       //CLOSES THE STREAM
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }else if (file.exists()){
                        try {
                            fileOutputStream = openFileOutput(strFileName, Context.MODE_APPEND);    //RETRIEVED THE FILE TO EDIT
                            fileOutputStream.write(strSearchString.getBytes());
                            fileOutputStream.write("\r\n".getBytes());
                            fileOutputStream.close();                                               //CLOSES THE OUTPUT STREAM
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    //endregion

                    Toast.makeText(SearchRecipe.this, "" + strSearchString, Toast.LENGTH_LONG).show();

                    //region CLEARS ARRAY LISTS THAT MAY CONTAIN PAST SEARCH RESULTS
                    recipes.clear();
                    recipeImages.clear();
                    recipeID.clear();
                    //endregion

                    //CONSTRUCTS API QUERY STRING URL
                    strYourServiceUrl = "http://api.campbellskitchen.com/BrandService.svc/api/search?ingredient=" + strSearchString + "&format=json&app_id=287ff47e&app_key=a37085350d61741dca2ce6a8ab070c72&total=200";

                    new AsyncTaskSearchRecipe().execute();  //EXECUTES NEW API QUERY RETRIEVING RESULTS

                    btnSearch.setEnabled(false);    //DISABLES THE SEARCH BUTTON SO THAT ONLY ONE SEARCH CAN TAKE PLACE AT A TIME
                    //btnSearch.setBackgroundResource(R.color.btnDisabledColor);
                    btnSearch.setBackgroundResource(R.drawable.custom_button_disabled_shape);
                    btnSearch.setTextColor(getResources().getColor(R.color.btnDisabledTextColor));

                } else {    //EXECUTED IF THERE IS AN INVALID SEARCH ENTRY
                    SearchCridentialMessage1.setText("Invalid Input!");
                    SearchCridentialMessage2.setText("Please Insert One Ingredient");
                    SearchCridentialMessage1.setVisibility(View.VISIBLE);
                    SearchCridentialMessage2.setVisibility(View.VISIBLE);
                    recipeList.setVisibility(View.INVISIBLE);
                }
            }
        }else{
            NoConnectionDialog();   //DISPLAYS NO CONNECTION DIALOG
        }
    }

    //SEARCHES API BY MULTI INGREDIENT
    public void MultiIngredientSearch() {

        if (CheckConnection()) {    //CHECKS THERE IS AN ACTIVE CONNECTION

            strSearchInput = String.valueOf(searchInput.getText()); //GETS THE USERS SEARCH INPUT

            if (strSearchInput.length() != 0) { //CHECKS THERE HAS BEEN AN INPUT

                String[] strIngredientsArray = strSearchInput.split(" ");   //SPLITS INPUT TO GET EACH ENTERED WORD
                strSearchString = "";

                if (strIngredientsArray.length > 1) {   //CHECKS THERE HAVE BEEN MULTIPLE WORDS ENTERED

                    //CONSTRUCTS THE SEARCH STRING IN THE STYLE SPECIFIED IN THE API DOCUMENTATION
                    for (int i = 0; i < strIngredientsArray.length; i++) {
                        strSearchString += strIngredientsArray[i] + "|";
                    }

                    //REMOVES THE FINAL CHARACTER FROM THE CONSTRUCTED SEARCH STRING
                    strSearchString = strSearchString.substring(0, strSearchString.length() - 1);

                    //region ADDS KEYWORD TO FILE
                    String strFileName = "Multi_Ingredient_Search_History"; //SETS FILE NAME
                    FileOutputStream fileOutputStream;
                    File file = getFileStreamPath(strFileName); //GEST THE PATH TO THE SPECIFIED FILE

                    if (file == null || !file.exists()){        //EXECUTED IF THE FILE DOES NOT EXIST
                        try {
                            fileOutputStream = openFileOutput(strFileName, MODE_PRIVATE);   //CREATES THE INTERNAL FILE
                            fileOutputStream.write(strSearchInput.getBytes());
                            fileOutputStream.write("\r\n".getBytes());
                            fileOutputStream.close();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }else if (file.exists()){       //EXECUTED IF THE FILE DOES EXIST
                        try {
                            fileOutputStream = openFileOutput(strFileName, Context.MODE_APPEND);        //RETRIEVES THE FILE TO EDIT
                            fileOutputStream.write(strSearchInput.getBytes());
                            fileOutputStream.write("\r\n".getBytes());
                            fileOutputStream.close();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    //endregion

                    Toast.makeText(SearchRecipe.this, "" + strSearchString, Toast.LENGTH_LONG).show();

                    //region CLEARS ARRAY LISTS THAT MAY CONTAIN PAST SEARCH RESULTS
                    recipes.clear();
                    recipeImages.clear();
                    recipeID.clear();
                    //endregion

                    //CONSTRUCTS THE API QUERY URL
                    strYourServiceUrl = "http://api.campbellskitchen.com/BrandService.svc/api/search?ingredient=" + strSearchString + "&format=json&app_id=287ff47e&app_key=a37085350d61741dca2ce6a8ab070c72&total=200";

                    new AsyncTaskSearchRecipe().execute();

                    btnSearch.setEnabled(false);    //DISABLES THE SEARCH BUTTON SO THAT ONLY ONE SEARCH CAN TAKE PLACE AT A TIME
                    //btnSearch.setBackgroundResource(R.color.btnDisabledColor);
                    btnSearch.setBackgroundResource(R.drawable.custom_button_disabled_shape);
                    btnSearch.setTextColor(getResources().getColor(R.color.btnDisabledTextColor));

                } else {    //EXECUTED IF THERE HAS BEEN AN INVALID SEARCH INPUT

                    SearchCridentialMessage1.setText("Invalid Input!");
                    SearchCridentialMessage2.setText("Please Insert Two or More Ingredients\nEach Se[erted by a Space");
                    SearchCridentialMessage1.setVisibility(View.VISIBLE);
                    SearchCridentialMessage2.setVisibility(View.VISIBLE);
                    recipeList.setVisibility(View.INVISIBLE);
                }
            }
        }else {
            NoConnectionDialog();   //SHOWS NO CONNECTION DIALOG
        }
    }

    //SEARCHES API BY PREPARATION TIME
    public void PreparationTimeSearch(){

        if (CheckConnection()) {    //CHECKS THERE IS AN ACTIVE CONNECTION

            strSearchInput = String.valueOf(searchInput.getText()); //GETS THE USER SEARCH ENTRY

            if (strSearchInput.length() != 0) { //CHECKS THERE HAS BEEN A SEARCH ENTRY

                String[] strPreparationArray = strSearchInput.split(" ");   //SPLITS THE SEARCH STRING TO GET EACH WORD ENTERED
                strSearchString = "";

                if (strPreparationArray.length == 1) {  //CHECKS THERE IS ONLY ONE WORD ENTERED

                    if (strPreparationArray[0].toString().matches("[0-9]+")) {  //CHECKS THAT THE STRING ONLY CONTAINS NUMBERS

                        //ADDS THE WORD TO THE SEARCH STRING
                        for (int i = 0; i < strPreparationArray.length; i++) {
                            strSearchString += strPreparationArray[i];
                        }

                        try {

                            intSearchNum = Integer.parseInt(strSearchString);

                        } catch (NumberFormatException nfe) {   //CATCHES NONE NUMERIC STRING

                            Toast.makeText(SearchRecipe.this, "Could not parse value", Toast.LENGTH_SHORT).show();
                            intSearchNum = 0;   //SETS DEFAULT VALUE IF EXCEPTION IS TRIGGERED
                        }

                        intSearchNum = Math.round((intSearchNum / 10) * 10);    //ROUNDS THE ENTERED NUMBER DOWN TO THE NEAREST 10
                        strSearchString = intSearchNum.toString();

                        //region ADDS KEYWORD TO FILE
                        String strFileName = "Preparation_Time_Search_History"; //SETS FILE NAME
                        FileOutputStream fileOutputStream;
                        File file = getFileStreamPath(strFileName);             //GETS PATH TO SPECIFIED FILE

                        if (file == null || !file.exists()){        //EXECUTED IF THE FILE DOES NOT EXIST
                            try {
                                fileOutputStream = openFileOutput(strFileName, MODE_PRIVATE);   //CREATES THE INTERNAL PRIVATE FILE
                                fileOutputStream.write(strSearchString.getBytes());
                                fileOutputStream.write("\r\n".getBytes());
                                fileOutputStream.close();
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }else if (file.exists()){       //EXECUTED IF THE FILE ALREADY EXISTS
                            try {
                                fileOutputStream = openFileOutput(strFileName, Context.MODE_APPEND);    //RETRIEVES THE FILE TO EDIT
                                fileOutputStream.write(strSearchString.getBytes());
                                fileOutputStream.write("\r\n".getBytes());
                                fileOutputStream.close();
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                        //endregion

                        Toast.makeText(SearchRecipe.this, "" + strSearchString, Toast.LENGTH_LONG).show();

                        //region CLEARS ARRAY LISTS THAT MAY CONTAIN PAST SEARCH RESULTS
                        recipes.clear();
                        recipeImages.clear();
                        recipeID.clear();
                        //endregion

                        //CONSTRUCTS THE API QUERY STRING
                        strYourServiceUrl = "http://api.campbellskitchen.com/brandservice.svc/api/search?preptime=" + strSearchString + "&format=json&app_id=287ff47e&app_key=a37085350d61741dca2ce6a8ab070c72";

                        new AsyncTaskSearchRecipe().execute();

                        btnSearch.setEnabled(false);    //DISABLES THE SEARCH BUTTON SO THAT ONLY ONE SEARCH CAN TAKE PLACE AT A TIME
                        //btnSearch.setBackgroundResource(R.color.btnDisabledColor);
                        btnSearch.setBackgroundResource(R.drawable.custom_button_disabled_shape);
                        btnSearch.setTextColor(getResources().getColor(R.color.btnDisabledTextColor));

                    } else {    //EXECUTED IF THERE IS AN ENTRY CONTAINING ANYTING OTHER THAN NUMBERS
                        SearchCridentialMessage1.setText("Invalid Input!");
                        SearchCridentialMessage2.setText("Please Insert a Numerical Value");
                        SearchCridentialMessage1.setVisibility(View.VISIBLE);
                        SearchCridentialMessage2.setVisibility(View.VISIBLE);
                        recipeList.setVisibility(View.INVISIBLE);
                    }
                } else {    //EXECUTED FOR GENERAL INVALD ENTRY
                    SearchCridentialMessage1.setText("Invalid Input!");
                    SearchCridentialMessage2.setText("Please Insert a Numerical Value");
                    SearchCridentialMessage1.setVisibility(View.VISIBLE);
                    SearchCridentialMessage2.setVisibility(View.VISIBLE);
                    recipeList.setVisibility(View.INVISIBLE);
                }
            }
        }else{
            NoConnectionDialog();   //DISPLAYS NO CONNECTION DIALOG
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationView.setCheckedItem(R.id.search_recipe);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        //CLOSES NAV DRAWER IF OPEN OR CALLS SUPER IF NOT
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            this.finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // INFLATES THE ACTION BAR MENU; ADDING THE ITEMS FROM THE XML IF PRESENT
        getMenuInflater().inflate(R.menu.search_recipe, menu);
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
            Intent logoutIntent = new Intent(SearchRecipe.this, RandomRecipes.class);
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

    public class AsyncTaskSearchRecipe extends AsyncTask<String, String, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //SETS VISIBILITY OF UI COMPONENTS
            SearchCridentialMessage1.setVisibility(View.INVISIBLE);
            SearchCridentialMessage2.setVisibility(View.INVISIBLE);
            progbarLoading.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {

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

                    if (json_message != null) {     //CHECKS THAT THE RETURNED JSON OBJECT HOLDS DATA

                        //region RETRIEVES RECIPE NAME
                        try {
                            strRecipeName = json_message.getString("name");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            strRecipeName = "not found";
                        }

                        strRecipeName = sm.StringManipulations(strRecipeName);
                        recipes.add(strRecipeName);
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
                            strImageUrl = (json_message.getString("recipelink"));   //RETRIEVES THE IMAGE URL FROM THE JSON OBJECT
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        if (strImageUrl.length() != 0) {    //EXECUTED IF IMAGE URL IS NOT EMPTY

                            URL u = new URL(strImageUrl);   //CREATES A NEW URL WITH THE ADDRESS OF THE RECIPE IMAGE

                            if (CheckConnection()) {
                                try {
                                    httpCon = (HttpURLConnection) u.openConnection();   //CREATES AND OPENS THE CONNECTION TO THE IMAGE URL
                                    intstatusCode = httpCon.getResponseCode();          //RETRIEVES THE RESPONSE CODE FROM THE URL REQUEST
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    intstatusCode = 201;
                                }
                            }else intstatusCode = 201;

                            InputStream is;

                            if (intstatusCode != 200) {                             //EXECUTED IF CONNECTION NOT ESTABLISHED SUCCESSFULLY
                                recipeImages.add(bitBackUpImage);                   //RECIPE IMAGE SET TO THE DEFAULT IMAGE
                            }
                            else{
                                if (CheckConnection()) {
                                    try {
                                        is = httpCon.getInputStream();                      //RETRIEVES THE IMAGE FROM THE URL
                                        bitRecipeImage = BitmapFactory.decodeStream(is);    //DECODES THE IMAGE AND STORED IN A BITMAP FORMAT
                                        recipeImages.add(bitRecipeImage);                   //ADDS RECIPE IMAGE TO ARRAY
                                        is.close();                                         //CLOSED THE INPUT STREAM
                                        httpCon.disconnect();                               //DISCONNECTS HTTP CONNECTION
                                    }catch (IOException e){
                                        e.printStackTrace();
                                        recipeImages.add(bitBackUpImage);                   //ADDS DEFAULT IMAGE TO ARRAY IF THE INPUT STREAM FAILS
                                    }
                                }else{
                                    recipeImages.add(bitBackUpImage);                       //ADDS DEFAULT IMAGE IF THERE IS NO CONNECTION IS LOST BETWEEN OPENING THE CONNECTION AND RETRIEVING THE DATA
                                }
                            }
                        }
                        else{
                            recipeImages.add(bitBackUpImage);                               //ADDS DEFAULT IMAGE IF THERE IS NO IMAGE FILE FOUND
                        }
                        //endregion
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            //region CALLS CUSTOM LIST ADAPTER PASSING THE DATA TO CREATE THE LIST VIEW WITH CONTENT
            ListView list = (ListView) findViewById(R.id.lstRecipes);
            Custom_ListView_Adapter custom_listView_adapter = new Custom_ListView_Adapter(SearchRecipe.this, recipes, recipeImages);
            list.setAdapter(custom_listView_adapter);
            //endregion

            if (recipes.size() >= 1) //EXECUTED IF THE API QUERY RETURNS RESULTS
            {
                SearchCridentialMessage1.setVisibility(View.INVISIBLE);
                SearchCridentialMessage2.setVisibility(View.INVISIBLE);
                progbarLoading.setVisibility(View.INVISIBLE);
                recipeList.setVisibility(View.VISIBLE);

            }else{   //EXECUTED IF THE API QUERY RETURNES NO RESULTS
                SearchCridentialMessage1.setText("Sorry! No Results Found");
                SearchCridentialMessage2.setText("Please Try Again");
                SearchCridentialMessage1.setVisibility(View.VISIBLE);
                SearchCridentialMessage2.setVisibility(View.VISIBLE);
                recipeList.setVisibility(View.INVISIBLE);
                progbarLoading.setVisibility(View.INVISIBLE);
            }

            btnSearch.setEnabled(true); //RE-ENABLES THE SEARCH BUTTON
            //btnSearch.setBackgroundResource(R.color.colorButtonColor);
            btnSearch.setBackgroundResource(R.drawable.custom_button_shape);
            btnSearch.setTextColor(getResources().getColor(R.color.colorText));
        }
    }

}
