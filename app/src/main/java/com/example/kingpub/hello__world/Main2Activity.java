package com.example.kingpub.hello__world;

import android.os.Bundle;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class Main2Activity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(ChangeWallpaper_Worker.class)
                .addTag("WC")
                .build();

        WorkManager.getInstance().enqueue(request);

        Log.v("OBTask", "Started One time Work "+request.getId());
    }

}
