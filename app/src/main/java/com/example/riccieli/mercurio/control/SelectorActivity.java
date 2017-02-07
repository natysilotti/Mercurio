package com.example.riccieli.mercurio.control;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.NumberPicker;

import com.example.riccieli.mercurio.R;

public class SelectorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selector);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NumberPicker np = (NumberPicker) findViewById(R.id.number_picker);
        np.setMinValue(1);// restricted number to minimum value i.e 1
        np.setMaxValue(10);// restricked number to maximum value i.e. 31
        np.setWrapSelectorWheel(true);

        np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {

            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

                // TODO Auto-generated method stub

                String Old = "Old Value : ";

                String New = "New Value : ";

            }
        });

    }
}

