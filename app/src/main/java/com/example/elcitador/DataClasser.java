package com.example.elcitador;

import android.util.Pair;

import java.util.HashMap;
import java.util.Map;

public class DataClasser {
    static public HashMap<String, Pair<Integer, HashMap<String, String>>> especialidadMap = new HashMap<String, Pair<Integer, HashMap<String, String>>>() {{
        put("Alergología", new Pair(2, new HashMap<String, String>() {{
            put("Consulta", "201.0000");
            put("Sucesiva", "202.0000");
        }}));
        put("Análisis clínicos", new Pair(3, new HashMap<String, String>() {{
            put("Test laboratorio", "13749.00");
        }}));
        put("Cardiología", new Pair(6, new HashMap<String, String>() {{
            put("Consulta", "953.0000");
        }}));
        put("Dermatología", new Pair(13, new HashMap<String, String>() {{
            put("Consulta", "3175.000");
            put("Sucesiva", "3176.000");
        }}));
        put("Digestivo", new Pair(15, new HashMap<String, String>() {{
            put("Consulta", "850.0000");
            put("Sucesiva", "851.0000");
        }}));
        put("Endocrinología", new Pair(16, new HashMap<String, String>() {{
            put("Consulta", "14714.00");
            put("Sucesiva", "14715.00");
        }}));
        put("Ginecología", new Pair(20, new HashMap<String, String>() {{
            put("Consulta", "4756.000");
            put("Sucesiva", "4758.000");
        }}));
        put("Medicina interna", new Pair(26, new HashMap<String, String>() {{
            put("Consulta", "8593.000");
            put("Sucesiva", "8594.000");
        }}));
        put("Medicina General", new Pair(69, new HashMap<String, String>() {{
            put("Consulta", "19845.00");
            put("Sucesiva", "19846.00");
        }}));
        put("Enfermería", new Pair(17, new HashMap<String, String>() {{
            put("Vacunación", "19310.00");
        }}));
        put("Oncología", new Pair(34, new HashMap<String, String>() {{
            put("Consulta", "11813.00");
            put("Sucesiva", "11814.00");
        }}));
        put("Unidad del dolor", new Pair(48, new HashMap<String, String>() {{
            put("Consulta", "19047.00");
            put("Sucesiva", "19048.00");
        }}));
        put("Neumología", new Pair(28, new HashMap<String, String>() {{
            put("Consulta", "3666.000");
            put("Sucesiva", "3667.000");
        }}));
        put("Rehabilitación", new Pair(44, new HashMap<String, String>() {{
            put("Consulta", "6472.000");
            put("Sucesiva", "6473.000");
        }}));
        put("Traumatología general", new Pair(47, new HashMap<String, String>() {{
            put("Consulta urgente", "13590.00");
            put("Consulta", "7693.000");
            put("Sucesiva", "7694.000");
        }}));
        put("Urología", new Pair(2, new HashMap<String, String>() {{
            put("Consulta checkeo", "26490.00");
            put("Consulta urgente", "13591.00");
            put("Consulta", "8356.000");
            put("Sucesiva", "8357.000");
        }}));
    }};

}
