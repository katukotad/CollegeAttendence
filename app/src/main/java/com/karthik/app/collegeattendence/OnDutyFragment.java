package com.karthik.app.collegeattendence;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
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
import java.io.EOFException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class OnDutyFragment extends Fragment {


    public OnDutyFragment() {
        // Required empty public constructor
    }
    boolean isThreadComplete = false;
    JSONObject jsonObject=null;
    private DatePickerDialog fromDatePickerDialog;
    private DatePickerDialog toDatePickerDialog;
    SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd");
    String a="",b="";
    String timeJson="",serverTime="",serverDate="";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v=inflater.inflate(R.layout.fragment_on_duty, container, false);

        final EditText fromdate=(EditText)v.findViewById(R.id.ondutyFragmentFromDateEditText);
        final EditText todate=(EditText)v.findViewById(R.id.ondutyFragmentToDateEditText);
        ImageView fromDate=(ImageView)v.findViewById(R.id.ondutyFragmentFromDateImageButton);
        ImageView toDate=(ImageView)v.findViewById(R.id.ondutyFragmentToDateImageButton);
        final EditText rollNo=(EditText)v.findViewById(R.id.ondutyFragmentRollNoEditText);
        final Spinner reason=(Spinner)v.findViewById(R.id.ondutyFragmentReasonSpinner);
        final EditText description=(EditText)v.findViewById(R.id.ondutyFragmentDescriptionEditText);
        Button save=(Button)v.findViewById(R.id.ondutySaveButton);
        final TextView studentName=(TextView)v.findViewById(R.id.ondutyFragmentStudentTextView);


        rollNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                Thread thread=new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            HttpStudentName http=new HttpStudentName();
                            b=http.doInBackground("rollNumber=" + rollNo.getText().toString().trim().replace("%"," "));
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        isThreadComplete=true;
                    }
                });
                isThreadComplete=false;
                thread.start();
                while (!isThreadComplete);
                studentName.setText(b.trim());
                if(s.toString().length()!=0){
                    studentName.setVisibility(View.VISIBLE);
                }else{
                    studentName.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


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
            serverDate=smp2.format(date1);
            Date date2=smp3.parse(jsonObject1.getString("time"));
            serverTime=smp3.format(date2);
        }catch (Exception e){
            e.printStackTrace();
            Calendar c=Calendar.getInstance();
            serverDate=smp2.format(c.getTime());
            serverTime=smp4.format(c.getTime());
        }

        Calendar cal = Calendar.getInstance();
        fromdate.setText(serverDate);
        todate.setText(serverDate);

        fromDatePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    Calendar newDate = Calendar.getInstance();
                    newDate.set(year, monthOfYear, dayOfMonth);
                    fromdate.setText(df.format(newDate.getTime()));
                }
            },Integer.parseInt(fromdate.getText().toString().split("-")[0]), Integer.parseInt(fromdate.getText().toString().split("-")[1])-1,Integer.parseInt(fromdate.getText().toString().split("-")[2]));

        toDatePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                todate.setText(df.format(newDate.getTime()));
            }
        },Integer.parseInt(todate.getText().toString().split("-")[0]), Integer.parseInt(todate.getText().toString().split("-")[1])-1,Integer.parseInt(todate.getText().toString().split("-")[2]));

        fromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            fromDatePickerDialog.show();
            }
        });
        toDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toDatePickerDialog.show();
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final HttpOnDuty http=new HttpOnDuty();
                if(rollNo.getText().toString().trim().length()>5){
                    try {
                        if (df.parse(fromdate.getText().toString()).before(df.parse(todate.getText().toString()))||fromdate.getText().toString().equals(todate.getText().toString())){
                            if(description.getText().toString().trim().length()>=5){

                                Thread thread=new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            InputStream inputStream = getActivity().openFileInput(getString(R.string.josn_file_name));
                                            if (inputStream != null) {
                                                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                                                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                                                String receiveString = "";
                                                StringBuilder stringBuilder = new StringBuilder();

                                                while ((receiveString = bufferedReader.readLine()) != null) {
                                                    stringBuilder.append(receiveString);
                                                }

                                                inputStream.close();
                                                jsonObject = new JSONObject(stringBuilder.toString());
                                            }
                                            Calendar c = Calendar.getInstance();
                                            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                            a = http.doInBackground("&rollNumber=" + rollNo.getText().toString().trim() + "&date=" + df.format(c.getTime()).toString() + "&staffId=" + jsonObject.optString("staffId") + "&fromdate=" + fromdate.getText().toString() + "&todate=" + todate.getText().toString() + "&type=" + reason.getSelectedItemPosition() + "&description=" + description.getText().toString().trim().replace("( )+",""));
                                            Toast.makeText(getActivity(), a, Toast.LENGTH_SHORT).show();
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                        isThreadComplete=true;
                                    }
                                });
                                isThreadComplete=false;
                                thread.start();
                                while (!isThreadComplete);
                                if(a.trim().equals("success")) {
                                    Fragment fg = DashboardFragment.newInstance();
                                    FragmentManager manager = getFragmentManager();
                                    manager.popBackStack();
                                    FragmentTransaction transaction = manager.beginTransaction();
                                    transaction.replace(R.id.fragmentholder, fg);
                                    transaction.commit();
                                    InputMethodManager inputManager = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                    inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                                    Toast.makeText(getActivity(), "Saved", Toast.LENGTH_SHORT).show();
                                }else if(a.trim().equals("No stundent Exit")){
                                    rollNo.setError("No student Exist");
                                    rollNo.requestFocus();
                                    Toast.makeText(getActivity(),"No student Exist", Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(getActivity(), "Connection Error", Toast.LENGTH_SHORT).show();
                                }
                            }else {
                               description.setError("Description minimum length is six");
                                description.requestFocus();
                            }
                        }else {
                            Toast.makeText(v.getContext(),"From and To date Error",Toast.LENGTH_SHORT).show();
                        }
                        }catch (Exception e){
                            e.printStackTrace();
                    }
                    }else{
                    rollNo.setError("Roll No minimum length is six");
                    rollNo.requestFocus();
                }
            }
        });
        return v;
    }

    public static android.support.v4.app.Fragment newInstance() {
        OnDutyFragment mFrgment = new OnDutyFragment();
        return mFrgment;
    }
}
class HttpOnDuty extends AsyncTask<String, Void, String>
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
            httpget = new HttpGet(Gobal.CallUrl+"odandmedical.php?"+get_url);
            String content = Client.execute(httpget, responseHandler);
            return content;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            //Toast.makeText(MainActivity.getAppContext(),"Connection Failed",Toast.LENGTH_SHORT).show();
        }
        return "connection Failed";
    }
}
class HttpStudentName extends AsyncTask<String, Void, String>
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
            httpget = new HttpGet(Gobal.CallUrl+"studentName.php?"+get_url);
            String content = Client.execute(httpget, responseHandler);
            return content;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            //Toast.makeText(MainActivity.getAppContext(),"Connection Failed",Toast.LENGTH_SHORT).show();
        }
        return "connection Failed";
    }
}