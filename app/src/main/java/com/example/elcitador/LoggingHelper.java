package com.example.elcitador;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LoggingHelper {
    private static final String TAG = "LoggingHelper";
    private static final String LOG_FILENAME = "http_log.txt";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US);

    public static void logHttpRequest(Context context, String request) {
        String logEntry = generateLogEntry(request);

        // Write the log entry to a file
        writeLogToFile(context, logEntry);

        // You can also store the log entry in a database or perform other operations
    }

    private static String generateLogEntry(String request) {
        String timestamp = DATE_FORMAT.format(new Date());
        return String.format("[%s]\n%s\n\n", timestamp, request);
    }

    private static void writeLogToFile(Context context, String logEntry) {
        FileWriter fileWriter = null;
        try {
            // Get the app-specific directory
            File logDir = new File(context.getExternalFilesDir(null), "logs");
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            File logFile = new File(logDir, LOG_FILENAME);
            fileWriter = new FileWriter(logFile, true);
            fileWriter.append(logEntry).append("\n");
            fileWriter.flush();
        } catch (IOException e) {
            Log.e(TAG, "Error writing log entry to file: " + e.getMessage());
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing file writer: " + e.getMessage());
                }
            }
        }
    }

    public static void deleteLogFile(Context context) {
        File logDir = new File(context.getExternalFilesDir(null), "logs");
        File logFile = new File(logDir, LOG_FILENAME);
        if (logFile.exists()) {
            boolean deleted = logFile.delete();
            if (deleted) {
                Log.d(TAG, "Log file deleted successfully");
            } else {
                Log.e(TAG, "Failed to delete log file");
            }
        }
    }

    public static String readLogFromFile(Context context) {
        FileInputStream fileInputStream = null;
        BufferedReader bufferedReader = null;
        try {
            // Get the app-specific directory
            File logDir = new File(context.getExternalFilesDir(null), "logs");
            File logFile = new File(logDir, LOG_FILENAME);
            if (!logFile.exists()) {
                return null; // Log file does not exist
            }

            // Read the log file contents
            fileInputStream = new FileInputStream(logFile);
            bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));

            StringBuilder logBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                logBuilder.append(line).append("\n");
            }

            return logBuilder.toString();
        } catch (IOException e) {
            Log.e(TAG, "Error reading log file: " + e.getMessage());
            return null;
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing buffered reader: " + e.getMessage());
                }
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing file input stream: " + e.getMessage());
                }
            }
        }
    }
}
