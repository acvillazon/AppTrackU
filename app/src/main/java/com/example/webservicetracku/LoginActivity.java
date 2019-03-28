package com.example.webservicetracku;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);
        findViewById(R.id.login_button).setOnClickListener(this);
        findViewById(R.id.registration_link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentForRegistration=new Intent();
                intentForRegistration.setClass(getApplicationContext(),RegistrationActivity.class);
                startActivity(intentForRegistration);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.login_button){
            Intent intentToBeCalled=new Intent();
            String userName=((EditText)findViewById(R.id.login_username_value)).getText()+"";
            String password=((EditText)findViewById(R.id.login_password_value)).getText()+"";
            intentToBeCalled.putExtra("callType", "userLogin");
            intentToBeCalled.putExtra("userName",userName);
            intentToBeCalled.putExtra("password",password);
            intentToBeCalled.setClass(this,MainActivity.class);
            startActivity(intentToBeCalled);
        }

    }
}
