package com.example.elcitador;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fillDropdowns();
        fillCitaData();

        Spinner especialidadSpinner = findViewById(R.id.especialidadSpinner);
        Spinner motivoSpinner = findViewById(R.id.motivoSpinner);
        Button button = findViewById(R.id.scheduleButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the selected item text from the spinners. Then, schedule task
                String selectedItem = especialidadSpinner.getSelectedItem().toString();
                Integer selectedItemValue = DataClasser.especialidadMap.get(selectedItem).first;
                String selectedMotivo = motivoSpinner.getSelectedItem().toString();
                String selectedMotivoValue = DataClasser.especialidadMap.get(selectedItem).second.get(selectedMotivo).toString();
                Toast.makeText(MainActivity.this, "Scheduling task--> ITEM:" + selectedItemValue + " MOTIV:" + selectedMotivoValue, Toast.LENGTH_SHORT).show();
                PosterScheduler.scheduleTask(MainActivity.this, selectedItemValue, selectedMotivoValue);
                Log.d(TAG, "Clicked schedule button");
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
        Log.d(TAG, "filled cita data");
        TextView date = findViewById(R.id.dateText);
        TextView hour = findViewById(R.id.hourText);
        TextView pro = findViewById(R.id.proText);
        TextView centro = findViewById(R.id.centroText);
        TextView lastQuery = findViewById(R.id.lastQueryTextView);
        lastQuery.setText(DataClasser.lastQueryDateTime);
        date.setText(DataClasser.fechaCita);
        hour.setText(DataClasser.horaCita);
        pro.setText(DataClasser.nombrePro);
        centro.setText(DataClasser.nombreCentro);
    }
}