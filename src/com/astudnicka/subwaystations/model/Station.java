package com.astudnicka.subwaystations.model;

import java.util.ArrayList;

/**
 * This class represents a point of interest that has geographical coordinates (latitude and
 * longitude) and a name that is displayed to the user.
 */
public class Station {

    private final double mLatitude;
    private final double mLongitude;
    private final String mName;
    private final String mLine;
    private final ArrayList<Entrance> mEntrances;

    /**
     * Initializes a new place with the specified coordinates and name.
     *
     * @param latitude the latitude of the place
     * @param longitude the longitude of the place
     * @param name the name of the place
     */
    public Station(double latitude, double longitude, String name, String line, ArrayList<Entrance> entrances) {
        mLatitude = latitude;
        mLongitude = longitude;
        mName = name;
        mLine = line;
        mEntrances = entrances;
    }

    /**
     * Gets the latitude of the place.
     *
     * @return the latitude of the place
     */
    public double getLatitude() {
        return mLatitude;
    }

    /**
     * Gets the longitude of the place.
     *
     * @return the longitude of the place
     */
    public double getLongitude() {
        return mLongitude;
    }

    /**
     * Gets the name of the place.
     *
     * @return the name of the place
     */
    public String getName() {
        return mName;
    }

    /**
     * Gets line of station
     *
     * @return line of station
     */
    public String getLine() {
        return mLine;
    }

    /**
     * Gets entrances to station
     *
     * @return array of entrances
     */
    public ArrayList<Entrance> getEntrances() {
        return mEntrances;
    }
    
}
