package com.in.den.android.othello;

import android.app.Application;
import othello.Engine.CommandInterface;

/**
 * Created by harumi on 07/01/2017.
 */

public class OthelloApplication extends Application {

    private MyCommandInterface mCommandInterface;

    @Override
    public void onCreate() {
       super.onCreate();

        mCommandInterface = new MyCommandInterface();
    }

    public MyCommandInterface getCommandInterface() {
        return mCommandInterface;
    }
}
