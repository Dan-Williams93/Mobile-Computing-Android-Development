package com.example.daniel.wil13458204_mobilecomputing;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import org.w3c.dom.ProcessingInstruction;

import java.lang.ref.PhantomReference;
import java.lang.reflect.Array;
import java.security.PublicKey;
import java.util.ArrayList;

/**
 * Created by Daniel Williams - WIL13458204 -  on 17/11/2015.
 */
public class SQLiteDatabaseAdapter {

    SQLiteDatabaseHelper helper;

    //CREATES NEW SQLDATABASEHELPER INSTANCE
    public SQLiteDatabaseAdapter(Context context){
        helper = new SQLiteDatabaseHelper(context);
    }

    //ADD NEWLY REGISTERED USER TO THE DATABASE USER TABLE
    public long registerNewUser(String strFirstName, String strLastName, String strUsername, String strPassword, String strImageUri, String strSecQuestion, String strSecAnswer){

        //RETRIEVES THE DATABASE
        SQLiteDatabase myDatabase = helper.getWritableDatabase();

        //region CREATES NEW CONTENT VALUES TO HOLD THE VALUES TO ADD AND THE COLUMNS THEY SHOULD BE PLACED IN
        ContentValues contentValues = new ContentValues();
        contentValues.put(SQLiteDatabaseHelper.USERTABLE_COL_2_FIRSTNAME, strFirstName);
        contentValues.put(SQLiteDatabaseHelper.USERTABLE_COL_3_LASTNAME, strLastName);
        contentValues.put(SQLiteDatabaseHelper.USERTABLE_COL_4_USERNAME, strUsername);
        contentValues.put(SQLiteDatabaseHelper.USERTABLE_COL_5_PASSWORD, strPassword);
        contentValues.put(SQLiteDatabaseHelper.USERTABLE_COL_6_IMAGEURI, strImageUri);
        contentValues.put(SQLiteDatabaseHelper.USERTABLE_COL_7_SECURITYQUESTION, strSecQuestion);
        contentValues.put(SQLiteDatabaseHelper.USERTABLE_COL_8_SECURITYANSWER, strSecAnswer);
        //endregion

        //ADDS THE CONTENT VALUES TO THE SPECIFIED DATABASE TABLE RETURNING THE NUMBER OF VALUE ADDED
        long id = myDatabase.insert(SQLiteDatabaseHelper.USER_TABLE_NAME, null, contentValues);

        return id;
    }

    //CHECKS THERE ARE NO USERS IN THE DATABASE WITH A SPECIFIED USERNAME
    public Integer checkUsernameAvailabilty(String strUsername){

        //RETRIEVES THE DATABASE
        SQLiteDatabase myDatabase = helper.getWritableDatabase();

        int count = 0;

        //SPECIFIES THE COLUMNS TO RETURN
        String[] columns = {SQLiteDatabaseHelper.USERTABLE_COL_4_USERNAME};

        //CREATES A CURSOR THAT HOLDS THE DATA PRESENT WITHIN THE COLUMN THAT MATCHED THE USERNAME PASSED INTO THE FUNCTION
        Cursor cursor = myDatabase.query(SQLiteDatabaseHelper.USER_TABLE_NAME, columns, SQLiteDatabaseHelper.USERTABLE_COL_4_USERNAME + " = '" + strUsername + "'", null, null, null, null);

        //ADDS THE TO COUNT FOR EACH ROW RETURNED FRO THE QUERY INDICATING THE NUMBER OF IDENTICAL USERNAME'S
        while (cursor.moveToNext()){
            count++;
        }

        return count;
    }

    //CHECKS THAT THERE IS A USER WITH THE PASSED CREDENTIALS IN THE USER DATABASE TABLE
    public Integer validateUserLogin(String strUsername, String strPassword){

        //GETS THE DATABASE
        SQLiteDatabase myDatabase = helper.getWritableDatabase();

        int intCount = 0;

        //SPECIFIES THE COLUMNS TO RETURN
        String[] columns = {SQLiteDatabaseHelper.USERTABLE_COL_4_USERNAME, SQLiteDatabaseHelper.USERTABLE_COL_5_PASSWORD};

        //CONSTRUCTS THE QUERY TO CHECK THE USER TABLE FOR ROWS WHERE USERNAME = 'username' and PASSWORD = 'password'
        String strSelectionString = SQLiteDatabaseHelper.USERTABLE_COL_4_USERNAME + " = '" + strUsername + "' AND " + SQLiteDatabaseHelper.USERTABLE_COL_5_PASSWORD + " = '" + strPassword + "'";

        //CONSTRUCTS A CURSOR TO HOLD THE RETURNED DATA
        Cursor cursor = myDatabase.query(SQLiteDatabaseHelper.USER_TABLE_NAME, columns, strSelectionString, null, null, null, null);

        //ADD TO THE COUNT FOR EACH ROW FOUND THAT MATCHES THE PASSED CREDENTIALS
        while (cursor.moveToNext()){
            intCount++;
        }

        return intCount;
    }

    //RETURNS THE USER ID OF THE ACTIVE USER PASSING USERNAME AND PASSWORD
    public Integer dbGetActiveUserID(String strUsername, String strPassword){

        //GETS THE DATABASE
        SQLiteDatabase myDatabase = helper.getWritableDatabase();

        //SPECIFIES THE COLUMNS TO RETURN
        String[] columns = {SQLiteDatabaseHelper.USERTABLE_COL_1_ID};
        Integer intActiveID = null;

        //CONSTRUCTS THE QUERY TO RETURN ROWS WHERE THE USERNAME AND PASSWORD EQUAL THE PASSED DATA
        String strGetIDString = SQLiteDatabaseHelper.USERTABLE_COL_4_USERNAME + " = '" + strUsername + "' AND " + SQLiteDatabaseHelper.USERTABLE_COL_5_PASSWORD + " = '" + strPassword + "'";

        //CONSTRUCTS A CURSOR TO HOLD TH RETURNED DATA
        Cursor cursor = myDatabase.query(SQLiteDatabaseHelper.USER_TABLE_NAME, columns, strGetIDString, null, null, null, null);

        //SETS THE ACTIVE USER ID FROM THE RETURNED DATA
        while (cursor.moveToNext()){
            int index1 = cursor.getColumnIndex(SQLiteDatabaseHelper.USERTABLE_COL_1_ID);
            intActiveID = cursor.getInt(index1);
        }

        return intActiveID;
    }

    //RETURNS THE USER ID OF THE ACTIVE USER PASSING THE USERNAME
    public Integer dbGetActiveUserIDFromUsername(String strUsername){

        //GETS THE DATABASE
        SQLiteDatabase myDatabase = helper.getWritableDatabase();

        //SPECIFIES THE COLUMNS TO RETRIEVE
        String[] columns = {SQLiteDatabaseHelper.USERTABLE_COL_1_ID};
        Integer intActiveID = 0;

        //CONSTRUCTS THE QUERY TO RETRIEVE ROWS WHERE THE USERNAME COLUMN EQUALS THE PASSED USERNAME
        String strGetIDString = SQLiteDatabaseHelper.USERTABLE_COL_4_USERNAME + " = '" + strUsername + "'";

        //CREATES A CURSOR TO HOLD THE RETURNED DATA
        Cursor cursor = myDatabase.query(SQLiteDatabaseHelper.USER_TABLE_NAME, columns, strGetIDString, null, null, null, null);

        //SETS THE ACTIVE USER ID FROM THE RETURNED DATA
        while (cursor.moveToNext()){
            int index1 = cursor.getColumnIndex(SQLiteDatabaseHelper.USERTABLE_COL_1_ID);
            intActiveID = cursor.getInt(index1);
        }

        return intActiveID;
    }

    //RETURNS THE FIRST NAME OF THE ACTIVE USER
    public String dbGetActiveUserFirstName(String strUsername, String strPassword){

        //GETS THE DATABASE
        SQLiteDatabase myDatabase = helper.getWritableDatabase();

        //SPECIFIES THE COLUMNS TO RETRIEVE
        String[] columns = {SQLiteDatabaseHelper.USERTABLE_COL_2_FIRSTNAME};
        String strFirstName = null;

        //CONSTRUCTS THE QUERY STRING TO QUERY THE DATABASE USING THE PASSED USERNAME AND PASSWORD
        String strGetFirstNameString = SQLiteDatabaseHelper.USERTABLE_COL_4_USERNAME + " = '" + strUsername + "' AND " + SQLiteDatabaseHelper.USERTABLE_COL_5_PASSWORD + " = '" + strPassword + "'";

        //CREATES A CURSOR TO HOLD THE RETURNED DATA
        Cursor cursor = myDatabase.query(SQLiteDatabaseHelper.USER_TABLE_NAME, columns, strGetFirstNameString, null, null, null, null);

        //SETS THE ACTIVE FIRST NAME TO THE DATA WITHIN THE CURSOR POSITION
        while (cursor.moveToNext()){
            int index1 = cursor.getColumnIndex(SQLiteDatabaseHelper.USERTABLE_COL_2_FIRSTNAME);
            strFirstName = cursor.getString(index1);
        }

        return strFirstName;
    }

    //RETURNS THE LAST NAME OF THE ACTIVE USER
    public String dbGetActiveUserLastName(String strUsername, String strPassword){

        //GETS THE DATABASE
        SQLiteDatabase myDatabase = helper.getWritableDatabase();

        //SPECIFIES WHICH COLUMNS TO RETRIEVE
        String[] columns = {SQLiteDatabaseHelper.USERTABLE_COL_3_LASTNAME};
        String strLastName = null;

        //CONSTRUCTS THE QUERY STRING THAT SHOULD RETURN THE LAST NAME VALUES FOR THE ROWS THAT MATCH THE PASSED USERNAME AND PASSWORD
        String strGetLastNameString = SQLiteDatabaseHelper.USERTABLE_COL_4_USERNAME + " = '" + strUsername + "' AND " + SQLiteDatabaseHelper.USERTABLE_COL_5_PASSWORD + " = '" + strPassword + "'";

        //CREATES A CURSOR TO HOLD THE RETURNED DATA
        Cursor cursor = myDatabase.query(SQLiteDatabaseHelper.USER_TABLE_NAME, columns, strGetLastNameString, null, null, null, null);

        //SETS THE LAST NAME TO THE VALUE OF THE CURSOR
        while (cursor.moveToNext()){
            int index1 = cursor.getColumnIndex(SQLiteDatabaseHelper.USERTABLE_COL_3_LASTNAME);
            strLastName = cursor.getString(index1);
        }

        return strLastName;
    }

    //RETURNS THE URI OF THE ACTIVE USERS PROFILE IMAGE
    public String dbGetActiveUserProfileUri(String strUsername, String strPassword){

        //GETS DATABASE
        SQLiteDatabase myDatabase = helper.getWritableDatabase();

        //SPECIFIES THE COLUMNS TO RETURN
        String[] columns = {SQLiteDatabaseHelper.USERTABLE_COL_6_IMAGEURI};
        String strImageUri = null;

        //CONSTRUCTS THE QUERY STRING TO RETURN THE ROWS THAT MATCH THE PASSED USERNAME AND PASSWORD
        String strGetUriString = SQLiteDatabaseHelper.USERTABLE_COL_4_USERNAME + " = '" + strUsername + "' AND " + SQLiteDatabaseHelper.USERTABLE_COL_5_PASSWORD + " = '" + strPassword + "'";

        //CREATES A CURSOR TO HOLD THE RETURNED DATA
        Cursor cursor = myDatabase.query(SQLiteDatabaseHelper.USER_TABLE_NAME, columns, strGetUriString, null, null, null, null);

        //SETS THE URI TO THE VALUE STORED WITHIN THE CURSOR POSITION
        while (cursor.moveToNext()){
            int index1 = cursor.getColumnIndex(SQLiteDatabaseHelper.USERTABLE_COL_6_IMAGEURI);
            strImageUri = cursor.getString(index1);
        }

        return strImageUri;
    }

    //CHECKS THE RECIPE TABLE TO SEE IF THE RECIPE AND RELEVANT JSON ALREADY EXISTS
    public Integer dbCheckDuplicateRecipe(int intRecipeID){

        //GETS DATABASE
        SQLiteDatabase myDatabase = helper.getWritableDatabase();
        int intCount = 0;

        //SPECIFIES THE COLUMNS TO RETURN
        String[] columns = {SQLiteDatabaseHelper.RECIPETABLE_COL_1_RECIPEID};

        //CONSTRUCTS QUERY STRING TO RETURN ROWS WHERE OF THE RECIPE TABLE WHERE THE RECIPE ID = THE PASSED RECIPE ID
        String strSelectionString = SQLiteDatabaseHelper.RECIPETABLE_COL_1_RECIPEID + " = '" + intRecipeID + "'";

        //CREATES A CURSOR TO HOLD THE RETURNED DATA
        Cursor cursor = myDatabase.query(SQLiteDatabaseHelper.RECIPE_TABLE_NAME, columns, strSelectionString, null, null, null, null);

        //ADDS TO THE COUNT FOR EACH ROW RETURNED FROM THE QUERY
        while (cursor.moveToNext()){
            intCount++;
        }

        return intCount;
    }

    //CHECKS AND RETURNS COUNT TO DETERMINE IF GIVEN RECIPE ID IS IN USERS FAVOURITES
    public Integer dbCheckFavourites(int intUserID, int intRecipeID){

        //GETS DATABASE
        SQLiteDatabase myDatabase = helper.getWritableDatabase();
        int intCount = 0;

        //SPECIFIES THE COLUMNS TO RETURN
        String[] columns = {SQLiteDatabaseHelper.FAVOURITES_COL_2_USERID, SQLiteDatabaseHelper.FAVOURITES_COL_3_RECIPEID};

        //CONSTRUCTS THE QUERY STRING TO RETURN ROWS THAT MATCH THE PASSED USER ID AND RECIPE ID
        String strSelectionString = SQLiteDatabaseHelper.FAVOURITES_COL_2_USERID + " = '" + intUserID + "' AND " + SQLiteDatabaseHelper.FAVOURITES_COL_3_RECIPEID + " = '" + intRecipeID + "'";

        //CREATES A CURSOR TO HOLD THE RETURNED DATA
        Cursor cursor = myDatabase.query(SQLiteDatabaseHelper.FAVOURITES_TABLE_NAME, columns, strSelectionString, null, null, null, null);

        //ADDS TO THE COUNT FOR EACH ROW RETURNED
        while (cursor.moveToNext()){
            intCount++;
        }

        return intCount;
    }

    //ADD RECIPE TO RECIPE TABLE PASSING RECIPE_ID RECIPE_NAME AND JSON_STRING
    public long dbAddToRecipes(int intRecipeID, String strRecipeName, String strRecipeJson){

        //GETS THE DATABASE
        SQLiteDatabase myDatabase = helper.getWritableDatabase();

        //region CONSTRUCTS A CONTENT VALUES TO HOLD THE VALUES AND COLUMNS TO INSERT INTO THE DATABASE
        ContentValues contentValues = new ContentValues();
        contentValues.put(SQLiteDatabaseHelper.RECIPETABLE_COL_1_RECIPEID, intRecipeID);
        contentValues.put(SQLiteDatabaseHelper.RECIPETABLE_COL_2_RECIPENAME, strRecipeName);
        contentValues.put(SQLiteDatabaseHelper.RECIPETABLE_COL_3_RECIPEJSON, strRecipeJson);
        //endregion

        //ADDS THE CONTENT VALUES TO THE SPECIFIED DATABASE TABLE RETURNING THE NUMBER OF VALUE ADDED
        long id = myDatabase.insert(SQLiteDatabaseHelper.RECIPE_TABLE_NAME, null, contentValues);

        return id;
    }

    //ADD THE RECIPE TO THE FAVOURITES TABLE
    public long dbAddToFavourites(int intUserID, int intRecipeID){

        //GETS DATABASE
        SQLiteDatabase myDatabase = helper.getWritableDatabase();

        //region CREATES CONTENT VALUES TO HOLD THE VALUES AND COLUMNS TO INSERT
        ContentValues contentValues = new ContentValues();
        contentValues.put(SQLiteDatabaseHelper.FAVOURITES_COL_2_USERID, intUserID);
        contentValues.put(SQLiteDatabaseHelper.FAVOURITES_COL_3_RECIPEID, intRecipeID);
        //endregion

        //ADDS THE CONTENT VALUES TO THE SPECIFIED DATABASE TABLE RETURNING THE NUMBER OF VALUE ADDED
        long id = myDatabase.insert(SQLiteDatabaseHelper.FAVOURITES_TABLE_NAME, null, contentValues);

        return id;}

    //RETURNS THE RECIPE IDS OF RECIPES IN USERS FAVOURITES
    public ArrayList<Integer> dbGetUserFavourites(int intUserID){

        //GETS DATABASE
        SQLiteDatabase myDatabase = helper.getWritableDatabase();
        ArrayList<Integer> arRecipeIDs = new ArrayList<Integer>();

        //SPECIFIES THE COLUMNS TO RETURN
        String[] columns = {SQLiteDatabaseHelper.FAVOURITES_COL_3_RECIPEID};

        //CONSTRUCTS THE QUERY TO RETURN ROWS THAT MATCH THE USER ID PASSED TO THE FUNCTION
        String strSelectionString = SQLiteDatabaseHelper.FAVOURITES_COL_2_USERID + " = '" + intUserID + "'";

        //CREATES A CURSOR TO HOLD THE DATA RETURNED FROM THE DATABASE
        Cursor cursor = myDatabase.query(SQLiteDatabaseHelper.FAVOURITES_TABLE_NAME, columns, strSelectionString, null, null, null, null);

        //ITERATED THROUGH THE RETURNED DATA ADDING EACH RECIPE ID TO THE ARRAY LIST
        while (cursor.moveToNext()){
            //int index1 = cursor.getColumnIndex(SQLiteDatabaseHelper.FAVOURITES_COL_3_RECIPEID);
            arRecipeIDs.add(cursor.getInt(0));
        }

        return arRecipeIDs;
    }

    //RETURN RECIPE JSON FROM RECIPE TABLE
    public String dbGetRecipeDetails(int intRecipeID){

        //GETS DATABASE
        SQLiteDatabase myDatabase = helper.getWritableDatabase();
        String strRecipeJson = null;

        //SPECIFIES THE COLUMNS TO RETURN
        String[] columns = {SQLiteDatabaseHelper.RECIPETABLE_COL_3_RECIPEJSON};

        //CONSTRUCTS THE QUERY STRING TO RETURN ROWS WHERE USERNAME = 'username' and PASSWORD = 'password'
        String strSelectionString = SQLiteDatabaseHelper.RECIPETABLE_COL_1_RECIPEID + " = '" + intRecipeID + "'";

        //CREATES A CURSOR TO HOLD THE RETURNED QUERY DATA
        Cursor cursor = myDatabase.query(SQLiteDatabaseHelper.RECIPE_TABLE_NAME, columns, strSelectionString, null, null, null, null);

        //SETS THE JSON STRING THE VALUE OF THE DATA IN THE CURRENT CURSOR POSITION
        while (cursor.moveToNext()){
            int index1 = cursor.getColumnIndex(SQLiteDatabaseHelper.RECIPETABLE_COL_3_RECIPEJSON);
            strRecipeJson = cursor.getString(index1);
        }

        return strRecipeJson;
    }

    //RETRIEVES THE USERS SECURITY QUESTION
    public String dbGetSecurityQuestion(String strUsername){

        //GETS DATABASE
        SQLiteDatabase myDatabase = helper.getWritableDatabase();
        String strSecQuestion = null;

        //SPECIFIES COLUMNS TO RETURN
        String[] columns = {SQLiteDatabaseHelper.USERTABLE_COL_7_SECURITYQUESTION};

        //CONSTRUCTS THE QUERY STRING TO RETURN ROWS THAT HOLD THE PASSED USERNAME
        String strSelectionString = SQLiteDatabaseHelper.USERTABLE_COL_4_USERNAME + " = '" + strUsername + "'";

        //CREATES A CURSOR TO HOLD THE RETURNED DATA
        Cursor cursor = myDatabase.query(SQLiteDatabaseHelper.USER_TABLE_NAME, columns, strSelectionString, null, null, null, null);

        //SETS THE SECURITY QUESTION TO THE VALUE IN THE CURRENT CURSOR POSITION
        while (cursor.moveToNext()){
            int index1 = cursor.getColumnIndex(SQLiteDatabaseHelper.USERTABLE_COL_7_SECURITYQUESTION);
            strSecQuestion = cursor.getString(index1);
        }

        return strSecQuestion;
    }

    //AUTHENTICATES THE USERS PASSWORD CHANGE
    public Integer dbAuthenticatePassChange(int intUserID, String strSecQuestion, String strSecAnswer){

        //GETS DATABASE
        SQLiteDatabase myDatabase = helper.getWritableDatabase();
        int intCount = 0;

        //SPECIFIES THE COLUMNS TO RETURN
        String[] columns = {SQLiteDatabaseHelper.USERTABLE_COL_7_SECURITYQUESTION, SQLiteDatabaseHelper.USERTABLE_COL_8_SECURITYANSWER};

        //CONSTRUCTS THE QUERY STRING TO RETRIEVE ROWS THAT HOLD THE PASSED USER ID, SECURITY QUESTION AND ANSWER
        String strSelectionString = SQLiteDatabaseHelper.USERTABLE_COL_1_ID + " = '" + intUserID + "' AND " + SQLiteDatabaseHelper.USERTABLE_COL_7_SECURITYQUESTION + " = '" + strSecQuestion + "' AND " + SQLiteDatabaseHelper.USERTABLE_COL_8_SECURITYANSWER + " = '" + strSecAnswer + "'";

        //CREATES CURSOR TO HOLD THE RETURNED QUERY DATA
        Cursor cursor = myDatabase.query(SQLiteDatabaseHelper.USER_TABLE_NAME, columns, strSelectionString, null, null, null, null);

        //ADDS TO THE COUNT FOR EACH ROW RETURNED
        while (cursor.moveToNext()){
            intCount++;
        }

        return intCount;
    }

    //COMPLETE PASSWORD CHANGE PROCESS
    public void dbChangePassword(int intUserID, String strNewPassword){

        //GETS DATABASE
        SQLiteDatabase myDatabase = helper.getWritableDatabase();

        //CREATES A CONTENT VALUES TO HOLD THE VALUES AND COLUMNS TO INSERT INTO THE DATABASE
        ContentValues contentValues = new ContentValues();
        contentValues.put(SQLiteDatabaseHelper.USERTABLE_COL_5_PASSWORD, strNewPassword);

        //SETS THE CONDITION OF THE DATABASE EDIT CLAUSE
        String[] arWhereCondition = {String.valueOf(intUserID)};

        //UPDATES THE SPECIFIED USER PASSWORD REPLACING EXISTING VALUE WITH THE NEW VALUE STORED IN THE CONTENT VALUES
        myDatabase.update(SQLiteDatabaseHelper.USER_TABLE_NAME, contentValues ,SQLiteDatabaseHelper.USERTABLE_COL_1_ID + " =? ", arWhereCondition);
    }

    //DELETE ALL FROM FAVOURITES FOR A GIVEN USER
    public Integer dbDeleteFavourites(int intUserID){

        //GETS DATABASE
        SQLiteDatabase myDatabase = helper.getWritableDatabase();

        //SETS THE CONDITION OF THE DATABASE DELETE CLAUSE
        String[] strWhereArgs = {String.valueOf(intUserID)};

        //CARRIED OUT THE DELETE PROCESS WHERE THE USER ID OF = THE SET CONDITION RETURNING A COUNT OF ITEMS DELETED
        int intCount = myDatabase.delete(SQLiteDatabaseHelper.FAVOURITES_TABLE_NAME, SQLiteDatabaseHelper.FAVOURITES_COL_2_USERID + " =? ", strWhereArgs);

        return intCount;
    }

    //DELETES ALL DATA STORED WITHIN THE DATABASE
    public Integer dbClearAppData(){

        //GETS DATABASE
        SQLiteDatabase myDatabase = helper.getWritableDatabase();
        int intCount = 0;

        //DELETES ALL ROWS OF EACH TABLE IN THE DATABASE ADDING THE NUMBER OF DELETIONS TO THE COUNT
        intCount += myDatabase.delete(SQLiteDatabaseHelper.FAVOURITES_TABLE_NAME, null, null);
        intCount += myDatabase.delete(SQLiteDatabaseHelper.USER_TABLE_NAME, null, null);
        intCount += myDatabase.delete(SQLiteDatabaseHelper.RECIPE_TABLE_NAME, null, null);

        return intCount;
    }

    //REMOVE A SPECIFIED USER FAVOURITE
    public Integer dbDeleteSingleFavourite(int intUserID, int intRecipeID){

        //GETS DATABASE
        SQLiteDatabase myDatabase = helper.getWritableDatabase();

        //SETS THE CONDITIONS OF THE DELETE CLAUSE
        String[] strWhereArgs = {String.valueOf(intUserID), String.valueOf(intRecipeID)};

        //PERFORMS THE DELETE STATEMENT ON ROWS WHERE THE USER ID AND RECIPE ID = THE PASSED IN VALUES, RETUNRING THE NUMBER OF ITEMS REMOVED
        int intCount = myDatabase.delete(SQLiteDatabaseHelper.FAVOURITES_TABLE_NAME, SQLiteDatabaseHelper.FAVOURITES_COL_2_USERID + " =? AND " + SQLiteDatabaseHelper.FAVOURITES_COL_3_RECIPEID + " =?", strWhereArgs);

        return intCount;
    }

    //CREATES A STATIC DATABASE SCHEME
    static class SQLiteDatabaseHelper extends SQLiteOpenHelper {

        //region VARIABLES
        private static final String DATABASE_NAME = "AppUsers";
        private static final Integer DATABASE_VERSION_NUMBER = 1;

        //region USER TABLE
        private static final String USER_TABLE_NAME = "USERS";
        private static final String USERTABLE_COL_1_ID = "ID";
        private static final String USERTABLE_COL_2_FIRSTNAME = "FIRSTNAME";
        private static final String USERTABLE_COL_3_LASTNAME = "LASTNAME";
        private static final String USERTABLE_COL_4_USERNAME = "USERNAME";
        private static final String USERTABLE_COL_5_PASSWORD = "PASSWORD";
        private static final String USERTABLE_COL_6_IMAGEURI = "IMAGE_URI";
        private static final String USERTABLE_COL_7_SECURITYQUESTION = "SEC_QUESTION";
        private static final String USERTABLE_COL_8_SECURITYANSWER = "SEC_ANSWER";

        //CREATE TABLE STRING STATING THE TABLE NAME, CULUMN NAMES AND THE COLUMN ATTRIBUTES
        private static final String CREATE_USER_TABLE = "CREATE TABLE " + USER_TABLE_NAME +
                " (" + USERTABLE_COL_1_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + USERTABLE_COL_2_FIRSTNAME + " TEXT, "
                + USERTABLE_COL_3_LASTNAME + " TEXT, " + USERTABLE_COL_4_USERNAME + " TEXT, " + USERTABLE_COL_5_PASSWORD + " TEXT, "
                + USERTABLE_COL_6_IMAGEURI + " TEXT, " + USERTABLE_COL_7_SECURITYQUESTION + " TEXT, " + USERTABLE_COL_8_SECURITYANSWER + " TEXT);";

        //DROP TABLE STRING INDICATION WHICH TABLE TO REMOVE
        private static final String DROP_USER_TABLE = "DROP TABLE IF EXISTS " + USER_TABLE_NAME;
        //endregion

        //region RECIPE TABLE
        private static final String RECIPE_TABLE_NAME = "RECIPES";
        private static final String RECIPETABLE_COL_1_RECIPEID = "RECIPE_ID";
        private static final String RECIPETABLE_COL_2_RECIPENAME = "RECIPE_NAME";
        private static final String RECIPETABLE_COL_3_RECIPEJSON = "JSON";

        //CREATE TABLE STRING STATING THE TABLE NAME, COLUMN HEADINGS AND THE COLUMN ATTRIBUTES
        private static final String CREATE_RECIPE_TABLE = "CREATE TABLE " + RECIPE_TABLE_NAME + " (" + RECIPETABLE_COL_1_RECIPEID + " INTEGER PRIMARY KEY, " + RECIPETABLE_COL_2_RECIPENAME + " TEXT, " +RECIPETABLE_COL_3_RECIPEJSON + " TEXT);";

        //DROP TABLE STRING STATING WHICH TABLE TO REMOVE
        private static final String DROP_RECIPE_TABLE = "DROP TABLE IF EXISTS " + RECIPE_TABLE_NAME;
        //endregion

        //region FAVOURITES TABLE
        private static final String FAVOURITES_TABLE_NAME = "FAVOURITES";
        private static final String FAVOURITES_COL_1_ROWID = "ROW_ID";
        private static final String FAVOURITES_COL_2_USERID = "USER_ID";
        private static final String FAVOURITES_COL_3_RECIPEID = "RECIPE_ID";

        //CREATE TABLE STRING STATING THE TABLE NAME, COLUMN HEADINGS AND THE COLUMN ATTRIBUTES
        private static final String CREATE_FAVOURITES_TABLE = "CREATE TABLE " + FAVOURITES_TABLE_NAME + " (" + FAVOURITES_COL_1_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + FAVOURITES_COL_2_USERID + " INTEGER, " + FAVOURITES_COL_3_RECIPEID + " INTEGER);";

        //DROP TABLE STRING STATING WHICH TABLE TO REMOVE
        private static final String DROP_FAVOURITES_TABLE = "DROP TABLE IF EXISTS " + FAVOURITES_TABLE_NAME;
        //endregion

        private Context context;
        //endregion

        //CONSTRUCTOR WHICH CONSTRUCTS THE SCHEME
        public SQLiteDatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION_NUMBER);
            this.context = context;
            //Toast.makeText(context, "CONSTRUCTOR CALLED", Toast.LENGTH_SHORT).show();
        }

        @Override
        //CREATES THE SPECIFIED TABLES ON CREATION OF THE SCHEME
        public void onCreate(SQLiteDatabase db) {

            //Toast.makeText(context, "onCreate CALLED", Toast.LENGTH_SHORT).show();

            try {

                //region TABLE CREATION
                db.execSQL(CREATE_USER_TABLE);
                db.execSQL(CREATE_RECIPE_TABLE);
                db.execSQL(CREATE_FAVOURITES_TABLE);
                //endregion

            } catch (SQLException e) {
                e.printStackTrace();
                Toast.makeText(context, "" + e, Toast.LENGTH_SHORT).show(); //SHOWS ERROR
            }
        }

        @Override
        //EXECUTED WHEN A NEW VERSION OF THE DATABASE SCHEME IS DEVELOPED AND VERSION NUMBER CHANGES
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            //Toast.makeText(context, "onUpgrade CALLED", Toast.LENGTH_SHORT).show();

            try {

                //region DROP TABLES
                db.execSQL(DROP_USER_TABLE);
                db.execSQL(DROP_RECIPE_TABLE);
                db.execSQL(DROP_FAVOURITES_TABLE);
                //endregion

                onCreate(db);   //CALLS THE CONSTRUCTOR TO BUILD THE NEW TABLES

            } catch (SQLException e) {
                e.printStackTrace();
                Toast.makeText(context, "" + e, Toast.LENGTH_SHORT).show(); //DISPLAYS ERROR
            }
        }
    }
}
