package com.karthik.app.collegeattendence;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends ActionBarActivity {


    public static android.support.v4.app.Fragment newInstance() {
        MainActivityFragment mFrgment = new MainActivityFragment();
        return mFrgment;
    }


    private static Context context;
    String a="";
    boolean server=false;
    boolean isThreadComplete=false;
    boolean session=false;
    Date serverDate;
    Date serverTime;
    String timeJson="";
    private Timer timer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainActivity.context = getApplicationContext();
        final String versionName=BuildConfig.VERSION_NAME;
        final int versionCode=BuildConfig.VERSION_CODE;

        final Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpVersion http = new HttpVersion();
                    a = http.doInBackground("versionName=" + versionName + "&versionCode=" + versionCode);
                }catch (Exception e){
                    e.printStackTrace();
                }
                isThreadComplete=true;
            }
        });
        isThreadComplete=false;
        thread.start();
        while(!isThreadComplete);
        if(a.toString().trim().equals("updatenow")){
            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
            alert.setCancelable(false);
            alert.setMessage("Update Available");
            alert.setPositiveButton("Update Now", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Thread thread1=new Thread(new Runnable() {
                        @Override
                        public void run() {
                            InstallApk apk=new InstallApk();
                            apk.doInBackground("apkfile/Attendance.apk");
                        }
                    });
                    thread1.start();
                }
            });
           // alert.setNegativeButton("Update Later", new DialogInterface.OnClickListener() {
          //     public void onClick(DialogInterface dialog, int whichButton) {
           //      dialog.cancel();
            //   }
         //  });
            AlertDialog alertDialog = alert.create();
            alertDialog.show();
        }

        InputStream inputStream=null;

        String imput="";
        try{
            Thread thread1=new Thread(new Runnable() {
                @Override
                public void run() {
                    HttpTime http=new HttpTime();
                    timeJson=http.doInBackground(Gobal.CallUrl+"time.php");
                    isThreadComplete=true;
                }
            });
            isThreadComplete=false;
            thread1.start();
            while (!isThreadComplete);
        }catch (Exception e){
            e.printStackTrace();
        }
        SimpleDateFormat smp1=new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat smp2=new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat smp3=new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat smp4=new SimpleDateFormat("HH:mm");
        try{
            JSONObject jsonObject=new JSONObject(timeJson);
            serverDate=smp2.parse(jsonObject.getString("date"));
            serverTime=smp3.parse(jsonObject.getString("time"));
            server=true;
        }catch (Exception e){
            e.printStackTrace();
            Calendar c = Calendar.getInstance();
            try {
                serverDate = smp2.parse(smp2.format(c.getTime()));
                serverTime = smp3.parse(smp3.format(c.getTime()));
                server=true;
            }catch (Exception e1){
                e1.printStackTrace();
            }
        }
            try {
                inputStream = getAppContext().openFileInput(getString(R.string.josn_file_name));
                if (inputStream != null) {
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String receiveString = "";
                    StringBuilder stringBuilder = new StringBuilder();

                    while ((receiveString = bufferedReader.readLine()) != null) {
                        stringBuilder.append(receiveString);
                    }
                    inputStream.close();
                    imput = stringBuilder.toString();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        try{
            if(server){
                JSONObject jsonObject=new JSONObject(imput);
                Date dd=smp2.parse(jsonObject.getString("Date"));
                Date dt=smp3.parse(jsonObject.getString("time"));
                if(smp2.format(dd).equals(smp2.format(serverDate))){
                    long l=serverTime.getTime()-dt.getTime();
                    int diffhours = (int) (l / (60 * 60 * 1000));
                    if(diffhours>0){
                        session=true;
                    }
                }else{
                    session=true;
                }
            }else{
                session=false;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
            if ((imput.equals(null) || inputStream == null || imput.equals(""))||session) {
                addFragments();
            } else {
                Fragment fg = DashboardFragment.newInstance();
                FragmentManager manager = getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.fragmentholder, fg);
                transaction.commit();
            }

    }


    public static Context getAppContext() {
        return MainActivity.context;
    }
    private void addFragments(){
        Fragment fg = MainActivityFragment.newInstance();
        FragmentManager manager=getSupportFragmentManager();
        FragmentTransaction transaction=manager.beginTransaction();
        transaction.replace(R.id.fragmentholder, fg);
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            FileOutputStream outputStream;
            String filename=getString(R.string.josn_file_name);
            try {
                outputStream = getAppContext().openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(("").getBytes());
                outputStream.close();
            }catch (Exception e){
                e.printStackTrace();
            }
            Fragment fg = MainActivityFragment.newInstance();
            FragmentManager manager=getSupportFragmentManager();
            manager.popBackStack();
            FragmentTransaction transaction=manager.beginTransaction();
            transaction.replace(R.id.fragmentholder, fg);
            transaction.commit();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
       } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }
    }

}
class HttpVersion extends AsyncTask<String, Void, String>
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
            httpget = new HttpGet(Gobal.CallUrl+"version.php?"+get_url);
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