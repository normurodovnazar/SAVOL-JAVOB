package com.normurodov_nazar.calcuator;

import static com.normurodov_nazar.calcuator.Hey.absValues;
import static com.normurodov_nazar.calcuator.Hey.backSpace;
import static com.normurodov_nazar.calcuator.Hey.darajalari;
import static com.normurodov_nazar.calcuator.Hey.ekub;
import static com.normurodov_nazar.calcuator.Hey.getNBS;
import static com.normurodov_nazar.calcuator.Hey.getNBY;
import static com.normurodov_nazar.calcuator.Hey.getOrtaArifmetik;
import static com.normurodov_nazar.calcuator.Hey.getOrtaGeometrik;
import static com.normurodov_nazar.calcuator.Hey.isCorrectFormat;
import static com.normurodov_nazar.calcuator.Hey.nForm;
import static com.normurodov_nazar.calcuator.Hey.nKopaytmasi;
import static com.normurodov_nazar.calcuator.Hey.naturalValues;
import static com.normurodov_nazar.calcuator.Hey.numList;
import static com.normurodov_nazar.calcuator.Hey.print;
import static com.normurodov_nazar.calcuator.Hey.qavslardanQutilish;
import static com.normurodov_nazar.calcuator.Hey.showPopupMenu;
import static com.normurodov_nazar.calcuator.Hey.showPopupMenuN;
import static com.normurodov_nazar.calcuator.Hey.showPopupMenuS;
import static com.normurodov_nazar.calcuator.Hey.sum;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.MobileAds;
import com.normurodov_nazar.calcuator.Other.ResultAdapter;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    String t = "";
    SharedPreferences preferences;
    TextView text;
    RecyclerView list;
    ResultAdapter adapter;
    Button b0, b1, b2, b3, b4, b5, b6, b7, b8, b9, clear, back, plus, minus, multiply, divide, open, close, dot, x;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MobileAds.initialize(this);
        preferences = getSharedPreferences("a",MODE_PRIVATE);
        switch (preferences.getString("theme","def")){
            case "day":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "night":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "def":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initVars();
    }

    private void initVars() {
        x = findViewById(R.id.x);
        x.setOnClickListener(view -> showPopupMenuS(MainActivity.this, view, new ArrayList<>(Arrays.asList(getString(R.string.day), getString(R.string.night),getString(R.string.system),getString(R.string.help))), (position, name) -> {
            switch (position){
                case 0:
                    preferences.edit().putString("theme","day").apply();
                    recreate();
                    break;
                case 1:
                    preferences.edit().putString("theme","night").apply();
                    recreate();
                    break;
                case 2:
                    preferences.edit().putString("theme","def").apply();
                    recreate();
                    break;
                default:
                    startActivity(new Intent(MainActivity.this,HelpActivity.class));
                    break;
            }
        },true));
        open = findViewById(R.id.open);
        open.setOnClickListener(this);
        close = findViewById(R.id.close);
        close.setOnClickListener(this);
        multiply = findViewById(R.id.multiply);
        multiply.setOnClickListener(this);
        divide = findViewById(R.id.divide);
        divide.setOnClickListener(this);
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
        clear.setOnClickListener(this);
        dot = findViewById(R.id.dot);
        dot.setOnClickListener(this);
        tm(preferences.getString("t",""));
        float f = preferences.getFloat("s", 20);
        Hey.size = f;
        text.setTextSize(TypedValue.COMPLEX_UNIT_PX,f);
        if (adapter!=null)adapter.setTextSize(f);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        float f = text.getTextSize();
        print("textSize", String.valueOf(text.getTextSize()));
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            f = f * 1.1f;
            Hey.size = f;
            preferences.edit().putFloat("s", f).apply();
            text.setTextSize(TypedValue.COMPLEX_UNIT_PX,f);
            adapter.setTextSize(f);
            print("textSizeIs", String.valueOf(f));
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            f = f * 0.9f;
            Hey.size = f;
            preferences.edit().putFloat("s", f).apply();
            text.setTextSize(TypedValue.COMPLEX_UNIT_PX,f);
            adapter.setTextSize(f);
            print("textSizeIs", String.valueOf(f));
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View view) {
        if (clear.equals(view)) {
            t = "";
        }
        if (open.equals(view)) {
            if (qavsOchish()) t += "z(";
        }
        if (close.equals(view)) {
            if (qavsYopish()) t += ")z";
        }
        if (b1.equals(view)) {
            t += "1";
        }
        if (b2.equals(view)) {
            t += "2";
        }
        if (b3.equals(view)) {
            t += "3";
        }
        if (b4.equals(view)) {
            t += "4";
        }
        if (b5.equals(view)) {
            t += "5";
        }
        if (b6.equals(view)) {
            t += "6";
        }
        if (b7.equals(view)) {
            t += "7";
        }
        if (b8.equals(view)) {
            t += "8";
        }
        if (b9.equals(view)) {
            t += "9";
        }
        if (b0.equals(view)) {
            t += "0";
        }
        if (dot.equals(view)) {
            if (t.charAt(t.length()-1)!='z') t += ".";
        }
        if (back.equals(view)) {
            if (!t.isEmpty()) {
                print("after", t);
                t = backSpace(t);
                print("before", t);
            }
        }
        if (plus.equals(view)) {
            if (amalYozishMumkinmi()) t = t.concat("!+");
        }
        if (minus.equals(view)) {
            if (amalYozishMumkinmi()) t = t.concat("!-");
        }
        if (multiply.equals(view)) {
            if (amalYozishMumkinmi() && !t.isEmpty()) t = t.concat("!*");
        }
        if (divide.equals(view)) {
            if (amalYozishMumkinmi() && !t.isEmpty()) t = t.concat("!/");
        }
        tm(t);
    }

    private boolean qavsYopish() {
        if (t.isEmpty()) return false;
        else {
            char c = t.charAt(t.length() - 1);
            if (c == '+') return false;
            else if (c == '-') return false;
            else if (c == '*') return false;
            else return c != '/';
        }
    }

    private boolean qavsOchish() {
        if (t.isEmpty()) return true;
        else {
            char c = t.charAt(t.length() - 1);
            if (c == '+') return true;
            else if (c == '-') return true;
            else if (c == '*') return true;
            else return c == '/';
        }
    }

    private void method() {
        setText();
        if (isCorrectFormat(t)) {
            Hey.calculate(qavslardanQutilish(t), numbers -> {
                ArrayList<Float> absNumbers = absValues(numbers), kvadratlari = darajalari(numbers, 2), kublari = darajalari(numbers, 3),
                        kvIldizlari = darajalari(absNumbers, 0.5), kubIldizlari = darajalari(absNumbers, 1d / 3);

                String result = nForm(sum(numbers)), kvadratlariSum = nForm(sum(kvadratlari)),
                        kublariSum = nForm(sum(kublari)), arifmetik = nForm(getOrtaArifmetik(numbers)),
                        geometrik = nForm(getOrtaGeometrik(absNumbers));
                ArrayList<Integer> naturalNumbers = naturalValues(absNumbers);
                int ekub = 1,ekuk = 0,nbs = 0,nby = 0;
                if (naturalNumbers.size()==1){
                    nbs = getNBS(naturalNumbers.get(0));
                    nby = getNBY(naturalNumbers.get(0));
                }else {
                    ekub = ekub(naturalNumbers);
                    ekuk = nKopaytmasi(naturalNumbers)/ekub;
                }
                int finalNbs = nbs;
                int finalNby = nby;
                int finalEkub = ekub;
                int finalEkuk = ekuk;
                adapter = new ResultAdapter(this, new ArrayList<>(Arrays.asList(
                        getString(R.string.result) + result,//0
                        numList(numbers,"A"),//1
                        numbers.size()==1 ? "" : getString(R.string.arif) + arifmetik,//2
                        getString(numbers.size()==1 ? R.string.kvadrati : R.string.kvadratlari) + numList(kvadratlari),//3
                        numbers.size()==1 ? "" : (getString(R.string.kvadratlariSum) + kvadratlariSum),//4
                        getString(numbers.size()==1 ? R.string.kubi : R.string.kublari) + numList(kublari),//5
                        numbers.size()==1 ? "" : (getString(R.string.kublariSum) + kublariSum),//6
                        numList(absNumbers,"B"),//7
                        getString(numbers.size()==1 ? R.string.kvadratIldizi : R.string.kvadratIldizlari) + numList(kvIldizlari),//8
                        getString(numbers.size()==1 ? R.string.kubIldizi : R.string.kubIldizlari) + numList(kubIldizlari),//9
                        numbers.size()==1 ? "" : getString(R.string.geo) + geometrik,//10
                        numList(naturalNumbers,'C'),//11
                        naturalNumbers.size()==1 ? getString(R.string.nbs)+nbs : getString(R.string.ekub) + ekub,//12
                        naturalNumbers.size()==1 ? getString(R.string.nby)+nby : getString(R.string.ekuk) +ekuk//13
                )), (i,v, as) -> {
                    switch (i) {
                        case 0:
                            tm(result);
                            break;
                        case 1:
                            print("size", String.valueOf(numbers.size()));
                            if (numbers.size() != 1)
                                showPopupMenu(this, v, numbers, (p, n) -> tm(n));
                            else tm(nForm(numbers.get(0)));
                            break;
                        case 2:
                            if (numbers.size()!=1) tm(arifmetik);
                            break;
                        case 3:
                            if (kvadratlari.size() != 1)
                                showPopupMenu(this, v, kvadratlari, (position, name) -> tm(name));
                            else tm(nForm(kvadratlari.get(0)));
                            break;
                        case 4:
                            if (numbers.size()!=1)tm(kvadratlariSum);
                            break;
                        case 5:
                            if (kublari.size() != 1)
                                showPopupMenu(this, v, kublari, (position, name) -> tm(name));
                            else tm(nForm(kublari.get(0)));
                            break;
                        case 6:
                            if (numbers.size()!=1) tm(kublariSum);
                            break;
                        case 7:
                            if (absNumbers.size()!=1) showPopupMenu(this, v, absNumbers, (position, name) -> tm(name));
                            else tm(nForm(kvIldizlari.get(0)));
                            break;
                        case 8:
                            if (kvIldizlari.size() != 1)
                                showPopupMenu(this, v, kvIldizlari, (p, n) -> tm(n));
                            else tm(nForm(kvIldizlari.get(0)));
                            break;
                        case 9:
                            if (kubIldizlari.size() != 1)
                                showPopupMenu(this, v, kubIldizlari, (p, n) -> tm(n));
                            else tm(nForm(kubIldizlari.get(0)));
                            break;
                        case 10:
                            if (numbers.size()!=1) tm(geometrik);
                            break;
                        case 11:
                            if (naturalNumbers.size()!=1) showPopupMenuN(this, v, naturalNumbers, (position, name) -> tm(name));
                            else tm(String.valueOf(naturalNumbers.get(0)));
                            break;
                        case 12:
                            tm(String.valueOf(naturalNumbers.size()==1 ? finalNbs : finalEkub));
                            break;
                        case 13:
                            tm(String.valueOf(naturalNumbers.size()==1 ? finalNby : finalEkuk));
                            break;
                    }
                });
                list.setAdapter(adapter);
                list.setLayoutManager(new LinearLayoutManager(this));
            });
        }
    }

    private void tm(String n) {
        t = n;
        method();
    }

    private boolean amalYozishMumkinmi() {
        if (t.isEmpty()) return true;
        else {
            String[] s = t.split("");
            String l = s[s.length - 1];
            return !l.equals("+") && !l.equals("-") && !l.equals("*") && !l.equals("/") && !l.equals("(");
        }
    }

    private void setText() {
        text.setText(t.replaceAll("!", "").replaceAll("z", ""));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        preferences.edit().putString("t",t).apply();
    }
}