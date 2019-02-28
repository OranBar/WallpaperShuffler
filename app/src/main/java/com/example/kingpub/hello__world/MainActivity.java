package com.example.kingpub.hello__world;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "com.kingpub.wallpaperShuffler.MSG" ;

    //--- {vars for shuffle
    public Bitmap[] wallpapers;
    public int currWallpaperIndex = 0;
    //----- vars for shuffle}

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


        Set<String> images_set = loadMainImagesSet();
        String first = "None";
        int totalImages = 0;
        if(images_set != null && images_set.size() != 0){
            first = images_set.iterator().next();
            totalImages = images_set.size();
        }
        Toast toast = Toast.makeText(getApplicationContext(), totalImages+" Images total. First is "+first, Toast.LENGTH_LONG);
        toast.show();



        //------- {Buttons setup
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
                changeWallpaperToTnmn(v);
                changeWallpaperAfterSeconds(10, false);
                changeWallpaperAfterSeconds(20, false);
                changeWallpaperAfterSeconds(30, false);
                changeWallpaperAfterSeconds(40, false);
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

    private Set<String> loadMainImagesSet(){
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.Images_Lists_SharedPrefName), Context.MODE_PRIVATE);

        Set<String> images_set = sharedPref.getStringSet(getString(R.string.main_image_list_key), new HashSet<String>());

        return images_set;
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
                    String[] images = (String[]) images_set.toArray();

                    newWallpaper = BitmapFactory.decodeFile(images[currWallpaperIndex]);
                    currWallpaperIndex = (currWallpaperIndex +1) %images.length;
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
            }
        };

        this.registerReceiver( receiver, new IntentFilter("com.example.kingpub.hello__world.Scheduled_WallpaperChange") );

        PendingIntent pintent = PendingIntent.getBroadcast( this, 0, new Intent("com.example.kingpub.hello__world.Scheduled_WallpaperChange"), 0 );
        AlarmManager manager = (AlarmManager)(this.getSystemService( Context.ALARM_SERVICE ));

        // set alarm to fire 5 sec (1000*5) from now (SystemClock.elapsedRealtime())
        manager.set( AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 1000*seconds, pintent );
    }

    public void changeWallpaper(View view, Bitmap bm){
        changeWallpaper(bm);
    }

    public void changeWallpaper(Bitmap bm){

        Log.v("OBTask","ChangeWallpaper!");

        WallpaperManager wallpaperManager =  WallpaperManager.getInstance(getApplicationContext());

        try {
            wallpaperManager.setBitmap(bm);
            wallpaperManager.setBitmap(bm, null ,true, WallpaperManager.FLAG_LOCK);
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

    private static int RESULT_LOAD_IMAGE = 1;



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

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            File file = new File(picturePath);
            String myFile = file.getParent() +"/"+ file.getName();

            AddImageReferences(picturePath);

            int total_images = loadMainImagesSet().size();

            Toast toast = Toast.makeText(getApplicationContext(), "Image reference to"+ myFile+" added. Total = "+total_images, Toast.LENGTH_LONG);
            toast.show();

//            ImageView imageView = (ImageView) findViewById(R.id.imgView);
//            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));

        }
    }

    public void AddImageReferences(String newImagePath) {
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.Images_Lists_SharedPrefName), Context.MODE_PRIVATE);

        //Don't touch this variable. It will mess things up!
        Set<String> loaded_set = sharedPref.getStringSet(getString(R.string.main_image_list_key), new HashSet<String>());
        //----
        Set<String> images_set = new HashSet<>(loaded_set);

        images_set.add(newImagePath);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putStringSet(getString(R.string.main_image_list_key), images_set);
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
