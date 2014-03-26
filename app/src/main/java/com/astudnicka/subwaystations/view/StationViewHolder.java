package com.astudnicka.subwaystations.view;

import android.graphics.Color;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.astudnicka.subwaystations.App;
import com.astudnicka.subwaystations.R;
import com.astudnicka.subwaystations.model.Entrance;
import com.astudnicka.subwaystations.model.Line;
import com.astudnicka.subwaystations.model.Station;
import com.astudnicka.subwaystations.model.Stations;
import com.astudnicka.subwaystations.util.AsyncTaskCompletionHandler;
import com.astudnicka.subwaystations.util.HttpGetTask;
import com.astudnicka.subwaystations.util.LocationUtils;
import com.astudnicka.subwaystations.util.MathUtils;
import com.astudnicka.subwaystations.util.Stopwatch;
import com.astudnicka.subwaystations.util.TimeUtils;
import com.squareup.picasso.Picasso;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Calendar;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Alex on 26/03/14.
 */
public class StationViewHolder {

    public static int MAX_IMAGE_WIDTH = 213;
    public static int MAX_IMAGE_HEIGHT = 360;

    @InjectView(R.id.lineLabel)
    TextView lineLabel;
    @InjectView(R.id.nameLabel)
    TextView nameLabel;
    @InjectView(R.id.prevStationTitleLabel)
    TextView prevStationTitleLabel;
    @InjectView(R.id.prevStationLabel)
    TextView prevStationLabel;
    @InjectView(R.id.nextStationTitleLabel)
    TextView nextStationTitleLabel;
    @InjectView(R.id.nextStationLabel)
    TextView nextStationLabel;
    @InjectView(R.id.distanceLabel)
    TextView distanceLabel;
    @InjectView(R.id.image)
    ImageView image;

    Station mStation;
    String fromStation;
    String prevStation;
    String nextStation;

    ArrayList<String> mPrevDepartures;
    ArrayList<String> mNextDepartures;

    public StationViewHolder(View view) {
        ButterKnife.inject(this, view);
    }

    public void setStation(Station station) {

        mStation = station;

        mPrevDepartures = new ArrayList<String>();
        mNextDepartures = new ArrayList<String>();

        final Location ll = LocationUtils.getLastLocation();
        float distance = MathUtils.getDistance(station.getLatitude(), station.getLongitude(), ll.getLatitude(), ll.getLongitude());

        StringBuilder mapURLStringBuilder = new StringBuilder();

        mapURLStringBuilder.append("https://maps.googleapis.com/maps/api/staticmap?sensor=false&size=213x360&style=feature:all|element:all|saturation:-100|lightness:-25|gamma:0.5|visibility:simplified&style=feature:roads|element:geometry&style=feature:landscape|element:geometry|lightness:-25");

        //&markers=icon:http%3A%2F%2Fmirror-api.appspot.com%2Fglass%2Fimages%2Fmap_dot.png|shadow:false|"+ll.getLatitude()+","+ll.getLongitude()+"

        if (station.getEntrances().size() > 0) {
            for (Entrance entrance : station.getEntrances()) {
                mapURLStringBuilder.append("&markers=color:0xF7594A|"+entrance.getLatitude()+","+entrance.getLongitude());
            }
        } else {
            mapURLStringBuilder.append("&markers=color:0xF7594A|"+station.getLatitude()+","+station.getLongitude());
        }

        String lineName = station.getLine();
        this.lineLabel.setText(lineName);

        if (lineName.equalsIgnoreCase("A")) {
            this.lineLabel.setTextColor(Color.GREEN);
        } else if (lineName.equalsIgnoreCase("B")) {
            this.lineLabel.setTextColor(Color.YELLOW);
        } else if (lineName.equalsIgnoreCase("C")) {
            this.lineLabel.setTextColor(Color.RED);
        } else {
            this.lineLabel.setTextColor(Color.WHITE);
        }

        this.nameLabel.setText(station.getName());

        this.distanceLabel.setText(String.format("%.1f km", distance));

        Picasso.with(App.get()).load(mapURLStringBuilder.toString()).resize(MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT).centerCrop().placeholder(R.drawable.ic_venue_placeholder).into(this.image);

        this.fromStation = station.getName();
        this.fromStation = Normalizer.normalize(this.fromStation, Normalizer.Form.NFD);
        this.fromStation = this.fromStation.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        this.fromStation = this.fromStation.replaceAll(" ", "+");

        Stations stations = new Stations(App.get());
        Line line = stations.getLineNamed(lineName);
        ArrayList<String> stationNames = line.getStations();

        this.prevStationTitleLabel.setText("> " + stationNames.get(0));
        this.nextStationTitleLabel.setText("> " + stationNames.get(stationNames.size()-1));

        int idx = stationNames.indexOf(station.getName());
        if (idx >= stationNames.size()-1) {
            this.prevStation = stationNames.get(idx-1);
            this.nextStation = null;
        } else if (idx <= 0) {
            this.prevStation = null;
            this.nextStation = stationNames.get(idx+1);
        } else {
            this.prevStation = stationNames.get(idx-1);
            this.nextStation = stationNames.get(idx+1);
        }

        if (this.prevStation != null) {

            this.prevStationLabel.setText("...");

            String toStation = Normalizer.normalize(prevStation, Normalizer.Form.NFD);
            toStation = toStation.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
            toStation = toStation.replaceAll(" ", "+");

            try {
                HttpGetTask getTask = new HttpGetTask(fromStation, toStation, new AsyncTaskCompletionHandler() {
                    @Override
                    public void resultCallback(ArrayList<String> departures) {
                        for (String departure : departures) {
                            if (!mPrevDepartures.contains(departure)) {
                                mPrevDepartures.add(departure);
                            }
                        }
                    }
                });
                getTask.execute();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        if (nextStation != null) {

            this.nextStationLabel.setText("...");

            String toStation = Normalizer.normalize(nextStation, Normalizer.Form.NFD);
            toStation = toStation.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
            toStation = toStation.replaceAll(" ", "+");

            try {
                HttpGetTask getTask = new HttpGetTask(fromStation, toStation, new AsyncTaskCompletionHandler() {
                    @Override
                    public void resultCallback(ArrayList<String> departures) {
                        for (String departure : departures) {
                            if (!mNextDepartures.contains(departure)) {
                                mNextDepartures.add(departure);
                            }
                        }
                    }
                });
                getTask.execute();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        mHandler.sendEmptyMessage(MSG_START_TIMER);

    }

    // -----------------

    void updateTimes() {

        if (prevStation != null) {
            if (mPrevDepartures != null && mPrevDepartures.size() >= 1) {
                double seconds = (double) TimeUtils.secondsDiff(Calendar.getInstance().getTime(), TimeUtils.GetItemDate(mPrevDepartures.get(0)));
                if (seconds > 60) {
                    int minutes = (int)Math.floor(seconds / 60.0);
                    this.prevStationLabel.setText(String.format("%d min %d sec", minutes, (int)seconds-(minutes*60)));
                } else {
                    if (seconds <= -30) {
                        this.prevStationLabel.setText("odjel");
                        mPrevDepartures.remove(0);
                    } else if (seconds > -30 && seconds <= 0) {
                        this.prevStationLabel.setText("ve stanici");
                    } else {
                        this.prevStationLabel.setText(String.format("%d sec", (int) seconds));
                    }
                }
            } else {
                this.prevStationLabel.setText("nejede");
            }
        } else {
            this.prevStationLabel.setText("konečná");
        }

        if (nextStation != null) {
            if (mNextDepartures != null && mNextDepartures.size() >= 1) {
                double seconds = (double)TimeUtils.secondsDiff(Calendar.getInstance().getTime(), TimeUtils.GetItemDate(mNextDepartures.get(0)));
                if (seconds > 60) {
                    int minutes = (int)Math.floor(seconds / 60.0);
                    this.nextStationLabel.setText(String.format("%d min %d sec", minutes, (int)seconds-(minutes*60)));
                } else {
                    if (seconds <= -30) {
                        this.nextStationLabel.setText("odjel");
                        mNextDepartures.remove(0);
                    } else if (seconds > -30 && seconds <= 0) {
                        this.nextStationLabel.setText("ve stanici");
                    } else {
                        this.nextStationLabel.setText(String.format("%d sec", (int) seconds));
                    }
                }
            } else {
                this.nextStationLabel.setText("nejede");
            }
        } else {
            this.nextStationLabel.setText("konečná");
        }
    }

    // -----------------

    final int MSG_START_TIMER = 0;
    final int MSG_STOP_TIMER = 1;
    final int MSG_UPDATE_TIMER = 2;

    Stopwatch timer = new Stopwatch();
    final int REFRESH_RATE = 100;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_START_TIMER:
                    timer.start(); //start timer
                    mHandler.sendEmptyMessage(MSG_UPDATE_TIMER);
                    break;

                case MSG_UPDATE_TIMER:
                    StationViewHolder.this.updateTimes();
                    mHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIMER,REFRESH_RATE); //text view is updated every second,
                    break;                                  //though the timer is still running
                case MSG_STOP_TIMER:
                    mHandler.removeMessages(MSG_UPDATE_TIMER); // no more updates.
                    timer.stop();//stop timer
                    StationViewHolder.this.updateTimes();
                    break;

                default:
                    break;
            }
        }
    };

}
