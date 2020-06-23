package com.acne_classifier.acneclassifier;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class remedy extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.remedy);
    }
    public void appointment(View v)
    {
        Intent i1 = new Intent(getApplicationContext(), appointment.class);
        startActivity(i1);
    }
}
