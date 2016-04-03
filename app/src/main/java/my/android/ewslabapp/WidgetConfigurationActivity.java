package my.android.ewslabapp;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class WidgetConfigurationActivity extends Activity implements AsyncLabLoadedListener<List<ComputerLab>> {

	private List<ComputerLab> labsList;
	private ProgressBar progress;
	private Context context;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_widget_configuration);
		progress = (ProgressBar) findViewById(R.id.configuration_progress);
		context = getApplicationContext();
		loadLabs();
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_favorites, menu);
		return true;
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch( item.getItemId() ) {
		case R.id.menu_ok:
			RadioGroup rgroup = (RadioGroup) findViewById(R.id.configuration_radiogroup);
			int favoriteLabIndex = rgroup.getCheckedRadioButtonId();
			
			SharedPreferences favoritePref = this.getSharedPreferences("favorite_lab", MODE_PRIVATE);
			SharedPreferences.Editor favoritesEditor = favoritePref.edit();
			favoritesEditor.remove("FAVORITE_LAB_INDEX");
			favoritesEditor.putInt("FAVORITE_LAB_INDEX", favoriteLabIndex);
			favoritesEditor.apply();

			Intent intent = getIntent();
			Bundle extras = intent.getExtras();
			int mAppWidgetId = 0;
			if(extras != null) {
				mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
			}
			
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			
			RemoteViews views = createWidgetRemoteViews(favoriteLabIndex);
			appWidgetManager.updateAppWidget(mAppWidgetId, views);
			
			Intent resultValue = new Intent();
			resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
			setResult(RESULT_OK, resultValue);
			
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	
	/** Returns true if the device is online **/
	private boolean isOnline() {
		ConnectivityManager cManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cManager.getActiveNetworkInfo();
		return netInfo != null && netInfo.isConnectedOrConnecting();
	}
	
	
	/** Call to the server, retrieve the JSON, parse it, and put the data into the ArrayList **/
	void loadLabs() {
		progress.setVisibility(View.VISIBLE);
		if( isOnline() ) {
			AsyncLoadLabs task = new AsyncLoadLabs(context, this);
			task.execute();
		}
		else {
			progress.setVisibility(View.GONE);
			Toast.makeText(context, "No internet connection found.", Toast.LENGTH_LONG).show();
			Intent resultValue = new Intent();
			setResult(RESULT_CANCELED, resultValue);
			finish();
		}
	}


	@Override
	public void onLabsLoaded(List<ComputerLab> result) {
		progress.setVisibility(View.GONE);
		if(result != null) {
			labsList = result;
			RadioGroup rgroup = (RadioGroup) findViewById(R.id.configuration_radiogroup);
			RadioButton rb;
			for(int i = 0; i < labsList.size(); i++) {
				rb = new RadioButton(context);
				rb.setText(labsList.get(i).getLabName());
				rb.setTextSize(24);
				rb.setTextColor(Color.BLACK);
				rb.setId(i);
				rb.setPadding(0,0,0,20);
				rgroup.addView(rb);
			}
			rgroup.check(0);
		}
		else {
			Toast.makeText(context, "Error loading labs.  Please try again.", Toast.LENGTH_LONG).show();
			Intent resultValue = new Intent();
			setResult(RESULT_CANCELED, resultValue);
			finish();
		}
	}
	
	
	private RemoteViews createWidgetRemoteViews(int favoriteLabIndex) {
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
		SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());	// 8:17 am
		String hour = sdf.format(c.getTime());
		
		String updateTime = day + " " + hour;
		view.setTextViewText(R.id.widget_updatetime, "Last Updated:  " + updateTime);
		
		return view;
	}
}
