package my.android.ewslabapp;

import android.content.Context;
import android.os.AsyncTask;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AsyncLoadLabs extends AsyncTask<String, Void, Integer> {

	private List<ComputerLab> labsList;
	private static final int JSON_SUCCESS = 0x0;
	private static final int JSON_FAIL = 0x1;
	private static final String JSON_URL = "https://my.engr.illinois.edu/labtrack/util_data_json.asp";
	private AsyncLabLoadedListener<List<ComputerLab>> callback;
	
	/** Constructor that allows context parameter **/
	public AsyncLoadLabs(Context context, AsyncLabLoadedListener<List<ComputerLab>> callback) {
		this.callback = callback;
		labsList = new ArrayList<ComputerLab>();
	}
	
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}
	
	
	@Override
	protected Integer doInBackground(String... params) {
		try {
			URL source = new URL(JSON_URL);
			BufferedReader in = new BufferedReader(new InputStreamReader(source.openStream()));
			String inputLine = in.readLine();
			in.close();

			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(inputLine);
			JSONArray array =  (JSONArray) json.get("data");

			for( int i = 0; i < array.size() ; i++) {
				JSONObject curr = (JSONObject) array.get(i);

				String labName = formatLabName((String) curr.get("strlabname"));
				int computersInUse = ((Long) curr.get("inusecount")).intValue();
				int totalComputers = ((Long) curr.get("machinecount")).intValue();

                if (!labName.equals("skip"))
				    labsList.add(new ComputerLab(labName, computersInUse, totalComputers));
			}
			
		} catch( Exception e ) {
			e.printStackTrace();
			return JSON_FAIL;
		}
		return JSON_SUCCESS;
	}
	
	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		
		// call the listener that will populate the UI
		if(result == JSON_FAIL)
			labsList = null;
		callback.onLabsLoaded(labsList);
	}
	
	
	/**
	 *  Translates the given lab name into a more reader-friendly version.
	 *  EX)  "SIEBL 0220" -> "Siebel 0220"
	 *  The lab name will be returned in its original form if it doesn't fit any of the expected 'if' cases
	 */
	private String formatLabName(String orig) {
		String[] split_string = orig.split(" ");
		String labName = split_string[0];
        String labNumber = "";
        if (split_string.length >= 2)
		    labNumber = split_string[1];

        if (labName.equalsIgnoreCase("ESPL") || labName.equalsIgnoreCase("ESB")
                || labName.equalsIgnoreCase("FAR") || labName.equalsIgnoreCase("PAR")
                || labName.equalsIgnoreCase("REC") || labName.equalsIgnoreCase("SDRP")
				|| labNumber.equalsIgnoreCase("L416"))
            return "skip";

		if(labName.equalsIgnoreCase("EH"))
			labName = "Engineering Hall";
		else if(labName.equalsIgnoreCase("EVRT"))
			labName = "Everitt Lab";
		else if(labName.equalsIgnoreCase("GELIB")) {
            labName = "Grainger";
            if (labNumber.equalsIgnoreCase("4th"))
                labNumber = "4th Floor";
        }
		else if(labName.equalsIgnoreCase("MEL"))
			labName = "Mech-E Lab";
		else if(labName.equalsIgnoreCase("SIEBL"))
			labName = "Siebel";
        else if (labName.equalsIgnoreCase("TB"))
            labName = "Transportation";
		
		return labName + " " + labNumber;
	}
}