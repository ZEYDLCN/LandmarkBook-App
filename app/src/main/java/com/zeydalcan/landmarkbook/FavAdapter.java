package com.zeydalcan.landmarkbook;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zeydalcan.landmarkbook.databinding.FavRowBinding;

import java.util.ArrayList;

public class FavAdapter extends RecyclerView.Adapter<FavAdapter.FavHolder>{
    ArrayList<Art> favlist;
    SQLiteDatabase database;

    public FavAdapter(ArrayList<Art> favlist) {
        this.favlist = favlist;
    }

    @NonNull
    @Override
    public FavHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FavRowBinding binding = FavRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);

        return new FavHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FavHolder holder, int position) {

        Log.i("FavAdapter","item name: " + favlist.get(position).name);
        holder.binding.item.setText(favlist.get(position).name);
        database=holder.itemView.getContext().openOrCreateDatabase("Arts", Context.MODE_PRIVATE,null);
        int id=favlist.get(position).id;

        holder.binding.cbHeart.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String query = "SELECT * FROM arts WHERE id = ?";
                String[] selectionArgs = {String.valueOf(id)};
                Cursor cursor = database.rawQuery(query, selectionArgs);
                int isFavIx=cursor.getColumnIndex("isFav");
                int idIx=cursor.getColumnIndex("id");

                while (cursor.moveToNext()) {
                    int isFav=cursor.getInt(isFavIx);
                    int id=cursor.getInt(idIx);

                    if (buttonView.isChecked()){
                        database.execSQL(" UPDATE arts SET isFav=0 WHERE id="+id+"");
                        holder.binding.cbHeart.setChecked(false);
                        favlist.remove(position);


                    } else {
                        database.execSQL(" UPDATE arts SET isFav=1 WHERE id="+id+"");
                        holder.binding.cbHeart.setChecked(true);

                    }
                }


                notifyDataSetChanged();
                cursor.close();


            }
        });
    }

    @Override
    public int getItemCount() {
        return favlist.size();
    }

    public class FavHolder extends RecyclerView.ViewHolder{
        private FavRowBinding binding;

        public FavHolder(FavRowBinding binding) {
            super(binding.getRoot());
            this.binding=binding;
        }
    }


    }
