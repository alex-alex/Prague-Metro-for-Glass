package com.astudnicka.subwaystations.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;

public class HttpGetTask extends AsyncTask<Void, Void, ArrayList<String>> {

	private String fromStation;
	private String toStation;
	private AsyncTaskCompletionHandler listener;
	
	public HttpGetTask (String fromStation, String toStation, AsyncTaskCompletionHandler listener) {
		this.fromStation = fromStation;
		this.toStation = toStation;
		this.listener = listener;
	}
	
	@Override
	protected ArrayList<String> doInBackground(Void... arg0) {
		
		String timeStr = new SimpleDateFormat("HH:mm", Locale.US).format(Calendar.getInstance().getTime());
		
        HttpClient client = new DefaultHttpClient();
        String url = "http://jizdnirady.idnes.cz/praha/spojeni/?f="+this.fromStation+"&t="+this.toStation+"&time="+timeStr+"&fc=301003&tc=301003&trt=302&direct=false&byarr=false&submit=true&af=true&lng=C";
        HttpGet httpget = new HttpGet(url);
		
		String serverStr = null;
        try {
			HttpResponse response = client.execute(httpget);
			serverStr = EntityUtils.toString(response.getEntity());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        Pattern pattern = Pattern.compile("<th class.*?time.*?>(.*?)</th>");
        Matcher matcher = pattern.matcher(serverStr);
        
        ArrayList<String> departures = new ArrayList<String>();  
        while (matcher.find()) {
        	departures.add(matcher.group(1));
        }
        
		return departures;
	}
	
	@Override
	protected void onPostExecute(ArrayList<String> result) {
		listener.resultCallback(result);
		super.onPostExecute(result);
	}

}
