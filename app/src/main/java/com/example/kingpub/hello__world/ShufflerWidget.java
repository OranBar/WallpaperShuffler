package com.example.kingpub.hello__world;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class ShufflerWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.shuffler_widget);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);


    }

    public void onButtonNextClick(View view){
        Log.v("ShufflerWidget", "I CLICKED THE MOTHERFUCKING BUTTON");
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {

            Intent intent = new Intent(context, ChangeWallpaper_BroadcastReceiver.class);


            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);


//            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.shuffler_widget);
            views.setOnClickPendingIntent(R.id.button_next, pendingIntent);

//
            appWidgetManager.updateAppWidget(appWidgetId, views);

//            updateAppWidget(context, appWidgetManager, appWidgetId);


//            Log.v("ShufflerWidget", "MeHere");
//            //Here!!
//
//            Intent intent = new Intent(context, ChangeWallpaper_BroadcastReceiver.class);
//            PendingIntent pendingIntent = PendingIntent.getService(context, 0, new Intent(context, ChangeWallpaper_BroadcastReceiver.class), Intent.FLAG_ACTIVITY_NEW_TASK);
//
////            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
//            // add infos for the service which file to download and where to store
////            intent.putExtra(ChangeWallpaper_BroadcastReceiver.DIR_LIST_URIS_STR_KEY, );
//
//            //----------
////            PendingIntent pendingIntent = PendingIntent.getService(context, 0, new Intent(context, ChangeWallpaper_BroadcastReceiver.class), Intent.FLAG_);
//            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.shuffler_widget);
//            views.setOnClickPendingIntent(R.id.button_next, pendingIntent);
//
//            appWidgetManager.updateAppWidget(appWidgetId, views);
//
//            updateAppWidget(context, appWidgetManager, appWidgetId);

        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

