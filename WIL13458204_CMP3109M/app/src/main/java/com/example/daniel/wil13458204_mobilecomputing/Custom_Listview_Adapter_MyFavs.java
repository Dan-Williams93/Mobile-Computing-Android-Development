package com.example.daniel.wil13458204_mobilecomputing;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Daniel Williams - WIL13458204 - on 28/11/2015.
 * CUSTOM LISTVIEW ADAPTER THAT ALLOWS FOR THE USE OF CARDVIEW, IMAGEVIEW, TEXTVIEW AND A BUTTON IN EACH OF THE LIST ROWS
 */
public class Custom_Listview_Adapter_MyFavs extends ArrayAdapter {

    //region GLOBAL VARIABLES
    private Activity context;
    private ArrayList<String> recipeName;
    private ArrayList<Bitmap> recipeImage;
    private ArrayList<Integer> recipeID;
    private Integer intUserID;
    private Integer intRecipeID;
    private ActiveUser au = ActiveUser.getInstance();
    private SQLiteDatabaseAdapter myDatabaseAdapter = new SQLiteDatabaseAdapter(getContext());
    //endregion

    //ADAPTER CONSTRUCTOR RECEIVING THE CALLING CONTEXT PLUS ARRAY LISTS CONTAINING THE RECIPE NAMES, RECIPE IMAGES AND RECIPEID'S
    public Custom_Listview_Adapter_MyFavs(Activity context, ArrayList<String> strRecipeName, ArrayList<Bitmap> RecipeImage, ArrayList<Integer> intRecipeID) {
        super(context, R.layout.custom_listviewrow_myfavs, strRecipeName);

        //region SETTING GLOBAL VARIABLES VALUE FROM PASSED DATA
        this.context = context;
        this.recipeName = strRecipeName;
        this.recipeImage = RecipeImage;
        this.recipeID = intRecipeID;
        //endregion
    }

    public View getView(final int position, View view, ViewGroup parent){

        LayoutInflater inflater = context.getLayoutInflater();
        View viewRow = inflater.inflate(R.layout.custom_listviewrow_myfavs, null, true); //INFLATES THE LIST PLACING THE COMPONENTS OF THE STATED XML FILE IN EACH ROW

        //CASTS ROW COMPONENTS
        TextView txtRecipeName = (TextView) viewRow.findViewById(R.id.rowText);
        ImageView imRecipeImage = (ImageView) viewRow.findViewById(R.id.rowImage);
        Button btnRemove = (Button)viewRow.findViewById(R.id.btnRemove);

        //SETS THE VALUES OF THE COMPONENTS TO THE VALUES OF THE ARRAY DATA PASSED IN AT THE POSITION OF THE CURRENT ROW
        imRecipeImage.setImageBitmap(recipeImage.get(position));
        txtRecipeName.setText(recipeName.get(position));

        btnRemove.setOnClickListener(new View.OnClickListener() {   //CREATES ONCLICK LISTENER FOR EACH ROW REMOVAL BUTTON
            @Override
            public void onClick(View v) {

                //region ALERT DIALOG FOR RECIPE REMOVAL
                AlertDialog.Builder alertNoActiveUser = new AlertDialog.Builder(getContext());
                alertNoActiveUser.setTitle("Remove from favourites");
                alertNoActiveUser.setMessage("Are you sure you want to remove this reipe from your favourites");
                alertNoActiveUser.setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                //region REMOVAL OF RECIPE
                                //GETS ACTIVE USER ID AND THE RECIPE ID OF THE RECIPE IN THE CURRENT ROW
                                intUserID = Integer.parseInt(au.getActiveUserID());
                                intRecipeID = recipeID.get(position);

                                //CALLS DATABASE METHOD TO DELETE THE RECIPE FROM USER FAVOURITES PASSING THE USER ID AND RECIPE ID
                                int intCount = myDatabaseAdapter.dbDeleteSingleFavourite(intUserID, intRecipeID);

                                //CHECKS ITEM HAS BEEN REMOVED FROM THE USERS FAVOURITES
                                if (intCount != 0) {

                                    //REMOVES ITEMS FROM THE ARRAY LISTS
                                    recipeName.remove(position);
                                    recipeImage.remove(position);

                                    notifyDataSetChanged(); //REMOVES ITEM FROM UI

                                    //REMOVES RECIPE ID FROM ARRAY LIST
                                    recipeID.remove(position);

                                    //RECREATES THE FAVOURITES ACTIVITY UPDATING THE STORED FAVOURITE DATA
                                    context.recreate();

                                    Toast.makeText(context, "Recipe Successfully Removed", Toast.LENGTH_SHORT).show();

                                }else
                                    Toast.makeText(getContext(), "Error Removing Recipe", Toast.LENGTH_SHORT).show();
                                //endregion
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();   //CLOSES OPEN DIALOG
                            }
                        });
                alertNoActiveUser.show();   //SHOWS CREATED DIALOG MESSAGE
                //endregion
            }
        });
        return viewRow;
    }
}
