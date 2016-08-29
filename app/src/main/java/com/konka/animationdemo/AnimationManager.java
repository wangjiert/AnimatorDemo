package com.konka.animationdemo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.net.Uri;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.VideoView;

/**
 * Created by wangjie on 16-8-17.
 */

public class AnimationManager{

    private boolean hasPerformed;

    private int animationX = 610;                           //旋转点的X坐标
    private int animationY = 315;                           //旋转点的Y坐标
    private int radius = 518;                               //动画半径
    private int imageWidth = 180;                           //圆环图片的宽度
    private int leftBgDuration = 500;                      //左边图片显示的动画时间
    private int circleAlphaDuration = 100;                   //圆环透明动画持续时间
    private int circleScaleDuration = 200;                  //圆环放大持续时间
    private int highLightStartInterval = 300;               //高亮动画开始的延迟时间
    private int ballHighLightDuration = 200;                //圆球高亮动画持续时间
    private int ballHightLightInterval = 2000;              //两次高亮动画的时间间隔
    private int startTimeInterval = 250;                    //小球在下面停留的时间
    private int endTimeInterval = 5000;                     //小球再上面停留的时间
    private int standardTimeInterval = 10;                  //动画执行的时间间隔
    private int timeInterval = standardTimeInterval;
    private int index = 1;
    private int toEnd = 0;
    private int forward = 1;
    private int drawableIndex = 1;
    private int[] drawableIds = { R.drawable.ball_4k, R.drawable.ball_mulitscreen, R.drawable.ball_miracast, R.drawable.ball_cpu,
            R.drawable.ball_capture,R.drawable.ball_homeshare,R.drawable.ball_nullmouse,
            R.drawable.ball_shop,R.drawable.ball_sss,R.drawable.ball_updateonline}; //图片的id

    private float circleScale = 1.05f; //圆环放大倍数

    private double[] endRidians = { 1.91, 1.64, 1.37, 1.1}; //四个小球停止的角度
    private double moveInterval = 0.1;                      //上升开始旋转的间隔角度
    private double impactInterval = 0.08;                   //碰撞后反转的角度
    private double downInterval = 0.37;                     //小球开始下降的间隔角度

    private ImageView imageView;

    private ImageView left_bg;

    public static String state;

    private CircleView[] circleView = new CircleView[4];

    private Drawable[] drawables;

    private Runnable task;

    private Matrix matrix;

    private Handler handler = new Handler();

    private ObjectAnimator leftAnimIn;

    private ObjectAnimator alphaInAnimator;

    private ObjectAnimator alphaOutAnimator;

    private ValueAnimator scaleAnim;

    private ValueAnimator valueAnimator;

    VideoView videoView;

    private AnimatorSet alphaScaleSet = new AnimatorSet();

    public AnimationManager(Activity context) {

        DisplayMetrics dm = new DisplayMetrics();

        context.getWindowManager().getDefaultDisplay().getMetrics(dm);

        animationY = dm.heightPixels - animationY;

        addCircle(context);

        task = new Runnable() {

            @Override
            public void run() {

                for( int i = toEnd; i < forward; i++) {

                    circleView[i].changeRadian();

                    circleView[i].setCircleCenter( (float) (animationX - radius * Math.sin(circleView[i].getCurrentRadian())),
                            (float) (animationY + radius * Math.cos(circleView[i].getCurrentRadian())));

                }

                addForward();

                handler.postDelayed( task, timeInterval);

            }

        };

        state="forward";

        //ImageView left_bg=(ImageView)context.findViewById(R.id.left_bg);

        leftAnimIn=ObjectAnimator.ofFloat( left_bg, "alpha", 0f, 1f);

        leftAnimIn.setDuration(leftBgDuration);

        leftAnimIn.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {

                super.onAnimationEnd(animation);

                handler.postDelayed(task, timeInterval);

            }

        });

        leftAnimIn.start();

        alphaInAnimator = ObjectAnimator.ofFloat( imageView, "alpha", 0f, 1f);

        alphaInAnimator.setDuration(circleAlphaDuration);

        alphaOutAnimator = ObjectAnimator.ofFloat( imageView, "alpha", 1f, 0f);

        alphaOutAnimator.setDuration(circleAlphaDuration);

        scaleAnim = ValueAnimator.ofFloat( 1.0f, circleScale);

        scaleAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {

                float scale=(float) valueAnimator.getAnimatedValue();

                Matrix matrix = new Matrix(AnimationManager.this.matrix);

                matrix.postScale( scale, scale, imageWidth / 2, imageWidth / 2);

                imageView.setImageMatrix(matrix);

            }

        });

        scaleAnim.setDuration(circleScaleDuration);

        scaleAnim.setRepeatCount(1);

        scaleAnim.setRepeatMode(ValueAnimator.REVERSE);

        alphaScaleSet.play(alphaInAnimator).before(scaleAnim);

        valueAnimator = ValueAnimator.ofInt(0,359);

        valueAnimator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationRepeat(Animator animation) {

                circleView[0].flag = !circleView[0].flag;

                circleView[0].setDegree(0);
            }

            @Override
            public void onAnimationEnd(Animator animation) {

                circleView[0].flag = !circleView[0].flag;

                circleView[0].setDegree(0);

                if(!hasPerformed) {

                    hasPerformed = !hasPerformed;

                    valueAnimator.setStartDelay(ballHightLightInterval);

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

        valueAnimator.setDuration(ballHighLightDuration);

        valueAnimator.setRepeatMode(ValueAnimator.RESTART);

        valueAnimator.setRepeatCount(1);

    }

    private void addForward() {

        switch (state){

            case "forward":

                if(forward < 4 && (circleView[forward - 1].getCurrentRadian() -
                        circleView[forward].getCurrentRadian()) > moveInterval) {

                    forward++;

                }

                else if(forward == 4 && toEnd < 4) {

                    if(toEnd == 0) {

                        if(circleView[toEnd].getCurrentRadian() >= circleView[toEnd].getEndRadian()) {

                            circleView[toEnd].initAcceleratedValue();

                            toEnd++;

                        }

                    }

                    else {

                        if(index < 4 && (circleView[index - 1].getCurrentRadian() - circleView[index].getCurrentRadian() < impactInterval)) {

                            if(index == 1) {

                                //imageView.setVisibility(View.VISIBLE);

                                //scaleAnim.start();

                                alphaScaleSet.start();

                            }

                            circleView[index].setState("accelerate");

                            circleView[index].initAcceleratedValue();

                            index++;

                        }

                        if(circleView[toEnd].getState().equals("accelerate") && (circleView[toEnd].getCurrentRadian() <= circleView[toEnd].getEndRadian())) {

                            circleView[toEnd].setState("normal");

                            circleView[toEnd].initAcceleratedValue();

                            toEnd++;

                        }

                    }

                }

                else if(toEnd == 4) {

                    index = 1;

                    state = "end";

                    timeInterval = endTimeInterval;

                    hasPerformed = false;

                    valueAnimator.setStartDelay(highLightStartInterval);

                    valueAnimator.start();

                }

                break;

            case "end":

                toEnd = 3;

                timeInterval = standardTimeInterval;

                state = "backward";

                alphaOutAnimator.start();

                break;

            case "backward":

                if(toEnd > 0 && (circleView[toEnd - 1].getCurrentRadian() - circleView[toEnd].getCurrentRadian()) > downInterval) {

                    toEnd--;

                }

                else if(toEnd == 0 && forward > 0) {

                    if(circleView[forward - 1].getCurrentRadian() <= circleView[forward - 1].getStartRadian()) {

                        circleView[forward - 1].initAcceleratedValue();

                        forward--;

                    }

                }

                else if(forward == 0) {

                    state = "stop";

                    timeInterval = startTimeInterval;

                }

                break;

            case "stop":

                changeDrawable();

                forward = 1;

                timeInterval = standardTimeInterval;

                state = "forward";

                break;

        }

    }

    private void changeDrawable() {

        for( int i = 0; i < 4; i++) {

            circleView[i].setDrawable(drawables[ (drawableIndex + i) % drawableIds.length]);

        }

        drawableIndex += 1;

        if(drawableIndex == drawableIds.length) {

            drawableIndex = 0;

        }

    }

    private void addCircle(Activity context) {

        drawables = new Drawable[drawableIds.length];

        for( int i = 0; i < drawableIds.length; i++) {

            drawables[i] = context.getResources().getDrawable( drawableIds[i], null);
        }

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams( ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        FrameLayout mainLayout = (FrameLayout) (context.findViewById(R.id.mainlayout));

        left_bg = new ImageView(context);

        left_bg.setLayoutParams(layoutParams);

        left_bg.setImageResource(R.drawable.left_bg);

        left_bg.setAlpha(0f);

        mainLayout.addView(left_bg);

        for( int i = 3; i >= 0; i--) {

            circleView[i] = new CircleView( context, drawables[i], endRidians[i], i);

            mainLayout.addView(circleView[i]);

            circleView[i].setCircleCenter( (float) (animationX - radius * Math.sin(circleView[i].getCurrentRadian())),
                    (float) (animationY + radius * Math.cos(circleView[i].getCurrentRadian())));

        }

        imageView = new ImageView(context);

        imageView.setLayoutParams(layoutParams);

        imageView.setImageResource(R.drawable.ball_circle);

        imageView.setX( (float) (animationX - radius * Math.sin(endRidians[0])) - imageWidth / 2);

        imageView.setY( (float) (animationY + radius * Math.cos(endRidians[0])) - imageWidth / 2);

        imageView.setScaleType(ImageView.ScaleType.MATRIX);

        matrix = imageView.getImageMatrix();

        imageView.setAlpha(0f);

        mainLayout.addView(imageView);

        videoView = new VideoView(context);

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {

                videoView.start();

            }

        });

        videoView.setVideoURI(Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.movie1));

        videoView.setLayoutParams(new FrameLayout.LayoutParams( 200, 200));

        videoView.setX(50);

        videoView.setY(100);

        videoView.start();

        videoView.requestFocus();

        mainLayout.addView(videoView);

    }

}
