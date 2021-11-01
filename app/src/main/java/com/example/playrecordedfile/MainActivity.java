package com.example.playrecordedfile;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainTag";

    // Layout
    Button btn_list;

    // List
    ArrayList<SampleData> dataArrayList;
    private MyAdapter myAdapter;

    // MediaPlayer
    private MediaPlayer mediaPlayer;
    private int stateMediaPlayer;
    private final int STATE_NOTSTARTER = 0;
    private final int STATE_PLAYING = 1;
    private final int STATE_PAUSING = 2;
    private static String folderName = "Call";


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermission();

        btn_list = findViewById(R.id.btn_list);
        btn_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRecordList();

                ListView listView = findViewById(R.id.listView);
                listView.setAdapter(myAdapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        String fileName = myAdapter.getItem(i).getRecord_name();

                        initMediaPlayer(fileName);
                        switch (stateMediaPlayer) {
                            case STATE_NOTSTARTER:
                                mediaPlayer.start();
                                stateMediaPlayer = STATE_PLAYING;
                                Toast.makeText(MainActivity.this, "재생", Toast.LENGTH_SHORT).show();
                                break;
                            case STATE_PLAYING:
                                mediaPlayer.pause();
                                stateMediaPlayer = STATE_PAUSING;
                                break;
                            case STATE_PAUSING:
                                mediaPlayer.start();
                                stateMediaPlayer = STATE_PLAYING;
                                break;
                        }
                    }
                });
            }
        });
    }

    private void initMediaPlayer(String fileName){
        File filepath = Environment.getExternalStorageDirectory();
        String path = filepath.getPath(); // /storage/emulated/0

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(path+"/"+folderName+"/"+fileName);
            mediaPlayer.prepare();
            stateMediaPlayer = STATE_NOTSTARTER;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void showRecordList(){
        dataArrayList = new ArrayList<SampleData>();
        myAdapter = new MyAdapter(MainActivity.this, dataArrayList);

        List<String> fileList = getRecordList();

        if(fileList.size() != 0){
            for (int i=0; i<5; i++){
                String file = fileList.get(i);
                dataArrayList.add(new SampleData(file));
            }
        } else{
            dataArrayList.add(0, new SampleData("녹음파일이 없습니다."));
        }
        myAdapter.notifyDataSetChanged();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private List<String> getRecordList(){
        File filepath = Environment.getExternalStorageDirectory();
        String path = filepath.getPath(); // /storage/emulated/0

        File directory = new File(path+"/"+folderName);
        File[] files = directory.listFiles();
        Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());

        List<String> filesNameList = new ArrayList<>();
        for (int i=0; i<files.length; i++){
            Log.d(TAG, files[i].getName());
            filesNameList.add(files[i].getName());
        }

        return filesNameList;
    }

    private void requestPermission(){
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(
                    this,
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 0
            );
        }
    }
}