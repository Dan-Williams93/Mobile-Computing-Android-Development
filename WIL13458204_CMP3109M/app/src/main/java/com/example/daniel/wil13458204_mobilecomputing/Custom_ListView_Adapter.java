package com.example.daniel.wil13458204_mobilecomputing;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Daniel Williams - WIL13458204 - on 16/11/2015.
 * CUSTOM LISTVIEW ADAPTER THAT ALLOWS FOR THE USE OF CARDVIEW, IMAGEVIEW AND TEXTVIEW IN EACH OF THE LIST ROWS
 */
public class Custom_ListView_Adapter extends ArrayAdapter{

    //region GLOBAL VARIABLES
    private Activity context;   //GETS CONTEXT OF CALLING ACTIVITY
    private ArrayList<String> recipeName;
    private ArrayList<Bitmap> recipeImage;
    //endregion

    //ADAPTER CONSTRUCTOR RECEIVING THE CALLING CONTEXT PLUS ARRAY LISTS CONTAINING THE RECIPE NAMES AND RECIPE IMAGES
    public Custom_ListView_Adapter(Activity context, ArrayList<String> strRecipeName, ArrayList<Bitmap> RecipeImage) {
        super(context, R.layout.custom_listviewrow, strRecipeName);

        //region SETTING GLOBAL VARIABLES VALUES FROM PASSED DATA
        this.context = context;
        this.recipeName = strRecipeName;
        this.recipeImage = RecipeImage;
        //endregion
    }

    public View getView(int position, View view, ViewGroup parent){

            LayoutInflater inflater = context.getLayoutInflater();
            View viewRow = inflater.inflate(R.layout.custom_listviewrow, null, true);       //INFLATES THE LIST PLACING THE COMPONENTS OF THE STATED XML FILE IN EACH ROW

            //CASTS ROW COMPONENTS
            TextView txtRecipeName = (TextView) viewRow.findViewById(R.id.rowText);
            ImageView imRecipeImage = (ImageView) viewRow.findViewById(R.id.rowImage);

            //SETS THE VALUES OF THE COMPONENTS TO THE VALUES OF THE ARRAY DATA PASSED IN AT THE POSITION OF THE CURRENT ROW
            imRecipeImage.setImageBitmap(recipeImage.get(position));
            txtRecipeName.setText(recipeName.get(position));

            return viewRow;
    }
}
