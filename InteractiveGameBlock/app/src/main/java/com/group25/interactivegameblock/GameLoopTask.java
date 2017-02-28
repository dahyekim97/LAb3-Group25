package com.group25.interactivegameblock;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.RelativeLayout;

import java.util.TimerTask;


/**
 * Created by Dahye on 2017-02-19.
 */
@TargetApi(11)


public class GameLoopTask extends TimerTask {

    Activity myActivity;
    Context myContext;
    RelativeLayout MyRL;
    int i = 0;

    //The down edge and the right edge that squre stops at
    public static int DownMost = 810;
    public static int RightMost = 810;

    // direction variable enumeration
    public enum gameDirection{ UP, DOWN, LEFT, RIGHT, NO_MOVEMENT }
    // current game direction is NO_MOVEMENT by default
    public static gameDirection currentGameDirection = gameDirection.NO_MOVEMENT;

    // Block object
    GameBlock newBlock;

    // Velocity components of bolck
    float vX = 0;
    float vY = 0;
    // Fixed acceleration value
    final float aX = 2.0f;
    final float aY = 2.0f;

    // Target Coordinate will be determined depending on the current direction
    static float targetCoordX= 0;
    static float targetCoordY= 0;


    public GameLoopTask(Activity myActivity, RelativeLayout myRL, Context myContext){

        this.myActivity = myActivity;
        this.myContext = myContext;
        this.MyRL = myRL;

        // Create block object
        createBlock();

    }

    private void createBlock(){
        // Create block object
        newBlock = new GameBlock(myContext, 0,0); //Or any (x,y) of your choice
        MyRL.addView(newBlock);
    }

    // Direction setter
    public void setDirection(gameDirection newDirection){
        this.currentGameDirection = newDirection;
        setTargetCoordinate(currentGameDirection);

        Log.d("Direction : " , currentGameDirection+"");
    }

    // Set Target Coordinate depending on current direction
    public void setTargetCoordinate(gameDirection dir){
        switch(dir){
            case UP:
                targetCoordY = 0;
                break;
            case DOWN:
                targetCoordY = DownMost;
                break;
            case RIGHT:
                targetCoordX = RightMost;
                break;
            case LEFT:
                targetCoordX = 0;
                break;
            case NO_MOVEMENT:

                break;
            default:
                targetCoordX = 0;
                targetCoordY = 0;

                break;

        }

    }
    @Override
    public void run() {

        myActivity.runOnUiThread(
                new Runnable() {
                    @Override
                        public void run() {

//                     checking the timer using Lod.d
//                        Log.d("count : ", i+"");
//                        i++;

                            // Set velocity value depending on the current direction
                            switch (currentGameDirection) {

                                case UP:
                                    if(newBlock.getY()>0) {
                                        vY -= aY;
                                    }
                                    else{
                                        vY=0;
                                    }
                                    break;

                                case DOWN:

                                    if(newBlock.getY() < targetCoordY) {

                                        vY += aY;

                                    }
                                    else if(newBlock.getY() >= targetCoordY){
                                        vY=0;
                                    }
                                    break;

                                case LEFT:
                                    if(newBlock.getX() > 0) {

                                        vX -= aX;

                                    }
                                    else if(newBlock.getX()<=0){

                                        vX=0;

                                    }
                                    break;

                                case RIGHT:
                                    if(newBlock.getX() < targetCoordX) {

                                        vX += aX;

                                    }
                                    else if(newBlock.getX() >= targetCoordX){
                                        vX=0;
                                    }
                                    break;
                                case NO_MOVEMENT:
                                    vX = 0;
                                    vY = 0;
                                    break;

                                default:
                                    currentGameDirection = gameDirection.NO_MOVEMENT;
                                    vX = 0;
                                    vY = 0;
                                    break;

                            }

                        // Move the block by adding velocity to its current position
                            newBlock.move(newBlock.getX()+vX,newBlock.getY()+vY);





                    }


                    }

        );

    }
}
