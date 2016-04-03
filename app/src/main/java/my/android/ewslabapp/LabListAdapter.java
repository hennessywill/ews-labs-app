package my.android.ewslabapp;

import android.app.Activity;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

public class LabListAdapter extends ArrayAdapter<ComputerLab> {

	List<ComputerLab> labsList = null;
	
	public LabListAdapter(Activity activity, List<ComputerLab> labsList) {
		super(activity, 0, labsList);
		this.labsList = labsList;
	}
	
	
	@Override
	public View getView(int position, View convertView, ViewGroup Parent) {
		Activity activity = (Activity) getContext();
		LayoutInflater inflater = activity.getLayoutInflater();
		View rowView = inflater.inflate(R.layout.lab_list_item, null);
		
		String labTitle = labsList.get(position).getLabName();
		int computersInUse = labsList.get(position).getComputersInUse();
		int totalComputers = labsList.get(position).getTotalComputers();
		int availableComputers = totalComputers - computersInUse;
		
		TextView textView = (TextView) rowView.findViewById(R.id.lab_item_room);
		textView.setText( labTitle );
		
		TextView availabilityView = (TextView) rowView.findViewById(R.id.lab_item_ratio);
		availabilityView.setText( availableComputers + " / " + totalComputers );
		
		setProgressBar( rowView, availableComputers, totalComputers );
		
		return rowView;
	}
	
	
	/** Sets the progress bar to the appropriate capacity and fill color **/
	public void setProgressBar( View rowView, int availableComputers, int totalComputers ) {
		ProgressBar pb = (ProgressBar) rowView.findViewById(R.id.lab_item_progressbar);

		int percent = availableComputers * 100 / totalComputers;
		
		Activity context = (Activity) getContext();
		Resources res = context.getResources();
		
		if(percent > 50) {	// green > 50%
			pb.setProgressDrawable(res.getDrawable(R.drawable.green_progress_bar));
		}
		else if(percent > 20) {	// yellow > 20%
			pb.setProgressDrawable(res.getDrawable(R.drawable.yellow_progress_bar));
		}
		else {	// red
			pb.setProgressDrawable(res.getDrawable(R.drawable.red_progress_bar));
		}
		
		pb.setProgress(availableComputers);
		pb.setMax(totalComputers);
	}
	
}
