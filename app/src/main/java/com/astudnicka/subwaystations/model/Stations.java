package com.astudnicka.subwaystations.model;

import com.astudnicka.subwaystations.R;
import com.astudnicka.subwaystations.util.MathUtils;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides access to a list of hard-coded landmarks (located in
 * {@code res/raw/landmarks.json}) that will appear on the compass when the user is near them.
 */
public class Stations {

    private static final String TAG = Stations.class.getSimpleName();

    /**
     * The threshold used to display a landmark on the compass.
     */
    private static final double MAX_DISTANCE_KM = 500;

    /**
     * The list of landmarks loaded from resources.
     */
    private final ArrayList<Station> mPlaces;
    private final ArrayList<Line> mLines;

    /**
     * Initializes a new {@code Landmarks} object by loading the landmarks from the resource
     * bundle.
     */
    public Stations(Context context) {
        mPlaces = new ArrayList<Station>();
        mLines = new ArrayList<Line>();

        // This class will be instantiated on the service's main thread, and doing I/O on the
        // main thread can be dangerous if it will block for a noticeable amount of time. In
        // this case, we assume that the landmark data will be small enough that there is not
        // a significant penalty to the application. If the landmark data were much larger,
        // we may want to load it in the background instead.
        String jsonString = readLandmarksResource(context);
        populatePlaceList(jsonString);
    }
    
    public ArrayList<Line> getLines() {
    	return mLines;
    }
    
    public Line getLineNamed(String lineName) {
    	for (Line line : mLines) {
    		if (line.getName().equalsIgnoreCase(lineName)) {
    			return line;
    		}
    	}
		return null;
    }

    /**
     * Gets a list of landmarks that are within ten kilometers of the specified coordinates. This
     * function will never return null; if there are no locations within that threshold, then an
     * empty list will be returned.
     */
    public List<Station> getNearbyLandmarks(Location location) {
        ArrayList<Station> nearbyPlaces = new ArrayList<Station>();

        for (Station knownPlace : mPlaces) {
            if (MathUtils.getDistance(location.getLatitude(), location.getLongitude(),
                    knownPlace.getLatitude(), knownPlace.getLongitude()) <= MAX_DISTANCE_KM) {
                nearbyPlaces.add(knownPlace);
            }
        }

        return nearbyPlaces;
    }

    /**
     * Populates the internal places list from places found in a JSON string. This string should
     * contain a root object with a "landmarks" property that is an array of objects that represent
     * places. A place has three properties: name, latitude, and longitude.
     */
    private void populatePlaceList(String jsonString) {
        try {
            JSONObject json = new JSONObject(jsonString);
            JSONArray array = json.optJSONArray("stations");

            if (array != null) {
                for (int i = 0; i < array.length(); i++) {
                    JSONObject object = array.optJSONObject(i);
                	//JSONArray object = array.optJSONArray(i);
                    Station place = jsonObjectToPlace(object);
                    if (place != null) {
                        mPlaces.add(place);
                    }
                }
            }
            
            JSONObject linesJson = json.optJSONObject("lines");
            if (linesJson != null) {
            	for (int i = 0; i < linesJson.names().length(); i++) {
            		String name = (String)linesJson.names().get(i);
            		JSONArray lineStationsJson = linesJson.optJSONArray(name);
            		if (lineStationsJson != null) {
            			ArrayList<String> lineStations = new ArrayList<String>();
            			for (int j = 0; j < lineStationsJson.length(); j++) {
            				lineStations.add(lineStationsJson.get(j).toString());
            			}
            			mLines.add(new Line(name, lineStations));
            		}
            	}
            }
            
        } catch (JSONException e) {
            Log.e(TAG, "Could not parse landmarks JSON string", e);
        }
    }

    /**
     * Converts a JSON object that represents a place into a {@link Place} object.
     */
    private Station jsonObjectToPlace(JSONObject object) {
        String name = object.optString("name");
        double latitude = object.optDouble("latitude", Double.NaN);
        double longitude = object.optDouble("longitude", Double.NaN);
        String line = object.optString("line");
        JSONArray jsonEntrances = object.optJSONArray("entrances");
        
        ArrayList<Entrance> entrances = new ArrayList<Entrance>();     
        if (jsonEntrances != null) { 
        	for (int i=0;i<jsonEntrances.length();i++){ 
        		try {
        			JSONObject jsonEntrance = jsonEntrances.getJSONObject(i);
        	        String entranceName = jsonEntrance.optString("name");
        	        double entranceLatitude = jsonEntrance.optDouble("latitude", Double.NaN);
        	        double entranceLongitude = jsonEntrance.optDouble("longitude", Double.NaN);
        			Entrance entrance = new Entrance(entranceLatitude, entranceLongitude, entranceName);
        			entrances.add(entrance);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        }
        
        if (!name.isEmpty() && !Double.isNaN(latitude) && !Double.isNaN(longitude)) {
            return new Station(latitude, longitude, name, line, entrances);
        } else {
            return null;
        }
    }

    /**
     * Reads the text from {@code res/raw/landmarks.json} and returns it as a string.
     */
    private static String readLandmarksResource(Context context) {
        InputStream is = context.getResources().openRawResource(R.raw.stations);
        StringBuffer buffer = new StringBuffer();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
                buffer.append('\n');
            }
        } catch (IOException e) {
            Log.e(TAG, "Could not read landmarks resource", e);
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e(TAG, "Could not close landmarks resource stream", e);
                }
            }
        }

        return buffer.toString();
    }
}
