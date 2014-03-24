package com.astudnicka.subwaystations.activity;

import android.content.Context;
import android.media.AudioManager;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.google.android.glass.media.Sounds;

import butterknife.InjectView;
//import butterknife.Optional;
import com.astudnicka.subwaystations.R;
import com.astudnicka.subwaystations.util.BaseAsyncTask;
import com.astudnicka.subwaystations.view.ProgressBar;

/**
 * Base activity which handles showing progress.
 *
 * @author David 'Destil' Vavra (david@vavra.me)
 */
public abstract class ProgressActivity extends BaseActivity {

    private static final int GRACE_PERIOD_DURATION = 4; // in seconds

    @InjectView(R.id.progress_bar)
    ProgressBar vProgressBar;
    @InjectView(R.id.progress_text)
    TextView vProgressText;

    private int gracePeriodResourceId;
    private GracePeriodListener gracePeriodListener;
    private BaseAsyncTask graceTask;
    private boolean mGraceGone = false;
    protected boolean mMenuItemSelected = false;

    @Override
    public void onStop() {
        interruptGrace();
        super.onStop();
    }

    @Override
    protected void onTap() {
        if (!mGraceGone) {
            interruptGrace();
            super.onTap(); // opens option menu
        }
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);
        if (!mMenuItemSelected && !mGraceGone) {
            restartGrace();
        }
    }

    protected void restartGrace() {
        if (graceTask != null) {
            showGracePeriod(gracePeriodResourceId, gracePeriodListener);
        }
    }

    private void interruptGrace() {
        if (graceTask != null) {
            graceTask.cancel(true);
            vProgressBar.stopProgress();
            vProgressBar.setVisibility(View.GONE);
            releaseWakeLock();
        }
    }

    protected void showProgress(int resourceId) {
        vProgressText.setText(resourceId);
        vProgressBar.setVisibility(View.VISIBLE);
        vProgressBar.startIndeterminate();
        vProgressText.setVisibility(View.VISIBLE);
        acquireWakeLock();
    }

    protected void showSuccess(int resourceId) {
        vProgressText.setText(resourceId);
        vProgressBar.setVisibility(View.GONE);
        vProgressText.setVisibility(View.VISIBLE);
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audio.playSoundEffect(Sounds.SUCCESS);
        releaseWakeLock();
    }

    protected void hideProgress() {
        vProgressBar.setVisibility(View.GONE);
        vProgressText.setVisibility(View.GONE);
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audio.playSoundEffect(Sounds.SUCCESS);
        releaseWakeLock();
    }

    protected void showError(int resourceId) {
        vProgressText.setText(resourceId);
        vProgressBar.setVisibility(View.GONE);
        vProgressText.setVisibility(View.VISIBLE);
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audio.playSoundEffect(Sounds.ERROR);
        releaseWakeLock();
    }

    protected void showGracePeriod(int resourceId, final GracePeriodListener listener) {
        gracePeriodResourceId = resourceId;
        gracePeriodListener = listener;
        vProgressText.setText(resourceId);
        vProgressBar.startProgress(GRACE_PERIOD_DURATION * 1000);
        vProgressBar.setVisibility(View.VISIBLE);
        vProgressText.setVisibility(View.VISIBLE);
        mGraceGone = false;
        mMenuItemSelected = false;
        acquireWakeLock();
        graceTask = new BaseAsyncTask() {

            @Override
            public void inBackground() {
                try {
                    Thread.sleep(GRACE_PERIOD_DURATION * 1000);
                } catch (InterruptedException e) {
                    // ignore
                }
            }

            @Override
            public void postExecute() {
                releaseWakeLock();
                mGraceGone = true;
                listener.onGracePeriodCompleted();
            }
        };
        graceTask.start();
    }

    protected interface GracePeriodListener {
        void onGracePeriodCompleted();
    }
}
