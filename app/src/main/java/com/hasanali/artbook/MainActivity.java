package com.hasanali.artbook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.hasanali.artbook.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    ArrayList<Art> artArrayList;
    ArtBookAdapter artBookAdapter;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        artArrayList = new ArrayList<>();
        getData();
        addRecyclerView();
    }

    private void getData() {
        try {
            database = this.openOrCreateDatabase("Arts", MODE_PRIVATE,null);
            Cursor cursor = database.rawQuery("SELECT * FROM arts", null);

            int nameIx = cursor.getColumnIndex("artname");
            int idIx = cursor.getColumnIndex("id");
            while (cursor.moveToNext()) {
                String name = cursor.getString(nameIx);
                int id = cursor.getInt(idIx);

                Art art = new Art(name,id);
                artArrayList.add(art);
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            e.getMessage();
        }
    }

    private void addRecyclerView() {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        artBookAdapter = new ArtBookAdapter(artArrayList);
        binding.recyclerView.setAdapter(artBookAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.add_art) {
            Intent intent = new Intent(this,ArtActivity.class);
            intent.putExtra("info","new"); // yeni art ekleneceğini bildiriyoruz.
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}