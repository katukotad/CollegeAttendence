package com.karthik.app.collegeattendence;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class AttendanceTakenListFragment extends Fragment {


    public AttendanceTakenListFragment() {
        // Required empty public constructor
    }
    public static android.support.v4.app.Fragment newInstance() {
        AttendanceTakenListFragment mFrgment = new AttendanceTakenListFragment();
        return mFrgment;
    }
    String data="";
    JSONObject jsonObject;
    boolean isThreadComplete = false;
    String a="",b="";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v=inflater.inflate(R.layout.fragment_attendance_taken_list, container, false);
        final ListView listView=(ListView)v.findViewById(R.id.listView2);
        HttpSendLate http;

            Thread thread=new Thread(new Runnable() {
                @Override
                public void run()
                {
                    try{
                        HttpSendLate http=new HttpSendLate();
                        Calendar c=Calendar.getInstance();
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                        a=http.doInBackground("rollNumber="+jsonObject.optString("staffrollnumber")+"&date="+df.format(c.getTime()).toString());
                        if(a.equals("connection Failed"))
                            Toast.makeText(v.getContext(),"Connection Error",Toast.LENGTH_SHORT).show();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    isThreadComplete = true;
                }
            });

        try{
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
        }catch (Exception e){
            e.printStackTrace();
        }
        isThreadComplete=false;
        thread.start();
        while (!isThreadComplete);
       //Toast.makeText(v.getContext(),a,Toast.LENGTH_SHORT).show();
        final List<HashMap<String, String>> stringList=new ArrayList<HashMap<String, String>>();
        try {
            JSONObject absenteeJson=new JSONObject(a);
            JSONArray jsonMainNode = absenteeJson.optJSONArray("absent");
            for(int i = 0; i<jsonMainNode.length();i++){
                JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
                String department = jsonChildNode.optString("department");
                String year =jsonChildNode.optString("year");
                String section = jsonChildNode.optString("section");
                String outPut = department+"::"+ year +"::"+ section +"::"+jsonChildNode.optString("subject")+"::"+jsonChildNode.optString("takendate")+"::"+jsonChildNode.optString("takentime");
                stringList.add(classList("class", outPut));
            }
            Log.d("Json hashMap", stringList.toString());
        }catch (Exception e){
            Log.e("Json Error",e.toString());
        }
        try{
            try {
                InputStream inputStream = getActivity().openFileInput(getString(R.string.josn_file_name_offline_att));
                if (inputStream != null) {
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String receiveString = "";
                    StringBuilder stringBuilder=new StringBuilder();
                    while ((receiveString = bufferedReader.readLine()) != null) {
                        stringBuilder.append(receiveString);
                    }
                    inputStream.close();
                    b=stringBuilder.toString();
                    JSONObject jsonObjectNode = new JSONObject(stringBuilder.toString());
                    JSONArray jsonMainNode=jsonObjectNode.optJSONArray("class");
                    for(int i = 0; i<jsonMainNode.length();i++){
                        JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
                        String department = jsonChildNode.optString("department");
                        String year =jsonChildNode.optString("year");
                        String section = jsonChildNode.optString("section");

                        String outPut = department+"::"+ year +"::"+ section +"::"+jsonChildNode.optString("subject")+"::"+jsonChildNode.optString("date")+"::"+jsonChildNode.optString("time");
                        stringList.add(classList("class", outPut));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            SimpleAdapter simpleAdapter=new SimpleAdapter(v.getContext(),stringList, android.R.layout.simple_list_item_1, new String[] {"class"},new int[]{android.R.id.text1});
            listView.setAdapter(simpleAdapter);
        }catch (Exception e){
            e.printStackTrace();
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = stringList.get(position).get("class");
                String stud = "", classId = "";
                String[] item1 = item.split("::");
                try {
                    JSONObject absenteeJson1=new JSONObject(a);
                    JSONArray classArray = absenteeJson1.optJSONArray("absent");
                    for (int i = 0; i < classArray.length(); i++) {
                        JSONObject classObject1 = classArray.getJSONObject(i);
                        if (classObject1.optString("department").equals(item1[0]) && classObject1.optString("year").equals(item1[1]) &&
                                classObject1.optString("section").equals(item1[2]) && classObject1.optString("subject").equals(item1[3])&&
                                classObject1.optString("takendate").equals(item1[4]) && classObject1.optString("takentime").equals(item1[5])) {
                            stud = classObject1.optString("student");
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    JSONObject absenteeJson1=new JSONObject(b);
                    JSONArray classArray = absenteeJson1.optJSONArray("class");
                    for (int i = 0; i < classArray.length(); i++) {
                        JSONObject classObject1 = classArray.getJSONObject(i);
                        if (classObject1.optString("department").equals(item1[0]) && classObject1.optString("year").equals(item1[1]) &&
                                classObject1.optString("section").equals(item1[2]) && classObject1.optString("subject").equals(item1[3])&&
                                classObject1.optString("date").equals(item1[4]) && classObject1.optString("time").equals(item1[5])) {
                            stud = classObject1.optString("rollNumber");
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //Toast.makeText(view.getContext(), item, Toast.LENGTH_SHORT).show();
                Bundle b = new Bundle();
                b.putString("class", item);
                b.putString("student",stud.replaceAll("\"",""));
                Fragment fg = LateAttendanceStudentSelectFragment.newInstance();
                fg.setArguments(b);
                FragmentManager manager = getFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.fragmentholder, fg);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        return v;
    }
    private HashMap<String, String> classList(String classS,String number){
        HashMap<String, String> employeeNameNo = new HashMap<String, String>();
        employeeNameNo.put(classS, number);
        return employeeNameNo;
    }

}

class HttpSendLate extends AsyncTask<String, Void, String>
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
            httpget = new HttpGet(Gobal.CallUrl+"lateattendance.php?"+get_url);
            String content = Client.execute(httpget, responseHandler);
            //Toast.makeText(MainActivity.getAppContext(),"Saved",Toast.LENGTH_SHORT).show();
            return content;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return "connection Failed";
    }
}