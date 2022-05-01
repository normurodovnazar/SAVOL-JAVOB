package com.normurodov_nazar.volleytest;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.normurodov_nazar.volleytest.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    RequestQueue queue;
    final String url = "https://www.google.com";
    ActivityMainBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityMainBinding.inflate(getLayoutInflater());
        View v = b.getRoot();
        setContentView(v);
        b.button.setOnClickListener(view -> {
            b.text.setText("loading");
            queue = Volley.newRequestQueue(MainActivity.this);
            StringRequest stringRequest;
            String u = b.url.getText().toString();
            if (u.isEmpty()){
                stringRequest = stringRequest(url);
            }else {
                stringRequest = stringRequest(u);
            }
            queue.add(stringRequest);
        });
    }

    private StringRequest stringRequest(String ur){
        return new StringRequest(Request.Method.GET, ur, response -> b.text.setText(response), error -> b.text.setText("Error:" + error.getLocalizedMessage()));
    }
}