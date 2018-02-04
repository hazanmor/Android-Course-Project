package com.demonized.androidproject;

import android.app.Activity;
import android.app.IntentService;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

public class StorageReadService extends IntentService {
    public static final String CURRENT = "Current";

    private Cursor cursor;

    public StorageReadService() {
        super("StorageReadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            getLocationData();
        }
        Intent dismiss = new Intent(CURRENT);
        sendBroadcast(dismiss);
    }

    public void getLocationData() {
        ArrayList<String> images = getAllShownImagesPath();

        Geocoder geocoder = new Geocoder(getApplicationContext());
        FileWriter fileWriter = null;
        BufferedWriter bw = null;

        for (int i = 0; i < images.size(); i++) {
            try {
                ExifInterface exif = new ExifInterface(images.get(i));
                float[] coords = new float[2];
                List<Address> list = null;
                if (exif.getLatLong(coords))
                    list = geocoder.getFromLocation(coords[0], coords[1], 3);
                if (list == null || list.isEmpty())
                    continue;
                MainActivity.database.add(new LineObject(coords[0] ,coords[1],images.get(i)));


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private ArrayList<String> getAllShownImagesPath() {
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;
        ArrayList<String> listOfAllImages = new ArrayList<String>();
        String absolutePathOfImage = null;
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

        cursor = getContentResolver().query(uri, projection, null,
                null, null);

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);
            listOfAllImages.add(absolutePathOfImage);
        }
        return listOfAllImages;
    }

}
