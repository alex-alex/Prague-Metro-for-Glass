package com.astudnicka.subwaystations.adapter;

import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.astudnicka.subwaystations.view.StationViewHolder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.squareup.picasso.Picasso;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
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
import com.astudnicka.subwaystations.util.TimeUtils;

/**
 * Adapter for list of venues.
 *
 * @author David 'Destil' Vavra (david@vavra.me)
 */
public class StationsAdapter extends CardScrollAdapter {

    private List<Station> mStations;

    public StationsAdapter(List<Station> stations) {
    	mStations = stations;
    }

    @Override
    public int getCount() {
        return mStations.size();
    }

    @Override
    public Object getItem(int i) {
        return mStations.get(i);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final StationViewHolder holder;
        if (view == null) {
            view = LayoutInflater.from(App.get()).inflate(R.layout.view_station, viewGroup, false);
            holder = new StationViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (StationViewHolder) view.getTag();
        }

        Station station = mStations.get(i);
        holder.setStation(station);

        return view;
    }

    @Override
    public int findIdPosition(Object o) {
        return -1;
    }

    @Override
    public int findItemPosition(Object o) {
        return mStations.indexOf(o);
    }

}
