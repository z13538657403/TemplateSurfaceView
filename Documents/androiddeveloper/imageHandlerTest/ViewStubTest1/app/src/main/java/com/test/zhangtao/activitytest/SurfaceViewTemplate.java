package com.test.zhangtao.activitytest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by zhangtao on 16/12/26.
 */

public class SurfaceViewTemplate extends SurfaceView implements SurfaceHolder.Callback , Runnable
{
    private SurfaceHolder mHolder;
    private Canvas mCanvas;
    //绘图的子线程
    private Thread t;
    //线程的控制开关
    private boolean isRunning;

    //盘块的奖项
    private String[] mStrs = new String[]{"单反相机" , "iPad" , "恭喜发财" , "IPHONE" , "服装一套" , "恭喜发财"};

    //文字的大小
    private float mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP , 20 , getResources().getDisplayMetrics());

    //绘制文本的画笔
    private Paint mTextPaint;

    //奖项的图片
    private int[] mImgs = new int[]{R.drawable.danfan , R.drawable.ipad , R.drawable.f040 ,
            R.drawable.iphone , R.drawable.meizi , R.drawable.f015};

    //与图片对应的Bitmap数组
    private Bitmap[] mImgsBitmap;

    //转盘的背景图
    private Bitmap mBgBitmap = BitmapFactory.decodeResource(getResources() , R.drawable.bg2);

    //盘块的颜色
    private int[] mColors = new int[]{0xFFFFC300 , 0xFFF17E01 , 0xFFFFC300 , 0xFFF17E01 , 0xFFFFC300 , 0xFFF17E01};

    private int mItemCount = 6;

    //盘块的范围
    private RectF mRange = new RectF();

    //转盘的中心位置
    private int mCenter;

    //这里的padding直接以paddingLeft为准
    private int mPadding;

    //整个盘块的直径
    private int mRadius;

    //绘制盘块的画笔
    private Paint mArcPaint;

    //盘块滚动的速度
    private double mSpeed = 0;

    //起始角度
    private volatile int mStartAngle = 0;

    //判断是否点击了停止按钮
    private boolean isShouldEnd;


    public SurfaceViewTemplate(Context context)
    {
        this(context , null);
    }

    public SurfaceViewTemplate(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        mHolder = getHolder();
        mHolder.addCallback(this);
        //设置可以获得焦点
        setFocusable(true);
        setFocusableInTouchMode(true);
        //设置常量
        setKeepScreenOn(true);
    }

    //强制将转盘的大小设置为正方形
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = Math.min(getMeasuredWidth() , getMeasuredHeight());

        mPadding = getPaddingLeft();
        mRadius = width - mPadding * 2;
        mCenter = width / 2;

        setMeasuredDimension(width , width);
    }

    @Override
    public void run()
    {
        //不断进行绘制
        while (isRunning)
        {
            long start = System.currentTimeMillis();
            drawPic();
            long end = System.currentTimeMillis();
            if (end - start < 50)
            {
                try
                {
                    Thread.sleep(50 - (end - start));
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private void drawPic()
    {
        try
        {
            mCanvas = mHolder.lockCanvas();
            if (mCanvas != null)
            {
                //draw something
                drawBg();

                float tmpAngle = mStartAngle;
                float sweepAngle = 360 / mItemCount;

                for (int i = 0 ; i < mItemCount ; i++)
                {
                    mArcPaint.setColor(mColors[i]);
                    mCanvas.drawArc(mRange , tmpAngle , sweepAngle , true , mArcPaint);

                    //绘制文本
                    drawText(tmpAngle , sweepAngle , mStrs[i]);

                    //绘制抽奖图片
                    drawIcon(tmpAngle , mImgsBitmap[i]);

                    tmpAngle += sweepAngle;
                }
                mStartAngle += mSpeed;

                //如果点击了停止按钮
                if (isShouldEnd)
                {
                    mSpeed -= 1;
                }

                if (mSpeed == 0)
                {
                    mSpeed = 0;
                    isShouldEnd = false;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (mCanvas != null)
            {
                mHolder.unlockCanvasAndPost(mCanvas);
            }
        }
    }

    private void drawIcon(float tmpAngle, Bitmap bitmap)
    {
        //图片的宽度为直接的1/8
        int imgWidth = mRadius / 8;

        float angle = (float) ((tmpAngle + 360 / mItemCount / 2) * Math.PI / 180);
        int x = (int) (mCenter + mRadius / 3 * Math.cos(angle));
        int y = (int) (mCenter + mRadius / 3 * Math.sin(angle));

        //确定图片的位置
        Rect rect = new Rect(x - imgWidth / 2 , y - imgWidth / 2 , x + imgWidth / 2, y + imgWidth / 2);
        mCanvas.drawBitmap(bitmap , null , rect , null);
    }

    private void drawText(float tmpAngle, float sweepAngle, String mStr)
    {
        Path path = new Path();
        path.addArc(mRange , tmpAngle , sweepAngle);

        float textWidth = mTextPaint.measureText(mStr);
        //利用水平偏移量让文字居中
        int hOffset = (int) (mRadius * Math.PI / mItemCount / 2 - textWidth / 2);
        int vOffset = mRadius / 2 / 6;
        mCanvas.drawTextOnPath(mStr , path , hOffset , vOffset , mTextPaint);
    }

    private void drawBg()
    {
        mCanvas.drawColor(0xffffff);
        mCanvas.drawRect(mPadding / 2 , mPadding / 2 ,
                getMeasuredWidth() - mPadding / 2 , getMeasuredHeight() - mPadding / 2 , mArcPaint);
        mCanvas.drawBitmap(mBgBitmap , null , new Rect(mPadding / 2 , mPadding / 2 ,
                getMeasuredWidth() - mPadding / 2 , getMeasuredHeight() - mPadding / 2) , null);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        //初始化盘块绘制画笔
        mArcPaint = new Paint();
        mArcPaint.setAntiAlias(true);
        mArcPaint.setDither(true);
        mArcPaint.setColor(0xffffffff);

        //初始化文字绘制画笔
        mTextPaint = new Paint();
        mTextPaint.setColor(0xffffffff);
        mTextPaint.setTextSize(mTextSize);

        //初始话盘块的绘制的范围
        mRange = new RectF(mPadding , mPadding , mPadding + mRadius , mPadding + mRadius);

        //初始化图片
        mImgsBitmap = new Bitmap[mItemCount];

        for (int i = 0 ; i < mItemCount ; i++)
        {
            mImgsBitmap[i] = BitmapFactory.decodeResource(getResources() , mImgs[i]);
        }

        isRunning = true;
        t = new Thread(this);
        t.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        isRunning = false;
    }

    public void luckyStart()
    {
        mSpeed = 20;
        isShouldEnd = false;
    }

    public void luckyEnd()
    {
        isShouldEnd = true;
    }

    public boolean luckyIsRunning()
    {
        return mSpeed != 0;
    }

    public boolean isShouldEnd()
    {
        return isShouldEnd;
    }
}
