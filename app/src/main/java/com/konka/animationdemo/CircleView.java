package com.konka.animationdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * TODO: document your custom view class.
 */
public class CircleView extends View {
    private int index;
    private int width;
    private int height;
    private int degree=0;
    private final static int startColor = 0x00EEEEEE;                  //渐变色的起始颜色
    private final static int endColor = 0xAAEEEEEE;                    //渐变色的终止颜色
    private final static int circleSpaceWidth = 38;                    //圆球的透明部分宽度
    private final static int circleEdgeWidth = 3;                      //圆球的边缘宽度

    private float distance ;
    private float radius;
    private final static float sqrt2 = 1.414213562f;

    private final static double forwardAcceleratedRatio = 0.001;     //圆球开始向上旋转的加速度
    private final static double bounceAcceleratedRatio = 0.002;      //圆球反弹时旋转加速度
    private final static double backwardAcceleratedRatio = 0.002;    //圆球向下时旋转加速度
    private final static double startSpeed = 0.015;                  //圆球开始旋转的速度
    private final static double bounceSpeed = 0.04;                  //圆球碰撞之后的起始速度
    private final static double returnSpeed = 0.0225;                //圆球反向旋转的起始速度
    private final static double startRadian = 0.0;                   //圆球开始旋转的夹角
    private double acceleratedValue = 0;
    private double currentRadian = startRadian;
    private double endRadian;

    private Path path = new Path();

    private Paint paint = new Paint();

    private RectF rectF;

    private String state = "normal";

    private Drawable mDrawable;

    public static boolean flag = true;

    public CircleView( Context context, Drawable drawable, double endRadian, int index) {

        super(context);

        this.mDrawable = drawable;

        this.endRadian = endRadian;

        this.index = index;

        width = View.MeasureSpec.makeMeasureSpec( 0, MeasureSpec.UNSPECIFIED);

        height = View.MeasureSpec.makeMeasureSpec( 0, MeasureSpec.UNSPECIFIED);

        measure( width, height);

        width = getMeasuredWidth();

        height = getMeasuredHeight();

        radius = ( width - 2 * circleSpaceWidth - 2 * circleEdgeWidth) / 2.0f;

        distance = radius * sqrt2;

        paint.setAntiAlias(true);

        paint.setStyle(Paint.Style.FILL);

        int left = circleSpaceWidth + circleEdgeWidth;

        float top = left + 2 * radius;

        rectF = new RectF( left, left, top, top);

    }

    public void initAcceleratedValue() {

        this.acceleratedValue = 0;

    }

    public void setDegree(int degree){

        this.degree = degree;

        invalidate();

    }

    public double getStartRadian() {

        return startRadian;

    }

    public double getEndRadian() {

        return endRadian;

    }

    public double getCurrentRadian() {

        return currentRadian;

    }

    public void setState(String state) {

        this.state = state;

    }

    public String getState() {

        return state;

    }

    public void setDrawable(Drawable drawable) {

        mDrawable = drawable;

        invalidate();

    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        if (mDrawable != null) {

            mDrawable.setBounds( 0, 0, width, height);

            mDrawable.draw(canvas);

        }

        if(AnimationManager.state.equals("end")){

            LinearGradient lg;

            float x = (float) (width / 2 - radius * Math.cos(degree * Math.PI / 360) / sqrt2);

            if(flag){

                lg = new LinearGradient( x - distance, x - distance, x, x, startColor, endColor, Shader.TileMode.MIRROR);

                path.arcTo( rectF, 225 - degree / 2, degree);

            }

            else{

                lg =new LinearGradient( x, x, x + distance, x + distance, startColor, endColor, Shader.TileMode.MIRROR);

                path.arcTo( rectF, 225 + degree / 2, 359 - degree);

            }

            paint.setShader(lg);

            canvas.drawPath( path, paint);

            path.reset();

        }

    }

    public void setCircleCenter(float x,float y){

        setX( x - width / 2);

        setY( y - height / 2);

    }

    public void changeRadian(){

        switch (AnimationManager.state){

            case "forward":

                if(state.equals("normal")){

                    currentRadian += startSpeed + acceleratedValue;

                    acceleratedValue += forwardAcceleratedRatio;

                    if(index == 0 && currentRadian > endRadian){

                        currentRadian = endRadian;
                    }

                }

                else{

                    currentRadian -= bounceSpeed - acceleratedValue;

                    if(bounceSpeed - acceleratedValue > 0.008){

                        acceleratedValue += bounceAcceleratedRatio;

                    }

                    if(currentRadian < endRadian){

                        currentRadian = endRadian;

                    }

                }

                break;

            case "backward":

                currentRadian -= returnSpeed + acceleratedValue;

                acceleratedValue += backwardAcceleratedRatio;

                if(currentRadian < startRadian){

                    currentRadian = startRadian;

                }

                break;

        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){

        setMeasuredDimension(mDrawable.getIntrinsicWidth(), mDrawable.getIntrinsicHeight());

    }

}
