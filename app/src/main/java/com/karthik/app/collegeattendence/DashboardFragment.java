package com.karthik.app.collegeattendence;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
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
import java.net.URLEncoder;

public class DashboardFragment extends Fragment {

    public static android.support.v4.app.Fragment newInstance() {
        DashboardFragment mFrgment = new DashboardFragment();
        return mFrgment;
    }
    public DashboardFragment() {
        // Required empty public constructor
    }

    JSONObject jsonObject=null;
    FileManagement fm=new FileManagement();
    String offline="";
    boolean isThreadComplete=false;
    String a="";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v=inflater.inflate(R.layout.fragment_dashboard, container, false);
        String js=fm.getTextFromFile(getString(R.string.josn_file_name_offline_att));
        offline=URLEncoder.encode(fm.getTextFromFile(getString(R.string.josn_file_name_offline_att)));
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                HttpSendOfflineAttendance http=new HttpSendOfflineAttendance();
                a=http.doInBackground("offline="+offline);
                isThreadComplete=true;
            }
        });
        if(offline.equals("")){
            //Toast.makeText(v.getContext(),"No offline data",Toast.LENGTH_SHORT).show();
        }else{
            try {
                isThreadComplete = false;
                thread.start();
                while (!isThreadComplete) ;
            }catch (Exception e){
                e.printStackTrace();
            }
            if(a.trim().equals("success")){
                FileOutputStream outputStream;
                String filename=getString(R.string.josn_file_name_offline_att);
                try {
                    outputStream = v.getContext().openFileOutput(filename, Context.MODE_PRIVATE);
                    outputStream.write(("").getBytes());
                    outputStream.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            //Toast.makeText(v.getContext(),"Inside Has offline data",Toast.LENGTH_SHORT).show();
        }
        final Button attendance=(Button)v.findViewById(R.id.button);
        final Button exception=(Button)v.findViewById(R.id.button2);
        final Button option=(Button)v.findViewById(R.id.button3);
        final Button logout=(Button)v.findViewById(R.id.button4);
        final TextView name=(TextView)v.findViewById(R.id.dashboardFragmantUserNameTextView);
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
            name.setText(jsonObject.getString("name"));
        }catch (Exception e){
            e.printStackTrace();
        }
        attendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fg = ClasslistFragment.newInstance();
                FragmentManager manager = getFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.fragmentholder, fg);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        exception.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fg = ExceptionsFragment.newInstance();
                FragmentManager manager = getFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.fragmentholder, fg);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        option.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileOutputStream outputStream;
                String filename=getString(R.string.josn_file_name);
                try {
                    outputStream = v.getContext().openFileOutput(filename, Context.MODE_PRIVATE);
                    outputStream.write(("").getBytes());
                    outputStream.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
                Fragment fg = MainActivityFragment.newInstance();
                FragmentManager manager=getFragmentManager();
                manager.popBackStack();
                FragmentTransaction transaction=manager.beginTransaction();
                transaction.replace(R.id.fragmentholder, fg);
                transaction.commit();
                //Toast.makeText(v.getContext(),"Settings Under Development",Toast.LENGTH_SHORT).show();
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fg = TimetableFragment.newInstance();
                FragmentManager manager = getFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.fragmentholder, fg);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        return v;
    }
}
class HttpSendOfflineAttendance extends AsyncTask<String, Void, String>
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
            httpget = new HttpGet(Gobal.CallUrl+"offline.php?"+get_url);
            String content = Client.execute(httpget, responseHandler);
            return content;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            //Toast.makeText(MainActivity.getAppContext(),"Error In Connection",Toast.LENGTH_SHORT).show();
        }
        return "connection Failed";
    }
}