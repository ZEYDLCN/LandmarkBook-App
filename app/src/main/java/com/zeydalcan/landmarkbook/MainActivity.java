package com.zeydalcan.landmarkbook;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.zeydalcan.landmarkbook.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    FirebaseFirestore firebaseFirestore;
    private ActivityMainBinding binding;
    ArrayList<Art> artArrayList;
    ArrayAdapter arrayAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        artArrayList=new ArrayList<>();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        arrayAdapter=new ArrayAdapter(artArrayList);
        binding.recyclerView.setAdapter(arrayAdapter);
        //getData();
        Button button=binding.favori;
        button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_star_24,0,0,0);



    }

    @Override
    protected void onResume() {
        super.onResume();
        getData();
    }

    public void favori(View view){
        Intent intent=new Intent(MainActivity.this,FavoritesActivity.class);
        startActivity(intent);
    }
    public void getData() {


        try {
            SQLiteDatabase database = this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);
            Cursor cursor = database.rawQuery("SELECT * FROM arts", null);

            int nameIx = cursor.getColumnIndex("name");
            int idIx = cursor.getColumnIndex("id");

            artArrayList.clear();

            while (cursor.moveToNext()) {
                String name = cursor.getString(nameIx);
                int id = cursor.getInt(idIx);
                Art art=new Art(name,id);
                artArrayList.add(art);
            }

            arrayAdapter.notifyDataSetChanged();
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



public void add(View view){
        Intent intentToDetail=new Intent(MainActivity.this,DetailsActivity.class);
        intentToDetail.putExtra("value","new");
        startActivity(intentToDetail);
    }



}