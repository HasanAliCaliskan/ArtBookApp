package com.hasanali.artbook;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.hasanali.artbook.databinding.ActivityArtBinding;

import java.io.ByteArrayOutputStream;

public class ArtActivity extends AppCompatActivity {

    private ActivityArtBinding binding;
    private int artId;
    private String info;
    ActivityResultLauncher<String> permissionLauncher;
    ActivityResultLauncher<Intent> activityResultLauncher;
    Bitmap selectedImage;
    SQLiteDatabase database;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityArtBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        getIntentExtras();
        setIntentResults();
        registerLauncher();
    }

    public void getIntentExtras() {
        intent = getIntent();
        info = intent.getStringExtra("info");
        artId = intent.getIntExtra("id",0);
    }

    public void setIntentResults() {
        if (info.equals("new")) {
            binding.buttonUpdate.setVisibility(View.INVISIBLE);
        } else {
            binding.buttonUpdate.setVisibility(View.INVISIBLE);
            binding.buttonSave.setVisibility(View.INVISIBLE);
            binding.nameText.setEnabled(false);
            binding.artistText.setEnabled(false);
            binding.yearText.setEnabled(false);
            binding.imageView.setEnabled(false);
            setDbInfos();
        }
    }

    public void intentToGoMain() {
        Intent intent = new Intent(ArtActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void setDbInfos() {
        try {
            database = this.openOrCreateDatabase("Arts", MODE_PRIVATE,null);
            Cursor cursor = database.rawQuery("SELECT * FROM arts WHERE id = ?",new String[] {String.valueOf(artId)});
            int artNameIx = cursor.getColumnIndex("artname");
            int painterNameIx = cursor.getColumnIndex("paintername");
            int yearIx = cursor.getColumnIndex("year");
            int imageIx = cursor.getColumnIndex("image");
            while (cursor.moveToNext()) {
                binding.nameText.setText(cursor.getString(artNameIx));
                binding.artistText.setText(cursor.getString(painterNameIx));
                binding.yearText.setText(cursor.getString(yearIx));
                byte[] bytes = cursor.getBlob(imageIx);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                binding.imageView.setImageBitmap(bitmap);
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            e.getMessage();
        }
    }

    public void saveDbInfos(String name, String artist, String year, byte[] byteArray) {
        try {
            database = this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);
            database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY, artname VARCHAR, paintername VARCHAR, year VARCHAR, image BLOB)");
            String sqlString = "INSERT INTO arts (artname, paintername, year, image) VALUES (?, ?, ?, ?)";
            SQLiteStatement sqLiteStatement = database.compileStatement(sqlString);

            sqLiteStatement.bindString(1, name);
            sqLiteStatement.bindString(2, artist);
            sqLiteStatement.bindString(3, year);
            sqLiteStatement.bindBlob(4,byteArray);
            sqLiteStatement.execute();
        } catch (Exception e) {
            e.printStackTrace();
            e.getMessage();
        }
    }

    public void updateDbInfos(String name, String artist, String year, byte[] byteArray) {
        try {
            database = this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);
            ContentValues cv = new ContentValues();
            cv.put("artname",name);
            cv.put("paintername",artist);
            cv.put("year",year);
            cv.put("image",byteArray);
            database.update("arts",cv,"id = ?", new String[]{String.valueOf(artId)});
        } catch (Exception e) {
            e.printStackTrace();
            e.getMessage();
        }
    }

    public void deleteDbInfos() {
        try {
            database = this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);
            database.execSQL("DELETE FROM arts WHERE id = ?", new String[] {String.valueOf(artId)});
        } catch (Exception e) {
            e.printStackTrace();
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
                                ImageDecoder.Source source = ImageDecoder.createSource(ArtActivity.this.getContentResolver(),imageData);
                                selectedImage = ImageDecoder.decodeBitmap(source);
                            } else {
                                selectedImage = MediaStore.Images.Media.getBitmap(ArtActivity.this.getContentResolver(),imageData);
                            }
                            binding.imageView.setImageBitmap(selectedImage);
                        } catch (Exception e) {
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
                    Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery);
                } else {
                    Toast.makeText(ArtActivity.this,"Permission needed!",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void selectImage(View view) {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Snackbar.make(view,"Permission needed for gallary",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                }).show();
            } else {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        } else {
            Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(intentToGallery);
        }
    }

    public void save(View view) {
        if (binding.nameText.getText().toString().matches("") || binding.artistText.getText().toString().matches("") ||
                binding.yearText.getText().toString().matches("") || selectedImage == null) {
            Toast.makeText(ArtActivity.this,"Fields cannot be left blank",Toast.LENGTH_LONG).show();
        } else {
            String name = binding.nameText.getText().toString();
            String artist = binding.artistText.getText().toString();
            String year = binding.yearText.getText().toString();
            Bitmap smallImage = makeSmallerImage(selectedImage,300);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            smallImage.compress(Bitmap.CompressFormat.PNG,50,byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            saveDbInfos(name, artist, year, byteArray);
            intentToGoMain();
        }
    }

    public void update(View view) {
        if (binding.nameText.getText().toString().matches("") || binding.artistText.getText().toString().matches("") ||
                binding.yearText.getText().toString().matches("")) {
            Toast.makeText(ArtActivity.this,"Fields cannot be left blank",Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(ArtActivity.this,"Updated",Toast.LENGTH_LONG).show();
            String name = binding.nameText.getText().toString();
            String artist = binding.artistText.getText().toString();
            String year = binding.yearText.getText().toString();

            byte[] byteArray;
            Bitmap bitmap;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            if (selectedImage != null) {
                bitmap = makeSmallerImage(selectedImage, 300);
            } else { // görsel seçilmezse
                bitmap = ((BitmapDrawable) binding.imageView.getDrawable()).getBitmap();
            }
            bitmap.compress(Bitmap.CompressFormat.PNG,50,byteArrayOutputStream);
            byteArray = byteArrayOutputStream.toByteArray();

            updateDbInfos(name, artist, year, byteArray);
            intentToGoMain();
        }
    }

    public Bitmap makeSmallerImage(Bitmap image, int maximumSize) {
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
        return image.createScaledBitmap(image,width,height,true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.art_menu,menu);
        if (info.equals("new")) {
            for (int i = 0; i < menu.size(); i++) {
                menu.getItem(i).setVisible(false);
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.update_art) {
            binding.buttonSave.setVisibility(View.INVISIBLE);
            binding.buttonUpdate.setVisibility(View.VISIBLE);
            binding.nameText.setEnabled(true);
            binding.artistText.setEnabled(true);
            binding.yearText.setEnabled(true);
            binding.imageView.setEnabled(true);
        }
        if (item.getItemId() == R.id.delete_art) {
            AlertDialog.Builder alert = new AlertDialog.Builder(ArtActivity.this);
            alert.setTitle("Delete").setMessage("Are you sure? All data will be deleted.");
            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    deleteDbInfos();
                    intentToGoMain();
                    Toast.makeText(ArtActivity.this,"Deleted",Toast.LENGTH_LONG).show();
                }
            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            alert.show();
        }
        return super.onOptionsItemSelected(item);
    }
}