package com.karthik.app.collegeattendence;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class ClasslistFragment extends Fragment {


    public ClasslistFragment() {
        // Required empty public constructor
    }

    JSONObject jsonObject;
    String[] studentArray = null, studentId = null;
    List<String> periodIdLoopData = new ArrayList<String>();
    List<String> allocationIdLoopData = new ArrayList<String>();
    String timeJson = "", serverTime = "", serverDate = "";
    Boolean isThreadComplete = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_classlist, container, false);
        //Toast.makeText(v.getContext(),"Class List",Toast.LENGTH_LONG).show();
        allocationIdLoopData.clear();
        periodIdLoopData.clear();


        try {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    HttpTime http = new HttpTime();
                    timeJson = http.doInBackground(Gobal.CallUrl + "time.php");
                    isThreadComplete = true;
                }
            });
            isThreadComplete = false;
            thread.start();
            while (!isThreadComplete) ;
        } catch (Exception e) {
            e.printStackTrace();
        }
        SimpleDateFormat smp1 = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat smp2 = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat smp3 = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat smp4 = new SimpleDateFormat("HH:mm");
        try {
            JSONObject jsonObject = new JSONObject(timeJson);
            Date date1 = smp2.parse(jsonObject.getString("date"));
            serverDate = smp1.format(date1);
            Date date2 = smp3.parse(jsonObject.getString("time"));
            serverTime = smp4.format(date2);
        } catch (Exception e) {
            e.printStackTrace();
            Calendar c = Calendar.getInstance();
            serverDate = smp1.format(c.getTime());
            serverTime = smp4.format(c.getTime());
        }
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
        } catch (FileNotFoundException e) {
            Log.e("Json File read", "File not found: " + e.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }

        final List<HashMap<String, String>> stringList = new ArrayList<HashMap<String, String>>();
        int day=0;
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
        try {

            Date timefrom = new SimpleDateFormat("HH:mm").parse(serverTime);
            Calendar calendarfrom = Calendar.getInstance();
            calendarfrom.setTime(timefrom);
            calendarfrom.add(Calendar.DATE, 1);
            FileManagement fm= new FileManagement();
            JSONObject limitTimeJson=new JSONObject(fm.getTextFromFile(getString(R.string.josn_file_name)));
            Date timeto = new SimpleDateFormat("HH:mm").parse(limitTimeJson.optString("timeLimit"));
            Calendar calendarto = Calendar.getInstance();
            calendarto.setTime(timeto);
            calendarto.add(Calendar.DATE, 1);
            calendarto.add(Calendar.MINUTE, 15);

            if (calendarfrom.after(calendarto)){
                final AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());
                alert.setTitle("Time Limit reached for today");
                DateFormat tf = new SimpleDateFormat("hh:mm a");
                alert.setMessage(" Time limit is reached "+tf.format(timeto));
                alert.setCancelable(false);
                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                alert.show();
            }else {

                try {
                    JSONArray jsonMainNode = jsonObject.optJSONArray("class");
                    int allocationIdLoop = 0;
                    int periodIdLoop = 0;
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
                                        String periodnumber = jsonArrayPeriod.getJSONObject(k).getString("periodnumber");
                                       // DateFormat tf=new SimpleDateFormat("hh:mm a");
                                       // Date timefrom1 = new SimpleDateFormat("HH:mm").parse(jsonArrayPeriod.getJSONObject(k).getString("fromDate"));
                                       // Date timeto1 = new SimpleDateFormat("HH:mm").parse(jsonArrayPeriod.getJSONObject(k).getString("toDate"));
                                        String outPut = department + "-" + year + "-" + section + "-" + jsonChildNode.optString("subjectName") + " -" + periodnumber;

                                        stringList.add(classList("class", outPut));
                                        allocationIdLoopData.add(allocationIdLoop, jsonChildNode.optString("subjectAollcationId"));
                                        allocationIdLoop++;
                                        periodIdLoopData.add(periodIdLoop, jsonArrayPeriod.getJSONObject(k).getString("periodId"));
                                        periodIdLoop++;
                                    }
                                }
                            }
                        }
                    }
                    Log.d("Json hashMap", stringList.toString());
                } catch (Exception e) {
                    Log.e("Json Error", e.toString());
                }
                final ListView listView = (ListView) v.findViewById(R.id.classListView);

                SimpleAdapter simpleAdapter = new SimpleAdapter(v.getContext(), stringList, android.R.layout.simple_list_item_1, new String[]{"class"}, new int[]{android.R.id.text1});
                listView.setAdapter(simpleAdapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String item = stringList.get(position).get("class");
                        String studentArray = "", studentId = "", periodId = "";
                        String subjectAollcationId = "";
                        String[] item1 = item.split("-");
                        try {
                            JSONArray classArray = jsonObject.optJSONArray("class");
                            for (int i = 0; i < classArray.length(); i++) {
                                JSONObject classObject1 = classArray.getJSONObject(i);
                                if (allocationIdLoopData.get(position).trim().equals(classObject1.optString("subjectAollcationId").trim())) {
                                    JSONArray jsonArrayPeriod = jsonObject.getJSONArray("period");
                                    for (int k = 0; k < jsonArrayPeriod.length(); k++) {
                                        if (jsonArrayPeriod.getJSONObject(k).getString("fromDate").trim().equals(item1[4].trim()) &&
                                                jsonArrayPeriod.getJSONObject(k).getString("toDate").trim().equals(item1[5].trim()))
                                            ;
                                        {
                                            periodId = jsonArrayPeriod.getJSONObject(k).getString("periodId").trim();
                                        }
                                    }
                                    subjectAollcationId = classObject1.optString("subjectAollcationId");
                                    JSONArray student = classObject1.optJSONArray("student");
                                    for (int j = 0; j < student.length(); j++) {
                                        JSONObject classObject2 = student.optJSONObject(j);
                                        studentArray = studentArray + "::" + classObject2.optString("stundetRollNumber") + " " + classObject2.optString("stundetName");
                                        studentId = studentId + "::" + classObject2.optString("studentId");
                                    }
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        //Toast.makeText(view.getContext(), item, Toast.LENGTH_SHORT).show();

                        try {
                            Bundle b = new Bundle();
                            b.putString("periodId", periodIdLoopData.get(position));
                            b.putString("class", item);
                            b.putString("subjectAollcationId", subjectAollcationId);
                            b.putString("student", studentArray.substring(2));
                            b.putString("classid", studentId.substring(2));
                            b.putString("exceptionType", "0");
                            b.putString("serverDate", serverDate);
                            b.putString("serverTime", serverTime);

                            Fragment fg = ClassAttendenceFragment.newInstance();
                            fg.setArguments(b);
                            FragmentManager manager = getFragmentManager();
                            FragmentTransaction transaction = manager.beginTransaction();
                            transaction.replace(R.id.fragmentholder, fg);
                            transaction.addToBackStack(null);
                            transaction.commit();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(v.getContext(), "Error In data", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch (Exception e){

        }
        return v;
    }

    public String[] getFromTimeAndToTime(String periodId) {
        FileManagement fm = new FileManagement();
        String[] a = null;
        String json = fm.getTextFromFile(getString(R.string.josn_file_name));
        try {
            JSONObject jsonObject = new JSONObject(json);

            JSONArray jsonArrayPeriod = jsonObject.getJSONArray("period");
            for (int i = 0; i < jsonArrayPeriod.length(); i++) {
                if (periodId.equals(jsonArrayPeriod.getJSONObject(i).optString("peroidId"))) {
                    a[0] = jsonArrayPeriod.getJSONObject(i).getString("fromDate");
                    a[1] = jsonArrayPeriod.getJSONObject(i).getString("toDate");
                    return a;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private HashMap<String, String> classList(String classS, String number) {
        HashMap<String, String> employeeNameNo = new HashMap<String, String>();
        employeeNameNo.put(classS, number);
        return employeeNameNo;
    }

    public static android.support.v4.app.Fragment newInstance() {
        ClasslistFragment mFrgment = new ClasslistFragment();
        return mFrgment;
    }

}
