package com.example.elcitador;

import static android.content.ContentValues.TAG;

import static com.example.elcitador.LoggingHelper.deleteLogFile;
import static com.example.elcitador.LoggingHelper.readLogFromFile;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private Handler handler;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = getSharedPreferences("CitasPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        fillDropdowns();
        fillCitaData();

        Spinner especialidadSpinner = findViewById(R.id.especialidadSpinner);
        Spinner motivoSpinner = findViewById(R.id.motivoSpinner);
        Button button = findViewById(R.id.scheduleButton);

        //Check whether there is some cita being searched
        if(!sharedPreferences.getString("currentEspecialidad", "").isEmpty()){
            button.setText("Dejar de buscar la cita");
        }

        // Periodically query the time of last cita
        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {
                // Call your function here
                fillCitaData();

                // Schedule the next execution after the interval
                handler.postDelayed(this, 2000L);
            }
        };
        handler.post(runnable);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(button.getText().equals("Quiero la cita")){
                    // Get the selected item text from the spinners. Then, schedule task
                    String selectedItem = especialidadSpinner.getSelectedItem().toString();
                    editor.putString("currentEspecialidad", selectedItem);
                    Integer selectedItemValue = DataClasser.especialidadMap.get(selectedItem).first;
                    String selectedMotivo = motivoSpinner.getSelectedItem().toString();
                    editor.putString("currentMotivo", selectedMotivo);
                    editor.apply();
                    String selectedMotivoValue = DataClasser.especialidadMap.get(selectedItem).second.get(selectedMotivo).toString();
                    Toast.makeText(MainActivity.this, "Scheduling task--> ITEM:" + selectedItemValue + " MOTIV:" + selectedMotivoValue, Toast.LENGTH_SHORT).show();
                    PosterScheduler.scheduleTask(MainActivity.this, selectedItemValue, selectedMotivoValue);
                    Log.d(TAG, "Started to search for la cita");
                    fillCitaData();
                    button.setText("Dejar de buscar la cita");
                }else{
                    button.setText("Quiero la cita");
                    PosterScheduler.deletePreviousAlarms(MainActivity.this);
                    Log.d(TAG, "Stopped searching for la cita");
                    TextView currentCita = findViewById(R.id.currentCitaTextView);
                    editor.putString("currentEspecialidad", "");
                    editor.putString("currentMotivo", "");
                    editor.putString("fechaCita", "");
                    editor.putString("diaSemana", "");
                    editor.putString("horaCita", "");
                    editor.putString("nombrePro", "");
                    editor.putString("nombreCentro", "");
                    editor.putString("lastQueryTime", "");
                    editor.apply();
                    fillCitaData();
                }

            }
        });

        Button updateButton = findViewById(R.id.updateButton);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fillCitaData();
            }
        });

        Button showLogButton = findViewById(R.id.logButton);
        showLogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogDialog();
            }
        });

        Button deleteLogButton = findViewById(R.id.deleteLogButton);
        deleteLogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteLogFile(MainActivity.this);
            }
        });

        Button manualQueryButton = findViewById(R.id.manualQueryButton);
        manualQueryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Manually query the data now
                String selectedItem = especialidadSpinner.getSelectedItem().toString();
                Integer selectedItemValue = DataClasser.especialidadMap.get(selectedItem).first;
                String selectedMotivo = motivoSpinner.getSelectedItem().toString();
                String selectedMotivoValue = DataClasser.especialidadMap.get(selectedItem).second.get(selectedMotivo).toString();
                Intent intent = new Intent(MainActivity.this, PosterBroadcastReceiver.class);
                intent.putExtra("especialidad", selectedItemValue); // Pass the integer value
                intent.putExtra("motivo", selectedMotivoValue); // Pass the string value
                Toast.makeText(MainActivity.this, "Scheduling task--> ITEM:" + selectedItemValue + " MOTIV:" + selectedMotivoValue, Toast.LENGTH_SHORT).show();
                PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
                try {
                    pendingIntent.send();
                    Log.d(TAG, "Successfully sent manual pending intent");
                } catch (PendingIntent.CanceledException e) {
                    Log.e(TAG, "Error manually sending pending intent");
                    e.printStackTrace();
                }
            }
        });

        //When we select an especialidad, fill motivo spinner
        especialidadSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected item
                Log.d(TAG, "Filling motivos spinner at position "+position);
                String selectedItem = especialidadSpinner.getSelectedItem().toString();
                ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, new ArrayList<>(DataClasser.especialidadMap.get(selectedItem).second.keySet()));
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                motivoSpinner.setAdapter(adapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });


    }

    private void fillDropdowns(){
        Spinner especialidadSpinner = findViewById(R.id.especialidadSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>(DataClasser.especialidadMap.keySet()));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        especialidadSpinner.setAdapter(adapter);
    }

    private void fillCitaData(){
        SharedPreferences sharedPreferences = this.getSharedPreferences("CitasPrefs", Context.MODE_PRIVATE);
        Log.d(TAG, "filled cita data");
        TextView date = findViewById(R.id.dateText);
        TextView hour = findViewById(R.id.hourText);
        TextView pro = findViewById(R.id.proText);
        TextView centro = findViewById(R.id.centroText);
        TextView lastQuery = findViewById(R.id.lastQueryTextView);
        TextView cita = findViewById(R.id.citaDataTextView);
        lastQuery.setText(sharedPreferences.getString("lastQueryTime", ""));
        date.setText(sharedPreferences.getString("fechaCita", ""));
        hour.setText(sharedPreferences.getString("horaCita", ""));
        pro.setText(sharedPreferences.getString("nombrePro", ""));
        centro.setText(sharedPreferences.getString("nombreCentro", ""));
        cita.setText(sharedPreferences.getString("currentEspecialidad", "")+" "+sharedPreferences.getString("currentMotivo", ""));

        TextView currentCita = findViewById(R.id.currentCitaTextView);
        currentCita.setText(sharedPreferences.getString("currentEspecialidad", "") + "\nMotivo:\n"+sharedPreferences.getString("currentMotivo", ""));
    }

    public void showLogDialog() {
        // Get the logged data
        String logData = LoggingHelper.readLogFromFile(this);

        // Create a ScrollView to make the dialog content scrollable
        ScrollView scrollView = new ScrollView(this);
        int padding = 100;
        scrollView.setPadding(padding, padding, padding, padding);

        // Create a TextView to display the log data
        TextView textView = new TextView(this);
        textView.setText(logData);
        textView.setTextAppearance(android.R.style.TextAppearance_Medium);

        // Add the TextView to the ScrollView
        scrollView.addView(textView);

        // Create and show the dialog with the ScrollView as the content
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Log Data");
        builder.setView(scrollView);
        builder.setPositiveButton("OK", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}