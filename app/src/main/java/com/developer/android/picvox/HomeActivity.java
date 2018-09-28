package com.developer.android.picvox;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

public class HomeActivity extends AppCompatActivity {

    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;

    public static int oneTimeOnly = 0;

    ImageView picvoxImage;
    Button playButton,stopButton,pauseButton;
    Button wwwButton,fbButton,twitterButton,itunesButton;
    Button saveButton,homeButton;
    private SeekBar seekBar;

    private Handler myHandler = new Handler();

    ProgressDialog progressDialog;

    String imageName = "", audioName = "", fbURL = "", wwwURL = "", twitterURL = "", itunesURL = "";
    MediaPlayer player = new MediaPlayer();
    int length = 0;
    String barcode_id;
    int mduration = 0;
    int mcurrentPosition = 0;
    int p;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initParams();

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playButton.setBackground(getResources().getDrawable(R.drawable.play_button));
                playButton.setEnabled(false);
                pauseButton.setBackground(getResources().getDrawable(R.drawable.pause));
                pauseButton.setEnabled(true);
                stopButton.setBackground(getResources().getDrawable(R.drawable.stop));
                stopButton.setEnabled(true);

                try {
                    start();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mduration = player.getDuration();
                mcurrentPosition = player.getCurrentPosition();

                if(oneTimeOnly == 0){
                    seekBar.setMax((int) mduration);
                    oneTimeOnly = 1;
                }

                seekBar.setProgress((int)mcurrentPosition);
                myHandler.postDelayed(UpdateSongTime,100);

            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playButton.setBackground(getResources().getDrawable(R.drawable.play));
                playButton.setEnabled(true);
                pauseButton.setBackground(getResources().getDrawable(R.drawable.pause));
                pauseButton.setEnabled(false);
                stopButton.setBackground(getResources().getDrawable(R.drawable.stop_button));
                stopButton.setEnabled(false);
                try
                {
                    stop();
                }catch(Exception e)
                {
                    e.printStackTrace();
                }

            }
        });
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playButton.setBackground(getResources().getDrawable(R.drawable.play));
                playButton.setEnabled(true);
                pauseButton.setBackground(getResources().getDrawable(R.drawable.pause_button));
                pauseButton.setEnabled(false);
                stopButton.setBackground(getResources().getDrawable(R.drawable.stop));
                stopButton.setEnabled(true);
                try
                {
                    pause();
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
        wwwButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!wwwURL.isEmpty())
                {
                    Intent launchBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse(wwwURL));
                    startActivity(launchBrowser);
                }else Toast.makeText(HomeActivity.this, "URL is empty", Toast.LENGTH_SHORT).show();

            }
        });

        fbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!fbURL.isEmpty())
                {
                    Intent launchBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse(fbURL));
                    startActivity(launchBrowser);
                }else
                    Toast.makeText(HomeActivity.this, "URL is empty", Toast.LENGTH_SHORT).show();

            }
        });
        twitterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!twitterURL.isEmpty())
                {
                    Intent launchBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse(twitterURL));
                    startActivity(launchBrowser);
                }else
                    Toast.makeText(HomeActivity.this, "URL is empty", Toast.LENGTH_SHORT).show();

            }
        });
        itunesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!itunesURL.isEmpty())
                {
                    Intent launchBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse(itunesURL));
                    startActivity(launchBrowser);
                }else
                    Toast.makeText(HomeActivity.this, "URL is empty", Toast.LENGTH_SHORT).show();

            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    if (!player.isPlaying()){
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.setMessage("\tLoading...");
                                        progressDialog.setCancelable(false);
                                        progressDialog.show();
                                    }
                                });
                                try{
                                    File cacheDir=new File(android.os.Environment.getExternalStorageDirectory(),"PicVox/Audio");
                                    if(!cacheDir.exists())
                                        cacheDir.mkdirs();

                                    File f=new File(cacheDir,audioName);
                                    URL url = new URL("http://thepicvox.com/Audios/" + audioName);

                                    InputStream input = new BufferedInputStream(url.openStream());
                                    OutputStream output = new FileOutputStream(f);

                                    byte data[] = new byte[1024];
                                    long total = 0;
                                    int count=0;
                                    while ((count = input.read(data)) != -1) {
                                        total++;
                                        Log.e("while","A"+total);

                                        output.write(data, 0, count);
                                    }

                                    output.flush();
                                    output.close();
                                    input.close();

                                }catch(Exception e){
                                    e.printStackTrace();
                                }finally {
                                    progressDialog.dismiss();
                                }
                            }
                        }).start();
                    }else{
                        Toast.makeText(HomeActivity.this, "While playing, you can not download!", Toast.LENGTH_SHORT).show();
                    }




            }
        });
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeActivity.this, MainActivity.class));
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
//                seekBar.setMax(player.getDuration() / 1000);
//                player.seekTo(progress);
//                if(b) {
//                    seekBar.setProgress(progress * 1000);
//                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        new AsyncURL().execute(barcode_id);
    }


    private void initParams(){
        picvoxImage = (ImageView)findViewById(R.id.picvox_image);
        playButton = (Button)findViewById(R.id.play_button);
        stopButton = (Button)findViewById(R.id.stop_button);
        pauseButton = (Button)findViewById(R.id.pause_button);

        wwwButton = (Button)findViewById(R.id.www_button);
        fbButton = (Button)findViewById(R.id.fb_button);
        twitterButton = (Button)findViewById(R.id.twitter_button);
        itunesButton = (Button)findViewById(R.id.itunes_button);

        saveButton = (Button)findViewById(R.id.save_button);
        homeButton = (Button)findViewById(R.id.home_button);
        seekBar = (SeekBar)findViewById(R.id.seekBar);
        progressDialog = new ProgressDialog(HomeActivity.this);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            barcode_id = extras.getString("BARCODEID");
        }
    }

    private class AsyncURL extends AsyncTask<String, String, String> {
        HttpURLConnection conn;
        URL url = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("\tLoading...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            try{
                url = new URL("http://thepicvox.com/websuperboy/fetch.php?barcode_id=" + strings[0]);

 //               url = new URL("http://open.honeymotors.net/connectme.asmx/SELECT_STOCKLIST_MOBILE?YEAR_min=&YEAR_max=&Milage_min=30000&Milage_max=80000&Cost_min=12000&Cost_max=18000&BodyType=Saloon,SUV&BrandName=Toyota&FreeText=Saloon%202018");
                //url = new URL("http://thepicvox.com/websuperboy/fetch.php?barcode_id=94917567");
                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("GET");

                int response = conn.getResponseCode();

                if (response == HttpURLConnection.HTTP_OK){
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();

                    String line;

                    while ((line = reader.readLine()) != null)
                    {
                        result.append(line);
                    }
                    return result.toString();
                }else{
                    return "unsuccessful";
                }
            }catch (MalformedURLException e)
            {
                e.printStackTrace();
            }catch(IOException e)
            {
                e.printStackTrace();
            }finally {
                conn.disconnect();
                progressDialog.dismiss();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                JSONArray jsonArray = new JSONArray(s);
                final int count = jsonArray.length();

                for (int i = 0 ; i < count ; i++)
                {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    imageName = jsonObject.getString("image_path");
                    audioName = jsonObject.getString("audio_path");
                    wwwURL = jsonObject.getString("website");
                    fbURL = jsonObject.getString("facebook");
                    twitterURL = jsonObject.getString("twitter");
                    itunesURL = jsonObject.getString("itunes");

                }
                String url = "http://thepicvox.com/uploads/" + imageName;
                String audioUrl = "http://thepicvox.com/Audios/" + audioName;
                Picasso.with(getApplicationContext()).load("http://thepicvox.com/uploads/" + imageName).centerCrop().resize(picvoxImage.getMeasuredWidth(),picvoxImage.getMeasuredHeight()).into(picvoxImage);
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                player.setDataSource("http://thepicvox.com/Audios/" + audioName);
                player.prepare();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        /*duration = player.getDuration();
                        player.seekTo(0);
                        currentPosition = player.getCurrentPosition();
                        seekBar.setMax(duration);*/
                    }
                });
            }catch (JSONException e){

                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }catch(Exception e)
            {
                e.printStackTrace();
            }finally {
            }
            super.onPostExecute(s);
        }
    }

    public void start() throws IOException {
        if(player != null){
            player.start();
        }
    }

    public void pause(){
        if(player != null){
            player.pause();
        }
    }

    public void stop(){
        if(player != null){
            player.pause();
            player.seekTo(0);
            mcurrentPosition = 0;
            seekBar.setProgress(0);
            oneTimeOnly = 0;
        }
    }

    @Override
    protected void onDestroy(){
        if(player!=null && player.isPlaying()) {
            player.pause();
            player.release();
            player = null;
        }

        super.onDestroy();
    }

    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            mcurrentPosition = player.getCurrentPosition();
            seekBar.setProgress((int)mcurrentPosition);
            myHandler.postDelayed(this, 100);
        }
    };

}
