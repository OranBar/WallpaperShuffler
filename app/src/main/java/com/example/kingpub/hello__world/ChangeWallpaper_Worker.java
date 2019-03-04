package com.example.kingpub.hello__world;

import android.app.NotificationManager;
import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
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
    public static final String DIR_URI_STR_KEY = "DIR_URI";

    private String workrequestId = "";


    public ChangeWallpaper_Worker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        Log.v("OBChangeWallpaper", "Create");
        workrequestId = " (WorkRequestId= "+this.getId()+")";
    }
//    @NonNull
//    @Override
//    public Result doWork() {
//
//        String wallpaperUriToSet_str = getInputData().getString(IMG_URI_STR_KEY);
//
//        Uri wallpaperUriToSet = Uri.parse(wallpaperUriToSet_str);
//        changeWallpaper(getBitmapFromImageUri(wallpaperUriToSet));
//
//        return Result.success();
//    }

    private static int Current_Index = -1;



    @NonNull
    @Override
    public Result doWork() {
        Log.v("OBChangeWallpaper","Starting change sequence"+workrequestId);
        String dirUri_str = getInputData().getString(DIR_URI_STR_KEY);

        if(Uri.parse(dirUri_str) == null){
            Log.e("OBError", "String param (="+dirUri_str+")is an invalid Uri.");
            return Result.failure();
        }

        Log.v("OBChangeWallpaper","Dir uri is "+dirUri_str+workrequestId);

        DocumentFile[] files = getFilesFromDir(Uri.parse(dirUri_str));

        Current_Index++;
        Uri wallpaperUriToSet = files[Current_Index %files.length].getUri();

        Log.v("OBChangeWallpaper","Chosen wallpaper uri is "+wallpaperUriToSet+workrequestId);
        changeWallpaper(getBitmapFromImageUri(wallpaperUriToSet));

        return Result.success();
    }

    public void changeWallpaper(Bitmap bm){

        Log.v("OBTask","Changing Wallpaper!"+workrequestId);

        WallpaperManager wallpaperManager =  WallpaperManager.getInstance(getApplicationContext());

        try {
            wallpaperManager.setBitmap(bm, null ,true, WallpaperManager.FLAG_LOCK);
            wallpaperManager.setBitmap(bm);
            Log.v("OBTask", "Change Wallpaper Successful"+workrequestId);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("OBTask", "Change Wallpaper FAILED"+workrequestId);
        }

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        r.play();
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

        Current_Index = Current_Index +1;

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
