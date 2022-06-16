package com.example.chessclock;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class TimePickerActivity extends AppCompatActivity {
    private NumberPicker secondPicker, minutePicker;

    private Button setTimeBtn;

    private TextInputEditText incrementText;

    private Intent intent;

    private final int MAIN_ACTIVITY_CODE = 2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_time);

        secondPicker = findViewById(R.id.numpicker_seconds);
        minutePicker = findViewById(R.id.numpicker_minutes);

        intent = new Intent();

        Bundle bundle = getIntent().getExtras();
        int prev_mins = 5, prev_secs = 0;
        // Set the previously set minutes and seconds for users' convenience
        if (bundle != null) {
            prev_mins = bundle.getInt("Minutes");
            prev_secs = bundle.getInt("Seconds");
        }

        secondPicker.setMinValue(0);
        secondPicker.setMaxValue(60);
        secondPicker.setWrapSelectorWheel(true);
        secondPicker.setValue(prev_secs);

        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(60);
        minutePicker.setWrapSelectorWheel(true);
        minutePicker.setValue(prev_mins);

        intent.putExtra("Minutes", prev_mins);
        intent.putExtra("Seconds", prev_secs);
        secondPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                int sec = numberPicker.getValue();
                intent.putExtra("Seconds", sec);
            }
        });

        minutePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                int min = numberPicker.getValue();
                intent.putExtra("Minutes", min);
            }
        });

        setTimeBtn = (Button) findViewById(R.id.selectTime);
        // Increment in seconds
        incrementText = findViewById(R.id.incrementText);
        setTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String temp = incrementText.getText().toString();
                int increment = (!temp.equals("")) ? Integer.parseInt(temp) : 0;
                intent.putExtra("Increment", increment);
                setResult(MAIN_ACTIVITY_CODE, intent);
                finish();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        setResult(RESULT_OK, intent);
    }
}
