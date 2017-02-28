package com.group25.interactivegameblock;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Timer;


public class MainActivity extends AppCompatActivity {

    static final int GAMEBOARD_DIMENSION = 1080; // Google pixel phone has 1,080 x 1,920 pixels (441ppi) screen resolution

    RelativeLayout rl;
    TextView tvAccelerometer;

    //gameloop
    Timer timer;
    GameLoopTask gameLoopTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        rl = (RelativeLayout) findViewById(R.id.relativelayout);
        rl.setBackgroundColor(Color.WHITE);
        rl.getLayoutParams().width = GAMEBOARD_DIMENSION;
        rl.getLayoutParams().height = GAMEBOARD_DIMENSION;
        //setting image background
        rl.setBackgroundResource(R.drawable.gameboard);

        //creating game loop
        gameLoopTask = new GameLoopTask(this, rl, getApplicationContext());
        timer = new Timer();
        timer.schedule(gameLoopTask, 0, 50);


        //textview for accelerometer direction display
        tvAccelerometer = (TextView) findViewById(R.id.tvAcc);
        tvAccelerometer.setText("Accelerometer Instantaneous Readings");
        tvAccelerometer.setTextColor(Color.BLACK);


        // Register only the Gravity-Compensated Accelerometer Readings
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor Accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        final AccelerometerEventListener accListener = new AccelerometerEventListener(tvAccelerometer,gameLoopTask);
        sensorManager.registerListener(accListener, Accelerometer, SensorManager.SENSOR_DELAY_GAME);



    }
}
