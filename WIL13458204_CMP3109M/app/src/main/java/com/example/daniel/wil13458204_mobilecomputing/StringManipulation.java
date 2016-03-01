package com.example.daniel.wil13458204_mobilecomputing;

/**
 * Created by Daniel on 18/11/2015.
 */
public class StringManipulation {

    //USED TO MANIPULATE THE RECIPE STRINGS RETURNED FROM THE API QUERY FOR DISPLAYING PURPOSES
    public String StringManipulations(String strString){

        //EACH MANIPULATION REPLACES A SPECIFIED STRING WITH A NULL VALUE
        strString = strString.replaceAll("<strong>", "");
        strString = strString.replaceAll("</strong>", "");
        strString = strString.replaceAll("</em>", "");
        strString = strString.replaceAll("<em>", "");
        strString = strString.replaceAll("<b>", "");
        strString = strString.replaceAll("</b>", "");
        strString = strString.replaceAll("&#160;", "");
        strString = strString.replaceAll("&#174;", "");
        strString = strString.replaceAll("&#176;", "");
        strString = strString.replaceAll("&#39;", "");
        strString = strString.replaceAll("</i>", "");
        strString = strString.replaceAll("<i>", "");
        strString = strString.replaceAll("&quot;", "");
        strString = strString.replaceAll("<br />", "");
        strString = strString.replaceAll("&#251;", "");
        strString = strString.replaceAll("&#146;", "");
        strString = strString.replaceAll("&#8217;", "");
        strString = strString.replaceAll("&#214;", "");
        strString = strString.replaceAll("&#241;", "");
        strString = strString.replaceAll("&#231;", "");
        strString = strString.replaceAll("&#233;", "");
        strString = strString.replaceAll("&#133;", "");
        strString = strString.replaceAll("&#131;", "");
        strString = strString.replaceAll("</span", "");
        strString = strString.replaceAll("<span lang=", "");
        strString = strString.replaceAll("\"", "");

        return strString;
    }

    //USED TO MANIPULATE THE RECIPE TIMING STRINGS RETURNED FROM THE API QUERY FOR DISPLAYING PURPOSES
    public String TimingManipulation(String strTiming) {
        strTiming = strTiming.replaceAll("mins", "m");
        strTiming = strTiming.replaceAll("hours", "h");
        strTiming = strTiming.replace("hour", "h");

        return strTiming;
    }
}
