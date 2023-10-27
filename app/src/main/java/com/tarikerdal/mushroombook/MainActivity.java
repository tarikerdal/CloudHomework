package com.tarikerdal.mushroombook;

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

import com.tarikerdal.mushroombook.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    ArrayList<Mushroom> mushroomArrayList;
    MushroomAdapter mushroomAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        mushroomArrayList = new ArrayList<>();


        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mushroomAdapter = new MushroomAdapter(mushroomArrayList);
        binding.recyclerView.setAdapter(mushroomAdapter);

        getData();

    }

    private void getData() {

        try {

            SQLiteDatabase sqLiteDatabase = this.openOrCreateDatabase("Mushroom" ,MODE_PRIVATE ,null);

            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM mushroom" , null);
            int originalIx = cursor.getColumnIndex("originalname");
            int idIx = cursor.getColumnIndex("id");

            while (cursor.moveToNext()) {

                String originalname = cursor.getString(originalIx);
                int id = cursor.getInt(idIx);
                Mushroom mushroom = new Mushroom(originalname ,id);
                mushroomArrayList.add(mushroom);
            }

            mushroomAdapter.notifyDataSetChanged();
            cursor.close();

        }catch (Exception e){
            e.printStackTrace();
        }

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.mushroom_menu ,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.add_mushroom) {

            Intent intent = new Intent(MainActivity.this , MushroomActivity.class);
            intent.putExtra("info" ,"new");
            startActivity(intent);
        }


        return super.onOptionsItemSelected(item);
    }
}