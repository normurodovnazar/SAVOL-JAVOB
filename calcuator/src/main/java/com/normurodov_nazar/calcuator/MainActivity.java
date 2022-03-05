package com.normurodov_nazar.calcuator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.normurodov_nazar.calcuator.Other.NumbersListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    String t = "";
    TextView text;
    ListView list;
    Button b0, b1, b2, b3, b4, b5, b6, b7, b8, b9, clear, back, plus,minus,multiply,divide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initVars();
    }

    private void initVars() {
        multiply = findViewById(R.id.multiply);multiply.setOnClickListener(this);
        divide = findViewById(R.id.divide);divide.setOnClickListener(this);
        list = findViewById(R.id.list);
        plus = findViewById(R.id.plus);
        plus.setOnClickListener(this);
        minus = findViewById(R.id.minus);
        minus.setOnClickListener(this);
        b0 = findViewById(R.id.b0);
        b0.setOnClickListener(this);
        b1 = findViewById(R.id.b1);
        b1.setOnClickListener(this);
        b2 = findViewById(R.id.b2);
        b2.setOnClickListener(this);
        b3 = findViewById(R.id.b3);
        b3.setOnClickListener(this);
        b4 = findViewById(R.id.b4);
        b4.setOnClickListener(this);
        b5 = findViewById(R.id.b5);
        b5.setOnClickListener(this);
        b6 = findViewById(R.id.b6);
        b6.setOnClickListener(this);
        b7 = findViewById(R.id.b7);
        b7.setOnClickListener(this);
        b8 = findViewById(R.id.b8);
        b8.setOnClickListener(this);
        b9 = findViewById(R.id.b9);
        b9.setOnClickListener(this);
        text = findViewById(R.id.t);
        back = findViewById(R.id.back);
        back.setOnClickListener(this);
        clear = findViewById(R.id.clear);
    }


    @Override
    public void onClick(View view) {
        if (b1.equals(view)){
            t+="1";
        }
        if (b2.equals(view)){
            t+="2";
        }
        if (b3.equals(view)){
            t+="3";
        }
        if (b4.equals(view)){
            t+="4";
        }
        if (b5.equals(view)){
            t+="5";
        }
        if (b6.equals(view)){
            t+="6";
        }
        if (b7.equals(view)){
            t+="7";
        }
        if (b8.equals(view)){
            t+="8";
        }
        if (b9.equals(view)){
            t+="9";
        }
        if (b0.equals(view)) {
            t += "0";
        }
        if (back.equals(view)) {
            if (!t.isEmpty()) {
                t = t.substring(0, t.length() - 1);
            }
        }
        if (plus.equals(view)) {
            if (lastIsNumber()) t = t.concat("!+");
        }
        if (minus.equals(view)){
            if (lastIsNumber()) t = t.concat("!-");
        }
        if (multiply.equals(view)){
            if (lastIsNumber()&&!t.isEmpty()) t = t.concat("!*");
        }
        if (divide.equals(view)){
            if (lastIsNumber()&&!t.isEmpty()) t = t.concat("!/");
        }
        Hey.calculate(this, t, numbers -> {
            float x = 0;
            for (float f:numbers) {
                x+=f;
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.text, new String[]{String.valueOf(x)});
            list.setAdapter(adapter);
        });
        setText();
    }

    private boolean lastIsNumber() {
        if (t.isEmpty()) return true; else{
            String[] s = t.split("");
            String l = s[s.length-1];
            return !l.equals("+") && !l.equals("-") && !l.equals("*") && !l.equals("/");
        }
    }

    private void setText() {
        text.setText(t.replaceAll("!",""));
    }
}