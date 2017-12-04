package com.karthik.app.collegeattendence;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {


    public SettingsFragment() {
        // Re
        //
        // quired empty public constructor
    }
    boolean isThreadComplete=false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v=inflater.inflate(R.layout.fragment_settings, container, false);
        Button password=(Button)v.findViewById(R.id.settingsFragmentPasswordChangeButton);
        password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* Alert Dialog Code Start*/
                AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());
                alert.setTitle("Change Password");
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(10, 10, 10, 10);
                final EditText input = new EditText(v.getContext());
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                input.setTransformationMethod(PasswordTransformationMethod.getInstance());
                input.setHint("Enter Old Password");
                input.setLayoutParams(params);

                final EditText input1 = new EditText(v.getContext());
                input1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                input1.setTransformationMethod(PasswordTransformationMethod.getInstance());
                input1.setHint("Enter New Password");
                input1.setLayoutParams(params);

                final EditText input2 = new EditText(v.getContext());
                input2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                input2.setTransformationMethod(PasswordTransformationMethod.getInstance());
                input2.setHint("Confirm Password");
                input2.setLayoutParams(params);

                LinearLayout ll=new LinearLayout(v.getContext());

                ll.setOrientation(LinearLayout.VERTICAL);
                ll.addView(input);
                ll.addView(input1);
                ll.addView(input2);
                ll.setLayoutParams(params);
                alert.setView(ll);

                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        final Editable oPass = input.getEditableText();
                        final String[] a = {""};
                        final Editable nPass = input1.getEditableText();
                        final Editable cPass = input2.getEditableText();
                        if (!nPass.toString().equals(oPass.toString())) {
                            if (nPass.toString().length() >= 4 && cPass.toString().length() >= 4) {
                                if (nPass.toString().equals(cPass.toString())) {
                                    try {
                                        Thread thread = new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                HttpSendChangePassword http = new HttpSendChangePassword();
                                                FileManagement fm = new FileManagement();
                                                String login = fm.getTextFromFile(getString(R.string.josn_file_name));
                                                try {
                                                    JSONObject jsonObject = new JSONObject(login);
                                                    a[0] = http.doInBackground("staffId=" + jsonObject.optString("staffId") + "&newPassword=" + nPass.toString() + "&oldPassword=" + oPass.toString());


                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                isThreadComplete = true;
                                            }
                                        });
                                        isThreadComplete = false;
                                        thread.start();
                                        while (!isThreadComplete) ;
                                        if (a[0].trim().equals("old password is incorrect")) {
                                            Toast.makeText(getActivity(), "old password is incorrect", Toast.LENGTH_SHORT).show();
                                        } else if (a[0].trim().equals("Password Updated")) {
                                            Toast.makeText(getActivity(), "Password Updated", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getActivity(), "Error In connection", Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    Toast.makeText(getActivity(), "new and Confirm password not matched", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getActivity(), "Minimum Length of Password 4", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getActivity(), "Old and New Password are same", Toast.LENGTH_SHORT).show();
                        }
                        InputMethodManager inputManager = (InputMethodManager) getView().getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputManager.hideSoftInputFromWindow(getView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                });
                alert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        InputMethodManager inputManager = (InputMethodManager) getView().getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputManager.hideSoftInputFromWindow(getView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                        dialog.cancel();

                    }
                });
                alert.show();
            }
        });
        return v;
    }
    public static android.support.v4.app.Fragment newInstance() {
        SettingsFragment mFrgment = new SettingsFragment();
        return mFrgment;
    }

}
class HttpSendChangePassword extends AsyncTask<String, Void, String>
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
            httpget = new HttpGet(Gobal.CallUrl+"changepassword.php?"+get_url);
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