package interdroid.swan.swanmonitor;

import interdroid.swancore.swanmain.ExpressionManager;
import interdroid.swancore.swanmain.SensorInfo;
import interdroid.swancore.swanmain.SwanException;
import interdroid.swancore.swanmain.ValueExpressionListener;
import interdroid.swancore.swansong.ExpressionFactory;
import interdroid.swancore.swansong.ExpressionParseException;
import interdroid.swancore.swansong.TimestampedValue;
import interdroid.swancore.swansong.ValueExpression;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class MainActivity extends Activity {
	List<SensorInfo> swanSensorList;
	ArrayList<SensorObject> activeSensors;
	SensorObject tempSensor;
	ListView sensorDataListView;
	ActionMode.Callback mCallback;
	ActionMode mMode;
	private int lastLongClicked;
	private boolean recording;
	// GPS
	GPSTrackExporter gpsFile = null;

	SharedPreferences prefs;

	// STATICS
	static final int SETTINGS_REQUESTCODE = 905;
	private static final int SENSOR_NOT_IN_LIST = 123456789;
	private static final String TAG = "SwanMonitor_MAIN";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initializeVariables();
		setupListView();
		createContextActionBar();
		createSwanMonitorDirectory();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
	}

	/**
	 * Creates the SwanMonitor directory (used for storing sensor exports) if it
	 * doesn't exit yet.
	 */
	private void createSwanMonitorDirectory() {
		File direct = new File(Environment.getExternalStorageDirectory()
				+ "/SwanMonitor");

		if (!direct.exists()) {
			if (direct.mkdir()) {
				Toast.makeText(this,
						"SwanMonitor directory created on SD Card.",
						Toast.LENGTH_SHORT).show();
			}

		}
	}

	/**
	 * Creates and handles the context action bar that is displayed when a
	 * sensor is long clicked
	 */
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
				mode.setTitle(activeSensors.get(lastLongClicked).getName());
				getMenuInflater().inflate(R.menu.longclick_context_menu, menu);
				return true;
			}

			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				switch (item.getItemId()) {
				case R.id.context_export_options:
					editExportOptions(lastLongClicked);
					mode.finish();
					break;
				case R.id.context_delete:
					Toast.makeText(
							getBaseContext(),
							activeSensors.get(lastLongClicked).getName()
									+ getResources().getString(
											R.string.sensorRemoved),
							Toast.LENGTH_SHORT).show();
					deleteSensorfromList(lastLongClicked);
					mode.finish();
					break;
				case R.id.context_edit:
					unregisterSensor(lastLongClicked);
					startActivityForResult(
							swanSensorList.get(
									activeSensors.get(lastLongClicked)
											.getSensorId())
									.getConfigurationIntent(),
							Integer.parseInt(activeSensors.get(lastLongClicked)
									.getRequestCode()));
					mode.finish();
					break;
				}
				return false;
			}
		};
	}

	/**
	 * Call to edit the export options for a given sensor
	 * 
	 * @param sensorPosition
	 *            the position of the sensor that you want to edit the export
	 *            options of.
	 */
	private void editExportOptions(final int sensorPosition) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getResources().getString(R.string.exportDialogTitle))
				.setItems(R.array.export_options_array,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								SensorObject sensor = activeSensors
										.get(sensorPosition);
								switch (which) {
								case 0:
									// Do not export
									sensor.setExportToFile(false);
									sensor.setExportToServer(false);
									updateListViewAdapter();
									break;
								case 1:
									// Export to file only (default)
									sensor.setExportToFile(true);
									sensor.setExportToServer(false);
									updateListViewAdapter();
									break;
								case 2:
									// Export to server only
									sensor.setExportToFile(false);
									sensor.setExportToServer(true);
									updateListViewAdapter();
									break;
								case 3:
									// export to file and server
									sensor.setExportToFile(true);
									sensor.setExportToServer(true);
									updateListViewAdapter();
									break;

								default:
									break;
								}
							}
						});
		builder.create();
		builder.show();
	}

	/**
	 * Initializes required variables
	 */
	private void initializeVariables() {
		recording = false;
		// Create empty sensorDetails list
		activeSensors = new ArrayList<SensorObject>();
		// Get available sensors from swan framework
		swanSensorList = ExpressionManager.getSensors(MainActivity.this);
	}

	/**
	 * Sets up the list view including the on(long)clicklisteners
	 */
	private void setupListView() {
		// Set up default listview
		sensorDataListView = (ListView) findViewById(R.id.listV_main);
		sensorDataListView.setEmptyView(findViewById(R.id.sensor_root_empty));
		sensorDataListView.setAdapter(new SensorListBaseAdapter(this,
				activeSensors));

		sensorDataListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> a, View v, int position,
					long id) {
				SensorObject currentSensor = (SensorObject) sensorDataListView
						.getItemAtPosition(position);
				String toastMessage = String.format(
						getResources().getString(R.string.holdSensorToEdit),
						currentSensor.getName());
				Toast.makeText(getApplicationContext(), toastMessage,
						Toast.LENGTH_SHORT).show();
			}
		});

		sensorDataListView
				.setOnItemLongClickListener(new OnItemLongClickListener() {

					@Override
					public boolean onItemLongClick(AdapterView<?> arg0,
							View arg1, int arg2, long arg3) {
						// Set lastLongClicked variable, used to know which
						// sensor (in
						// displayed_sensors_list) has to be edited.
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.default_actionbar, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.add_sensor:
			createAndShowSensorDialog();
			break;
		case R.id.start_exporting:
			if (activeSensors.isEmpty()) {
				Toast.makeText(getBaseContext(),
						getResources().getString(R.string.addSensorFirst),
						Toast.LENGTH_LONG).show();
				break;
			}
			if (recording) {
				item.setTitle(getResources().getString(R.string.record));
				item.setIcon(R.drawable.rec_icon);
				stopRecording();

			} else {
				item.setTitle(getResources().getString(R.string.stop));
				item.setIcon(R.drawable.ic_media_stop);
				startRecording();
			}
			break;
		case R.id.check_register:
			checkRegisteredSensors();
			break;
		case R.id.exports:
			startFileBrowser();
			break;
		case R.id.settings:
			settings();
			break;
		case R.id.about:
			about();
			break;
		default:
			break;
		}

		return true;
	}

	/**
	 * Call to start recording sensor data
	 */
	private void startRecording() {
		Toast.makeText(this,
				getResources().getString(R.string.startedRecording),
				Toast.LENGTH_SHORT).show();
		recording = true;
		setTitle(getResources().getString(R.string.recording));
	}

	/**
	 * Call to stop recording sensor data
	 */
	private void stopRecording() {
		Toast.makeText(this,
				getResources().getString(R.string.stoppedRecording),
				Toast.LENGTH_SHORT).show();
		recording = false;
		closeAllExportingFiles();
		setTitle(getResources().getString(R.string.app_name));
		if (gpsFile != null) {
			gpsFile.closeFile();
			gpsFile = null;
		}
	}

	/**
	 * Displays the about dialog
	 */
	private void about() {
		AboutDialog about = new AboutDialog(this);
		about.setTitle(getResources().getString(R.string.about));
		about.show();
	}

	/**
	 * Starts the settings activity
	 */
	private void settings() {
		Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
		MainActivity.this
				.startActivityForResult(myIntent, SETTINGS_REQUESTCODE);
	}

	/**
	 * Starts the file browser
	 */
	private void startFileBrowser() {
		Intent myIntent = new Intent(MainActivity.this,
				FileBrowseActivity.class);
		MainActivity.this.startActivity(myIntent);
	}

	/**
	 * Creates a sensorDetails object that stores all data for a sensor then
	 * adds this object to the list with displayed sensors
	 * 
	 * @param item
	 *            the sensor that should be added to the list.
	 */
	private void addItemToList(int item) {

		// Create new Object to store all sensor info
		tempSensor = new SensorObject();
		// Set name of sensor
		tempSensor.setName(swanSensorList.get(item).toString());
		// Set initial string for readings
		tempSensor.setReadings(getResources().getString(R.string.noReading));
		// Set sensorId, used to call startActivityForResult
		tempSensor.setSensorId(item);
		// Add drawable from Swan Framework to Sensor details.
		tempSensor.setIcon(swanSensorList.get(item).getIcon());

		// Store the corresponding units
		ArrayList<String> units = swanSensorList.get(item).getUnits();
		tempSensor.setUnits(units);

		// Check if valuepath "status_text" is available and set flag
		tempSensor.setHasStatusTextVP(swanSensorList.get(item).getValuePaths()
				.contains("status_text"));

		// Set exported (default Swan behaviour)
		tempSensor.setExportToFile(false);
		tempSensor.setExportToServer(false);

		// Start the sensor configuration
		startActivityForResult(swanSensorList.get(item)
				.getConfigurationIntent(), Integer.parseInt(tempSensor
				.getRequestCode()));
	}

	/**
	 * Deletes sensor from list and unregisters its expression.
	 * 
	 * @param item
	 *            position of the sensor that should be deleted from the list
	 */
	private void deleteSensorfromList(int item) {
		unregisterSensor(item);
		activeSensors.remove(item);
		sensorDataListView.setAdapter(new SensorListBaseAdapter(this,
				activeSensors));
	}

	/**
	 * Calls registerSensor() on all sensors in the list.
	 */
	private void registerSensorsWithSwan() {
		System.out.println("from resume...");
		int i = 0;
		while (i < activeSensors.size()) {
			registerSensor(i);
			i++;
		}
	}

	private int getSensorPositionFromRequestCode(String requestCode) {
		for (int i = 0; i < activeSensors.size(); i++) {
			if (activeSensors.get(i).getRequestCode().equals(requestCode)) {
				return i;
			}
		}
		return SENSOR_NOT_IN_LIST;
	}

	/**
	 * Registers the sensors expression with swan
	 * 
	 * @param sensorPosition
	 *            the position of the sensor to be registered.
	 */
	private void registerSensor(final int sensorPosition) {
		System.out.println("register sensor " + sensorPosition);
		if (activeSensors.get(sensorPosition).registeredWithSwan()) {
			System.out.println("Sensor " + sensorPosition
					+ " already registered");
			return;
		}
		try {
			String expression = activeSensors.get(sensorPosition)
					.getExpression();

			Log.e("Roshan","Expression: "+expression);

			Log.e("Roshan","Expression after parsing:"+ExpressionFactory.parse(expression).toParseString());
			String id = activeSensors.get(sensorPosition).getRequestCode();
			ExpressionManager.registerValueExpression(this, id,
					(ValueExpression) ExpressionFactory.parse(expression),
					new ValueExpressionListener() {

						@Override
						public void onNewValues(String id,
								TimestampedValue[] arg1) {
							System.out.println("on new values...");
							if (arg1 != null && arg1.length > 0) {
								// Can not use static sensorPosition because the
								// position might have changed
								// Because another sensor was deleted
								handleReading(arg1[0],
										getSensorPositionFromRequestCode(id));
							} else {
								Log.d(TAG,
										activeSensors
												.get(getSensorPositionFromRequestCode(id))
												.getExpression()
												+ " returned null reading");
							}

						}
					});
			System.out.println("registered!");
			// Mark sensor as successfully registered and update listview
			activeSensors.get(sensorPosition).setRegisteredWithSwan(true);
			updateListViewAdapter();

			if (activeSensors.get(sensorPosition).hasStatusTextVP()
					&& !activeSensors.get(sensorPosition)
							.statusRegisteredWithSwan()) {
				// if status text is available also register this and show
				// update in title bar of sensor
				registerStatusText(sensorPosition);
			}

		} catch (ExpressionParseException e) {
			System.out
					.println("Problem parsing expression (in method registerSensor)");
			e.printStackTrace();
		} catch (SwanException e) {
			System.out.println("Swan Exception (in method registerSensor)");
			activeSensors.get(sensorPosition).setRegisteredWithSwan(false);
			e.printStackTrace();
		}
		System.out.println("done");
	}

	/**
	 * Registers the status text expression with SWAN
	 * 
	 * @param sensorPosition
	 *            the position of the sensor to be (status) registered
	 */
	private void registerStatusText(final int sensorPosition) {
		String statusTextExpression = activeSensors.get(sensorPosition)
				.getExpression();
		statusTextExpression = statusTextExpression.replace(
				SensorTools.parseExpression(statusTextExpression)[1],
				"status_text");
		String statusId = activeSensors.get(sensorPosition)
				.getStatusRequestCode();
		final String id = activeSensors.get(sensorPosition).getRequestCode();

		try {

			ExpressionManager.registerValueExpression(this, statusId,
					(ValueExpression) ExpressionFactory
							.parse(statusTextExpression),
					new ValueExpressionListener() {

						@Override
						public void onNewValues(String arg0,
								TimestampedValue[] arg1) {
							if (arg1 != null && arg1.length > 0) {
								handleStatusReading(arg1[0].getValue()
										.toString(),
										getSensorPositionFromRequestCode(id));
							} else {
								System.out.println(activeSensors.get(
										sensorPosition).getExpression()
										+ " returned null reading");
							}

						}
					});

			activeSensors.get(sensorPosition).setStatusRegisteredWithSwan(true);
		} catch (ExpressionParseException e) {
			System.out
					.println("Problem parsing expression (in method registerStatusText)");
			e.printStackTrace();
		} catch (SwanException e) {
			System.out.println("Swan Exception (in method registerStatusText)");
			activeSensors.get(sensorPosition)
					.setStatusRegisteredWithSwan(false);
			e.printStackTrace();
		}
	}

	/**
	 * Calls unregisterSensor() on all sensors in the list.
	 */
	private void unregisterSensorsWithSwan() {
		int i = 0;
		while (i < activeSensors.size()) {
			unregisterSensor(i);
			i++;
		}
		recording = false;
		setTitle(getResources().getString(R.string.app_name));
	}

	/**
	 * Unregisters the expression of this sensor from SWAN
	 * 
	 * @param sensorPosition
	 *            the position of the sensor that should be unregistered
	 */
	private void unregisterSensor(int sensorPosition) {
		String id = activeSensors.get(sensorPosition).getRequestCode();
		// If status is available for this sensor, unregister that first.
		if (activeSensors.get(sensorPosition).hasStatusTextVP()) {
			unregisterStatusText(sensorPosition);
		}

		if (activeSensors.get(sensorPosition).registeredWithSwan()) {
			ExpressionManager.unregisterExpression(this, id);

			System.out.println("Unregistered sensorId: " + id);
			activeSensors.get(sensorPosition).setRegisteredWithSwan(false);

		} else {
			System.out.println(activeSensors.get(sensorPosition)
					.getExpression() + " was already unregistered");
		}

	}

	/**
	 * Unregisters the sensors status expression from SWAN
	 * 
	 * @param sensorPosition
	 *            the position of the sensor that should be unregistered
	 */
	private void unregisterStatusText(int sensorPosition) {
		String statusId = activeSensors.get(sensorPosition)
				.getStatusRequestCode();
		if (activeSensors.get(sensorPosition).statusRegisteredWithSwan()) {
			ExpressionManager.unregisterExpression(this, statusId);
			activeSensors.get(sensorPosition)
					.setStatusRegisteredWithSwan(false);
		} else {
			Log.e(TAG, activeSensors.get(sensorPosition).getName()
					+ " status sensor was already unregistered");
		}
	}

	/**
	 * Creates the add sensor dialog. If a sensor is selected makes the call to
	 * add it to the sensor list
	 */
	private void createAndShowSensorDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle(getResources().getString(R.string.sensorSelection));
		builder.setAdapter(new SensorSelectSpinnerAdapter(MainActivity.this,
				R.layout.spinner_row, swanSensorList),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						addItemToList(item);
					}
				});

		builder.create().show();
	}

	/**
	 * 
	 * @param reading
	 *            this is the data received by SWAN
	 * @param sensorPosition
	 *            the Position of the sensor that should be updated with this
	 *            data
	 */
	private void handleReading(TimestampedValue reading, int sensorPosition) {
		if (sensorPosition == SENSOR_NOT_IN_LIST) {
			Log.e(TAG, "Error: incorrect sensor Position");
			return;
		}
		SensorObject sensor = activeSensors.get(sensorPosition);
		// Check if the value is a location object
		if (reading == null || reading.getValue() == null) {
			return;
		}
		if (reading.getValue().getClass().equals(Location.class)) {
			String locationReading = handleLocationObject(reading);
			sensor.setReadings(locationReading);
			updateListViewAdapter();
			return;
		}

		String unit = sensor.getCurrentUnit();
		String data = reading.getValue().toString();

		// Check whether the data string should be shortened
		if (data.contains(".") && data.length() > 6
				&& (!((unit.equals("lat") || unit.equals("long"))))) {
			// data = data.substring(0, 6);
		}

		String dataMessage = String.format(
				getResources().getString(R.string.newReadingFormatted), data)
				+ " " + unit;
		sensor.setReadings(dataMessage);

		if (recording) {
			handleExport(reading, sensor);
		}
		updateListViewAdapter();

	}

	/**
	 * Handles a location object to generate a TCX/GPX file
	 * 
	 * @param reading
	 *            TimestampedValue containing a location object
	 */
	private String handleLocationObject(TimestampedValue reading) {
		// Check if user wants gpx/tcx to be generated
		boolean isTCX = getExportFormat();

		if (prefs.getBoolean(
				getResources().getString(R.string.exportTracksKey), true)) {
			if (recording) {
				if (gpsFile == null) {
					gpsFile = new GPSTrackExporter(isTCX);
				}
				Toast.makeText(this, "Location object detected",
						Toast.LENGTH_SHORT).show();
				gpsFile.addTrackPoint((Location) reading.getValue());
				if (isTCX) {
					return getResources().getString(R.string.TCXFileGenerated);
				} else {
					return getResources().getString(R.string.GPXFileGenerated);
				}
			} else {
				// Message when not recording
				return getResources().getString(
						R.string.TrackFileWillBeGenerated);
			}
		}
		// Message when GPX/TCX export disabled in settings.
		return getResources().getString(R.string.TrackFileWillNotBeGenerated);

	}

	/**
	 * Exports sensor data to all required destinations.
	 * 
	 * @param reading
	 * @param sensor
	 */
	private void handleExport(TimestampedValue reading, SensorObject sensor) {
		if (sensor.exportToFile()) {
			sensor.export(reading);
		}
		if (sensor.exportToServer()) {
			try {
				exportToServer(reading, sensor);
			} catch (Exception e) {
				Log.i(TAG, "Failed to output to server");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Exports the sensor reading to the server defined in the settings in JSON
	 * format.
	 * 
	 * @param reading
	 *            the data returned by SWAN
	 * @param sensorPosition
	 *            the position of the corresponding Sensor
	 */
	private void exportToServer(TimestampedValue reading, SensorObject sensor) {
		JSONObject json = buildJSONObject(reading, sensor);
		String serverIP = prefs.getString(
				getResources().getString(R.string.serverAddressKey),
				"127.0.0.1");
		String serverPort = prefs.getString(
				getResources().getString(R.string.serverPortKey), "6789");
		new SendJSONObjectToServer().execute(serverIP, serverPort,
				json.toString());
	}

	/**
	 * Creates a JSON Object that contains the sensor information including the
	 * reading.
	 * 
	 * @param reading
	 *            the sensor data retrieved from SWAN
	 * @param sensorPosition
	 *            the position of the corresponding sensor
	 * @return returns a json object with sensor information
	 */
	private JSONObject buildJSONObject(TimestampedValue reading,
			SensorObject sensor) {
		JSONObject json = new JSONObject();
		try {
			json.put("timestamp", reading.getTimestamp());
			json.put("reading", "" + reading.getValue());
			json.put("sensorName", sensor.getName());
			json.put("unit", sensor.getCurrentUnit());
			String expression = sensor.getExpression();
			json.put("expression", sensor.getExpression());
			json.put("valuePath", SensorTools.parseExpression(expression)[1]);
		} catch (JSONException e) {
			Log.w(TAG, "Failed to create JSONObject");
			e.printStackTrace();
		}
		return json;
	}

	/**
	 * Used to transfer data to server
	 * 
	 * @param String
	 *            ... [server][port][(JSON)String to send]
	 * 
	 */
	private class SendJSONObjectToServer extends
			AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			try {
				String sentence = params[2];
				int port = Integer.parseInt(params[1]);
				Socket clientSocket = new Socket(params[0], port);
				DataOutputStream outToServer = new DataOutputStream(
						clientSocket.getOutputStream());
				outToServer.writeBytes(sentence + '\n');
				outToServer.flush();
				clientSocket.close();
			} catch (UnknownHostException e) {
				Log.i(TAG, "Host could not be resolved");
				e.printStackTrace();
			} catch (IOException e) {
				Log.i(TAG, "IOException when sending JSONObject");
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}
	}

	/**
	 * Displays the status of a sensor
	 * 
	 * @param reading
	 *            this is the data received by SWAN
	 * @param sensorPosition
	 *            the Position of the sensor that should be updated with this
	 *            data
	 */
	private void handleStatusReading(String reading, int sensorPosition) {
		activeSensors.get(sensorPosition).setStatus(reading);
		updateListViewAdapter();
	}

	/**
	 * Checks the expression of the sensor and sets a corresponding unit name
	 * 
	 * @param sensor
	 *            : set the correct unit
	 */
	private void setCorrespondingUnit(SensorObject sensor) {
		String unit = "";
		ArrayList<String> valuePaths = swanSensorList.get(sensor.getSensorId())
				.getValuePaths();

		String valuePath = SensorTools.parseExpression(sensor.getExpression())[1];
		for (int i = 0; i < valuePaths.size(); i++) {
			if (valuePath.equals(valuePaths.get(i).toString())) {
				if(sensor.getUnits().size() > i) {
					unit = sensor.getUnits().get(i);
					break;
				}
			}
		}

		sensor.setCurrentUnit(unit);
	}

	/**
	 * Checks whether sensors have been correctly registered with SWAN if not
	 * will try to register the sensors previously unregistered
	 */
	private void checkRegisteredSensors() {
		int i = 0;
		for (SensorObject s : activeSensors) {
			if (!s.registeredWithSwan()) {
				Log.i(TAG, "Retrying register: " + s.getExpression());
				registerSensor(i);
			}
			if (s.hasStatusTextVP()) {
				if (!s.statusRegisteredWithSwan()) {
					Log.i(TAG, "Retrying status register: " + s.getExpression());
					registerStatusText(i);
				}
			}
			i++;
		}
	}

	/**
	 * Closes all files that are were opened for exporting
	 */
	private void closeAllExportingFiles() {
		int i = 0;
		while (i < activeSensors.size()) {
			if (activeSensors.get(i).exportToFile()) {
				activeSensors.get(i).stopExporting();
			}
			i++;
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == SETTINGS_REQUESTCODE) {
				// User returned from settings activity, update preferences.
				prefs = PreferenceManager.getDefaultSharedPreferences(this);
				return;
			}

			// If temp sensor is not null, this is a newly added sensor, so add
			// it to the active list.
			if (tempSensor != null) {
				activeSensors.add(tempSensor);
				tempSensor = null;
			}
			for (int i = 0; i < activeSensors.size(); i++) {
				if (Integer.parseInt(activeSensors.get(i).getRequestCode()) == requestCode) {
					SensorObject sensor = activeSensors.get(i);
					sensor.setExpression(data.getStringExtra("Expression"));
					setCorrespondingUnit(sensor);

					// sleep(200);

					// Register sensor to start receiving data
					registerSensor(i);
					// Refreshes the listview to show expression
					updateListViewAdapter();
					break;
				}
			}

		}
	}

	/**
	 * Call to refresh data in sensor list
	 */
	private void updateListViewAdapter() {
		((BaseAdapter) sensorDataListView.getAdapter()).notifyDataSetChanged();
	}

	/**
	 * Method used to get info about export format of gps track from sharedprefs
	 * 
	 * @return returns true if TCX is desired, false in case of GPX
	 */
	private boolean getExportFormat() {
		String type = prefs.getString(
				getResources().getString(R.string.trackExportFormatkey), "TCX");
		if (type.equals("TCX")) {
			return true;
		}
		return false;
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onPause() {
		System.out.println("onPause");
		super.onPause();
		unregisterSensorsWithSwan();
	}

	@Override
	protected void onResume() {
		System.out.println("onResume");
		super.onResume();
		registerSensorsWithSwan();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterSensorsWithSwan();

	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exitByBackKey();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	protected void exitByBackKey() {
		String alertMessage = getResources().getString(R.string.exitApp);
		if (recording) {
			alertMessage = alertMessage
					+ getResources().getString(R.string.exitWhileRecording);
		}
		AlertDialog alertbox = new AlertDialog.Builder(this)
				.setMessage(alertMessage)
				.setPositiveButton(getResources().getString(R.string.yes),
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface arg0, int arg1) {

								finish();
								// Unregister all sensors
								unregisterSensorsWithSwan();

								// Stop recording if necessary
								if (recording) {
									stopRecording();
								}

							}
						})
				.setNegativeButton(getResources().getString(R.string.no),
						new DialogInterface.OnClickListener() {

							// do something when the button is clicked
							public void onClick(DialogInterface arg0, int arg1) {
							}
						}).show();

	}

}