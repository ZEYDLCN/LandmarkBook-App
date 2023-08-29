package com.zeydalcan.landmarkbook;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.zeydalcan.landmarkbook.databinding.ActivityDetailsBinding;
import com.zeydalcan.landmarkbook.databinding.ActivityFavoritesBinding;

import java.util.ArrayList;

public class FavoritesActivity extends AppCompatActivity {

    private ActivityFavoritesBinding binding;



    ArrayList<Art> favList;
    FavAdapter favAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFavoritesBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        favList=new ArrayList<>();
        binding.favoriView.setLayoutManager(new LinearLayoutManager(FavoritesActivity.this));
        favAdapter=new FavAdapter(favList);
        binding.favoriView.setAdapter(favAdapter);

        getData();
    }
    public void getData()
    {
        try {
            SQLiteDatabase database = this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);
            Cursor cursor = database.rawQuery("SELECT * FROM arts", null);
            int nameIx = cursor.getColumnIndex("name");
            int idIx = cursor.getColumnIndex("id");
            int isFavIx=cursor.getColumnIndex("isFav");


            while (cursor.moveToNext()) {
                int isFav=cursor.getInt(isFavIx);
                if (isFav != 0){
                    String name = cursor.getString(nameIx);
                    int id = cursor.getInt(idIx);
                    Art art=new Art(name,id);
                    favList.add(art);
                }
                else {

                }

            }
            cursor.close();
            favAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.e("FavoriSorun",e.getLocalizedMessage());
            e.printStackTrace();
        }


    }
}