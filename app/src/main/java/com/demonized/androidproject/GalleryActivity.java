package com.demonized.androidproject;

import android.Manifest;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

public class GalleryActivity extends AppCompatActivity implements RecognitionListener {
    private final String TAG = "GalleryActivity";
    public static final String ACTION = "UPDATE";
    private ArrayList<LineObject> database=null;
    private SpeechRecognizer speech = null;
    private String speechInput = null;
    private Intent recognizerIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        if(database==null)
            database = new ArrayList<>(MainActivity.database);
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);
        ((ProgressBar)findViewById(R.id.progress_gallery)).setMax(140);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "en-US");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 64);
        speech.startListening(recognizerIntent);
        SpeechThread speechThread = new SpeechThread();
        speechThread.start();
        getSupportFragmentManager().beginTransaction().replace(R.id.alternate_third,(android.support.v4.app.Fragment)new MapFragment()).commit();
    }

    @Override
    public void onReadyForSpeech(Bundle bundle) {
        Log.d(TAG, "OnReady");
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.d(TAG, "OnBeginning");
    }

    @Override
    public void onRmsChanged(float v) {
        if(v>=0) {
            ((ProgressBar) findViewById(R.id.progress_gallery)).setProgress((int) v * 10);
            ((ProgressBar) findViewById(R.id.progress_gallery)).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBufferReceived(byte[] bytes) {
        Log.d(TAG, "OnBufferReceived");
    }

    @Override
    public void onEndOfSpeech() {


        Log.d(TAG, "OnEnd");
    }

    @Override
    public void onError(int i) {
        Log.d(TAG, "Error is: " + getErrorText(i));
    }

    @Override
    protected void onResume(){
        super.onResume();
        speech.startListening(recognizerIntent);
    }

    @Override
    protected void onPause(){
        super.onPause();
        speech.cancel();
    }
    @Override
    public void onResults(Bundle results) {
        speech.stopListening();
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = "";
        for (String result : matches)
            text += result + "\n";
        ((TextView) findViewById(R.id.keywords)).setText(matches.get(0));
        Log.d(TAG, "OnResults");
        speechInput = text;
    }

    @Override
    public void onPartialResults(Bundle bundle) {
        Log.d(TAG, "OnPartial");
    }

    @Override
    public void onEvent(int i, Bundle bundle) {
        Log.d(TAG, "OnEvent");
    }

    class SpeechThread extends Thread {
        ArrayList<LineObject> paths = null;

        @Override
        public void run() {
            super.run();
            while (true) {
                try {
                    sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    if (Thread.currentThread().isInterrupted())
                        return;
                }
                paths = crossCheck(speechInput);
                if(paths!=null && !paths.isEmpty()) {
                    for (int i = 0; i + 2 < paths.size(); i++) {
                        final int j=i;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(ACTION);
                                intent.putExtra("LAT",paths.get(j).lat);
                                intent.putExtra("LON",paths.get(j).lon);
                                sendBroadcast(intent);
                                Picasso.with(getApplicationContext()).load(new File(paths.get(j).file)).fit().centerInside().into(((ImageView) findViewById(R.id.main_image)));
                                Picasso.with(getApplicationContext()).load(new File(paths.get(j+1).file)).fit().centerInside().into(((ImageView) findViewById(R.id.alternate_first)));
                                Picasso.with(getApplicationContext()).load(new File(paths.get(j+2).file)).fit().centerInside().into(((ImageView) findViewById(R.id.alternate_second)));

                            }
                        });
                        try{
                            sleep(5000);
                        }catch(InterruptedException e){
                            if(Thread.currentThread().isInterrupted())
                                return;
                        }
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        speech.startListening(recognizerIntent);
                    }
                });

                try {
                    sleep(5000);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                    if (Thread.currentThread().isInterrupted())
                        return;
                }
            }
        }

    }


    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (speech == null) {
            if (recognizerIntent == null) {
                recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                        "en-US");
                recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                        this.getPackageName());
                recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
                recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 64);
            }
            speech = SpeechRecognizer.createSpeechRecognizer(this);
            speech.setRecognitionListener(this);
            speech.startListening(recognizerIntent);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (speech != null) {
            speech.destroy();
            Log.i(TAG, "destroy");
        }
    }

    public ArrayList<LineObject> crossCheck(String input) {
        List<Address> addressList = null;
        ArrayList<LineObject> images = new ArrayList<>();
        if(input==null)
            return null;
        if (input != null || !input.isEmpty()) {
            String[] temp = input.split(" ");
            Geocoder geocoder = new Geocoder(this);
            for (int i = 0; i < temp.length; i++) {
                try {
                    addressList = geocoder.getFromLocationName(temp[i], 3);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (addressList == null || addressList.isEmpty())
                    continue;
                for (int j = 0; j < addressList.size(); j++) {
                    Address speechAddress = addressList.get(j);
                    for (int k = 0; k < database.size(); k++) {
                        LineObject lineObject = database.get(k);
                        if(Math.sqrt(Math.pow(speechAddress.getLatitude()-lineObject.lat,2)+Math.pow(speechAddress.getLongitude()-lineObject.lon,2))<4.0)
                            images.add(lineObject);

                    }
                }
            }
            if(images.size()>2)
                return images;
        }
        return null;
    }
}
