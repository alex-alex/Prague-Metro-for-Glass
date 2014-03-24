package com.astudnicka.subwaystations.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.MotionEvent;
import android.view.View;

//import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

import butterknife.ButterKnife;
//import cz.destil.glasquare.BuildConfig;
//import cz.destil.glasquare.api.Auth;

/**
 * Base activity which handles common stuff like analytics.
 *
 * @author David 'Destil' Vavra (david@vavra.me)
 */
abstract public class BaseActivity extends Activity {

    private GestureDetector mGestureDetector;
    private PowerManager.WakeLock wakeLock;

    /**
     * Override in children.
     */
    protected int getLayoutId() {
        return -1;
    }

    /**
     * Override in children.
     */
    protected View getLayoutView() {
        return null;
    }

    /**
     * Override in children
     */
    protected void loadData() {

    }

    /**
     * You can override this in child. Default implemention is to open options menu.
     */
    protected void onTap() {
        openOptionsMenu();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean hasUi = false;
        if (getLayoutId() == -1) {
            if (getLayoutView() != null) {
                setContentView(getLayoutView());
                hasUi = true;
            }
        } else {
            setContentView(getLayoutId());
            hasUi = true;
        }
        if (hasUi) {
            ButterKnife.inject(this);
            loadData();
            mGestureDetector = createGestureDetector(this);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        
    }

    @Override
    public void onStop() {
        super.onStop();
        
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /*if (requestCode == LoginActivity.REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Auth.saveToken(data.getStringExtra(LoginActivity.EXTRA_TOKEN));
                loadData();
            } else {
                finish();
            }
        }*/
    }

    private GestureDetector createGestureDetector(Context context) {
        GestureDetector gestureDetector = new GestureDetector(context);
        gestureDetector.setBaseListener(new GestureDetector.BaseListener() {
            @Override
            public boolean onGesture(Gesture gesture) {
                if (gesture == Gesture.TAP) {
                    onTap();
                    return true;
                }
                return false;
            }
        });
        return gestureDetector;
    }

    /*
     * Send generic motion events to the gesture detector
     */
    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (mGestureDetector != null) {
            return mGestureDetector.onMotionEvent(event);
        }
        return false;
    }

    public void acquireWakeLock() {
        if (wakeLock == null) {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "subwaystations");
            wakeLock.acquire();
        }
    }

    public void releaseWakeLock() {
        if (wakeLock != null) {
            if (wakeLock.isHeld()) {
                wakeLock.release();
                wakeLock = null;
            }
        }
    }
}
