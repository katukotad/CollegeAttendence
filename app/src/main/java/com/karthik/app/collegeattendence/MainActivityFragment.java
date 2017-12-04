package com.karthik.app.collegeattendence;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    public MainActivityFragment() {
    }

    String timeJson="",serverTime="",serverDate="",mobileDate="";

//    private Timer timer;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View  v=inflater.inflate(R.layout.fragment_login, container, false);

        final EditText userId=(EditText)v.findViewById(R.id.loginFragmentUseridEditText);
        final EditText password=(EditText)v.findViewById(R.id.loginFragmentPasswordEditText);
        final Button login=(Button)v.findViewById(R.id.loginButton);
        try {
            SharedPreferences sharedPref = getActivity().getSharedPreferences(
                    getString(R.string.preference_file_key), getActivity().MODE_PRIVATE);
            String sessionvalue = sharedPref.getString(getString(R.string.sharedpreference_session), getString(R.string.sharedpreference_session_default_value));
        }catch (Exception e){
            Log.e("Error session",e.toString());
        }
        TelephonyManager telephonyManager = (TelephonyManager)v.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.getDeviceId();
//        String uri = "@drawable/error_warning_icon.png";
//        int imageResource = getResources().getIdentifier(uri, null,null);
//        final Drawable res = getResources().getDrawable(imageResource);

       /* login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String co="";
                login.setEnabled(false);
                if(userId.getText().toString().trim().length()>=6){
                    if(password.getText().toString().trim().length()>=4) {
                            co=loginCall(userId.getText().toString().trim(),password.getText().toString().trim());
                       if (co.equals("1")) {
                           Fragment fg = DashboardFragment.newInstance();
                           FragmentManager manager = getFragmentManager();
                           FragmentTransaction transaction = manager.beginTransaction();
                           transaction.replace(R.id.fragmentholder, fg);
                           InputMethodManager inputManager = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                           inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                           //userId1.setTitle(userId.getText().toString().trim()+"\nLogout");
                           transaction.commit();
                       }else if(co.equals("2")) {
                           Toast.makeText(v.getContext(), "Connection Error contact IT team", Toast.LENGTH_LONG).show();
                       }else{
                           Toast.makeText(v.getContext(), "Invalid Password or User Id", Toast.LENGTH_LONG).show();
                           password.setText("");
                       }
                    }
                    else{
                        password.setError("Minimum password's length 4");
                    }
                }else{
                    userId.setError("Minimum user Id's length 6");
                }

                login.setEnabled(true);
            }
        });*/
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject jsonObject=null;
                try{
                    Thread thread=new Thread(new Runnable() {
                        @Override
                        public void run() {
                            HttpTime http=new HttpTime();
                            timeJson=http.doInBackground(Gobal.CallUrl+"time.php");
                            isThreadComplete=true;
                        }
                    });
                    isThreadComplete=false;
                    thread.start();
                    while (!isThreadComplete);
                }catch (Exception e){
                    e.printStackTrace();
                }
                SimpleDateFormat smp1=new SimpleDateFormat("dd-MM-yyyy");
                SimpleDateFormat smp2=new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat smp3=new SimpleDateFormat("HH:mm:ss");
                SimpleDateFormat smp4=new SimpleDateFormat("HH:mm");
                try{
                    JSONObject jsonObject1=new JSONObject(timeJson);
                    Date date1=smp2.parse(jsonObject1.getString("date"));
                    serverDate=smp1.format(date1);
                    Date date2=smp3.parse(jsonObject1.getString("time"));
                    serverTime=smp4.format(date2);
                }catch (Exception e){
                    e.printStackTrace();
                    Calendar c=Calendar.getInstance();
                    serverDate=smp1.format(c.getTime());
                    serverTime=smp4.format(c.getTime());
                }

                try{
                    InputStream inputStream = getActivity().openFileInput(getString(R.string.josn_file_name));
                    if ( inputStream != null ) {
                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                        String receiveString = "";
                        StringBuilder stringBuilder = new StringBuilder();

                        while ( (receiveString = bufferedReader.readLine()) != null ) {
                            stringBuilder.append(receiveString);
                        }

                        inputStream.close();
                        jsonObject =new JSONObject( stringBuilder.toString());
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                SimpleDateFormat d2 = new SimpleDateFormat("dd-MM-yyyy");
                Calendar calendar = Calendar.getInstance();
                String mobileDate = d2.format(calendar.getTime());

                try {
                    if (serverDate.equals(mobileDate)) {

                        String co="";
                        login.setEnabled(false);
                        if(userId.getText().toString().trim().length()>=6){
                            if(password.getText().toString().trim().length()>=4) {
                                co=loginCall(userId.getText().toString().trim(),password.getText().toString().trim());
                                if (co.equals("1")) {
                                    Fragment fg = DashboardFragment.newInstance();
                                    FragmentManager manager = getFragmentManager();
                                    FragmentTransaction transaction = manager.beginTransaction();
                                    transaction.replace(R.id.fragmentholder, fg);
                                    InputMethodManager inputManager = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                    inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                                    //userId1.setTitle(userId.getText().toString().trim()+"\nLogout");
                                    transaction.commit();
                                }else if(co.equals("2")) {
                                    Toast.makeText(v.getContext(), "Connection Error contact IT team", Toast.LENGTH_LONG).show();
                                }else{
                                    Toast.makeText(v.getContext(), "Invalid Password or User Id", Toast.LENGTH_LONG).show();
                                    password.setText("");
                                }
                            }
                            else{
                                password.setError("Minimum password's length 4");
                            }
                        }else{
                            userId.setError("Minimum user Id's length 6");
                        }
                        login.setEnabled(true);

                    }else{
                        Toast.makeText(v.getContext(), "Please change your mobile Date TO Current Date", Toast.LENGTH_LONG).show();
                    }


                }catch (Exception e){
                    e.printStackTrace();
                }


            }
        });


        return v;
    }
    public static android.support.v4.app.Fragment newInstance() {
        MainActivityFragment mFrgment;
        mFrgment = new MainActivityFragment();
        return mFrgment;
    }

    //login Server call starts
    String a1="";
    JSONObject a=null;
    boolean isThreadComplete=false;

    private String loginCall(final String userId,final String password) {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpSend http = new HttpSend();
                    a=new JSONObject(http.doInBackground("?rollNumber=" + userId + "&password=" + password));
                    Log.d("Respone",a.toString());
                    a1= a.getString("login");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                isThreadComplete = true;
            }
        });
        isThreadComplete=false;
        thread.start();
        FileOutputStream outputStream;
        String filename=getString(R.string.josn_file_name);
        while( ! isThreadComplete );
        if(a1.equals("1")){
           try {
               outputStream = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
               outputStream.write(a.toString().getBytes());
               outputStream.close();
           }catch (Exception e){
            e.printStackTrace();
            }
        }else {
            return "2";
        }
        return a1;
    }

    //Login Server Call Ends
}
 class HttpSend extends AsyncTask<String, Void, String>
{
    @Override
    protected String doInBackground(String... str) {
        try
        {
            String get_url = str[0].replace(" ", "%20");
            HttpParams httpParameters = new BasicHttpParams();
            int timeoutConnection = 3000;
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            int timeoutSocket = 60000;
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
            HttpClient Client = new DefaultHttpClient(httpParameters);
            HttpGet httpget;
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            httpget = new HttpGet(Gobal.CallUrl+"login.php"+get_url);
            String content = Client.execute(httpget, responseHandler);
            return content;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return "connection Failed";
    }
}