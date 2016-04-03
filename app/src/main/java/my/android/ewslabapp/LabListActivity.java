package my.android.ewslabapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

public class LabListActivity extends Activity implements AsyncLabLoadedListener<List<ComputerLab>> {

	private ListView list;
	private SwipeRefreshLayout mSwipeRefreshLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		list = (ListView) findViewById(R.id.lab_list_view);
		mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.green, R.color.yellow, R.color.red);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				loadLabs();
			}
		});

		loadLabs();
		reminderToastAboutWidget();
	}
	
	private void loadLabs() {
		if(isOnline()) {
			AsyncLoadLabs task = new AsyncLoadLabs(getApplicationContext(), this);
			task.execute();
		}
		else {
			Toast.makeText(getApplicationContext(), "No internet connection found.", Toast.LENGTH_LONG).show();
			mSwipeRefreshLayout.setRefreshing(false);
		}
	}

	/** Implements the abstract interface method onLabsLoaded
	 *  Called when the AsyncLoadLabs is done loading the data **/
	public void onLabsLoaded(List<ComputerLab> labsList) {
		if(labsList != null) {
			setListAdapter(new LabListAdapter(LabListActivity.this, labsList));
			mSwipeRefreshLayout.setRefreshing(false);
		}
		else
			Toast.makeText(getApplicationContext(), "Error loading labs.  Please try again.", Toast.LENGTH_LONG).show();
	}

	/** Returns true if the device is online **/
	private boolean isOnline() {
		ConnectivityManager cManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cManager.getActiveNetworkInfo();
		return netInfo != null && netInfo.isConnectedOrConnecting();
	}
	
	private void setListAdapter( LabListAdapter adapter ) {
		list.setAdapter( adapter );
	}

	/** Remind the user to check out the homescreen widget once in a while **/
	private void reminderToastAboutWidget() {
		SharedPreferences widgetFile = this.getSharedPreferences("widget_reminder_file", MODE_PRIVATE);
		SharedPreferences.Editor widgetPrefEditor = widgetFile.edit();
		int totalReminders = widgetFile.getInt("total_reminder_count", 0);
		
		if( totalReminders < 5 ) {	// after a user has been reminded 5 times, stop reminding
			int currCount = widgetFile.getInt("current_count", 0);
			if( currCount % 10 == 0) {
				 Toast.makeText(getApplicationContext(), "Try the home screen widget!", Toast.LENGTH_LONG).show();
				 widgetPrefEditor.remove("total_reminder_count").putInt("total_reminder_count", totalReminders + 1);
			}
			 widgetPrefEditor.remove("current_count").putInt("current_count", (currCount + 1) % 10 );
		}

		widgetPrefEditor.apply();
	}
}