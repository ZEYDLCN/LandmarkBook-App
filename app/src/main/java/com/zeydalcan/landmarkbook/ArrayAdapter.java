package com.zeydalcan.landmarkbook;





import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zeydalcan.landmarkbook.databinding.RecyclerRowBinding;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class ArrayAdapter extends RecyclerView.Adapter<ArrayAdapter.ArtHolder> {
    SQLiteDatabase favDb;
    ArrayList<Art> starArrayList;
    ArrayList<Art> artArrayList;
    ArrayList<Art> favArrayList = new ArrayList<>();

    private String TAG = "ExceptionForMainFav";

    public ArrayAdapter(ArrayList<Art> artArrayList) {
        this.artArrayList = artArrayList;
    }

    public class ArtHolder extends RecyclerView.ViewHolder{
        private RecyclerRowBinding binding;

        public ArtHolder(RecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding=binding;

        }


    }


    @NonNull
    @Override
    public ArtHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        RecyclerRowBinding recyclerRowBinding=RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new ArtHolder(recyclerRowBinding);
    }



    @Override
    public void onBindViewHolder(@NonNull ArtHolder holder, int position) {
        holder.binding.item.setText(artArrayList.get(position).name);

        int id=artArrayList.get(position).id;

        try {

            favDb = holder.itemView.getContext().openOrCreateDatabase("Arts", Context.MODE_PRIVATE, null);
            String query = "SELECT * FROM arts WHERE id = ?";
            String[] selectionArgs = {String.valueOf(id)};
            Cursor cursor = favDb.rawQuery(query, selectionArgs);
            int isFavIx = cursor.getColumnIndex("isFav");

            while (cursor.moveToNext()) {
                int isFav=cursor.getInt(isFavIx);

                if (isFav == 1){
                    holder.binding.cbHeart.setChecked(true);
                } else {
                    holder.binding.cbHeart.setChecked(false);
                }
            }


        } catch (Exception e){
            Log.e(TAG,"hata: " + e.getLocalizedMessage());
        }


        holder.binding.cbHeart.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                try {
                    favDb = holder.itemView.getContext().openOrCreateDatabase("Arts", Context.MODE_PRIVATE, null);
                    String query = "SELECT * FROM arts WHERE id = ?";
                    String[] selectionArgs = {String.valueOf(id)};
                    Cursor cursor = favDb.rawQuery(query, selectionArgs);
                    int idIx = cursor.getColumnIndex("id");

                    while (cursor.moveToNext()) {
                        int Favid= cursor.getInt(idIx);

                        if (compoundButton.isChecked()){
                            favDb.execSQL(" UPDATE arts SET isFav=1 WHERE id="+Favid+"");
                            holder.binding.cbHeart.setChecked(true);


                        } else {
                            favDb.execSQL(" UPDATE arts SET isFav=0 WHERE id="+id+"");
                            holder.binding.cbHeart.setChecked(false);

                        }
                    }
                    cursor.close();
                    notifyDataSetChanged();
                    }
                catch (Exception e){
                    Log.e(TAG,e.getLocalizedMessage());
                }

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent=new Intent(holder.itemView.getContext(), DetailsActivity.class);
                intent.putExtra("value","old");
                intent.putExtra("artId",artArrayList.get(position).id);
                holder.itemView.getContext().startActivity(intent);
            }

        });


    }

    @Override
    public int getItemCount() {
        return artArrayList.size();
    }



    }
