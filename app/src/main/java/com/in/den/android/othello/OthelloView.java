package com.in.den.android.othello;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.app.AlertDialog;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

import othello.Engine.CommandInterfaceListener;
import othello.Engine.Move;
import othello.Engine.Score;

/**
 * Created by harumi on 04/01/2017.
 */

public class OthelloView extends View implements CommandInterfaceListener {

    private static final float mBordExtSize = 12F;
    private static final float mSpaceCircle = 6F;
    private static final float mBordIntLineSize = 3F;
    private static final float paddingBorder = 20f;
    private static final float mDashbordDesiredHeight = 200f;
    private static final float mDashbordDesiredWidth = 200f;

    private float mTextScoreSize = 60F;
    private float mSpaceDashBord = 200f;
    private int mGameBordColor = Color.parseColor("#21a540");
    private int mBordExtColor = Color.parseColor("#041c09");
    private int mBordIntColor = Color.BLACK;
    private int mScoreColor = Color.RED;
    private int mBGColor = Color.WHITE;

    private Context mContext;
    private MyCommandInterface mCommandInterface;
    private float mGameBordSize;
    private float mCaseSize;
    private float mDiffWidthHeight;
    private float mCaseSizeHalf;
    private float mDashbordWidth;
    private float mDashbordHeight;
    private float mSizeGameBordPaddingBorder;
    private float mSizeGameBordPaddingBorderSpaceDashBord;
    private boolean bPortrait;
    private Paint mPaintBordExt;
    private Paint mPaintBordInt;
    private Paint mPaintSimple;
    private Paint bordPaint;
    private Bitmap mBufferedImage;
    private Bitmap mBitmapRefresh;
    private Drawable mDrawableJetonBis;
    private Drawable mDrawableJeton;
    private TextPaint mPaintScore;
    private Region mDashBordRegion;
    private Region mGameBordRegion;

    public OthelloView(Context context) {
        this(context, null);
    }

    public OthelloView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        OthelloApplication othelloApplication =
                (OthelloApplication)((Activity)mContext).getApplication();
        mCommandInterface = othelloApplication.getCommandInterface();

        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs){
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OthelloView, 0, 0);
        mBGColor = a.getColor(R.styleable.OthelloView_bgColor, Color.WHITE);
        mScoreColor = a.getColor(R.styleable.OthelloView_scoreColor, Color.RED);
        mTextScoreSize = a.getInt(R.styleable.OthelloView_scoreSize, 60);
        mSpaceDashBord = a.getDimensionPixelSize(R.styleable.OthelloView_spaceDashBord, 250);
        a.recycle();


        DisplayMetrics metrics = new DisplayMetrics();
        Display display = ((WindowManager) context.getSystemService(context.WINDOW_SERVICE))
                .getDefaultDisplay();
        display.getMetrics(metrics);
        mBufferedImage = Bitmap.createBitmap(metrics.widthPixels, metrics.heightPixels,
                Bitmap.Config.ARGB_8888);

        mPaintBordExt = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintBordExt.setStrokeWidth(mBordExtSize);
        mPaintBordExt.setColor(mBordExtColor);
        mPaintBordExt.setStyle(Paint.Style.STROKE);

        mPaintBordInt = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintBordInt.setStrokeWidth(mBordIntLineSize);
        mPaintBordInt.setColor(mBordIntColor);

        mPaintScore = new TextPaint(Paint.ANTI_ALIAS_FLAG);

        mPaintScore.setTextSize(mTextScoreSize);
        mPaintScore.setColor(mScoreColor);

        mPaintSimple = new Paint();

        bordPaint = new Paint();
        bordPaint.setColor(mGameBordColor);

        mDrawableJeton = getResources().getDrawable(R.drawable.shape_oval);

        mDrawableJetonBis = getResources().getDrawable(R.drawable.shape_oval2);

        mBitmapRefresh = BitmapFactory.decodeResource(
                getResources(), R.drawable.icon_refresh);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w != oldw || h != oldh) {
            setBordPamaters(w, h);
        }
    }

    private void setBordPamaters(int w, int h) {

        if (h >= w) {
            bPortrait = true;
            mGameBordSize = w - (mBordExtSize * 2) - paddingBorder;

            mDashbordWidth = mGameBordSize;
            mDiffWidthHeight = h - w;
            mDashbordHeight = mDiffWidthHeight - mSpaceDashBord - mDashbordDesiredHeight > 0 ?
                    mDashbordDesiredHeight : mDiffWidthHeight - mSpaceDashBord;

        } else {
            bPortrait = false;
            mGameBordSize = h - (mBordExtSize * 2) - paddingBorder;
            mDiffWidthHeight = w - h;

            mDashbordHeight = mGameBordSize;
            mDashbordWidth = mDiffWidthHeight - mSpaceDashBord - mDashbordDesiredWidth > 0 ?
                    mDashbordDesiredWidth : mDiffWidthHeight - mSpaceDashBord;
        }

        mCaseSize = mGameBordSize / 8;
        mCaseSizeHalf = mCaseSize / 2;

        mSizeGameBordPaddingBorder = mGameBordSize + paddingBorder;
        mSizeGameBordPaddingBorderSpaceDashBord = mSizeGameBordPaddingBorder + mSpaceDashBord;


        drawGamebord();
    }

    private void drawGamebord() {
        Canvas canvas = new Canvas();
        canvas.setBitmap(mBufferedImage);

        canvas.drawColor(mBGColor);

        RectF gameBorderBound = new RectF(paddingBorder, paddingBorder,
                mSizeGameBordPaddingBorder, mSizeGameBordPaddingBorder);

        mGameBordRegion = new Region((int)paddingBorder, (int)paddingBorder,
                (int) mSizeGameBordPaddingBorder, (int) mSizeGameBordPaddingBorder);

        canvas.drawRect(gameBorderBound, bordPaint);


        for (int i = 0; i <= 8; i++) {

            Paint p;
            if (i == 0 || i == 8) {
                p = mPaintBordExt;
            } else {
                p = mPaintBordInt;
            }

            float pos = i * mCaseSize + paddingBorder;

            //horizontal line
            canvas.drawLine(paddingBorder - mBordExtSize / 2, pos,
                    mGameBordSize + paddingBorder + mBordExtSize / 2, pos, p);
            //vertical line
            canvas.drawLine(pos, paddingBorder, pos, mGameBordSize + paddingBorder, p);

        }

        if (bPortrait) {

            drawDashBordHorizontal(canvas,
                    paddingBorder,
                    mSizeGameBordPaddingBorderSpaceDashBord);

            mDashBordRegion = new Region((int) paddingBorder,
                    (int) mSizeGameBordPaddingBorderSpaceDashBord,
                    (int) mSizeGameBordPaddingBorder,
                    (int) (mSizeGameBordPaddingBorderSpaceDashBord+ mDashbordHeight));

        } else {

            drawDashBordVertical(canvas, mSizeGameBordPaddingBorderSpaceDashBord, paddingBorder);

            mDashBordRegion = new Region((int) mSizeGameBordPaddingBorderSpaceDashBord,
                    (int) paddingBorder,
                    (int) (mSizeGameBordPaddingBorderSpaceDashBord + mDashbordWidth),
                    (int) mSizeGameBordPaddingBorder);
        }
    }



    private void setScoreHorizontal(Canvas canvas, int scoreBlack, int scoreWhite,
                                    float left, float top) {

        String sScore = scoreBlack + "  :  " + scoreWhite;
        float textWidth = mPaintScore.measureText(sScore, 0, sScore.length());
        Rect rect = new Rect();
        mPaintScore.getTextBounds(sScore, 0, sScore.length(), rect);
        int width = rect.width();
        int height = rect.height();

        float margewidth = (mCaseSize * 2 - width) / 2;
        float margeheight = (mDashbordDesiredHeight - height) / 2;

        setScore(canvas, sScore, textWidth, left + margewidth, top + margeheight);

    }


    private void setScoreVertical(Canvas canvas, int scoreBlack, int scoreWhite,
                                  float left, float top) {

        String sScore = scoreBlack + "\n    \n" + scoreWhite;
        float textWidth = mPaintScore.measureText(sScore, 0, sScore.length());
        Rect rect = new Rect();
        mPaintScore.getTextBounds(sScore, 0, sScore.length(), rect);
        int width = rect.width();
        int height = rect.height();

        float margewidth = (mDashbordDesiredWidth - width) / 2;
        float margeheight = (mCaseSize * 2 - height) / 2;

        setScore(canvas, sScore, textWidth, left + margewidth, top);
    }

    private void setScore(Canvas canvas, String sScore, float textWidth, float left, float top) {
        StaticLayout scorelayout = new StaticLayout(sScore, mPaintScore, (int) textWidth,
                Layout.Alignment.ALIGN_CENTER, 1f, 0f, true);
        canvas.save();
        canvas.translate(left, top);
        scorelayout.draw(canvas);
        canvas.restore();
    }

    private void drawPlayersJetonHorizontal (Canvas canvas, float left, float top) {
        canvas.save();
        canvas.translate(left, top);
        drawJetonDashBord(mCaseSize / 4, mDashbordHeight / 2 - mCaseSizeHalf,
                mCommandInterface.getPlayerColor(), canvas);
        canvas.restore();
    }

    private void drawPlayersJetonVertical (Canvas canvas, float left, float top) {
        canvas.save();
        canvas.translate(left, top);
        drawJetonDashBord(mDashbordWidth / 2 - mCaseSizeHalf, mCaseSize / 4,
                mCommandInterface.getPlayerColor(), canvas);
        canvas.restore();
    }


    private void drawDashBordHorizontal(Canvas canvas, float left, float top) {

        canvas.save();
        canvas.translate(left, top);

        RectF dashBordBound = new RectF(0, 0,
                mGameBordSize, mDashbordHeight
        );

       // drawJeton(mCaseSize / 4, mDashbordHeight / 2 - mCaseSizeHalf, mPlayerColor, canvas);

        canvas.drawLine(mCaseSize + mCaseSizeHalf, 0, mCaseSize + mCaseSizeHalf,
                mDashbordHeight, mPaintBordInt);

        drawJeton(mCaseSize * 2, mDashbordHeight / 2 - mCaseSizeHalf,
                mCommandInterface.mJetonBlackColor, canvas);

        //score here

        drawJetonDashBord(mCaseSize * 5, mDashbordHeight / 2 - mCaseSizeHalf,
                mCommandInterface.mJetonWhiteColor, canvas);

        canvas.drawLine(mCaseSize * 6 + mCaseSizeHalf, 0, mCaseSize * 6 + mCaseSizeHalf,
                mDashbordHeight, mPaintBordInt);

        canvas.drawBitmap(mBitmapRefresh, mCaseSize * 7 - mCaseSize / 4, mCaseSize / 4, mPaintSimple);

        canvas.drawRect(dashBordBound, mPaintBordExt);

        canvas.restore();
    }

    private void drawDashBordVertical(Canvas canvas, float left, float top) {

        canvas.save();
        canvas.translate(left, top);

        RectF dashBordBound = new RectF(0, 0,
                mDashbordWidth, mDashbordHeight
        );

        // here players jeton color

        canvas.drawLine(0, mCaseSize + mCaseSizeHalf, mDashbordWidth, mCaseSize + mCaseSizeHalf,
                mPaintBordInt);

        drawJeton(mDashbordWidth / 2 - mCaseSizeHalf, mCaseSize * 2,
                mCommandInterface.mJetonBlackColor, canvas);

        //score placed here

        drawJetonDashBord(mDashbordWidth / 2 - mCaseSizeHalf, mCaseSize * 5,
                mCommandInterface.mJetonWhiteColor, canvas);

        canvas.drawLine(0, mCaseSize * 6 + mCaseSizeHalf, mDashbordWidth,
                mCaseSize * 6 + mCaseSizeHalf, mPaintBordInt);

        canvas.drawBitmap(mBitmapRefresh, mDashbordWidth / 2 - mCaseSizeHalf, mCaseSize * 7 - mCaseSize / 4, mPaintSimple);

        canvas.drawRect(dashBordBound, mPaintBordExt);

        canvas.restore();
    }

    private int[] getPositionJeton(float x, float y) {

        //Game engine expect the range from 1 to 8

        int xpos = findCase(x) + 1;
        int ypos = findCase(y) + 1;

        int[] pos = new int[]{xpos, ypos};
        return pos;
    }

    private int findCase(float pos) {
        int ifound = -1;
        for (int i = 0; i < 8; i++) {

            if (pos >= mCaseSize * i && pos < mCaseSize * (i + 1)) {
                ifound = i;
                break;
            }
        }

        return ifound;
    }


    private void drawJetonDashBord(float left, float top, int color, Canvas canvas) {

        mDrawableJetonBis.setBounds(getRectJeton(left, top));
        ((GradientDrawable) mDrawableJetonBis).setColor(color);
        if(color == mCommandInterface.mJetonWhiteColor) {
            //setborder
            ((GradientDrawable) mDrawableJetonBis).setStroke(3, mCommandInterface.mJetonBlackColor);
        }
        mDrawableJetonBis.draw(canvas);
    }

    private void drawJeton(float left, float top, int color, Canvas canvas) {

        mDrawableJeton.setBounds(getRectJeton(left, top));
        ((GradientDrawable) mDrawableJeton).setColor(color);
        mDrawableJeton.draw(canvas);
    }



    private Rect getRectJeton(float left, float top) {

        float l = left + mSpaceCircle;
        float t = top + mSpaceCircle;
        float r = left + mCaseSize - mSpaceCircle;
        float b = top + mCaseSize - mSpaceCircle;

        Rect rect = new Rect((int) l, (int) t, (int) r, (int) b);
        return rect;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap(mBufferedImage, 0, 0, mPaintSimple);

        synchronized (mCommandInterface) {

            for (int x = 1; x < 9; x++) {
                for (int y = 1; y < 9; y++) {
                    int coloroccupy = mCommandInterface.GetSquare(x, y);
                    int color;
                    if (coloroccupy == Score.WHITE) {
                        color = Color.WHITE;
                    } else if (coloroccupy == Score.BLACK) {
                        color = Color.BLACK;
                    } else {
                        continue;
                    }

                    drawJeton(mCaseSize * (x - 1) + paddingBorder,
                            mCaseSize * (y - 1) + paddingBorder, color, canvas);

                }
            }
        }

        //score
        if (bPortrait) {

            drawPlayersJetonHorizontal(canvas,   paddingBorder,
                    mSizeGameBordPaddingBorderSpaceDashBord);


            setScoreHorizontal(canvas, mCommandInterface.GetScoreBlack(),
                    mCommandInterface.GetScoreWhite(),
                    paddingBorder + mCaseSize * 3,
                    mSizeGameBordPaddingBorderSpaceDashBord);
        }
        else {

            drawPlayersJetonVertical(canvas,
                    mSizeGameBordPaddingBorderSpaceDashBord, paddingBorder);

            setScoreVertical(canvas, mCommandInterface.GetScoreBlack(),
                    mCommandInterface.GetScoreWhite(),
                    mSizeGameBordPaddingBorderSpaceDashBord,
                    paddingBorder + mCaseSize * 3);

        }

    }


    private void makeMyMove(int xsquare, int ysquare) {
        if (!mCommandInterface.MakeMoveIsPossible(xsquare, ysquare))
            return;

        int player = mCommandInterface.GetWhoseTurn();
        mCommandInterface.MakeMove(xsquare, ysquare);

        invalidate();


        if (mCommandInterface.MoveIsPossible() &&
                mCommandInterface.GetWhoseTurn() != player
               ) {
            //computer's turn

            mCommandInterface.ComputeMove(this);

        }
    }

    private CommandInterfaceListener getCommandInterfaceListener() {
        return this;
    }

    private void restartDialog() {

        final String[] items = {"You start. Black is your side.", "Computer starts. White is your side." };
        int defaultItem = 0;
        final List<Integer> checkedItems = new ArrayList<>();
        checkedItems.add(defaultItem);
        new AlertDialog.Builder(mContext)
                .setTitle("Restart")
                .setSingleChoiceItems(items, defaultItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        checkedItems.clear();
                        checkedItems.add(which);
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mCommandInterface.NewGameIsPossible()) {

                            mCommandInterface.NewGame();
                            if (checkedItems.get(0) == 1) {
                                //chage side asked
                                //computer plays first He takes Black color
                                mCommandInterface.setWhite2Player();
                                mCommandInterface.ComputeMove(getCommandInterfaceListener());
                            }
                            else {
                                mCommandInterface.setBlack2Player();
                            }

                            invalidate();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                float xpos = event.getX();
                float ypos = event.getY();

                if(mGameBordRegion.contains((int)xpos, (int)ypos)) {
                    int[] squarepos = getPositionJeton(xpos, ypos);
                    int xsquare = squarepos[0];
                    int ysquare = squarepos[1];
                    if (xsquare > 0 && ysquare > 0) {
                        makeMyMove(xsquare, ysquare);
                    }
                }
                else if(mDashBordRegion.contains((int)xpos, (int)ypos)) {
                    //lauch refresh dialog
                    restartDialog();
                }
                break;
            }
            default: {

            }
        }
        return true;
    }

    //Override method of CommandInterfaceListner
    @Override
    public void ComputationFinished(Move m) {
        ((Activity) mContext).runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        invalidate();
                    }
                }
        );
    }
}
