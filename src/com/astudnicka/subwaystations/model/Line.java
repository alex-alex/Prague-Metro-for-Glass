package com.astudnicka.subwaystations.model;

import java.util.ArrayList;

public class Line {
	
	private final String mName;
	private final ArrayList<String> mStations;
	
    /**
     * Initializes a new line with the specified stations.
     *
     * @param stations of the line
     */
    public Line(String name, ArrayList<String> stations) {
    	mName = name;
        mStations = stations;
    }

    /**
     * Gets the name of the line.
     *
     * @return the name of the line
     */
    public String getName() {
        return mName;
    }
    
    /**
     * Gets stations of the line.
     *
     * @return stations of the line
     */
    public ArrayList<String> getStations() {
        return mStations;
    }

}
