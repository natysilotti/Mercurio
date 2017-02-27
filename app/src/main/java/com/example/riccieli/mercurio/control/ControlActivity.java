package com.example.riccieli.mercurio.control;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.example.riccieli.mercurio.R;

/**
 * Created by Riccieli on 05/12/2016.
 */

public class ControlActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }
    public void ClearText(View v) {
        EditText editText = (EditText) findViewById(R.id.input_text);

    }
}
