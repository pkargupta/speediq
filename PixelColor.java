package com.agnik.priyankakargupta.speediq;

import android.app.Activity;

import java.util.HashMap;

/**
 * Created by Priyanka Kargupta on 4/22/2017.
 */
public class PixelColor {
    private HashMap<String, String> colorvals;
    public String selectedcolor;
    public boolean validcolor;
    private static final String[] possiblecolors = {"red", "orange", "yellow", "green", "blue", "black", "white", "grey", "cyan", "magenta"};
    private Activity activity;

    public PixelColor(Activity activity){
        colorvals = new HashMap();
        this.activity = activity;
        //setUp();
    }
    public void setSelectedColor(String color){
        selectedcolor = color;
    }
    public boolean isColor(float[] hsv){

        validcolor = checkColor(selectedcolor);
        if(validcolor) {
            String tempVal = "";
            if (hsv[2] < 0.2) tempVal = "black";
            else if (hsv[2] > 0.8) tempVal = "white";
            else if (hsv[1] < 0.25) tempVal = "grey";
            else if (hsv[0] < 30) tempVal = "red";
            else if (hsv[0] < 90) tempVal = "yellow";
            else if (hsv[0] < 150) tempVal = "green";
            else if (hsv[0] < 210) tempVal = "cyan";
            else if (hsv[0] < 270) tempVal = "blue";
            else if (hsv[0] < 330) tempVal = "magenta";
            else tempVal = "red";

            return tempVal.equalsIgnoreCase(selectedcolor);

        }
        return false;
    }
    public String getColor(float[] hsv){
        String tempVal = "";
        if (hsv[2] < 0.2) tempVal = "black";
        else if (hsv[2] > 0.8) tempVal = "white";
        else if (hsv[1] < 0.25) tempVal = "grey";
        else if (hsv[0] < 30) tempVal = "red";
        else if (hsv[0] < 90) tempVal = "yellow";
        else if (hsv[0] < 150) tempVal = "green";
        else if (hsv[0] < 210) tempVal = "cyan";
        else if (hsv[0] < 270) tempVal = "blue";
        else if (hsv[0] < 330) tempVal = "magenta";
        else tempVal = "red";

        return tempVal;
    }
    /*
    private void setUp(){
        try {
            //FileReader fr = new FileReader("colors.txt");
            InputStream is = activity.getResources().openRawResource(R.raw.colors);

            String sCurrentLine;
            //BufferedReader br = new BufferedReader(new FileReader("colors.txt"));
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));


            while ((sCurrentLine = br.readLine()) != null) {
                String[] current = sCurrentLine.split(" ");
                //colorvals.put(new int[]{Integer.valueOf(current[0]), Integer.valueOf(current[1]), Integer.valueOf(current[2])}, current[3]);
                colorvals.put(current[0] + "," + current[1] + "," + current[2], current[3]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    */
    private boolean checkColor(String color) {
        for (int i = 0; i < possiblecolors.length; i++) {
            if (possiblecolors[i].equals(color))
                return true;
        }
        return false;
    }
}