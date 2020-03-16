package com.example.etpgroup12;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    public static final int CONNECTION_TIMEOUT = 10000;
    public static final int READ_TIMEOUT = 15000;
    public EditText IDInput;
    public Spinner CourseInput;
    public EditText DateInput;
    public Spinner HourInput;
    public Spinner SessionInput;

    public DatePickerDialog picker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Variable References
        IDInput = (EditText) findViewById(R.id.idinput);
        CourseInput = (Spinner) findViewById(R.id.coursespin);
        DateInput = (EditText) findViewById(R.id.dateinput);
        HourInput = (Spinner) findViewById(R.id.hourspin);
        SessionInput = (Spinner) findViewById(R.id.sessioninput);


        DateInput.setInputType(InputType.TYPE_NULL);
        DateInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                // date picker dialog
                picker = new DatePickerDialog(MainActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                DateInput.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                            }
                        }, year, month, day);
                picker.show();
            }
        });

    }


    public void submitData(View arg0) {

        //Get variables from corresponding fields
        final String ID = "IT" + IDInput.getText().toString();
        final String Course = CourseInput.getSelectedItem().toString();
        //final String Date = DateInput.getText().toString();
        final String Date = DateInput.getText().toString();
        final String Hour = HourInput.getSelectedItem().toString();
        final String Session = SessionInput.getSelectedItem().toString();

        Object[] obj = new Object[5];
        obj[0] = ID;
        obj[1] = Course;
        obj[2] = Date;
        obj[3] = Hour;
        obj[4] = Session;

        //Initialize AsyncLoad class with variables
        new AsyncLoad().execute(obj);
    }

    private class AsyncLoad extends AsyncTask<Object, Void, String>
    {
        ProgressDialog pdLoading = new ProgressDialog(MainActivity.this);
        HttpURLConnection conn;
        URL url = null;

        @Override
        protected void onPreExecute(){
            super.onPreExecute();

            pdLoading.setMessage("\tLoading...");
            pdLoading.setCancelable(false);
            pdLoading.show();
        }

        @Override
        protected String doInBackground(Object... objects) {
            try{
                //URL ADDRESS
                url = new URL("https://travelgowaip.000webhostapp.com/login.inc.php");
            } catch (MalformedURLException e){
                e.printStackTrace();
                return "exception";
            }
            try{
                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection)url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("POST");

                // setDoInput and setDoOutput method depict handling of both send and receive
                conn.setDoInput(true);
                conn.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("ID", String.valueOf(objects[0]))
                        .appendQueryParameter("Course", String.valueOf(objects[1]))
                        .appendQueryParameter("Date", String.valueOf(objects[2]))
                        .appendQueryParameter("Hour", String.valueOf(objects[3]))
                        .appendQueryParameter("Session", String.valueOf(objects[4]));
                String query = builder.build().getEncodedQuery();

                // Open connection for sending data
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                conn.connect();

            } catch (IOException el) {
                el.printStackTrace();
                return "exception";
            }

            try {
                int response_code = conn.getResponseCode();

                // Check if successful connection made
                if (response_code == HttpURLConnection.HTTP_OK) {

                    // Read data sent from server
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    // Pass data to onPostExecute method
                    return (result.toString());

                } else {
                    return ("unsuccessful");
                }
            } catch (IOException e){
                e.printStackTrace();
                return "exception";
            } finally{
                conn.disconnect();
            }


        }

        @Override
        protected void onPostExecute(String result){

            //this method will be running on UI thread

            pdLoading.dismiss();

            if(result.equalsIgnoreCase("true"))
            {
                /* Here launching another activity when login successful. If you persist login state
                use sharedPreferences of Android. and logout button to clear sharedPreferences.
                 */

                Intent intent = new Intent(MainActivity.this,SuccessActivity.class);
                startActivity(intent);
                MainActivity.this.finish();

            }else if (result.equalsIgnoreCase("false")){

                Toast.makeText(MainActivity.this, "Information Not Uploaded", Toast.LENGTH_LONG).show();

            } else if (result.equalsIgnoreCase("exception") || result.equalsIgnoreCase("unsuccessful")) {

                Toast.makeText(MainActivity.this, "Connection Unsuccessful", Toast.LENGTH_LONG).show();

            } else{

                Toast.makeText(MainActivity.this, "TestConn", Toast.LENGTH_LONG).show();
            }
        }
    }


}
