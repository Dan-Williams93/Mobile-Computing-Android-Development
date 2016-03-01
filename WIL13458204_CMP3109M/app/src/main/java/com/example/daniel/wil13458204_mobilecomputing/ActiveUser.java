package com.example.daniel.wil13458204_mobilecomputing;

import android.content.SharedPreferences;
import android.net.Uri;

import java.net.URI;

/**
 * Created by Daniel Williams - WIL13458204 - on 19/11/2015.
 * SINGLETON CLASS WHICH HOLDS THE DATA OF THE CURRENTLY ACTIVE USER, AN INSTANCE OF THIS CLASS CAN BE CALLED FROM ANY APPLICATION
 * ACTIVITY PROVIDING THE ABILITY TO SET AND GET USER DATA.
 */
public class ActiveUser {

    //region GLOBAL VARIABLES
    private static final ActiveUser instance = new ActiveUser(); //CREATES INSTANCE OF ACTIVE USER
    private String strUsername = "No Active User", strFirstName = null, strLastname = null, strUserID = null,
            strDefaultProfileImageUri = "android.resource://com.example.daniel.wil13458204_mobilecomputing/" + R.drawable.anonymous;
    private Integer intUserID = 0;
    private static final String strSharedPrefsName = "MyPreferences";
    private Uri profileImageUri = Uri.parse("android.resource://com.example.daniel.wil13458204_mobilecomputing/" + R.drawable.anonymous);
    //endregion

    private ActiveUser(){}

    //RETURNS INSTANCE OF SINGLETON ACTIVE USER CLASS
    public static ActiveUser getInstance(){
        return instance;
    }

    //region ACTIVE USER SET METHODS
    //SET ACTIVE USER ID
    public void setActiveUserID(int intUserID){
        this.intUserID = intUserID;
    }

    //SET ACTIVE USER FIRST NAME
    public void setActiveFirstName(String strFirstName){
        this.strFirstName = strFirstName;
    }

    //SET ACTIVE USER LAST NAME
    public void setActiveLastName(String strLastname){
        this.strLastname = strLastname;
    }

    //SET ACTIVE USER USERNAME
    public void setActiveUsername(String strUsername){
        this.strUsername = strUsername;
    }

    //SET ACTIVE USER PROFILE IMAGE PATH
    public void setProfileImageUri(String strProfilImageUri){
        this.profileImageUri = Uri.parse(strProfilImageUri);
    }
    //endregion

    //region ACTIVE USER GET METHODS
    //RETURNS ACTIVE USER ID
    public String getActiveUserID(){
        strUserID = String.valueOf(intUserID);
        return strUserID;
    }

    //RETURNS ACTIVE USER PROFILE IMAGE PATH
    public Uri getProfilePicUri(){
        return profileImageUri;
    }

    //GET ACTIVE USER FIRST NAME
    public String getActiveFirstName(){
       return strFirstName;
    }

    //GET ACTIVE USER LAST NAME
    public String getActiveLastName(){
        return strLastname;
    }

    //GET ACTIVE USERNAME
    public String getActiveUsername(){
        return strUsername;
    }

    //RETURNS DEFAULT PROFILE IMAGE PATH
    public String getDefaultProfilePicUri(){
        return strDefaultProfileImageUri;
    }
    //endregion

    //LOGS OUT ACTIVE USER RESETTING ACTIVE USER CLASS VARIABLES TO DEFAULT VALUES
    public void userLogout(){
        strUsername = "No Active User";
        strFirstName = null;
        strLastname = null;
        intUserID = 0;
        profileImageUri = Uri.parse(strDefaultProfileImageUri);
    }
}
