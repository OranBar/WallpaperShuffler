package com.example.kingpub.hello__world;

import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.provider.DocumentFile;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ChangeWallpaper_Worker extends Worker {

    public static final String IMG_URI_STR_KEY = "IMG_URI";

    private int index;

    public ChangeWallpaper_Worker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        index=0;
    }

    @NonNull
    @Override
    public Result doWork() {

        String wallpaperUriToSet_str = getInputData().getString(IMG_URI_STR_KEY);

        Uri wallpaperUriToSet = Uri.parse(wallpaperUriToSet_str);
        changeWallpaper(getBitmapFromImageUri(wallpaperUriToSet));

        return Result.success();
    }

    public void changeWallpaper(Bitmap bm){

        Log.v("OBTask","ChangeWallpaper!");

        WallpaperManager wallpaperManager =  WallpaperManager.getInstance(getApplicationContext());

        try {
            wallpaperManager.setBitmap(bm);
            wallpaperManager.setBitmap(bm, null ,true, WallpaperManager.FLAG_LOCK);
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Bitmap getBitmapFromImageUri(Uri imageUri){
        Bitmap newWallpaper = null;

        try {
            ContentResolver contentResolver = getApplicationContext().getContentResolver();
            InputStream is = null;
            is = contentResolver.openInputStream(imageUri);
            newWallpaper = BitmapFactory.decodeStream(is);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return newWallpaper;
    }

    public void changeWallpaperAfterSeconds_WorkManager(int seconds, final Uri imageUri){

        Data uri_data = new Data.Builder()
                .putString(ChangeWallpaper_Worker.IMG_URI_STR_KEY, imageUri.toString())
                .build();

        OneTimeWorkRequest myWork =
                new OneTimeWorkRequest.Builder(ChangeWallpaper_Worker.class)
                        .setInputData(uri_data)
                        .setInitialDelay(seconds, TimeUnit.SECONDS)// Use this when you want to add initial delay or schedule initial work to `OneTimeWorkRequest` e.g. setInitialDelay(2, TimeUnit.HOURS)
                        .build();

        WorkManager.getInstance().enqueue(myWork);
    }

    public void scheduleNextWallpaper() {
        Set<String> dirSet = loadDirSet();

        //For now we take the first directory
        DocumentFile[] dirFiles = getFilesFromDir( Uri.parse(dirSet.iterator().next() ));
        int dirSetLength = dirFiles.length;

        Toast statrtSequenceToast = Toast.makeText(getApplicationContext(), "Starting Sequence : "+dirSetLength+" elements", Toast.LENGTH_LONG);
        statrtSequenceToast.show();

        index = index+1;

    }


    private Set<String> loadDirSet(){
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(getApplicationContext().getString(R.string.Images_Lists_SharedPrefName), Context.MODE_PRIVATE);

        Set<String> images_set = sharedPref.getStringSet(getApplicationContext().getString(R.string.images_dirs_key), new HashSet<String>());

        return images_set;
    }



    private DocumentFile[] getFilesFromDir(Uri folderUri){
        DocumentFile documentFile = DocumentFile.fromTreeUri(getApplicationContext(), folderUri);

        ContentResolver contentResolver = getApplicationContext().getContentResolver();

        DocumentFile[] files = documentFile.listFiles();
        return files;
    }
}
