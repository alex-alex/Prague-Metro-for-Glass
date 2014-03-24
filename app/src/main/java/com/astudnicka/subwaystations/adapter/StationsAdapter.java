package com.astudnicka.subwaystations.adapter;

import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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

    public static int MAX_IMAGE_WIDTH = 213;
    public static int MAX_IMAGE_HEIGHT = 360;
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
        final ViewHolder holder;
        if (view == null) {
            view = LayoutInflater.from(App.get()).inflate(R.layout.view_station, viewGroup, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        Station station = mStations.get(i);
        
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
        holder.name.setText("(" + lineName + ") " + station.getName());
        holder.distance.setText(String.format("%.1f km", distance));
        Picasso.with(App.get()).load(mapURLStringBuilder.toString()).resize(MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT).centerCrop().placeholder(R.drawable.ic_venue_placeholder).into(holder.image);
        
    	String fromStation = station.getName();
    	fromStation = Normalizer.normalize(fromStation, Form.NFD);
    	fromStation = fromStation.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    	fromStation = fromStation.replaceAll(" ", "+");
    	final String firstStation, lastStation;
    	final String nextStation, prevStation;
        
        Stations stations = new Stations(App.get());
        Line line = stations.getLineNamed(lineName);
        ArrayList<String> stationNames = line.getStations();

        firstStation = stationNames.get(0);
        lastStation = stationNames.get(stationNames.size()-1);
        
        int idx = stationNames.indexOf(station.getName());
        if (idx >= stationNames.size()-1) {
        	prevStation = stationNames.get(idx-1);
        	nextStation = null;
        } else if (idx <= 0) {
        	prevStation = null;
        	nextStation = stationNames.get(idx+1);
        } else {
        	prevStation = stationNames.get(idx-1);
        	nextStation = stationNames.get(idx+1);
        }

        if (prevStation != null) {

        	holder.category.setText("> "+firstStation+": ...");
        	
        	String toStation = Normalizer.normalize(prevStation, Form.NFD);
        	toStation = toStation.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        	toStation = toStation.replaceAll(" ", "+");
        	
            try {
                HttpGetTask getTask = new HttpGetTask(fromStation, toStation, new AsyncTaskCompletionHandler() {
    				@Override
    				public void resultCallback(ArrayList<String> departures) {
    					if (departures.size() >= 1) {
        					double minutes = (double)TimeUtils.secondsDiff(Calendar.getInstance().getTime(), TimeUtils.GetItemDate(departures.get(0)));
        					String unit;
        					if (minutes > 60) {
        						minutes = minutes/60.0;
        						unit = "min";
        					} else {
        						unit = "sec";
        					}
        					holder.category.setText(String.format("> %s: %.2f %s", firstStation, minutes, unit));
    					} else {
        					holder.category.setText(String.format("> %s: nejede", firstStation));
    					}
    				}
    			});
                getTask.execute();
    		} catch (Exception e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
        	
        } else {
        	holder.category.setText("> "+firstStation+": konečná");
        }
        
        if (nextStation != null) {
        	
        	holder.hours.setText("> "+lastStation+": ...");

        	String toStation = Normalizer.normalize(nextStation, Form.NFD);
        	toStation = toStation.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        	toStation = toStation.replaceAll(" ", "+");
        	
            try {
                HttpGetTask getTask = new HttpGetTask(fromStation, toStation, new AsyncTaskCompletionHandler() {
    				@Override
    				public void resultCallback(ArrayList<String> departures) {
    					if (departures.size() >= 1) {
    						double minutes = (double)TimeUtils.secondsDiff(Calendar.getInstance().getTime(), TimeUtils.GetItemDate(departures.get(0)));
        					String unit;
        					if (minutes > 60) {
        						minutes = minutes/60.0;
        						unit = "min";
        					} else {
        						unit = "sec";
        					}
        					holder.hours.setText(String.format("> %s: %.2f %s", lastStation, minutes, unit));
    					} else {
        					holder.category.setText(String.format("> %s: nejede", lastStation));
    					}
    				}
    			});
                getTask.execute();
    		} catch (Exception e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
        	
        } else {
        	holder.hours.setText("> "+lastStation+": konečná");
        }

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

    static class ViewHolder {
        @InjectView(R.id.name)
        TextView name;
        @InjectView(R.id.category)
        TextView category;
        @InjectView(R.id.hours)
        TextView hours;
        @InjectView(R.id.distance)
        TextView distance;
        @InjectView(R.id.image)
        ImageView image;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
