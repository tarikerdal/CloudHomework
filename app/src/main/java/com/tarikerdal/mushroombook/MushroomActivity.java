package com.tarikerdal.mushroombook;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
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
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.tarikerdal.mushroombook.databinding.ActivityMushroomBinding;

import java.io.ByteArrayOutputStream;

public class MushroomActivity extends AppCompatActivity {

    private ActivityMushroomBinding binding;
    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> permissionLauncher;
    Bitmap selectedImage;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMushroomBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        registerLauncher();

        Intent intent = getIntent();
        String info = intent.getStringExtra("info");

        if (info.equals("new")) {

            binding.originalEditText.setText("");
            binding.commonEditText.setText("");
            binding.poisonEditText.setText("");
            binding.button.setVisibility(View.VISIBLE);

            binding.imageView.setImageResource(R.drawable.selectimage);

        }else {
            int mushroomId = intent.getIntExtra("mushroomId" ,0);
            binding.button.setVisibility(View.INVISIBLE);
            binding.imageView.setClickable(false);
            binding.originalEditText.setEnabled(false);
            binding.commonEditText.setEnabled(false);
            binding.poisonEditText.setEnabled(false);

            try {
                database = this.openOrCreateDatabase("Mushroom" ,MODE_PRIVATE ,null);
                Cursor cursor = database.rawQuery("SELECT * FROM mushroom WHERE id = ?" ,new String[] {String.valueOf(mushroomId)});

                int originalnameIx = cursor.getColumnIndex("originalname");
                int commonnameIx = cursor.getColumnIndex("commonname");
                int ispoisonIx = cursor.getColumnIndex("ispoison");
                int imageIx = cursor.getColumnIndex("image");

                while (cursor.moveToNext()) {

                    binding.originalEditText.setText(cursor.getString(originalnameIx));
                    binding.commonEditText.setText(cursor.getString(commonnameIx));
                    binding.poisonEditText.setText(cursor.getString(ispoisonIx));


                    byte[] bytes = cursor.getBlob(imageIx);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes ,0 ,bytes.length);
                    binding.imageView.setImageBitmap(bitmap);

                }

                cursor.close();

            }catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    public Bitmap makeSmallerImage(Bitmap image ,int maximumSize){

        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;

        if (bitmapRatio > 1) {
            //landscape image
            width = maximumSize;
            height = (int) (width / bitmapRatio);

        }else {
            //portrait iamge

            height = maximumSize;
            width = (int) (height / bitmapRatio);
        }

        return image.createScaledBitmap(image ,width ,height ,true);
    }

    public void save(View view) {

        String original = binding.originalEditText.getText().toString();
        String common = binding.commonEditText.getText().toString();
        String poison = binding.poisonEditText.getText().toString();

        Bitmap smallImage = makeSmallerImage(selectedImage , 300);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG ,50 ,outputStream);
        byte[] byteArray = outputStream.toByteArray();

        try {

            database = this.openOrCreateDatabase("Mushroom" ,MODE_PRIVATE ,null);
            database.execSQL("CREATE TABLE IF NOT EXISTS mushroom (id INTEGER PRIMARY KEY , originalname VARCHAR ,commonname VARCHAR ,ispoison VARCHAR ,image BLOB) ");

            String sqlString = "INSERT INTO mushroom (originalname ,commonname ,ispoison ,image) VALUES(? ,? ,? ,?)";
            SQLiteStatement sqLiteStatement = database.compileStatement(sqlString);
            sqLiteStatement.bindString(1 ,original);
            sqLiteStatement.bindString(2 ,common);
            sqLiteStatement.bindString(3 ,poison);
            sqLiteStatement.bindBlob(4 ,byteArray);
            sqLiteStatement.execute();

        }catch (Exception e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(MushroomActivity.this ,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);


    }

    public void selectImage(View view) {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                Snackbar.make(view ,"Permission Needed for Continue" ,Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //request permission
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                }).show();

            }else {
                //request permission
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }

        }else {
            //gallery
            Intent intentToGallery = new Intent(Intent.ACTION_PICK , MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(intentToGallery);
        }

    }

    private void registerLauncher() {

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    Intent intentFromResult = result.getData();
                    if (intentFromResult != null) {
                        Uri imageData = intentFromResult.getData();

                        try {
                            if (Build.VERSION.SDK_INT >= 28) {
                                ImageDecoder.Source source = ImageDecoder.createSource(MushroomActivity.this.getContentResolver() , imageData);
                                selectedImage = ImageDecoder.decodeBitmap(source);
                                binding.imageView.setImageBitmap(selectedImage);
                            }else {
                                selectedImage = MediaStore.Images.Media.getBitmap(MushroomActivity.this.getContentResolver() ,imageData);
                                binding.imageView.setImageBitmap(selectedImage);
                            }

                        }catch(Exception e) {

                            e.printStackTrace();
                        }

                    }

                }
            }
        });

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result) {
                    //permission granted
                    Intent intentToGallery = new Intent(Intent.ACTION_PICK ,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery);

                }else{
                    //permission denied
                    Toast.makeText(MushroomActivity.this, "Permission needed!!", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

}