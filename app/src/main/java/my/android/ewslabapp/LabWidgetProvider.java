package my.android.ewslabapp;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class LabWidgetProvider extends AppWidgetProvider implements AsyncLabLoadedListener<List<ComputerLab>> {

	private Context context;
	private AppWidgetManager manager;
	private int[] appWidgetIds;
	
	@Override
	public void onUpdate(Context myContext, AppWidgetManager myManager, int[] myAppWidgetIds) {
		context = myContext;
		manager = myManager;
		appWidgetIds = myAppWidgetIds;
		loadLabs();
	}

	
	/** Call to the server, retrieve the JSON, parse it, and put the data into the ArrayList **/
	void loadLabs() {
		try {
			AsyncLoadLabs task = new AsyncLoadLabs(context, this);
			task.execute();	// waits for the AsyncTask to finish
		} catch(Exception e) {
			e.printStackTrace();
		}
	}


	/** Called by the AsyncLoadLabs task when it finishes **/
	@Override
	public void onLabsLoaded(List<ComputerLab> labsList) {
		if( labsList == null ) {
			RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.widget_greenbar_layout);
			view.setTextViewText(R.id.widget_item_room, "A network error occured.  Please try again.");
			return;
		}
		
		
		SharedPreferences favoritePref = context.getSharedPreferences("favorite_lab", Activity.MODE_PRIVATE);
		int favoriteLabIndex = favoritePref.getInt("FAVORITE_LAB_INDEX", 0);
		if(favoriteLabIndex < 0 || favoriteLabIndex >= labsList.size()) {
			favoriteLabIndex = 0;
		}
		
		String labName = labsList.get(favoriteLabIndex).getLabName();
		int totalComputers = labsList.get(favoriteLabIndex).getTotalComputers();
		int availableComputers = totalComputers - labsList.get(favoriteLabIndex).getComputersInUse();
		int percent = availableComputers * 100 / totalComputers;
		
		RemoteViews view = null;
		if( percent > 50 ) {	// green > 50%
			view = new RemoteViews(context.getPackageName(), R.layout.widget_greenbar_layout);
			view.setProgressBar(R.id.widget_item_greenprogressbar, totalComputers, availableComputers, false);
		}
		else if( percent > 20 ) {	// yellow > 20%
			view = new RemoteViews(context.getPackageName(), R.layout.widget_yellowbar_layout);
			view.setProgressBar(R.id.widget_item_yellowprogressbar, totalComputers, availableComputers, false);
		}
		else {	// red
			view = new RemoteViews(context.getPackageName(), R.layout.widget_redbar_layout);
			view.setProgressBar(R.id.widget_item_redprogressbar, totalComputers, availableComputers, false);
		}
		
		view.setTextViewText(R.id.widget_item_room, labName);
		view.setTextViewText(R.id.widget_item_ratio, availableComputers + " / " + totalComputers);
		
		Calendar c = Calendar.getInstance(TimeZone.getDefault());
		String day = c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
		SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
		String hour = sdf.format(c.getTime());
		
		String updateTime = day + " " + hour;
		view.setTextViewText(R.id.widget_updatetime, "Last Updated:  " + updateTime);
		
		int appWidgetId = appWidgetIds[0];
		manager.updateAppWidget(appWidgetId, view);
	}
}