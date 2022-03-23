package com.normurodov_nazar.volleytest;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class MainActivity extends AppCompatActivity {
    TextView text;

    RequestQueue queue;
    final String url = "https://www.google.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intiVars();
    }

    private void intiVars() {
        text = findViewById(R.id.text);
        queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response -> {
            text.setText(response);
        }, error -> {
            text.setText("Error:"+error.getLocalizedMessage());
        });
        queue.add(stringRequest);
    }
}