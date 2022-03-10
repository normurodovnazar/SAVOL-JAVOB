package com.normurodov_nazar.calcuator;

import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.PopupMenu;

import com.normurodov_nazar.calcuator.Other.FirstOrder;
import com.normurodov_nazar.calcuator.Other.ItemClickListener;
import com.normurodov_nazar.calcuator.Other.NumbersListener;

import java.util.ArrayList;
import java.util.Arrays;

public class Hey {

    public static float size = -1;

    public static String calculate(String t, NumbersListener listener) {
        print("calculating", t);
        ArrayList<String> list = getArray(t, "!");
        while (expressionContains(list, "*") || expressionContains(list, "/")) firstOrder(list);
        ArrayList<Float> numbers = new ArrayList<>();
        for (String s : list) {
            try {
                float f = Float.parseFloat(s);
                numbers.add(f);
            } catch (NumberFormatException e) {
                print("calculate Exception", e.getLocalizedMessage());
            }
        }
        listener.onNumbers(numbers);
        return String.valueOf(sum(numbers));
    }

    public static String qavslardanQutilish(String t){
        print("qavslardanQutilish", t);
        ArrayList<String> list = getArray(t, "z");
        removeEmpties(list);
        for (int i = 0; i < list.size(); i++) {
            String x = list.get(i);
            print("analyzing", x);
            print("isSingleQavs " + x, String.valueOf(isSingleQavs(x)));
            if (isSingleQavs(x)) {
                list.set(i, computeSingleQavs(x));
            }
        }
        String s = join(list);
        if (s.contains("--") || s.contains("+-")) {
            if (s.contains("(")) {
                String r = replaceAll(s);
                print("after completion", r);
                r = putZ(r);
                return qavslardanQutilish(r);
            } else {
                print("all done", s);
                return s;
            }
        } else {
            if (s.contains("(")) {
                s = putZ(s);
                print("after completion", s);
                return qavslardanQutilish(s);
            } else {
                print("all done", s);
                return s;
            }
        }
    }

    public static String putZ(String t) {
        ArrayList<String> chars = getArray(t, "");
        for (int i = 0; i < chars.size(); i++) {
            if (chars.get(i).equals("(")) chars.set(i, "z(");
            if (chars.get(i).equals(")")) chars.set(i, ")z");
        }
        return join(chars);
    }

    public static boolean isSingleQavs(String t) {
        return t.charAt(0) == '(' && t.charAt(t.length() - 1) == ')';
    }

    public static String replaceAll(String t) {
        t = t.replaceAll("--", "+");
        while (t.contains("+-")) {
            t = t.replace("+-", "-");
        }
        return t;
    }

    public static String computeSingleQavs(String t) {
        ArrayList<String> list = getArray(t, "");
        if (!list.isEmpty()) {
            list.remove(list.size() - 1);
            list.remove(0);
            return calculate(join(list), numbers -> {});
        } else return "0";
    }

    public static float sum(ArrayList<Float> floats){
        float f = 0;
        for (float x : floats) f+=x;
        return f;
    }

    public static float kopaytmasi(ArrayList<Float> numbers){
        float f = 1;
        for (float x : numbers) f*=x;
        return f;
    }

    public static int nKopaytmasi(ArrayList<Integer> numbers){
        int f = 1;
        for (int x : numbers) f*=x;
        return f;
    }

    public static void firstOrder(ArrayList<String> list) {
        for (int i = 0; i < list.size(); i++) {
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
                } catch (NumberFormatException e) {
                    print("firstOrder exception", e.getLocalizedMessage());
                }
            }
        }
    }

    public static String join(ArrayList<String> list) {
        StringBuilder s = new StringBuilder();
        for (String x : list) s.append(x);
        return s.toString();
    }

    public static ArrayList<String> getArray(String t, String regex) {
        ArrayList<String> list = new ArrayList<>(Arrays.asList(t.split(regex)));
        if (regex.equals("")) {
            removeEmpties(list);
        }
        return list;
    }

    public static String backSpace(String t) {
        ArrayList<String> list = getArray(t, "");
        if (list.size() >= 2) {
            if (list.get(list.size() - 2).equals("!")) {
                list.remove(list.size() - 1);
                list.remove(list.size() - 1);
            } else if (list.get(list.size() - 1).equals("z") || list.get(list.size() - 1).equals("(")) {
                list.remove(list.size() - 1);
                list.remove(list.size() - 1);
            } else list.remove(list.size() - 1);
        } else list.remove(list.size() - 1);

        return join(list);
    }

    public static void removeEmpties(ArrayList<String> list) {
        ArrayList<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).isEmpty() || list.get(i).replaceAll(" ", "").isEmpty()) {
                indexes.add(i);
            }
            list.set(i, list.get(i).replaceAll(" ", ""));
        }
        for (int i : indexes) list.remove(i);
    }

    public static boolean isCorrectFormat(String t) {
        int i1 = 0, i2 = 0;
        for (int i = 0; i < t.length(); i++) {
            if (t.charAt(i) == '(') i1++;
        }
        for (int i = 0; i < t.length(); i++) {
            if (t.charAt(i) == ')') i2++;
        }
        print("i1 in " + t, String.valueOf(i1));
        print("i2 in " + t, String.valueOf(i2));
        return i1 == i2;
    }

    public static boolean expressionContains(ArrayList<String> list, String ch) {
        for (String s : list) {
            if (s.contains(ch)) {
                if (!s.equals(ch)) return true;
            }
        }
        return false;
    }

    public static void print(String tag, String s) {
        Log.e(tag, s);
    }

    public static float getOrtaArifmetik(ArrayList<Float> numbers){
        return sum(numbers)/ numbers.size();
    }

    public static float getOrtaGeometrik(ArrayList<Float> numbers){
        return (float) Math.pow(kopaytmasi(numbers),1d/numbers.size());
    }

    public static String numList(ArrayList<Float> numbers){
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i=0;i< numbers.size();i++){
            if (i == numbers.size()-1) sb.append(nForm(numbers.get(i))); else
                sb.append(nForm(numbers.get(i))).append(",");
        }
        sb.append("}");
        return sb.toString();
    }

    public static String numList(ArrayList<Integer> numbers,char c){
        StringBuilder sb = new StringBuilder();
        sb.append(c).append("={");
        for (int i=0;i< numbers.size();i++){
            if (i == numbers.size()-1) sb.append(nForm(numbers.get(i))); else
                sb.append(nForm(numbers.get(i))).append(",");
        }
        sb.append("}");
        return sb.toString();
    }

    public static String numList(ArrayList<Float> numbers,String n){
        StringBuilder sb = new StringBuilder();
        sb.append(n).append("={");
        for (int i=0;i< numbers.size();i++){
            if (i == numbers.size()-1) sb.append(nForm(numbers.get(i))); else
                sb.append(nForm(numbers.get(i))).append(",");
        }
        sb.append("}");
        return sb.toString();
    }

    public static ArrayList<Float> absValues(ArrayList<Float> numbers){
        ArrayList<Float> x = new ArrayList<>();
        for (float f : numbers) x.add(Math.abs(f));
        return x;
    }

    public static ArrayList<Integer> naturalValues(ArrayList<Float> absValues){
        ArrayList<Integer> integers = new ArrayList<>();
        for (float f : absValues) if (f>1) integers.add((int) f);
        return integers;
    }

    public static ArrayList<Float> darajalari(ArrayList<Float> numbers,double d){
        ArrayList<Float> k = new ArrayList<>();
        for (float f : numbers) k.add((float) Math.pow(f,d));
        return k;
    }

    public static String nForm(float f){
        int i = (int) f;
        if (i==f){
            return String.valueOf(i);
        }else
        return String.valueOf(f);
    }

    public static int ekub(ArrayList<Integer> numbers){
        int min = getMin(numbers);
        for (int i = min;i>=1;i--){
            if (divideListTo(numbers,i)==0) {
                return i;
            }
        }
        return 1;
    }

    public static int divideListTo(ArrayList<Integer> numbers, int i) {
        int q = 0;
        for (int n : numbers){
            q+=n%i;
        }
        return q;
    }

    public static int getNBS(int number){
        int q = 0;
        for (int i = number;i>=1;i--) {
            if (number%i==0) q+=1;
        }
        return q;
    }

    public static int getNBY(int number){
        int q = 0;
        for (int i = number;i>=1;i--) if (number%i==0) q+=i;
        return q;
    }

    public static int getMin(ArrayList<Integer> numbers){
        if (numbers.size()!=0){
            int min = numbers.get(0);
            for (int i : numbers) {
                if (i < min) min = i;
            }
            print("min in " + numbers.toString() + " is", String.valueOf(min));
            return min;
        }else return 1;
    }

    public static void showPopupMenu(Context context, View item, ArrayList<Float> items, ItemClickListener listener) {
        PopupMenu menu = new PopupMenu(context, item);
        for (int i=0;i<items.size();i++) {
            menu.getMenu().add(Menu.NONE, Menu.NONE, i, nForm(items.get(i)));
        }
        menu.setOnMenuItemClickListener(item1 -> {
            listener.onItemClick(item1.getOrder(), String.valueOf(item1.getTitle()));
            return true;
        });
        menu.show();
    }
    public static void showPopupMenuN(Context context, View item, ArrayList<Integer> items, ItemClickListener listener) {
        PopupMenu menu = new PopupMenu(context, item);
        for (int i=0;i<items.size();i++) {
            menu.getMenu().add(Menu.NONE, Menu.NONE, i, String.valueOf(items.get(i)));
        }
        menu.setOnMenuItemClickListener(item1 -> {
            listener.onItemClick(item1.getOrder(), String.valueOf(item1.getTitle()));
            return true;
        });
        menu.show();
    }

    public static void showPopupMenuS(Context context, View item, ArrayList<String> items, ItemClickListener itemClickListener, boolean showNow) {
        PopupMenu menu = new PopupMenu(context, item);
        for (String s : items) {
            menu.getMenu().add(Menu.NONE, Menu.NONE, items.indexOf(s), s);
        }
        menu.setOnMenuItemClickListener(item1 -> {
            itemClickListener.onItemClick(item1.getOrder(), null);
            return true;
        });
        if (showNow) menu.show();
    }
}
