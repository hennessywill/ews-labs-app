/** Abstract class that can be used to listen for when the AsyncTask finishes
 *  Use onLabLoaded(T result) to use the ArrayList with retrieved data to populate UI
 */

package my.android.ewslabapp;

public interface AsyncLabLoadedListener<T> {
	
	public void onLabsLoaded(T result);
	
}