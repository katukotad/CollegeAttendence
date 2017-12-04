package com.karthik.app.collegeattendence;


import android.os.Bundle;
import  android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class NewClassFragment extends Fragment {

    FileManagement fm=new FileManagement();
    ArrayList<String> subName=new ArrayList<String>();
    ArrayList<String> subId=new ArrayList<String>();
    ArrayList<String> periodName=new ArrayList<String>();
    ArrayList<String> periodId=new ArrayList<String>();
    ArrayAdapter<String> adapter=null,adapter1=null;
    String timeJson="",serverTime="",serverDate="";
    Boolean isThreadComplete=false;


    public NewClassFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v=inflater.inflate(R.layout.fragment_new_class, container, false);
        final Spinner classSpinner=(Spinner)v.findViewById(R.id.newClassFragmentClassSpinner);
        final Spinner periodSpinner=(Spinner)v.findViewById(R.id.newClassFragmentPeriodSpinner);
        Button save=(Button)v.findViewById(R.id.newClassFragmentSaveButton);


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

        String json = fm.getTextFromFile(getString(R.string.josn_file_name));
        JSONObject jsonObject;
        try {
            subId.clear();
            subName.clear();
            jsonObject=new JSONObject(json);
            JSONArray classJsonArray=jsonObject.optJSONArray("class");
            for(int i=0;i<classJsonArray.length();i++){
                JSONObject jsonObject1=classJsonArray.getJSONObject(i);
                subName.add(i,jsonObject1.optString("departmentName")+"-"+jsonObject1.optString("year")+"-"+jsonObject1.optString("section")+"-"+jsonObject1.optString("subjectName"));
                subId.add(i,jsonObject1.optString("subjectAollcationId"));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            jsonObject=new JSONObject(json);
            JSONArray periodJsonArray=jsonObject.optJSONArray("period");
            periodName.clear();
            periodId.clear();
            int j=0;
            for(int i=0;i<periodJsonArray.length();i++){
                JSONObject jsonObject1=periodJsonArray.getJSONObject(i);
                Date timefrom = new SimpleDateFormat("HH:mm").parse(jsonObject1.getString("fromDate"));
                Calendar calendarfrom = Calendar.getInstance();
                calendarfrom.setTime(timefrom);
                calendarfrom.add(Calendar.DATE, 1);

                Date timeto = new SimpleDateFormat("HH:mm").parse(jsonObject1.getString("toDate"));
                Calendar calendarto = Calendar.getInstance();
                calendarto.setTime(timeto);
                calendarto.add(Calendar.DATE, 1);
                calendarto.add(Calendar.MINUTE,15);

                Calendar calendarCurrent1 = Calendar.getInstance();
                Calendar calendarCurrent = Calendar.getInstance();
                Date timeCurrent = new SimpleDateFormat("HH:mm").parse(serverTime);
                calendarCurrent.setTime(timeCurrent);
                calendarCurrent.add(Calendar.DATE, 1);
//                if(calendarCurrent.before(calendarto) && calendarCurrent.after(calendarfrom)){
                    //DateFormat tf=new SimpleDateFormat("hh:mm a");
                    //Date timefrom1 = new SimpleDateFormat("HH:mm").parse(jsonObject1.optString("fromDate"));
                    //Date timeto1 = new SimpleDateFormat("HH:mm").parse(jsonObject1.optString("toDate"));
                String periodnumber = jsonObject1.optString("periodnumber");
                    periodName.add(j, periodnumber);
                    periodId.add(j, jsonObject1.optString("periodId"));
//                }
            }
            j=0;
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            adapter = new ArrayAdapter<String>(MainActivity.getAppContext(), R.layout.list_row, subName);
            classSpinner.setAdapter(adapter);
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            adapter1 = new ArrayAdapter<String>(MainActivity.getAppContext(), R.layout.list_row, periodName);
            periodSpinner.setAdapter(adapter1);
        }catch (Exception e){
            e.printStackTrace();
        }
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String studentArray="",studentId="";
                if(periodId!=null && periodId.size()>0){
                    JSONObject jsonObject;
                    try {
                        jsonObject = new JSONObject(fm.getTextFromFile(getString(R.string.josn_file_name)));
                        JSONArray classJsonArray=jsonObject.getJSONArray("class");
                        for (int i=0;i<classJsonArray.length();i++){
                            JSONObject classJsonObject=classJsonArray.getJSONObject(i);
                            if(classJsonObject.optString("subjectAollcationId").equals(subId.get(classSpinner.getSelectedItemPosition()))){
                               JSONArray studentJsonArray=classJsonObject.getJSONArray("student");
                                for (int j=0;j<studentJsonArray.length();j++){
                                    JSONObject studentJsonObject = studentJsonArray.optJSONObject(j);
                                    studentArray=studentArray+"::"+studentJsonObject.optString("stundetRollNumber") +" "+studentJsonObject.optString("stundetName");
                                    studentId = studentId+"::"+studentJsonObject.optString("studentId");
                                }
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    try {
                        Bundle b = new Bundle();
                        b.putString("periodId", periodId.get(periodSpinner.getSelectedItemPosition()));
                        b.putString("class", subName.get(classSpinner.getSelectedItemPosition()) + " " + periodName.get(periodSpinner.getSelectedItemPosition()));
                        b.putString("subjectAollcationId", subId.get(classSpinner.getSelectedItemPosition()));
                        b.putString("student", studentArray.substring(2));
                        b.putString("classid", studentId.substring(2));
                        b.putString("exceptionType", "1");
                        b.putString("serverDate",serverDate);
                        b.putString("serverTime",serverTime);
                        Fragment fg = ClassAttendenceFragment.newInstance();
                        fg.setArguments(b);
                        FragmentManager manager = getFragmentManager();
                        FragmentTransaction transaction = manager.beginTransaction();
                        transaction.replace(R.id.fragmentholder, fg);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(v.getContext(),"Error In data",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(v.getContext(),"Period Invaid",Toast.LENGTH_SHORT).show();
                }
            }
        });
        return v;
    }
    public static android.support.v4.app.Fragment newInstance() {
        NewClassFragment mFrgment = new NewClassFragment();
        return mFrgment;
    }
}
