package com.karthik.app.collegeattendence;


import android.util.Log;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class
FileManagement {
    public String getTextFromFile(String fileName){
        try {
            InputStream inputStream = MainActivity.getAppContext().openFileInput(fileName);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                return stringBuilder.toString();
            }else{
                return "";
            }
        }
        catch (FileNotFoundException e) {
            Log.e("Json File read", "File not found: " + e.toString());
        } catch (Exception e) {
            e.printStackTrace();
        };
        return "filenotexist";
    }
}
