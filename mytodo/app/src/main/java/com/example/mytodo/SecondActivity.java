package com.example.mytodo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Calendar;

public class SecondActivity extends AppCompatActivity {

    private DatePickerDialog.OnDateSetListener listener;

    TextView name;
    TextView description;
    SeekBar hardness;
    SeekBar progress;
    TextView date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        name = findViewById(R.id.editTextName);
        description = findViewById(R.id.editTextDescription);
        hardness = findViewById(R.id.seekBar);
        progress = findViewById(R.id.seekBar2);
        date = findViewById(R.id.textViewDate);

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(SecondActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        listener,
                        year, month, day);

                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        listener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String dateString = String.format("%02d.%02d.%04d", dayOfMonth, month + 1, year);
                date.setText(dateString);
            }
        };

        name.setText(getIntent().getExtras().getString("name"));
        description.setText(getIntent().getExtras().getString("description"));

        hardness.setMax(100);
        hardness.setProgress(getIntent().getExtras().getInt("hardness"));

        progress.setMax(100);
        progress.setProgress(getIntent().getExtras().getInt("progress"));

        date.setText(getIntent().getExtras().getString("date"));

        final Button but = findViewById(R.id.button);

        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = name.getText().toString().trim();
                but.setEnabled(!input.isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();

                intent.putExtra("name", name.getText().toString());
                intent.putExtra("description", description.getText().toString());
                intent.putExtra("hardness", hardness.getProgress());
                intent.putExtra("progress", progress.getProgress());

                Log.d("date:", date.getText().toString());

                intent.putExtra("date", date.getText().toString());

                setResult(1,  intent);

                finish(); //- удалить экземпляр
            }
        });

        date.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                MainActivity.color(name, MainActivity.importance(hardness.getProgress(), progress.getProgress(), date.getText().toString()));
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        hardness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                MainActivity.color(name, MainActivity.importance(progress, SecondActivity.this.progress.getProgress(), date.getText().toString()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                MainActivity.color(name, MainActivity.importance(hardness.getProgress(), progress, date.getText().toString()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        MainActivity.color(name, MainActivity.importance(hardness.getProgress(), progress.getProgress(), date.getText().toString()));
    }

}