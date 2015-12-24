package interdroid.swan.swanmonitor;

import java.io.File;
import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class FileBrowseActivity extends Activity {

	private ListView filenamesListView;
	private ActionMode.Callback mCallback;
	private ActionMode mMode;
	private int lastLongClicked;
	private File[] filelist;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		setupFileListView();
		createContextActionBar();
	}

	private void setupFileListView() {
		// Retrieve files in directory
		File dir = new File(Environment.getExternalStorageDirectory().toString() + "/SwanMonitor");
		filelist = dir.listFiles();
		ArrayList<String> filenameList = new ArrayList<String>();
		for (int i = 0; i < filelist.length; i++) {
			filenameList.add(filelist[i].getName());
		}

		// Set up default listview
		filenamesListView = (ListView) findViewById(R.id.listV_main);
		filenamesListView.setEmptyView(findViewById(R.id.file_root_empty));
		filenamesListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, filenameList));
		
		
		setUpListeners();
	}


	/**
	 * Sets up (long) click listeners for file browser
	 */
	private void setUpListeners() {
		filenamesListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {

				openAsTextFile(position);

			}
		});

		filenamesListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				lastLongClicked = arg2;
				if (mMode != null) {
					return false;
				} else {
					mMode = startActionMode(mCallback);
				}
				return true;
			}
		});
	}
	
	/**
	 * Opens the file as text/plain in device default editor
	 * @param position The file position that should be opened
	 */
	private void openAsTextFile(int position) {
		Intent intent = new Intent(Intent.ACTION_EDIT);
		Uri uri = Uri.parse("file://" + filelist[position].getAbsolutePath());
		intent.setDataAndType(uri, "text/plain");
		startActivity(intent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// go to previous activity when application icon in action bar is clicked
			Intent intent = new Intent(this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void createContextActionBar() {
		mCallback = new ActionMode.Callback() {

			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				return false;
			}

			@Override
			public void onDestroyActionMode(ActionMode mode) {
				mMode = null;
			}

			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				mode.setTitle(filelist[lastLongClicked].getName());
				getMenuInflater().inflate(R.menu.filebrowse_longclick_context_menu, menu);
				return true;
			}

			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				switch (item.getItemId()) {
				case R.id.context_delete:
					if (filelist[lastLongClicked].delete()) {
						Toast.makeText(getApplicationContext(), "File deleted", Toast.LENGTH_SHORT).show();
						setupFileListView();
					} else {
						Toast.makeText(getApplicationContext(), "Can't delete file", Toast.LENGTH_SHORT).show();
					}
					mode.finish();
					break;
				}
				return false;
			}
		};
	}

}
