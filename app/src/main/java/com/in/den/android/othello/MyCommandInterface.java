package com.in.den.android.othello;

import android.graphics.Color;

import othello.Engine.CommandInterface;

/**
 * Created by harumi on 07/01/2017.
 */

public class MyCommandInterface extends CommandInterface {

    public static final int mJetonWhiteColor = Color.WHITE;
    public static final int mJetonBlackColor = Color.BLACK;
    private int mPlayerColor;

    public MyCommandInterface() {
        mPlayerColor = mJetonBlackColor;
    }

    public void setBlack2Player() {
        mPlayerColor = mJetonBlackColor;
    }

    public void setWhite2Player() {
        mPlayerColor = mJetonWhiteColor;
    }

    public int getPlayerColor() {
        return mPlayerColor;
    }
}
