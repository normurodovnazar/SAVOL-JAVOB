package com.normurodov_nazar.calcuator;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.normurodov_nazar.calcuator.Other.FirstOrder;
import com.normurodov_nazar.calcuator.Other.NumbersListener;

import java.util.ArrayList;
import java.util.Arrays;

public class Hey {
    public static void calculate(Context context, String t, NumbersListener numbersListener) {
        ArrayList<String> list = new ArrayList<>(Arrays.asList(t.split("!")));
        firstOrder(list);
        ArrayList<Float> numbers = new ArrayList<>();
        for (String s : list) {
            float f;
            try {
                f = Float.parseFloat(s);
                numbers.add(f);
            } catch (NumberFormatException e) {
                showToast(context, "Illegal format:" + e.getLocalizedMessage());
            }
        }
        numbersListener.onNumbers(numbers);

    }

    public static void firstOrder(ArrayList<String> list) {
        for (int i = 0; i < list.size(); i++) {
            print(list.get(i));
            FirstOrder order = FirstOrder.none;
            if (list.get(i).contains("*")) {
                order = FirstOrder.multiply;
            }
            if (list.get(i).contains("/")) {
                order = FirstOrder.divide;
            }
            if (order != FirstOrder.none) {
                float f1, f2;
                try {
                    f1 = Float.parseFloat(list.get(i - 1).replace("!", ""));
                    f2 = Float.parseFloat(list.get(i).replace("!", "").replace(order == FirstOrder.multiply ? "*" : "/", ""));
                    float f = order == FirstOrder.multiply ? f1 * f2 : f1 / f2;
                    String s = "";
                    if (f >= 0) {
                        s += "+" + f;
                    } else {
                        s += "" + f;
                    }
                    list.set(i - 1, s);
                    list.remove(i);
                    firstOrder(list);
                } catch (NumberFormatException e) {
                    print(e.getLocalizedMessage());
                }
            }
        }
    }

    public static void print(String s) {
        Log.e("x", s);
    }

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}
