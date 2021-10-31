package com.normurodov_nazar.savol_javob.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.My;
import com.normurodov_nazar.savol_javob.MyD.Item;
import com.normurodov_nazar.savol_javob.MyD.ItemAdapter;
import com.normurodov_nazar.savol_javob.MyD.ItemClickListener;
import com.normurodov_nazar.savol_javob.R;

import java.util.ArrayList;

public class SelectItem extends AppCompatActivity {
    EditText search;
    RecyclerView list;
    Button addNew;
    ImageView searchIcon,addItem;
    ArrayList<String> selected = new ArrayList<>();
    CollectionReference reference;
    ItemAdapter adapter = null;
    ArrayList<Item> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_item);
        initVars();
    }

    private void onSearch() {
        String text = search.getText().toString();
        if (text.isEmpty()) empty();
        else Hey.collectionListener(this, reference, docs -> {
                items = new ArrayList<>();
                for(String name : selected) items.add(new Item(name));
                for (DocumentSnapshot doc : docs) {
                    String name = Item.fromDoc(doc).getName();
                    if(name.toLowerCase().contains(text.toLowerCase())) if(!selected.contains(name)) items.add(new Item(name));
                }
                showResult(items);
            }, errorMessage -> {

            });
    }

    private void initVars() {
        addItem = findViewById(R.id.addItems);addItem.setOnClickListener(v -> ready());
        searchIcon = findViewById(R.id.searchIcon);searchIcon.setOnClickListener(v -> onSearch());
        search = findViewById(R.id.itemText);search.setOnKeyListener((v, keyCode, event) -> true);
        list = findViewById(R.id.itemList);
        addNew = findViewById(R.id.addNew);addNew.setOnClickListener(v -> addDoc());
        reference = FirebaseFirestore.getInstance().collection(getIntent().getStringExtra("a"));
        if(My.result.isEmpty()) empty(); else {
            selected = My.result;
            for(String n:selected) items.add(new Item(n));
            showResult(items);
        }
    }

    private void ready() {
        if (selected.size()>0){
            My.result = selected;
            My.isSuccess = true;
            finish();
        }else Hey.showAlertDialog(this,getString(R.string.you_not_selected)).setOnDismissListener(dialog -> finish());
    }

    private void addDoc() {
        String text = search.getText().toString();
        if(!text.isEmpty() )Hey.addItem(this,reference,text);
    }

    private void empty() {
        addNew.setVisibility(View.INVISIBLE);
        list.setVisibility(View.INVISIBLE);
    }

    private void showResult(ArrayList<Item> items) {
        list.setVisibility(View.VISIBLE);
        addNew.setVisibility(View.INVISIBLE);
        ItemClickListener listener = (position, name) -> {
            if (selected.contains(name)) selected.remove(name);
            else selected.add(name);
            adapter.setChecked(selected);
            adapter.notifyItemChanged(position);
        };

        adapter = new ItemAdapter(this, items, selected, listener);
        list.setAdapter(adapter);
        list.setLayoutManager(new LinearLayoutManager(this));
        if(items.size() == selected.size()) noResult();
    }

    private void noResult() {
        list.setVisibility(View.VISIBLE);
        //noResults.setVisibility(View.VISIBLE);
        addNew.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Hey.print("a","A");
        return super.onKeyDown(keyCode, event);
    }
}