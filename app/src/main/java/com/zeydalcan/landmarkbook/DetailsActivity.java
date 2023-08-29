package com.zeydalcan.landmarkbook;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.LauncherActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.zeydalcan.landmarkbook.databinding.ActivityDetailsBinding;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class DetailsActivity extends AppCompatActivity {
    SQLiteDatabase database;
    private ActivityDetailsBinding binding;
    private FirebaseStorage firebaseStorage;
    private FirebaseFirestore firebaseFirestore;

    private String TAG = "DetailsActivity";
    private StorageReference storageReference;
     public  Bitmap selectedImage;
    Uri imagedata;
    ActivityResultLauncher<Intent> intentActivityResultLauncher;
    ActivityResultLauncher<String> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        registerLauncher();
        firebaseStorage=FirebaseStorage.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();
        storageReference=firebaseStorage.getReference();
        database = this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);


        Intent intent = getIntent();
        String info = intent.getStringExtra("value");

        if (info.matches("new")) {
            binding.nameText.setText("");
            binding.button.setVisibility(View.VISIBLE);

            Bitmap selectImage = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.select);
            binding.imageView.setImageBitmap(selectImage);


        } else {
            int artId = intent.getIntExtra("artId",1);
            binding.button.setVisibility(View.INVISIBLE);

            try {


                Cursor cursor = database.rawQuery("SELECT * FROM arts WHERE id = ?",new String[] {String.valueOf(artId)});

                int artNameIx = cursor.getColumnIndex("name");
                int imageIx = cursor.getColumnIndex("image");

                while (cursor.moveToNext()) {

                    binding.nameText.setText(cursor.getString(artNameIx));
                    byte[] bytes = cursor.getBlob(imageIx);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    binding.imageView.setImageBitmap(bitmap);


                }

                cursor.close();

            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG,e.getLocalizedMessage());
            }
        }



    }
    public void save(View view){
        String name=binding.nameText.getText().toString();
        Bitmap smallImage=makeSmallerImage(selectedImage,300);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG,50,outputStream);
         byte[] byteArray = outputStream.toByteArray();
        try {
            database=this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);
            database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY,name VARCHAR, image BLOB, isFav INTEGER)");
            String sqlString = "INSERT INTO arts (name, image, isFav) VALUES (?, ?,0)";
            SQLiteStatement sqLiteStatement = database.compileStatement(sqlString);
            sqLiteStatement.bindString(1,name);
            sqLiteStatement.bindBlob(2,byteArray);
            sqLiteStatement.execute();

        }catch (Exception e) {
            Log.e(TAG,"error: " + e.getLocalizedMessage());
            e.printStackTrace();
        }
        Intent intentToMain=new Intent(DetailsActivity.this, MainActivity.class);
        intentToMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intentToMain);


    }
    public void select(View view){
        if (ContextCompat.checkSelfPermission(DetailsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(DetailsActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE))
            {
                //request permission with snackbar
                Snackbar.make(view,"Permission needed to use this app", BaseTransientBottomBar.LENGTH_INDEFINITE)
                        .setAction("Give Permission", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //request permission
                                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                            }
                        }).show();

            }
            else {
                //request permission
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }


        }else{
            //to gallery
            Intent intentToGallery=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intentActivityResultLauncher.launch(intentToGallery);
        }
    }
    public void registerLauncher(){
        intentActivityResultLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode()==RESULT_OK){
                    Intent intentFromResult=result.getData();
                    if (intentFromResult!=null){
                        imagedata=intentFromResult.getData();
                        try {

                            if (Build.VERSION.SDK_INT >= 28) {
                                ImageDecoder.Source source = ImageDecoder.createSource(DetailsActivity.this.getContentResolver(),imagedata);
                                selectedImage = ImageDecoder.decodeBitmap(source);
                                binding.imageView.setImageBitmap(selectedImage);

                            } else {
                                selectedImage = MediaStore.Images.Media.getBitmap(DetailsActivity.this.getContentResolver(),imagedata);
                                binding.imageView.setImageBitmap(selectedImage);
                            }

                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    }

                }


        });
        /////////////////////////////////////////////////////////////////////////////////////
        permissionLauncher=registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result){
                    //yes
                    Intent intentToGallery=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intentActivityResultLauncher.launch(intentToGallery);

                }else{
                    //no
                    Toast.makeText(DetailsActivity.this,"You must give permission",Toast.LENGTH_LONG).show();

                }

            }
        });

    }
    public  Bitmap makeSmallerImage(Bitmap image, int maximumSize) {

        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;

        if (bitmapRatio > 1) {
            width = maximumSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maximumSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image,width,height,true);
    }

}