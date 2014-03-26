package com.astudnicka.subwaystations.activity;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.view.Menu;
import android.view.MenuItem;

import com.astudnicka.subwaystations.R;
import com.astudnicka.subwaystations.adapter.StationsAdapter;
import com.astudnicka.subwaystations.model.Station;
import com.astudnicka.subwaystations.model.Stations;
import com.astudnicka.subwaystations.util.IntentUtils;
import com.astudnicka.subwaystations.util.LocationUtils;
import com.astudnicka.subwaystations.util.MathUtils;

class StationsComparator implements Comparator<Station> {
	  @Override
	  public int compare(Station x, Station y) {
	        final Location ll = LocationUtils.getLastLocation();
	        float distance1 = MathUtils.getDistance(x.getLatitude(), x.getLongitude(), ll.getLatitude(), ll.getLongitude());
	        float distance2 = MathUtils.getDistance(y.getLatitude(), y.getLongitude(), ll.getLatitude(), ll.getLongitude());
	        return distance1 < distance2 ? -1
	   	         : distance1 > distance2 ? 1
	   	         : 0;
	  }

}

/**
 * Activity with list of venues.
 *
 * @author Alex Studnicka (alex@studnicka.me)
 */
public class StationsActivity extends CardScrollActivity {

    public static final String EXTRA_QUERY = "query";
    public static final String EXTRA_TYPE = "type";
    public static final int TYPE_EXPLORE = 0;
    public static final int TYPE_SEARCH = 1;
    private Stations mStations;
    private Station mSelectedStation;

    public static void call(Activity activity, int type, String query) {
        Intent intent = new Intent(activity, StationsActivity.class);
        intent.putExtra(EXTRA_TYPE, type);
        intent.putExtra(EXTRA_QUERY, query);
        activity.startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.station, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mSelectedStation != null) {

        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mSelectedStation != null) {
            switch (item.getItemId()) {
                case R.id.menu_navigate:
                    IntentUtils.launchNavigation(this, mSelectedStation.getLatitude(), mSelectedStation.getLongitude(), "w");
                    return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void loadData() {
    	
    	mStations = new Stations(this);
    	
        showProgress(R.string.loading);
        
        LocationUtils.getRecentLocation(new LocationUtils.LocationListener() {
        	
	        @Override
	        public void onLocationAcquired(Location location) {
	        	
	            List<Station> stationsList = mStations.getNearbyLandmarks(location);
	            if (stationsList.size() > 0) {
	            	
	            	StationsComparator comparator = new StationsComparator();
	            	Collections.sort(stationsList, comparator);
	            	
	                showContent(new StationsAdapter(stationsList), new CardSelectedListener() {
	                    @Override
	                    public void onCardSelected(Object item) {
	                        mSelectedStation = (Station) item;
	                        openOptionsMenu();
	                    }
	                });
	            } else {
	            	showError(R.string.no_stations_nearby);
	            }
	        	
	        }

			@Override
			public void onLocationFailed() {
				showError(R.string.no_location);
			}
	        
        });

    }

}
