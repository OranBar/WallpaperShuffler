package com.example.kingpub.hello__world;

import android.app.IntentService;
import android.app.Service;
import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;
import android.os.*;
import android.util.Log;

public class WallpaperService extends IntentService {

    public static final String WALLPAPER_URIS = "WALLPAPER_URIS";

    private Handler handler = new Handler();
    public static volatile boolean shouldStop = false;

    public WallpaperService() {
        super("OB_Wallpaper_Service");
    }

    private class ProgressTimerTask extends TimerTask {
        @Override
        public void run() {
            int currentMinutes = 0; // set your time here
//            changeWallpapers(currentMinutes);
        }
    }

    private ProgressTimerTask timeTask;

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String[] wallpperUris_str = intent.getExtras().getStringArray(WALLPAPER_URIS);

        foo(wallpperUris_str, 0);
    }


    private void foo (final String[] wallpaperUris_str, final int index){

        Log.v("OB","foo "+index);
        final Uri wallpaperUri = Uri.parse(wallpaperUris_str[index]);

        final boolean keepGoing = index < wallpaperUris_str.length && shouldStop == false; //Add more stoppers?

        handler.postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        changeWallpaper(getBitmapFromImageUri(wallpaperUri));

                        if(keepGoing){
                            foo(wallpaperUris_str, index+1);
                        }
                    }
                }
                , 1000 * 10);



    }

//    @Override
//    protected void onHandleIntent(@Nullable Intent intent) {
//        Timer progressTimer = new Timer();
//        timeTask = new ProgressTimerTask();
//        progressTimer.scheduleAtFixedRate(timeTask, 0, 1000);
//    }

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

    public void changeWallpaper(Bitmap bm){

        Log.v("OBTask","ChangeWallpaper!");

        WallpaperManager wallpaperManager =  WallpaperManager.getInstance(getApplicationContext());

        try {
            wallpaperManager.setBitmap(bm);
            wallpaperManager.setBitmap(bm, null ,true, WallpaperManager.FLAG_LOCK);
//            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
//            r.play();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
