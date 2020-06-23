package com.acne_classifier.acneclassifier;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class register extends Activity {
    public static final String TAG = "TAG";
    EditText mFullName, mEmail, mPassword,mconfirm;
    Button mRegisterBtn;
    int check = 0;
    String whatsappString;
    CheckBox checkwhatsapp;
    TextView mLoginBtn;
    FirebaseAuth fAuth;
    ProgressBar progressBar;
    DatabaseReference ref;
    register_data database_insert;
    FirebaseDatabase database;
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        mFullName = findViewById(R.id.name);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.pass);
        mconfirm = findViewById(R.id.confirm);
        mRegisterBtn = findViewById(R.id.register);
        mLoginBtn = findViewById(R.id.login);
        fAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressbar);
        database_insert = new register_data();
        ref = database.getInstance().getReference("user_database");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                count = 0;
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    count++;
                }
                Log.e("count", String.valueOf(count));
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (fAuth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }


        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), login.class));
            }
        });

    }

    public void register(View v) {
        Log.e("Clicked", "register");
        final String emailS = mEmail.getText().toString().trim();
        String passwordS = mPassword.getText().toString().trim();
        final String fullNameS = mFullName.getText().toString();
        final String confirmS = mconfirm.getText().toString();

        if (TextUtils.isEmpty(emailS)) {
            mEmail.setError("Email is Required.");
            return;
        }


        if (TextUtils.isEmpty(passwordS)) {
            mPassword.setError("Password is Required.");
            return;
        }

        if (passwordS.length() < 6) {
            mPassword.setError("Password Must be >= 6 Characters");
            return;
        }
        if (!confirmS.equals(passwordS)) {
            Log.e("password",passwordS);
            mconfirm.setError("Passwords do not match");
            return;
        }


        Log.e("Before", "Progress bar");

        progressBar.setVisibility(View.VISIBLE);

        // register the user in firebase

        fAuth.createUserWithEmailAndPassword(emailS, passwordS).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    database_insert.setEmail(emailS);
                    database_insert.setName(fullNameS);
                    ref.child("user" + (count + 1)).setValue(database_insert);
                    Toast.makeText(register.this, "User Created", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));

                } else {
                    Toast.makeText(register.this, "Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }


}



