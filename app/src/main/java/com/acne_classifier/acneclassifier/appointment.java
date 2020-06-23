package com.acne_classifier.acneclassifier;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class appointment extends Activity {
    DatabaseReference ref;
    appointment_data database_insert;
    FirebaseDatabase database;
    Button appoint;
    int count;
    EditText name,email,phone;
    RadioGroup gender;
    TextView date,time;
    DatePickerDialog.OnDateSetListener dateSetListener;
    TimePickerDialog.OnTimeSetListener timeSetListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.appointment);
        database_insert=new appointment_data();
        name=findViewById(R.id.name);
        date=findViewById(R.id.date);
        appoint=findViewById(R.id.appoint);
        time=findViewById(R.id.time);
        email=findViewById(R.id.email);
        phone=findViewById(R.id.phone);
        gender=findViewById(R.id.gender);
        ref=database.getInstance().getReference("appointment_database");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                count=0;
                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    count++;
                }
                Log.e("count",String.valueOf(count));
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                // Create a new instance of DatePickerDialog and return it
                DatePickerDialog datePickerDialog=new DatePickerDialog(
                        appointment.this,android.R.style.Theme_Holo_Light_Dialog_MinWidth,dateSetListener,year,month,day);
                datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                datePickerDialog.show();
            }
        });

        dateSetListener=new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month=month+1;
                Log.e("on date set(mm/dd/yyyy",month+"/"+dayOfMonth+"/"+year);
                date.setText(dayOfMonth+"/"+month+"/"+year);
            }
        };

        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog=new TimePickerDialog(
                        appointment.this,timeSetListener,hour,minute,false);
                timePickerDialog.getWindow().setBackgroundDrawable((new ColorDrawable(Color.TRANSPARENT)));
                timePickerDialog.show();
            }
        });

        timeSetListener =new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hour, int minutes) {
                String timeSet = "";
                if (hour > 12) {
                    hour -= 12;
                    timeSet = "PM";
                } else if (hour == 0) {
                    hour += 12;
                    timeSet = "AM";
                } else if (hour == 12){
                    timeSet = "PM";
                }else{
                    timeSet = "AM";
                }

                String min = "";
                if (minutes < 10)
                    min = "0" + minutes ;
                else
                    min = String.valueOf(minutes);


                // Append in a StringBuilder
                String aTime = new StringBuilder().append(hour).append(':')
                        .append(min ).append(" ").append(timeSet).toString();
                time.setText(aTime);
            }
        };

        appoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleDateFormat sdfd = new SimpleDateFormat("dd/MM/yyyy");
                String currdate = sdfd.format(System.currentTimeMillis());
                Date dateobj = new Date();
                SimpleDateFormat sdft = new SimpleDateFormat("hh:mm a");
                String currTime=sdft.format(dateobj);
                if(date.getText().toString().equals("Select Date"))
                    date.setText(currdate);
                if(time.getText().toString().equals("Select Time"))
                    time.setText(currTime.toString());

                database_insert.setName(name.getText().toString());
                database_insert.setDate(date.getText().toString());
                database_insert.setTime(time.getText().toString());
                database_insert.setEmail(email.getText().toString());
                database_insert.setPhone(phone.getText().toString());
                int gen=gender.getCheckedRadioButtonId();
                RadioButton radioButton = (RadioButton) findViewById(gen);
                database_insert.setGender(radioButton.getText().toString());;

                ref.child("appointment"+(count+1)).setValue(database_insert);
                count=0;
                name.setText("");
                date.setText("Select Date");
                time.setText("Select Time");
                email.setText("");
                phone.setText("");
                gender.clearCheck();
                Toast.makeText(appointment.this, "Appointment Booked Successfully", Toast.LENGTH_SHORT).show();
            }
        });

    }

}

