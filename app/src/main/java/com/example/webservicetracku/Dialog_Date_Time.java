package com.example.webservicetracku;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.webservicetracku.DateDialog;
import com.example.webservicetracku.R;
import com.example.webservicetracku.TimeDialog;

import java.util.Calendar;

public class Dialog_Date_Time extends DialogFragment implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    TextView textViewDateInicio;
    TextView textViewDateFinal;
    Button btnfixed;


    boolean Inicio=true;
    public Activity activity;
    int year, month, dayOfMonth, hourOfDay, minute;
    long unix1, unix2;

    public interface Dialog_Date_TimeInterface {
        void SendResult(long uni1, long unix2);
    }

    Dialog_Date_TimeInterface dialog_date_timeInterface;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_date_time,null);

        textViewDateInicio = v.findViewById(R.id.DateIni);
        textViewDateFinal = v.findViewById(R.id.DateEnd);
        textViewDateFinal.setVisibility(View.INVISIBLE);
        btnfixed = v.findViewById(R.id.btnfixed);

        textViewDateInicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View vi) {
                Inicio=true;
                textViewDateFinal.setVisibility(View.INVISIBLE);
            }
        });

        textViewDateFinal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateDialog dateDialog = new DateDialog();
                dateDialog.blocked=true;
                dateDialog.hourBloked=unix1;
                dateDialog.show(getActivity().getSupportFragmentManager(),"dateDialog");
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setView(v);

        return builder.create();

    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        this.year = year; this.month=month; this.dayOfMonth=dayOfMonth;
        new TimeDialog().show(getActivity().getSupportFragmentManager(),"TimeDialog");
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        this.hourOfDay=hourOfDay; this.minute=minute;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH,month);
        calendar.set(Calendar.YEAR,year);
        calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
        calendar.set(Calendar.HOUR_OF_DAY,hourOfDay);
        calendar.set(Calendar.MINUTE,minute);
        long unix = calendar.getTimeInMillis()/1000;

        if(Inicio){
            this.unix1=unix;
            Inicio=false;
            textViewDateFinal.setVisibility(View.VISIBLE);
        }else{
            this.unix2=unix;
            dialog_date_timeInterface.SendResult(unix1,unix2);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            dialog_date_timeInterface = (Dialog_Date_TimeInterface) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }
}
