package com.konka.animationdemo;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
import android.widget.FrameLayout;
import android.widget.ImageView;


/**
 * Created by wangjie on 16-8-17.
 */
public class AnimationManager{
    ImageView imageView;
    private double[] endRidians={1.91,1.64,1.37,1.1};
    public static double initSpeed=0.015;
    public static double initRadian=0.0;
    public static double initAcceleratedRatio=0.001;
    private double initAcceleratedSpeed=0.04;
    private double moveInterval=0.1;
    private double impactInterval=0.08;


    private int index=1;
    public static String state;
    private   int forward=1;
    private   int toEnd=0;
    private int animationX;
    private int animationY;
    private int radius;
    private int timeInterval;
    private CircleView[] circleView = new CircleView[4];
    private int[] drawableIds={R.drawable.ball_4k,R.drawable.ball_mulitscreen,R.drawable.ball_miracast,R.drawable.ball_cpu};
    private Drawable[] drawables;
    private Runnable task;

    Handler handler = new Handler();
    ObjectAnimator leftAnimIn;
    ValueAnimator scaleAnim;
    Matrix matrix;
    ValueAnimator valueAnimator;
    private boolean hasPerformed;
    private int drawableIndex=1;
    public AnimationManager(Activity context){


        DisplayMetrics dm = new DisplayMetrics();
       context.getWindowManager().getDefaultDisplay().getMetrics(dm);
        animationX = context.getResources().getInteger(R.integer.animationX);
        animationY = dm.heightPixels-context.getResources().getInteger(R.integer.animationY);
        radius = context.getResources().getInteger(R.integer.radius);
        timeInterval = context.getResources().getInteger(R.integer.timeInterval);

        addCircle(context);
        task = new Runnable() {
            @Override
            public void run() {
                for(int i = toEnd;i<forward;i++){
                    circleView[i].changeRadian();
                    circleView[i].setCircleCenter((float)(animationX-radius*Math.sin(circleView[i].getCurrentRadian())),
                            (float)(animationY+radius*Math.cos(circleView[i].getCurrentRadian())));
                }
                addForward();
                handler.postDelayed(task, timeInterval);
            }
        };
        state="forward";

        ImageView left_bg=(ImageView)context.findViewById(R.id.left_bg);
        leftAnimIn=ObjectAnimator.ofFloat(left_bg,"alpha",0f,1f);
        leftAnimIn.setDuration(2000);
        leftAnimIn.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                handler.postDelayed(task, timeInterval);
            }
        });

        leftAnimIn.start();


        scaleAnim = ValueAnimator.ofFloat(1.0f,1.05f);
        scaleAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {

                float scale=(float)valueAnimator.getAnimatedValue();
                Matrix matrix = new Matrix(AnimationManager.this.matrix);
                matrix.postScale(scale,scale,imageView.getWidth()/2,imageView.getHeight()/2);
                imageView.setImageMatrix(matrix);
            }
        });
        scaleAnim.setDuration(200);
        scaleAnim.setRepeatCount(1);
        scaleAnim.setRepeatMode(ValueAnimator.REVERSE);

        valueAnimator = ValueAnimator.ofInt(0,359);
        valueAnimator.addListener(new AnimatorListenerAdapter(){

            @Override
            public void onAnimationRepeat(Animator animation) {
                circleView[0].flag=!circleView[0].flag;
                circleView[0].setDegree(0);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                circleView[0].flag=!circleView[0].flag;
                circleView[0].setDegree(0);
                if(!hasPerformed){
                    hasPerformed = !hasPerformed;
                    valueAnimator.setStartDelay(2000);
                    valueAnimator.start();
                }
            }
        });
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int degree = (int)valueAnimator.getAnimatedValue();

                circleView[0].setDegree(degree);
            }
        });
        valueAnimator.setDuration(200);
        valueAnimator.setRepeatMode(ValueAnimator.RESTART);
        valueAnimator.setRepeatCount(1);
    }

    public void addForward() {
        switch (state){
            case "forward":
                if(forward<4&&(circleView[forward-1].getCurrentRadian()-
                        circleView[forward].getCurrentRadian())>moveInterval){
                    forward++;
                }
                else if(forward==4&&toEnd<4){
                    if(toEnd==0){
                        if(circleView[toEnd].getCurrentRadian()>=circleView[toEnd].getEndRadian()){
                            toEnd++;
                        }
                    }
                    else{

                        if(index<4&&(circleView[index-1].getCurrentRadian()-circleView[index].getCurrentRadian()<impactInterval)){

                            if(index==1){
                                imageView.setVisibility(View.VISIBLE);
                                scaleAnim.start();
                            }
                            circleView[index].setState("accelerate");

                            circleView[index].setStartSpeed(initAcceleratedSpeed);

                            index++;
                        }

                        if(circleView[toEnd].getState().equals("accelerate")&&(circleView[toEnd].getCurrentRadian()<=circleView[toEnd].getEndRadian())){
                            circleView[toEnd].setState("normal");
                            toEnd++;
                        }

                    }

                }
                else if(toEnd==4){

                    index=1;
                    state="end";
                    timeInterval*=500;
                    hasPerformed=false;
                    valueAnimator.setStartDelay(300);
                    valueAnimator.start();
                }
                break;
            case "end":

                initSpeed(initSpeed*1.5);
                toEnd=3;
                timeInterval/=500;
                state="backward";
                imageView.setVisibility(View.INVISIBLE);
                break;
            case "backward":
                if(toEnd>0&&(circleView[toEnd-1].getCurrentRadian()-circleView[toEnd].getCurrentRadian())>0.37){
                    toEnd--;
                }
                else if(toEnd==0&&forward>0){
                    if(circleView[forward-1].getCurrentRadian()<=circleView[forward-1].getStartRadian()){
                        forward--;
                    }
                }
                else if(forward==0){
                    state="stop";
                    timeInterval*=25;
                }
                break;
            case "stop":
                changeDrawable();
                initSpeed(initSpeed);
                forward=1;
                timeInterval/=25;
                state="forward";
                break;
        }

    }

    private void changeDrawable(){
        for(int i = 0;i < 4;i++){
            circleView[i].setDrawable(drawables[(drawableIndex+i)%drawableIds.length]);
        }
        drawableIndex+=1;
        if(drawableIndex==drawableIds.length){
            drawableIndex=0;
        }
    }

    private void initSpeed(double speed){
        for(int i=0;i<4;i++){
            circleView[i].setStartSpeed(speed);
        }
    }

    private void addCircle(Activity context){
        drawables=new Drawable[drawableIds.length];
        for(int i = 0;i < drawableIds.length;i++){
            drawables[i]=context.getResources().getDrawable(drawableIds[i],null);
        }

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        FrameLayout mainLayout = (FrameLayout)(context.findViewById(R.id.mainlayout));

        Drawable ball_circle=context.getResources().getDrawable(R.drawable.ball_circle,null);
        for(int i=3;i>=0;i--){
            circleView[i] = new CircleView(context,drawables[i],endRidians[i],i);
            mainLayout.addView(circleView[i]);
            circleView[i].setCircleCenter((float)(animationX-radius*Math.sin(circleView[i].getCurrentRadian())),
                    (float)(animationY+radius*Math.cos(circleView[i].getCurrentRadian())));
        }




        imageView= new ImageView(context);
        imageView.setLayoutParams(layoutParams);
        imageView.setImageDrawable(ball_circle);
        imageView.setX(31.515678f);
        imageView.setY(502.64264f);
        imageView.setScaleType(ImageView.ScaleType.MATRIX);
        matrix=imageView.getImageMatrix();
        imageView.setVisibility(View.INVISIBLE);
        mainLayout.addView(imageView);


    }

}
