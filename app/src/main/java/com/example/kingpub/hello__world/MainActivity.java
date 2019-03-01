package com.example.kingpub.hello__world;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.*;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "com.kingpub.wallpaperShuffler.MSG" ;


    //--- {vars for shuffle
    public Bitmap[] wallpapers;
    public int currWallpaperIndex = 0;
    //----- vars for shuffle}

    private static ArrayList<BroadcastReceiver> CURRENT_ACTIVE_RECIEVERS = new ArrayList<BroadcastReceiver>();
    private TextView console;
    private Button[] consoleButtons;

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

        //--- {Setup for shuffle
        Bitmap tnmn = BitmapFactory.decodeResource(getResources(), R.drawable.tmnt);
        Bitmap goku = BitmapFactory.decodeResource(getResources(), R.drawable.goku_ultra_instinct);
        Bitmap cool = BitmapFactory.decodeResource(getResources(), R.drawable.cool_android_wallpaper_08);
        Bitmap pexels = BitmapFactory.decodeResource(getResources(), R.drawable.pexels_photo_799443);

        wallpapers = new Bitmap[]{tnmn, goku, cool, pexels};
        currWallpaperIndex = 0;
        //----- Setup for shuffle}


        displayTotalImagesToast();

        SetupConsoleButtons();

        final Button test = (Button) findViewById(R.id.test);
        test.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pickMultiplePictures();
            }
        });


        final Button changeWallpaper_btn1 = (Button) findViewById(R.id.WPbutton1);
        changeWallpaper_btn1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                changeWallpaperToTnmn(v);
            }
        });

        Button changeWallpaper_btn2 = (Button) findViewById(R.id.WPbutton2);
        changeWallpaper_btn2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                changeWallpaperToGoku(v);
            }
        });

        Button changeTwiceButton = (Button) findViewById(R.id.changeButton);
        changeTwiceButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int imageSetLength =loadMainImagesSet().size();

                Log("Starting Sequence : "+imageSetLength +" " +
                        "elements");
                Toast statrtSequenceToast = Toast.makeText(getApplicationContext(), "Starting Sequence : "+imageSetLength+" elements", Toast.LENGTH_LONG);
                statrtSequenceToast.show();


                for (int i=0; i<imageSetLength;i++){
                    changeWallpaperAfterSeconds_2((10 * i) +1);
                }

            }
        });

        Button addImageButton = (Button) findViewById(R.id.addImageButton);
        addImageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pickPhotoFromPhone();
            }
        });
        //------- Buttons setup}
    }

    private void SetupConsoleButtons() {
        console = (TextView) findViewById(R.id.console_textview);

        consoleButtons = new Button[3];
        consoleButtons[0] = (Button) findViewById(R.id.ConsoleBtn1);
        consoleButtons[1] = (Button) findViewById(R.id.ConsoleBtn2);
        consoleButtons[2] = (Button) findViewById(R.id.ConsoleBtn3);

        //------- {Buttons setup

        consoleButtons[0].setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                displayTotalImagesToast();
            }
        });
        consoleButtons[0].setText("Tot_Imgs");

        consoleButtons[1].setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pickFolder();
            }
        });
        consoleButtons[1].setText("Pick Folder");
    }

    private void displayTotalImagesToast() {
        Set<String> images_set = loadMainImagesSet();
        String first = "None";
        int totalImages = 0;
        if(images_set != null && images_set.size() != 0){
            first = images_set.iterator().next();
            totalImages = images_set.size();
        }
        Toast toast = Toast.makeText(getApplicationContext(), totalImages+" Images total. First is "+first, Toast.LENGTH_LONG);
        toast.show();
    }

    private Set<String> loadMainImagesSet(){
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.Images_Lists_SharedPrefName), Context.MODE_PRIVATE);

        Set<String> images_set = sharedPref.getStringSet(getString(R.string.main_images_paths_key), new HashSet<String>());

        return images_set;
    }

    public void changeWallpaperAfterSeconds_2(int seconds) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Bitmap newWallpaper = null;

                Set<String> images_set = loadMainImagesSet();
                ArrayList<String> imagesUris_str = new ArrayList<>(images_set);

                String imageUri_str = imagesUris_str.get(currWallpaperIndex);
                Uri imageUri = Uri.parse(imageUri_str);

                ContentResolver contentResolver = getContentResolver();
                InputStream is = null;
                try {
                    is = contentResolver.openInputStream(imageUri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                newWallpaper = BitmapFactory.decodeStream(is);

//                newWallpaper = BitmapFactory.decodeFile(imagePath);

                currWallpaperIndex = (currWallpaperIndex +1) %imagesUris_str.size();


                changeWallpaper(newWallpaper);


                CharSequence text = "Changing wallpaper to "+currWallpaperIndex;
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(getApplicationContext(), text, duration);
                toast.show();
            }
        }, 1000 * seconds);
    }

    public void changeWallpaperAfterSeconds(int seconds) {
        changeWallpaperAfterSeconds(seconds, false);
    }

    public void changeWallpaperAfterSeconds(int seconds, final boolean pickFromMainImgList){

        Log.v("OBTask","ChangeWallpaperAfterSeconds");

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent _ )
            {
                Bitmap newWallpaper = null;

                if(pickFromMainImgList){
                    Set<String> images_set = loadMainImagesSet();
                    ArrayList<String> images = new ArrayList<String>(images_set);

                    String imagePath = images.get(currWallpaperIndex);

                    newWallpaper = BitmapFactory.decodeFile(imagePath);
                    currWallpaperIndex = (currWallpaperIndex +1) %images.size();
                }else{
                    newWallpaper = wallpapers[currWallpaperIndex];
                    currWallpaperIndex = (currWallpaperIndex +1) %wallpapers.length;
                }

                changeWallpaper(newWallpaper);


                CharSequence text = "Changing wallpaper to "+currWallpaperIndex;
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

                context.unregisterReceiver( this ); // this == BroadcastReceiver, not Activity
                CURRENT_ACTIVE_RECIEVERS.remove(this);
            }
        };

        this.registerReceiver( receiver, new IntentFilter("com.example.kingpub.hello__world.Scheduled_WallpaperChange") );

        CURRENT_ACTIVE_RECIEVERS.add(receiver);

        PendingIntent pintent = PendingIntent.getBroadcast( this, 0, new Intent("com.example.kingpub.hello__world.Scheduled_WallpaperChange"), 0 );
        AlarmManager manager = (AlarmManager)(this.getSystemService( Context.ALARM_SERVICE ));

        // set alarm to fire 5 sec (1000*5) from now (SystemClock.elapsedRealtime())
        manager.set( AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 1000*seconds, pintent );
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


    public void changeWallpaperToTnmn(View view){
        Bitmap tnmn = BitmapFactory.decodeResource(getResources(), R.drawable.tmnt);
        changeWallpaper(view, tnmn);

        currWallpaperIndex = 0;
    }

    public void changeWallpaperToGoku(View view){
        Bitmap goku = BitmapFactory.decodeResource(getResources(), R.drawable.goku_ultra_instinct);
        changeWallpaper(view, goku);

        currWallpaperIndex = 1;
    }

    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int RESULT_LOAD_MULTIPLE_IMAGES = 2;
    private static final int RESULT_LOAD_FOLDER = 3;

    public void pickFolder(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, RESULT_LOAD_FOLDER);
    }

    public void pickMultiplePictures() {

        Intent i = new Intent(
                Intent.ACTION_GET_CONTENT
//                ,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        );
        i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        i.setType("*/*");

        startActivityForResult(i, RESULT_LOAD_MULTIPLE_IMAGES);


//        Button buttonLoadImage = (Button) findViewById(R.id.buttonLoadPicture);
//        buttonLoadImage.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View arg0) {
//
//                Intent i = new Intent(
//                        Intent.ACTION_PICK,
//                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//
//                startActivityForResult(i, RESULT_LOAD_IMAGE);
//            }
//        });
    }


    public void pickPhotoFromPhone() {

        Intent i = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(i, RESULT_LOAD_IMAGE);


//        Button buttonLoadImage = (Button) findViewById(R.id.buttonLoadPicture);
//        buttonLoadImage.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View arg0) {
//
//                Intent i = new Intent(
//                        Intent.ACTION_PICK,
//                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//
//                startActivityForResult(i, RESULT_LOAD_IMAGE);
//            }
//        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_FOLDER && resultCode == RESULT_OK && null != data) {
            Uri folderUri = data.getData();

            DocumentFile documentFile = DocumentFile.fromTreeUri(this, folderUri);

            ContentResolver contentResolver = getContentResolver();

            for (DocumentFile file : documentFile.listFiles()) {

                if (file.isDirectory()) { // if it is sub directory
                    // Do stuff with sub directory
                } else {
                    // Do stuff with normal file
                    try {
                        InputStream is = contentResolver.openInputStream(file.getUri());
                        Bitmap myImage = BitmapFactory.decodeStream(is);
                        String fileUri_str = file.getUri().toString();
                        AddImageReferences_Path(fileUri_str);

//                        changeWallpaper(myImage);
                    } catch (Exception e) {

                    }
                }
                Log(file.getUri() + "\n");
            }
        }
    }

//    public void AddAllImagesFromFolder(Uri folderUri){
//
////        TraverseFolder(folderUri, opOnFile);
//
//    }

//    private void TraverseFolder(Uri folderUri, Consumer<DocumentFile> operationOnFile){
//        DocumentFile documentFile = DocumentFile.fromTreeUri(this, folderUri);
//
//        for (DocumentFile file : documentFile.listFiles()) {
//
//            if (file.isDirectory()) { // if it is sub directory
//                continue;
//                //Maybe here we would start reading files recursively. But for now let's not
//            } else {
//                // Do stuff with normal file
//                operationOnFile.accept(file);   //Functional!
//            }
//
//            Log(file.getUri() + "\n");
//        }
//    }

    public void AddImageReferences_Path(String newImagePath) {
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.Images_Lists_SharedPrefName), Context.MODE_PRIVATE);

        //Don't touch this variable. It will mess things up!
        Set<String> loaded_set = sharedPref.getStringSet(getString(R.string.main_images_paths_key), new HashSet<String>());
        //----
        Set<String> images_set = new HashSet<>(loaded_set);

        images_set.add(newImagePath);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putStringSet(getString(R.string.main_images_paths_key), images_set);
        editor.apply();
    }

    public void AddDirReference(Uri directoryUri) {
        String directoryUri_str = directoryUri.toString();
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.Images_Lists_SharedPrefName), Context.MODE_PRIVATE);

        //Don't touch this variable. It will mess things up!
        Set<String> loaded_set = sharedPref.getStringSet(getString(R.string.images_dirs_key), new HashSet<String>());
        //----
        Set<String> images_set = new HashSet<>(loaded_set);

        images_set.add(directoryUri_str);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putStringSet(getString(R.string.images_dirs_key), images_set);
        editor.apply();
    }

    //non mi pare affidabilissimo sto timer. Magari per tempi pìù lunghi funzionerà bene uguale
    public void SetAlarm()
    {
        final Button button = null; // replace with a button from your own UI
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent _ )
            {
                button.setBackgroundColor( Color.RED );
                context.unregisterReceiver( this ); // this == BroadcastReceiver, not Activity
            }
        };

        this.registerReceiver( receiver, new IntentFilter("com.blah.blah.somemessage") );

        PendingIntent pintent = PendingIntent.getBroadcast( this, 0, new Intent("com.blah.blah.somemessage"), 0 );
        AlarmManager manager = (AlarmManager)(this.getSystemService( Context.ALARM_SERVICE ));

        // set alarm to fire 5 sec (1000*5) from now (SystemClock.elapsedRealtime())
        manager.set( AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 1000*5, pintent );
    }


}
