package com.group25.interactivegameblock;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

/**
 * Created by Dahye on 2017-02-19.
 */
@TargetApi(11)

public class GameBlock extends ImageView {

    private final float IMAGE_SCALE = 1.0f;

    private float myCoorX = 10;
    private float myCoorY = 10;



    GameLoopTask.gameDirection myDir;

    public GameBlock(Context context, int coordX, int coordY){
        super(context);

        this.myCoorX = coordX;
        this.myCoorY = coordY;

        this.setImageResource(R.drawable.gameblock);
        this.setScaleX(IMAGE_SCALE);
        this.setScaleY(IMAGE_SCALE);

        this.setX(myCoorX);
        this.setY(myCoorY);

    }


    //direction setter
    public void setDirection(GameLoopTask.gameDirection newDirection){
        this.myDir = newDirection;

    }

    public void move(float myCoorX, float myCoorY){
        this.myCoorX = myCoorX;
        this.myCoorY = myCoorY;

        this.setX(myCoorX);
        this.setY(myCoorY);

    }
}
