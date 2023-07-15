package com.example.elcitador;

import static android.provider.Settings.System.DATE_FORMAT;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public class PosterBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "PosterBroadcastReceiver";

    @SuppressLint("StaticFieldLeak")
    @Override
    public void onReceive(Context context, Intent intent) {
        // Perform your HTTP POST request here
        Log.d(TAG, "HTTP POST request triggered");

        int especialidad = intent.getIntExtra("especialidad", 0);
        String motivo = intent.getStringExtra("motivo");

        if(especialidad==0)
        {
            return;
        }


        // Perform HTTP POST request in the background using an AsyncTask
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                StringBuilder responseBuilder = new StringBuilder();
                //HTTP request
                try {
                    // Construct the URL with the arguments
                    SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
                    String urlString = "https://www.quironsalud.es/idcsalud-client/cm/quironsalud/pdp-api/v1/citas/huecos" +
                            "?codCitacion=" + 0 +
                            "&idPrestacion=" + URLEncoder.encode(motivo, "UTF-8") +
                            "&idGarante=" + 2 +
                            "&idEspecialidad=" + especialidad +
                            "&idGestion=" + ThreadLocalRandom.current().nextInt(1000, 2738191 + 1) +
                            "&financialType=" + 1 +
                            "&garanteHIS=" + "false" +
                            "&espPresHIS=" + "false" +
                            "&fechaInicio= " + URLEncoder.encode(DATE_FORMAT.format(new Date()), "UTF-8") +
                            "&horaInicio= " + "000000" +
                            "&tipoBusqueda=" + "SIMPLE" +
                            "&idProvincia=" + 45 +
                            "&isLast=" + "false";
                    URL url = new URL(urlString);

                    // Open a connection to the URL
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");

                    // Read the response from the server
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Log.d(TAG, "HTTP POST request succeeded");
                        InputStream inputStream = connection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            responseBuilder.append(line);
                        }
                        reader.close();
                    } else {
                        // Handle the error case
                        Log.e(TAG, "HTTP POST request failed with response code: " + responseCode);
                    }
                    // Close the connection
                    connection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "Exception occurred during HTTP POST request: " + e.getMessage());
                }
                return responseBuilder.toString();
            }

            @Override
            protected void onPostExecute(String response) {
                // Use the response as needed
                Log.d(TAG, "Parsing HTTP POST response: " + response);
                SharedPreferences sharedPreferences = context.getSharedPreferences("CitasPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                try {
                    JSONArray jsonArray = new JSONArray(response);

                    // Check if the JSON array is not empty
                    if (jsonArray.length() > 0) {
                        // Get the first JSON object from the array
                        JSONObject jsonObject = jsonArray.getJSONObject(0);

                        // Extract the desired fields
                        String horaCitaStr = jsonObject.getString("horaCitaStr");
                        String fechaCitaStr = jsonObject.getString("fechaCitaStr");
                        String nombreProfesional = jsonObject.getString("nombreProfesional");
                        String diaSemanaCita = jsonObject.getString("diaSemanaCita");
                        String nombreCentroAsociado = jsonObject.getString("nombreCentroAsociado");

                        //Log the data
                        LoggingHelper.logHttpRequest(context, "H:"+horaCitaStr+" F:"+fechaCitaStr+" N:"+nombreProfesional+" D:"+diaSemanaCita+" C:"+nombreCentroAsociado);

                        // Use the extracted values as needed
                        Log.d(TAG, "horaCitaStr: " + horaCitaStr);
                        Log.d(TAG, "fechaCitaStr: " + fechaCitaStr);
                        Log.d(TAG, "nombreProfesional: " + nombreProfesional);
                        Log.d(TAG, "diaSemanaCita: " + diaSemanaCita);
                        Log.d(TAG, "nombreCentroAsociado: " + nombreCentroAsociado);

                        //If we find that the new date is sooner than any other we have found,
                        //then show notification
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                        try {
                            if(sharedPreferences.getString("fechaCita", "").isEmpty())
                            {
                                //First time we find a date
                                editor.putString("fechaCita", jsonObject.getString("fechaCitaStr"));
                                editor.putString("diaSemana", jsonObject.getString("diaSemanaCita"));
                                editor.putString("horaCita", jsonObject.getString("horaCitaStr"));
                                editor.putString("nombrePro", jsonObject.getString("nombreProfesional"));
                                editor.putString("nombreCentro", jsonObject.getString("nombreCentroAsociado"));
                                editor.apply();
                                showNotification(context);
                                return;
                            } else {
                                Date date1 = dateFormat.parse(fechaCitaStr);
                                Date date2 = dateFormat.parse(sharedPreferences.getString("fechaCita", ""));

                                if (date1.compareTo(date2) < 0) {
                                    //Found sooner date
                                    editor.putString("fechaCita", jsonObject.getString("fechaCitaStr"));
                                    editor.putString("diaSemana", jsonObject.getString("diaSemanaCita"));
                                    editor.putString("horaCita", jsonObject.getString("horaCitaStr"));
                                    editor.putString("nombrePro", jsonObject.getString("nombreProfesional"));
                                    editor.putString("nombreCentro", jsonObject.getString("nombreCentroAsociado"));
                                    editor.apply();
                                    showNotification(context);
                                } else if (date1.compareTo(date2) > 0) {
                                    editor.putString("fechaCita", jsonObject.getString("fechaCitaStr"));
                                    editor.putString("diaSemana", jsonObject.getString("diaSemanaCita"));
                                    editor.putString("horaCita", jsonObject.getString("horaCitaStr"));
                                    editor.putString("nombrePro", jsonObject.getString("nombreProfesional"));
                                    editor.putString("nombreCentro", jsonObject.getString("nombreCentroAsociado"));
                                    editor.apply();
                                    Log.d(TAG, "Found cita for "+fechaCitaStr+" at "+horaCitaStr+" but was later");
                                } else {
                                    Log.d(TAG, "Found the same cita for "+fechaCitaStr+" at "+horaCitaStr);
                                }
                            }

                            //Save date and time of last query
                            Date currentDate = new Date();
                            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                            editor.putString("lastQueryTime", dateTimeFormat.format(currentDate));
                            editor.apply();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "Exception occurred during parsing of JSON: " + e.getMessage());
                }
            }
        }.execute();

    }

    public void showNotification(Context context){
        // Create a notification channel if running on Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            String channelId = "elcitador_id";
            String channelName = "Canal ElCitador";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            notificationManager.createNotificationChannel(channel);
        }

        Log.d(TAG, "Showing notification");
        SharedPreferences sharedPreferences = context.getSharedPreferences("CitasPrefs", Context.MODE_PRIVATE);
        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "elcitador_id")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("NUEVA CITA DETECTADA")
                .setContentText("Cita el "+sharedPreferences.getString("fechaCita", "")+" a las "+sharedPreferences.getString("horaCita", "")+ " con "+sharedPreferences.getString("nombrePro", "")+ " en "+sharedPreferences.getString("nombreCentro", ""))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(new long[]{0, 500, 250, 500});

        // Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1, builder.build());
    }
}