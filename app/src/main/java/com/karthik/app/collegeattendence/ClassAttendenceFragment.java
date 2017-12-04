package com.karthik.app.collegeattendence;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 */
public class ClassAttendenceFragment extends Fragment {


    public ClassAttendenceFragment() {
        // Required empty public constructor
    }

    String[] studentArray = null;
    String[] studentId = null;
    Boolean isThreadComplete = false;
    ArrayAdapter<String> adapter;
    ProgressDialog progressDialog;
    String a = null;
    JSONObject jsonObject =null;
    JSONObject jsonObject1 = new JSONObject();
    JSONArray jsonArray = new JSONArray();
    JSONArray jsonArray1 = new JSONArray();
    JSONObject mainJsonObject = new JSONObject();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_class_attendence, container, false);

        String strtext = getArguments().getString("class");
        final TextView heading = (TextView) v.findViewById(R.id.classAttendenceClassNmaeTextView);
        final Button save = (Button) v.findViewById(R.id.attendenceFragmentSaveButton);
        final EditText search = (EditText) v.findViewById(R.id.classAttendanceSearchEditText);
        final ImageView clear = (ImageView) v.findViewById(R.id.classAttendanceSearchClearImageView);

        heading.setText(strtext);
        try {
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
        catch (FileNotFoundException e) {
            Log.e("Json File read", "File not found: " + e.toString());

        } catch (Exception e) {
            e.printStackTrace();
        };
        final ListView listclass = (ListView) v.findViewById(R.id.listView);
        try {
            adapter = new ArrayAdapter<String>(v.getContext(),
                    android.R.layout.simple_list_item_checked, getArguments().getString("student").split("::"));
            listclass.setChoiceMode(listclass.CHOICE_MODE_MULTIPLE);
            listclass.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search.setText("");
                clear.setVisibility(View.INVISIBLE);
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SparseBooleanArray choices1 = listclass.getCheckedItemPositions();
                String[] studentName = getArguments().getString("student").split("::");
                String[] studentId1 = getArguments().getString("classid").split("::");
                StringBuilder choicesString1 = new StringBuilder();
                StringBuilder choicesName1 = new StringBuilder();
                StringBuilder nonChoicesString = new StringBuilder();

                for (int i = 0; i < choices1.size(); i++) {
                    if (choices1.valueAt(i) == true) {
                        if (i != (choices1.size() - 1)) {
                            choicesString1.append(studentId1[choices1.keyAt(i)].split(" ")[0]).append(",");
                            choicesName1.append(studentName[choices1.keyAt(i)]).append("\n");
                        } else {
                            choicesString1.append(studentId1[choices1.keyAt(i)].split(" ")[0]);
                            choicesName1.append(studentName[choices1.keyAt(i)]);
                        }
                    }
                }
                int j=0;
                for(int i=0;i<studentId1.length;i++){
                    boolean l=true;
                    for(int k=0;k < choices1.size(); k++){
                        if(choices1.keyAt(k)==i && choices1.valueAt(k)){
                            l=false;
                            break;
                        }
                    }
                    if(l){
                        if(j==0){
                            nonChoicesString.append(studentId1[i]);
                            j=1;
                        }else{
                            nonChoicesString.append(",").append(studentId1[i]);
                        }
                    }
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("List Of Absentees");
                builder.setMessage(choicesName1);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing but close the dialog
                        for (int p9 = 0; p9 < getArguments().getString("periodId").split("::").length; p9++) {
                            final int p8 = p9;
                            Thread thread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    SparseBooleanArray choices = listclass.getCheckedItemPositions();
                                    String[] studentName = getArguments().getString("student").split("::");
                                    String[] studentId1 = getArguments().getString("classid").split("::");
                                    StringBuilder choicesString = new StringBuilder();
                                    StringBuilder choicesName = new StringBuilder();
                                    StringBuilder nonChoicesString = new StringBuilder();
                                    for (int i = 0; i < choices.size(); i++) {
                                        if (choices.valueAt(i) == true) {
                                            if (i != (choices.size() - 1)) {
                                                choicesString.append(studentId1[choices.keyAt(i)].split(" ")[0]).append(",");
                                                choicesName.append(studentName[choices.keyAt(i)]).append(",");
                                            } else {
                                                choicesString.append(studentId1[choices.keyAt(i)].split(" ")[0]);
                                                choicesName.append(studentName[choices.keyAt(i)]);
                                            }
                                        }
                                    }
                                    int j=0;
                                    for(int i=0;i<studentId1.length;i++){
                                        boolean l=true;
                                        for(int k=0;k < choices.size(); k++){
                                            if(choices.keyAt(k)==i && choices.valueAt(k)){
                                                l=false;
                                                break;
                                            }
                                        }
                                        if(l){
                                            if(j==0){
                                                nonChoicesString.append(studentId1[i]);
                                                j=1;
                                            }else{
                                                nonChoicesString.append(",").append(studentId1[i]);
                                            }
                                        }
                                    }
                                    String absentees = choicesString.toString();
                                    String presentees=nonChoicesString.toString();

                                    try {
                                        HttpSendAttendance http = new HttpSendAttendance();
                                        Calendar c = Calendar.getInstance();
                                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                                        SimpleDateFormat tf = new SimpleDateFormat("HH:mm:ss");
                                        a = http.doInBackground("subjectAllocationId=" + getArguments().getString("subjectAollcationId") + "&rollNumber=" + absentees +"&rollNumber1="+presentees+ "&date=" + getArguments().getString("serverDate") + "&time=" + getArguments().getString("serverTime") + ":00" + "&periodId=" + getArguments().getString("periodId").split("::")[p8] + "&exceptionType=" + getArguments().getString("exceptionType") + "&staffId=" + jsonObject.optString("staffId"));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    isThreadComplete = true;
                                }
                            });
                            isThreadComplete = false;

                            thread.start();
                            while (!isThreadComplete) ;
                            if (a.trim().equals("success")) {
                                Fragment fg = DashboardFragment.newInstance();
                                FragmentManager manager = getFragmentManager();
                                manager.popBackStack();
                                FragmentTransaction transaction = manager.beginTransaction();
                                transaction.replace(R.id.fragmentholder, fg);
                                transaction.commit();
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setMessage("Attendance Saved Successfully in Server")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                //do things
                                            }
                                        });
                                AlertDialog alert = builder.create();
                                alert.show();
                                //Toast.makeText(MainActivity.getAppContext(), "Saved", Toast.LENGTH_SHORT).show();
                            } else if (a.trim().equals("Already inserted")) {
                                Fragment fg = DashboardFragment.newInstance();
                                FragmentManager manager = getFragmentManager();
                                manager.popBackStack();
                                FragmentTransaction transaction = manager.beginTransaction();
                                transaction.replace(R.id.fragmentholder, fg);
                                transaction.commit();
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setMessage("Already Attendance taken")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                //do things
                                            }
                                        });
                                AlertDialog alert = builder.create();
                                alert.show();
                                //Toast.makeText(MainActivity.getAppContext(), "Already Attendance taken", Toast.LENGTH_SHORT).show();
                            } else {
                                String[] studentId = getArguments().getString("classid").split("::");
                                FileOutputStream outputStream;
                                String filename = getString(R.string.josn_file_name_offline_att);
                                SparseBooleanArray choices = listclass.getCheckedItemPositions();
                                String choosed = "";
                                StringBuilder choicesString = new StringBuilder();
                                StringBuilder nonChoicesString = new StringBuilder();
                                for (int i = 0; i < choices.size(); i++) {
                                    if (choices.valueAt(i) == true) {
                                        if (i != (choices.size() - 1))
                                            choicesString.append(studentId[choices.keyAt(i)]).append(",");
                                        else
                                            choicesString.append(studentId[choices.keyAt(i)]);
                                    }
                                }
                                int jk=0;
                                for(int i=0;i<studentId.length;i++){
                                    boolean l=true;
                                    for(int k=0;k < choices.size(); k++){
                                        if(choices.keyAt(k)==i && choices.valueAt(k)){
                                            l=false;
                                            break;
                                        }
                                    }
                                    if(l){
                                        if(jk==0){
                                            nonChoicesString.append(studentId[i]);
                                            jk=1;
                                        }else{
                                            nonChoicesString.append(",").append(studentId[i]);
                                        }
                                    }
                                }
                                String absentees = choicesString.toString();
                                String presentees=nonChoicesString.toString();
                                try {
                                    Calendar c = Calendar.getInstance();
                                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                                    SimpleDateFormat tf = new SimpleDateFormat("HH:mm:ss");

                                    jsonObject1.put("subjectAllocationId", getArguments().getString("subjectAollcationId"));
                                    jsonObject1.put("rollNumber", absentees);
                                    jsonObject1.put("rollNumber1", presentees);
                                    jsonObject1.put("exceptionType", getArguments().getString("exceptionType"));
                                    jsonObject1.put("date", getArguments().getString("serverDate"));
                                    jsonObject1.put("time", getArguments().getString("serverTime") + ":00");
                                    jsonObject1.put("periodId", getArguments().getString("periodId").split("::")[p8]);
                                    jsonObject1.put("staffId", jsonObject.optString("staffId"));
                                    JSONArray jsonArray2 = new JSONArray();
                                    jsonArray2.put(jsonObject1);
                                    mainJsonObject.put("absentees", jsonArray2);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                try {

                                    //TO read file from out side
                                    InputStream inputStream = getActivity().openFileInput(getString(R.string.josn_file_name_offline_att));
                                    if (inputStream != null) {
                                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                                        String receiveString = "";
                                        StringBuilder stringBuilder = new StringBuilder();

                                        while ((receiveString = bufferedReader.readLine()) != null) {
                                            stringBuilder.append(receiveString);
                                        }
                                        inputStream.close();
                                        JSONObject absenteeJson = new JSONObject(stringBuilder.toString());
                                        jsonArray1 = absenteeJson.optJSONArray("absentees");
                                        jsonArray1.put(jsonObject1);
                                        JSONObject j = new JSONObject();
                                        j.put("absentees", jsonArray1);
                                        try {
                                            //To write file to out side
                                            outputStream = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
                                            outputStream.write(j.toString().getBytes());
                                            outputStream.close();
                                            //Toast.makeText(getActivity(), "Saved", Toast.LENGTH_SHORT).show();
                                            Fragment fg = DashboardFragment.newInstance();
                                            FragmentManager manager = getFragmentManager();
                                            manager.popBackStack();
                                            FragmentTransaction transaction = manager.beginTransaction();
                                            transaction.replace(R.id.fragmentholder, fg);
                                            transaction.commit();
                                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                            builder.setMessage("Attendance Saved in Offline please Check in report")
                                                    .setCancelable(false)
                                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            //do things
                                                        }
                                                    });
                                            AlertDialog alert = builder.create();
                                            alert.show();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                } catch (Exception e) {
                                    try {
                                        outputStream = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
                                        outputStream.write(mainJsonObject.toString().getBytes());
                                        outputStream.close();
                                        //Toast.makeText(getActivity(), "Saved", Toast.LENGTH_SHORT).show();
                                        Fragment fg = DashboardFragment.newInstance();
                                        FragmentManager manager = getFragmentManager();
                                        manager.popBackStack();
                                        FragmentTransaction transaction = manager.beginTransaction();
                                        transaction.replace(R.id.fragmentholder, fg);
                                        transaction.commit();
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                        builder.setMessage("Attendance Saved in Offline please Check in report")
                                                .setCancelable(false)
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        //do things
                                                    }
                                                });
                                        AlertDialog alert = builder.create();
                                        alert.show();
                                    } catch (Exception e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });

        search.addTextChangedListener(new TextWatcher() {
            String[] studentName = getArguments().getString("student").split("::");
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int index = 0;
                for (int i = 0; i < studentName.length; i++) {
                    if (studentName[i].toLowerCase().contains(s.toString().toLowerCase())) {
                        index = i;
                        break;
                    }
                }
                listclass.smoothScrollToPosition(index);

                if (s.toString().length() > 0) {
                    clear.setVisibility(View.VISIBLE);
                } else {
                    clear.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return v;
    }

    public static android.support.v4.app.Fragment newInstance() {
        ClassAttendenceFragment mFrgment = new ClassAttendenceFragment();
        return mFrgment;
    }
}

class HttpSendAttendance extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... str) {
        try {
            String get_url = str[0].replace(" ", "%20");
            HttpParams httpParameters = new BasicHttpParams();
            int timeoutConnection = 3000;
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            int timeoutSocket = 60000;
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
            HttpClient Client = new DefaultHttpClient(httpParameters);
            HttpGet httpget;
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            httpget = new HttpGet(Gobal.CallUrl + "studentabsentee.php?" + get_url);
            String content = Client.execute(httpget, responseHandler);
            //Toast.makeText(MainActivity.getAppContext(),"Saved",Toast.LENGTH_SHORT).show();
            return content;
        } catch (Exception e) {
            e.printStackTrace();
            //Toast.makeText(MainActivity.getAppContext(),"Connection Failed",Toast.LENGTH_SHORT).show();
        }
        return "connection Failed";
    }
}