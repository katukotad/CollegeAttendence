package com.karthik.app.collegeattendence;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class TimetableFragment extends Fragment {


    public TimetableFragment() {
        // Required empty public constructor
    }

    String list="";
    String timeJson="",serverTime="",serverDate="";
    Boolean isThreadComplete=false;
    ArrayAdapter<String> adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v=inflater.inflate(R.layout.fragment_timetable, container, false);
        JSONObject jsonObject=null;
        int q=0;
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
        Calendar calendar = Calendar.getInstance();
        int day=0;
        SimpleDateFormat d2=new SimpleDateFormat("dd-MM-yyyy");
        try {
            JSONArray jsonArrayDayOrder=jsonObject.optJSONArray("dayOrder");
            for (int d = 0; d < jsonArrayDayOrder.length(); d++) {
                if (serverDate.equals(jsonArrayDayOrder.getJSONObject(d).optString("orderDate"))) {
                    day = jsonArrayDayOrder.getJSONObject(d).getInt("dayOrder");
                    break;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        try{
            JSONArray jsonMainNode = jsonObject.optJSONArray("class");
            for(int i = 0; i<jsonMainNode.length();i++){
                JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
                JSONArray time=jsonChildNode.optJSONArray("time");
                for (int j=0;j<time.length();j++){
                    JSONObject jsonTimeObject=time.getJSONObject(j);
                    if(jsonTimeObject.optInt("day")==day){
                        JSONArray jsonArrayPeriod=jsonObject.getJSONArray("period");
                        for(int k=0;k<jsonArrayPeriod.length();k++) {
                            if (jsonTimeObject.optString("peroidId").equals(jsonArrayPeriod.getJSONObject(k).getString("periodId"))) {
                                String department = jsonChildNode.optString("departmentName");
                                String year = jsonChildNode.optString("year");
                                String section = jsonChildNode.optString("section");
                                //String periodnumber = jsonChildNode.optString("periodnumber");
                                String periodnumber = jsonArrayPeriod.getJSONObject(k).getString("periodnumber");
                                /*DateFormat tf=new SimpleDateFormat("hh:mm a");
                               Date timefrom = new SimpleDateFormat("HH:mm").parse(jsonArrayPeriod.getJSONObject(k).getString("fromDate"));
                                Date timeto = new SimpleDateFormat("HH:mm").parse(jsonArrayPeriod.getJSONObject(k).getString("toDate"));*/
                                String outPut = department + "-" + year + "-" + section + "-" + jsonChildNode.optString("subjectName") + " -" + periodnumber;
                                list=list+"::#"+outPut;
                            }
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        final ListView listclass = (ListView) v.findViewById(R.id.listView3);
        try{
            adapter=new ArrayAdapter<String>(v.getContext(),
                    android.R.layout.simple_list_item_1,list.substring(3).split("::#"));

            listclass.setAdapter(adapter);
        }catch (Exception e){
            e.printStackTrace();
        }
        return v;
    }
    public static android.support.v4.app.Fragment newInstance() {
        TimetableFragment mFrgment = new TimetableFragment();
        return mFrgment;
    }

}
