package com.example.kingpub.hello__world;

import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.util.Calendar;
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
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

public class MainActivity extends AppCompatActivity {

    private TextView console;
    private Button[] consoleButtons;

    PeriodicWorkRequest changeWallpaper_work = null;

    private void Log(String arg){
        String consoleText = console.getText().toString();
        consoleText = consoleText + arg;
        console.setText(consoleText);
        Log.v("ObTask", arg);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BindOnClick_AndChangeNames_OfAllButtons();
    }

    private void BindOnClick_AndChangeNames_OfAllButtons() {
        BindOnClick_AndChangeNames_First_Row_Buttons();
        BindOnClick_AndChangeNames_OfConsoleButtons();
        BindOnClick_CheckService();
        BindOnClick_OfChangeButton();
        BindOnClick_OfStopper();
        BindOnClick_AndChangeNames_OfTestButton();
        BindSoundSwitch();
    }

    private void BindOnClick_AndChangeNames_First_Row_Buttons() {
        Button firstButton = (Button) findViewById(R.id.WPbutton1);
        Button secondButton = (Button) findViewById(R.id.WPbutton2);

        firstButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SharedPreferences sharedPref = getSharedPreferences(getString(R.string.OBbWallpaperShuffler_SharedPrefName), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putStringSet(getString(R.string.images_dirs_key), new HashSet<>());
                editor.apply();
            }
        });
        firstButton.setText("Clear Directories");

        secondButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

            }
        });
        secondButton.setText("Debug");
    }

    private void BindOnClick_CheckService(){
        Button checkServiceBtn = (Button) findViewById(R.id.checkBtn);
        checkServiceBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String dbgMsg = "Worker running? "+isWorkScheduled("WC");
                Log.v("OBTask", dbgMsg);
                Toast startSequenceToast = Toast.makeText(getApplicationContext(), dbgMsg, Toast.LENGTH_LONG);
                startSequenceToast.show();
            }
        });
    }

    private boolean isWorkScheduled(String tag) {
        WorkManager instance = WorkManager.getInstance();
        ListenableFuture<List<WorkInfo>> statuses = instance.getWorkInfosByTag(tag);
        try {
            boolean running = false;
            List<WorkInfo> workInfoList = statuses.get();
            for (WorkInfo workInfo : workInfoList) {
                WorkInfo.State state = workInfo.getState();
                running = running | state == WorkInfo.State.RUNNING | state == WorkInfo.State.ENQUEUED;
            }
            return running;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
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

                displayTotalDirsAndFiles();

            }
        });
        consoleButtons[0].setText("Tot_Dirs/Imgs");

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
                Set<String> dirSet = loadDirSet();

                m_uri_data = new Data.Builder()
                        .putString(ChangeWallpaper_Worker.DIR_URI_STR_KEY, "")
                        .putStringArray(ChangeWallpaper_Worker.DIR_LIST_URIS_STR_KEY, (dirSet.toArray(new String[0])))
                        .build();

                OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(ChangeWallpaper_Worker.class)
                        .setInputData(m_uri_data)
                        .addTag("WC")
                        .build();

                WorkManager.getInstance().enqueue(request);

                Log.v("OBTask", "Started One time Work "+request.getId());
            }
        });
        consoleButtons[2].setText("Next Wallpaper");
    }

    private void displayTotalDirsAndFiles() {
        int total = 0;
        for(String uri_str : loadDirSet()){
            total += getFilesFromDir(Uri.parse(uri_str)).length;
        }

        Toast startSequenceToast = Toast.makeText(getApplicationContext(), "Total Directories: "+loadDirSet().size()+"\nTotal Images: "+total, Toast.LENGTH_LONG);
        startSequenceToast.show();

        Log.v("OBTask", "Total Directories: "+loadDirSet().size()+"\nTotal Images: "+total);
    }

    private Data m_uri_data;

    private void BindOnClick_OfChangeButton(){
        final Button changeTwiceButton = (Button) findViewById(R.id.changeButton);
        changeTwiceButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Set<String> dirSet = loadDirSet();

                displayTotalDirsAndFiles();

                m_uri_data = new Data.Builder()
                        .putStringArray(ChangeWallpaper_Worker.DIR_LIST_URIS_STR_KEY, (dirSet.toArray(new String[0])))
                        .build();

                OneTimeWorkRequest onetime_changeWallpaper_work = new OneTimeWorkRequest.Builder(ChangeWallpaper_Worker.class)
                        .setInputData(m_uri_data)
                        .addTag("WC")
                        .build();

                WorkManager.getInstance().enqueue(onetime_changeWallpaper_work);

//                changeWallpaper_work = new PeriodicWorkRequest.Builder(ChangeWallpaper_Worker.class, 15, TimeUnit.MINUTES, 1, TimeUnit.MINUTES)
//                        .setInputData(m_uri_data)
//                        .addTag("WC")
//                        .build();

//                WorkManager.getInstance().enqueue(changeWallpaper_work);
//                WorkManager.getInstance().enqueueUniquePeriodicWork("ChangeWallpaper_Loop", ExistingPeriodicWorkPolicy.REPLACE, changeWallpaper_work);

//                Log( "Started Work "+changeWallpaper_work.getId()+" at time "+ Calendar.getInstance().getTime());

            }
        });

        changeTwiceButton.setText("> Change Once <");
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
                    Log.v("OBTasks", "attempt to kill service");
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

    private void BindSoundSwitch(){
        Switch soundSwitch = (Switch) findViewById(R.id.switch1);
        soundSwitch.setChecked(ChangeWallpaper_Worker.Sound_On_Change);
        soundSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ChangeWallpaper_Worker.Sound_On_Change = isChecked;
            }
        });

    }

    private Set<String> loadDirSet(){
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.OBbWallpaperShuffler_SharedPrefName), Context.MODE_PRIVATE);

        Set<String> images_set = sharedPref.getStringSet(getString(R.string.images_dirs_key), new HashSet<String>());

        return images_set;
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

    public void AddDirReference(Uri directoryUri) {
        String directoryUri_str = directoryUri.toString();
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.OBbWallpaperShuffler_SharedPrefName), Context.MODE_PRIVATE);

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
