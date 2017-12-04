package com.karthik.app.collegeattendence;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
/**
 * A simple {@link Fragment} subclass.
 */
public class LateAttendanceStudentSelectFragment extends Fragment {
    public LateAttendanceStudentSelectFragment() {
        // Required empty public constructor
    }
    String list="";
    String[] studentArray;
    Boolean isThreadComplete=false;
    String a=null;
    JSONObject jsonObject1=new JSONObject();
    JSONArray jsonArray1=new JSONArray();
    JSONObject mainJsonObject=new JSONObject();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v=inflater.inflate(R.layout.fragment_late_attendance_student_select, container, false);

        final TextView heading=(TextView)v.findViewById(R.id.lateAttendanceClassNameTextView);
        final ListView listclass=(ListView)v.findViewById(R.id.lateAttendanceListView);
        final Button save=(Button)v.findViewById(R.id.lateAttendanceSaveButton);
        String strtext=getArguments().getString("class");
        heading.setText(strtext);

        try{
            list=getArguments().getString("student");
            studentArray=list.replace("[","").replace("]","").split(",");
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            listclass.setChoiceMode(listclass.CHOICE_MODE_MULTIPLE);
            listclass.setAdapter(new ArrayAdapter<String>(v.getContext(),
                    android.R.layout.simple_list_item_checked, studentArray));
        }catch (Exception e){
            e.printStackTrace();
        }
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SparseBooleanArray choices = listclass.getCheckedItemPositions();
                        String choosed="";
                        StringBuilder choicesString = new StringBuilder();
                        for (int i = 0; i < choices.size(); i++)
                        {

                            if(choices.valueAt(i) == true) {
                                if (i != (choices.size() - 1))
                                    choicesString.append(studentArray[choices.keyAt(i)].split("-")[0]).append(",");
                                else
                                    choicesString.append(studentArray[choices.keyAt(i)].split("-")[0]);
                            }
                        }
                        JSONObject jsonObject = null;
                        String absentees=choicesString.toString();
                        String[] classDetails=getArguments().getString("class").split("::");
                        try {
                            HttpSendLateAttendance http = new HttpSendLateAttendance();
                            {
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
                            }
                            Calendar c = Calendar.getInstance();
                            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                            SimpleDateFormat tf = new SimpleDateFormat("HH:mm:ss");
                            a=http.doInBackground("year="+classDetails[1]+"&rollNumber="+absentees+"&department="+classDetails[0]+"&section="+classDetails[2]+"&date="+df.format(c.getTime()).toString()+"&time="+tf.format(c.getTime()).toString()+"&staffId="+jsonObject.optString("staffrollnumber")+"&subject="+classDetails[3]);
                            //Toast.makeText(getActivity(), a, Toast.LENGTH_LONG).show();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        isThreadComplete = true;
                    }
                });
                isThreadComplete=false;
                ProgressDialog progressDialog = ProgressDialog.show(v.getContext(), "", "Please Wait", true,false);
                thread.start();
                while(!isThreadComplete);
                progressDialog.dismiss();
                if(a.trim().equals("success")) {
                    Fragment fg = DashboardFragment.newInstance();
                    FragmentManager manager = getFragmentManager();
                    manager.popBackStack();
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.replace(R.id.fragmentholder, fg);
                    transaction.commit();
                    Toast.makeText(getActivity(), "Saved", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(v.getContext(),"Connection Error",Toast.LENGTH_SHORT).show();
                        AlertDialog.Builder builder2=new AlertDialog.Builder(v.getContext());
                        builder2.setMessage("To save Late attendance in offline");
                        builder2.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                FileOutputStream outputStream;
                                SparseBooleanArray choices = listclass.getCheckedItemPositions();
                                String choosed = "";
                                StringBuilder choicesString = new StringBuilder();
                                for (int i = 0; i < choices.size(); i++) {

                                    if (choices.valueAt(i) == true) {
                                        if (i != (choices.size() - 1))
                                            choicesString.append(studentArray[choices.keyAt(i)].split(" ")[0]).append(",");
                                        else
                                            choicesString.append(studentArray[choices.keyAt(i)].split(" ")[0]);
                                    }
                                }
                                JSONObject jsonObject = null;
                                String absentees = choicesString.toString();
                                String[] classDetails = getArguments().getString("class").split("::");
                                try {
                                    HttpSendAttendance http = new HttpSendAttendance();
                                    {
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
                                    }
                                    Calendar c = Calendar.getInstance();
                                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                                    SimpleDateFormat tf = new SimpleDateFormat("HH:mm:ss");

                                    jsonObject1.put("year", classDetails[1]);
                                    jsonObject1.put("rollNumber", absentees);
                                    jsonObject1.put("department", classDetails[0]);
                                    jsonObject1.put("section", classDetails[2]);
                                    jsonObject1.put("date", df.format(c.getTime()).toString());
                                    jsonObject1.put("time", tf.format(c.getTime()).toString());
                                    jsonObject1.put("staffId", jsonObject.optString("staffrollnumber"));
                                    jsonObject1.put("subject", classDetails[3]);
                                    JSONArray jsonArray2=new JSONArray();
                                    jsonArray2.put(jsonObject1);
                                    mainJsonObject.put("class",jsonArray2);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                try {
                                    InputStream inputStream = getActivity().openFileInput(getString(R.string.json_file_name_offline_late));
                                    if (inputStream != null) {
                                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                                        String receiveString = "";
                                        StringBuilder stringBuilder = new StringBuilder();

                                        while ((receiveString = bufferedReader.readLine()) != null) {
                                            stringBuilder.append(receiveString);
                                        }
                                        inputStream.close();
                                        JSONObject absenteeJson=new JSONObject(stringBuilder.toString());
                                        jsonArray1= absenteeJson.optJSONArray("class");
                                        jsonArray1.put(jsonObject1);
                                        JSONObject j=new JSONObject();
                                        j.put("class",jsonArray1);
                                        try {
                                            outputStream = getActivity().openFileOutput(getString(R.string.json_file_name_offline_late), Context.MODE_PRIVATE);
                                            outputStream.write(j.toString().getBytes());
                                            outputStream.close();
                                            Toast.makeText(getActivity(),"Saved in offline many",Toast.LENGTH_SHORT).show();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                } catch (Exception e) {
                                    try {
                                        outputStream = getActivity().openFileOutput(getString(R.string.json_file_name_offline_late), Context.MODE_PRIVATE);
                                        outputStream.write(mainJsonObject.toString().getBytes());
                                        outputStream.close();
                                        Toast.makeText(getActivity(),"Saved in offline new",Toast.LENGTH_SHORT).show();
                                    } catch (Exception e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            }
                        });
                        builder2.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        builder2.show();

                }
            }

        });
        return v;
    }
    public static android.support.v4.app.Fragment newInstance() {
        LateAttendanceStudentSelectFragment mFrgment = new LateAttendanceStudentSelectFragment();
        return mFrgment;
    }

}
class HttpSendLateAttendance extends AsyncTask<String, Void, String>
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
            httpget = new HttpGet(Gobal.CallUrl+"latestudentabsentee.php?"+get_url);
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