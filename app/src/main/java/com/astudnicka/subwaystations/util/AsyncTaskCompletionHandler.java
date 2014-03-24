package com.astudnicka.subwaystations.util;

import java.util.ArrayList;

public interface AsyncTaskCompletionHandler {

	void resultCallback(ArrayList<String> departures);
	
}
