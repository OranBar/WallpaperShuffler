package com.example.kingpub.hello__world;

import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Looper;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.*;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "com.kingpub.wallpaperShuffler.MSG" ;

    //--- {vars for shuffle
    public int currWallpaperIndex = 0;
    //----- vars for shuffle}

    private TextView console;
    private Button[] consoleButtons;

    PeriodicWorkRequest changeWallpaper_work = null;

    private void Log(String arg){
        String consoleText = console.getText().toString();
        consoleText = consoleText + arg;
        console.setText(consoleText);
        Log.v("ObLog", arg);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        currWallpaperIndex = 0;
        //----- Setup for shuffle}
//        displayTotalImagesToast();

        BindOnClick_AndChangeNames_OfAllButtons();
    }

    private void BindOnClick_AndChangeNames_OfAllButtons() {
        BindOnClick_AndChangeNames_OfConsoleButtons();
        BindOnClick_OfChangeButton();
        BindOnClick_OfStopper();
        BindOnClick_AndChangeNames_OfTestButton();
    }

    private void BindOnClick_AndChangeNames_OfConsoleButtons() {
        console = (TextView) findViewById(R.id.console_textview);

        consoleButtons = new Button[3];
        consoleButtons[0] = (Button) findViewById(R.id.ConsoleBtn1);
        consoleButtons[1] = (Button) findViewById(R.id.ConsoleBtn2);
        consoleButtons[2] = (Button) findViewById(R.id.ConsoleBtn3);

        //------- {Buttons setup

        //First
        consoleButtons[0].setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Do Nothing
            }
        });
        consoleButtons[0].setText("Tot_Imgs");

        //Second
        consoleButtons[1].setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pickFolder();
            }
        });
        consoleButtons[1].setText("Pick Folder");

        //Third
        consoleButtons[2].setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Do nothing
            }
        });
//        consoleButtons[2].setText("Pick Folder");
    }

    private Data m_uri_data;

    private void BindOnClick_OfChangeButton(){
        final Button changeTwiceButton = (Button) findViewById(R.id.changeButton);
        changeTwiceButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Set<String> dirSet = loadDirSet();

                //For now we take the first directory
                Uri firstDirectoryUri = Uri.parse(dirSet.iterator().next());
                DocumentFile[] dirFiles = getFilesFromDir( firstDirectoryUri );
                int dirSetLength = dirFiles.length;

                Log("Starting Sequence : "+dirSetLength+" " +
                        "elements");
                Toast startSequenceToast = Toast.makeText(getApplicationContext(), "Starting Sequence : "+dirSetLength+" elements", Toast.LENGTH_LONG);
                startSequenceToast.show();

                m_uri_data = new Data.Builder()
                        .putString(ChangeWallpaper_Worker.DIR_URI_STR_KEY, firstDirectoryUri.toString())
                        .build();

                changeWallpaper_work = new PeriodicWorkRequest.Builder(ChangeWallpaper_Worker.class, 15, TimeUnit.MINUTES, 1, TimeUnit.MINUTES)
                        .setInputData(m_uri_data)
                        .addTag("WC")
                        .build();

//                WorkManager.getInstance().enqueue(changeWallpaper_work);
                WorkManager.getInstance().enqueueUniquePeriodicWork("ChangeWallpaper_Loop", ExistingPeriodicWorkPolicy.REPLACE, changeWallpaper_work);

                Log.v("OBTask", "Started Work "+changeWallpaper_work.getId());

//                Intent changeWallpaper_intent = new Intent(getApplicationContext(), WallpaperService.class);
//
//                String[] wallpaperUris_str = new String[5];
//                for(int i=0; i<5; i++){
//                    wallpaperUris_str[i] = dirFiles[i].getUri().toString();
//                }
//
//                changeWallpaper_intent.putExtra(WallpaperService.WALLPAPER_URIS, wallpaperUris_str);
//                startService(changeWallpaper_intent);

//                OneTimeWorkRequest firstRequest = changeWallpaperAfterSeconds_WorkManager(11, dirFiles[0].getUri());
//                WorkContinuation continuation = WorkManager.getInstance().beginWith(firstRequest);
//
//                continuation.then(changeWallpaperAfterSeconds_WorkManager(11, dirFiles[1].getUri()));
//                continuation.then(changeWallpaperAfterSeconds_WorkManager(11, dirFiles[2].getUri()));
//
//                continuation.enqueue();


//                for (int i=0; i < dirSetLength; i++){
//                    changeWallpaperAfterSeconds_WorkManager((10 * i) +1, dirFiles[i].getUri());
//                }

            }
        });
    }

    private void BindOnClick_OfStopper() {
        Button addImageButton = (Button) findViewById(R.id.addImageButton);
        addImageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Tag
                Log.v("OBTask", "My Work is "+WorkManager.getInstance().getWorkInfosByTag("WC").toString());
                Log.v("OBTask", "Is Canceled = "+WorkManager.getInstance().getWorkInfosByTag("WC").isCancelled());
                Log.v("OBTask", "Is Done = "+WorkManager.getInstance().getWorkInfosByTag("WC").isDone());

                if(WorkManager.getInstance().getWorkInfosByTag("WC").cancel(true)){
                    Log.v("OBTasks", "Second attempt to kill service");
                    WorkManager.getInstance().pruneWork();
                }

                Log.v("OBTask", "--My Work is "+WorkManager.getInstance().getWorkInfosByTag("WC").toString());
                Log.v("OBTask", "--Is Canceled = "+WorkManager.getInstance().getWorkInfosByTag("WC").isCancelled());
                Log.v("OBTask", "--Is Done = "+WorkManager.getInstance().getWorkInfosByTag("WC").isDone());
                //------------------------

                ListenableFuture<List<WorkInfo>> info = WorkManager.getInstance().getWorkInfosForUniqueWork("ChangeWallpaper_Loop");
                Log.v("OBTask", "(Unique) My Work is "+info.toString());
                Log.v("OBTask", "(Unique) Is Canceled = "+info.isCancelled());
                Log.v("OBTask", "(Unique) Is Done = "+info.isDone());

                WorkManager.getInstance().cancelUniqueWork("ChangeWallpaper_Loop");
                WorkManager.getInstance().pruneWork();

                Log.v("OBTask", "--(Unique) My Work is "+info.toString());
                Log.v("OBTask", "--(Unique) Is Canceled = "+info.isCancelled());
                Log.v("OBTask", "--(Unique) Is Done = "+info.isDone());

                Log.v("OBTask","Stopped Wallpaper change task??");
            }
        });
        addImageButton.setText("Stopper");
    }

    private void BindOnClick_AndChangeNames_OfTestButton() {
        //Test button
        final Button test = (Button) findViewById(R.id.test);
        test.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.v("OBTask", "My Work is "+WorkManager.getInstance().getWorkInfosByTag("WC").toString());
                Log.v("OBTask", "Is Canceled = "+WorkManager.getInstance().getWorkInfosByTag("WC").isCancelled());
                Log.v("OBTask", "Is Done = "+WorkManager.getInstance().getWorkInfosByTag("WC").isDone());

                ListenableFuture<List<WorkInfo>> info = WorkManager.getInstance().getWorkInfosForUniqueWork("ChangeWallpaper_Loop");

                Log.v("OBTask", "(Unique) My Work is "+info.toString());
                Log.v("OBTask", "(Unique) Is Canceled = "+info.isCancelled());
                Log.v("OBTask", "(Unique) Is Done = "+info.isDone());

                WorkManager.getInstance().cancelAllWork();

                Log.v("OBTask", "--My Work is "+WorkManager.getInstance().getWorkInfosByTag("WC").toString());
                Log.v("OBTask", "--Is Canceled = "+WorkManager.getInstance().getWorkInfosByTag("WC").isCancelled());
                Log.v("OBTask", "--Is Done = "+WorkManager.getInstance().getWorkInfosByTag("WC").isDone());

                Log.v("OBTask", "--(Unique) My Work is "+info.toString());
                Log.v("OBTask", "--(Unique) Is Canceled = "+info.isCancelled());
                Log.v("OBTask", "--(Unique) Is Done = "+info.isDone());
            }
        });
        test.setText("SuperStopper");


    }

    //TODO: fix this method.
//    private void displayTotalImagesToast() {
//        Set<String> images_set = loadMainImagesSet();
//        String first = "None";
//        int totalImages = 0;
//        if(images_set != null && images_set.size() != 0){
//            first = images_set.iterator().next();
//            totalImages = images_set.size();
//        }
//        Toast toast = Toast.makeText(getApplicationContext(), totalImages+" Images total. First is "+first, Toast.LENGTH_LONG);
//        toast.show();
//    }

    private Set<String> loadDirSet(){
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.Images_Lists_SharedPrefName), Context.MODE_PRIVATE);

        Set<String> images_set = sharedPref.getStringSet(getString(R.string.images_dirs_key), new HashSet<String>());

        return images_set;
    }

    public OneTimeWorkRequest changeWallpaperAfterSeconds_WorkManager(int seconds, final Uri imageUri){

        Data uri_data = new Data.Builder()
                .putString(ChangeWallpaper_Worker.IMG_URI_STR_KEY, imageUri.toString())
                .build();

        OneTimeWorkRequest myWork =
                new OneTimeWorkRequest.Builder(ChangeWallpaper_Worker.class)
                        .setInputData(uri_data)
                        .setInitialDelay(seconds, TimeUnit.SECONDS)// Use this when you want to add initial delay or schedule initial work to `OneTimeWorkRequest` e.g. setInitialDelay(2, TimeUnit.HOURS)
        .build();

        return myWork;
    }

    public void changeWallpaperAfterSeconds(int seconds, final Uri imageUri){
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Bitmap newWallpaper = null;

                try {
                    ContentResolver contentResolver = getContentResolver();
                    InputStream is = null;
                    is = contentResolver.openInputStream(imageUri);
                    newWallpaper = BitmapFactory.decodeStream(is);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return;
                }

                changeWallpaper(newWallpaper);

                CharSequence text = "Changing wallpaper to "+imageUri.toString();
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(getApplicationContext(), text, duration);
                toast.show();
            }
        }, 1000 * seconds);
    }


//    @Override
//    protected void onStop()
//    {
//        for(BroadcastReceiver curr : CURRENT_ACTIVE_RECIEVERS ){
//            try{
//                unregisterReceiver(curr);
//            }catch(IllegalArgumentException e){ ; }
//        }
//        super.onStop();
//    }

    public void changeWallpaper(View view, Bitmap bm){
        changeWallpaper(bm);
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

    private static final int RESULT_LOAD_FOLDER = 3;

    public void pickFolder(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, RESULT_LOAD_FOLDER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_FOLDER && resultCode == RESULT_OK && null != data) {
            Uri folderUri = data.getData();
            AddDirReference(folderUri);
        }
    }

    private DocumentFile[] getFilesFromDir(Uri folderUri){
        DocumentFile documentFile = DocumentFile.fromTreeUri(this, folderUri);

        ContentResolver contentResolver = getContentResolver();

        DocumentFile[] files = documentFile.listFiles();
        return files;
    }

    private void selectWallpaperFromFolder(Uri folderUri){
        DocumentFile documentFile = DocumentFile.fromTreeUri(this, folderUri);

        ContentResolver contentResolver = getContentResolver();

        DocumentFile[] files = documentFile.listFiles();

        ;  //For now, we pick first. Then, we do random

        currWallpaperIndex = currWallpaperIndex+1;
        DocumentFile file = files[ currWallpaperIndex%files.length ];

        if (file.isDirectory()) { // if it is sub directory
            // Do stuff with sub directory
            Log("ERROR: Found dir instead of file");
        } else {
            // Do stuff with normal file
            try {
                InputStream is = contentResolver.openInputStream(file.getUri());
                Bitmap myImage = BitmapFactory.decodeStream(is);
                changeWallpaper(myImage);
//                        changeWallpaper(myImage);
            } catch (Exception e) {

            }
        }
    }

    public void AddDirReference(Uri directoryUri) {
        String directoryUri_str = directoryUri.toString();
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.Images_Lists_SharedPrefName), Context.MODE_PRIVATE);

        //Treat loaded set as immutable please.
        Set<String> loaded_set_immutable = sharedPref.getStringSet(getString(R.string.images_dirs_key), new HashSet<String>());
        //----
        Set<String> images_set = new HashSet<>(loaded_set_immutable);

        images_set.add(directoryUri_str);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putStringSet(getString(R.string.images_dirs_key), images_set);
        editor.apply();
    }

}
