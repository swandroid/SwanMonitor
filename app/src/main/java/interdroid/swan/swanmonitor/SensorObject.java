package interdroid.swan.swanmonitor;

import interdroid.swan.swansong.TimestampedValue;

import java.util.ArrayList;

import android.graphics.drawable.Drawable;

public class SensorObject {

	static int currentCode = 1234;

	/**
	 * Sets up the Sensor Object with the required default values
	 */
	SensorObject() {
		this.registeredWithSwan = false;
		this.statusRegisteredWithSwan = false;
		this.requestCode = "" + (currentCode++);
	}

	/**
	 * Set the name of sensor
	 * 
	 * @param name
	 *            Set the name of the sensor
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the name of the sensor
	 * 
	 * @return The name of the sensor
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the status of the sensor
	 * 
	 * @param status
	 *            The status of the sensor as a String
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * Returns the status of the sensor
	 * 
	 * @return The status of the sensor
	 */
	public String getStatus() {
		if (status == null) {
			return "";
		}
		return status;
	}

	/**
	 * Returns the name and status of the sensor
	 * 
	 * @return the name and status (between parentheses) of this sensor. (e.g.
	 *         "Battery (not charging)")
	 */
	public String getNameWithStatus() {
		if (getStatus().equals("")) {
			return name;
		} else {
			return name + " (" + status + ")";
		}
	}

	/**
	 * Set the reading (latest value/data) of this sensor
	 * 
	 * @param readings
	 *            A string containing the latest data
	 */
	public void setReadings(String readings) {
		this.readings = readings;
	}

	/**
	 * Get the (latest) readings of this sensor
	 * 
	 * @return The last added reading of this sensor
	 */
	public String getReadings() {
		return readings;
	}

	/**
	 * Set the sensorId of this sensor
	 * 
	 * @param sensorId
	 *            the id of this sensor
	 */
	public void setSensorId(int sensorId) {
		this.sensorId = sensorId;
	}

	/**
	 * Returns the sensorId of this sensor
	 * 
	 * @return the Id of this sensor
	 */
	public int getSensorId() {
		return sensorId;
	}

	/**
	 * Set the expression
	 * 
	 * @param expression
	 *            The expression that will be used to register with SWAN
	 */
	public void setExpression(String expression) {
		this.expression = expression;
	}

	/**
	 * Get the current expression of the sensor
	 * 
	 * @return the expression of the sensor (to register with SWAN)
	 */
	public String getExpression() {
		return expression;
	}

	/**
	 * Returns a unique request code
	 * 
	 * @return A unique request code
	 */
	public String getRequestCode() {
		return requestCode;
	}

	/**
	 * Returns a unique Status request code
	 * 
	 * @return A unique statusRequest code
	 */
	public String getStatusRequestCode() {
		return requestCode + "status";
	}

	/**
	 * Set a Drawable as the icon for this sensor
	 * 
	 * @param icon
	 *            The Drawable icon of this sensor
	 */
	public void setIcon(Drawable icon) {
		this.icon = icon;
	}

	/**
	 * Returns the Drawable icon of this sensor
	 * 
	 * @return A drawable icon of the sensor
	 */
	public Drawable getIcon() {
		return icon;
	}

	/**
	 * Set the units array
	 * 
	 * @param units
	 *            An arrayList<string> of units in the same order as the
	 *            valuePaths
	 */
	public void setUnits(ArrayList<String> units) {
		this.units = units;
	}

	/**
	 * Get a list of all the units for this sensor
	 * 
	 * @return An arrayList of strings with the units for this sensor. Same
	 *         ordering as the valuePaths
	 */
	public ArrayList<String> getUnits() {
		return units;
	}

	/**
	 * Set to true if a "status_text" valuePath is available
	 * 
	 * @param hasStatusTextVP
	 *            set to true if "status_text" valuepath is available
	 */
	public void setHasStatusTextVP(boolean hasStatusTextVP) {
		this.hasStatusTextVP = hasStatusTextVP;
	}

	/**
	 * Used to check if the sensor has a Status_text valuepath
	 * 
	 * @return True if this sensor has a "status_text" valuePath
	 */
	public boolean hasStatusTextVP() {
		return hasStatusTextVP;
	}

	/**
	 * Set to true if expression was succesfully registered to SWAN
	 * 
	 * @param registeredWithSwan
	 *            set to true if successfully registered to SWAN
	 */
	public void setRegisteredWithSwan(boolean registeredWithSwan) {
		this.registeredWithSwan = registeredWithSwan;
	}

	/**
	 * Returns true if sensor expression was successfully registered to SWAN
	 * 
	 * @return True if expression successfully registered to SWAN
	 */
	public boolean registeredWithSwan() {
		return registeredWithSwan;
	}

	/**
	 * Set to true if status expression was successfully registered to SWAN
	 * 
	 * @param statusRegisteredWithSwan
	 *            true if Status expression was successfully registered to SWAN
	 */
	public void setStatusRegisteredWithSwan(boolean statusRegisteredWithSwan) {
		this.statusRegisteredWithSwan = statusRegisteredWithSwan;
	}

	/**
	 * Returns true if sensor status expression is successfully registered to
	 * SWAN
	 * 
	 * @return True if successfully registered
	 */
	public boolean statusRegisteredWithSwan() {
		return statusRegisteredWithSwan;
	}

	/**
	 * Set whether sensor data should be exported to a file
	 * 
	 * @param exportToFile
	 *            True if export to file is desired
	 */
	public void setExportToFile(boolean exportToFile) {
		this.exportToFile = exportToFile;
		// If file no longer has to be exported, close file if required.
		if (!exportToFile) {
			if (file != null) {
				file.closeFile();
				file = null;
			}
		}
	}

	/**
	 * Returns whether sensor data should be exported to a file
	 * 
	 * @return True if data should be exported to a file
	 */
	public boolean exportToFile() {
		return exportToFile;
	}

	/**
	 * Set whether sensor data should be exported to a server
	 * 
	 * @param exportToServer
	 *            True if export to server is desired
	 */
	public void setExportToServer(boolean exportToServer) {
		this.exportToServer = exportToServer;
		// If becomes false probably have to make sure to close socket
	}

	/**
	 * Returns whether the sensor data should be exported to a server
	 * 
	 * @return True if data should be exported to server
	 */
	public boolean exportToServer() {
		return this.exportToServer;
	}

	/**
	 * Export current reading to a file
	 * 
	 * @param reading
	 *            The reading retrieved by the sensor
	 */
	public void export(TimestampedValue reading) {
		// If this is the first reading start by creating the file and the first
		// line
		if (file == null) {
			String sensorName = SensorTools.parseExpression(getExpression())[0];
			file = new FileExporter(sensorName + "_" + requestCode, CSV,
					firstLine);
		}
		String[] data = SensorTools.parseExpression(getExpression());
		String dataLine = data[0] + ";" + data[1] + ";"
				+ reading.getValue().toString() + ";" + reading.getTimestamp();
		file.addLine(dataLine);
	}

	/**
	 * Call to end exporting (closes open files)
	 */
	public void stopExporting() {
		if (file != null) {
			file.closeFile();
		}
		file = null;
	}

	/**
	 * Set the unit that matches the current valuePath
	 * 
	 * @param unit
	 *            The unit that matches the current valuePath
	 */
	public void setCurrentUnit(String unit) {
		this.currentUnit = unit;
	}

	/**
	 * Returns the unit that matches the current valuePath
	 * 
	 * @return the current unit of the values that the sensor is producing
	 */
	public String getCurrentUnit() {
		return currentUnit;
	}

	/**
	 * The icon that matches the Sensor
	 */
	private Drawable icon;
	/**
	 * Name = Name of the sensor Status = Current status of the sensor of
	 * available Readings = The data retrieved by the sensor Expression = The
	 * expression used to connect to SWAN requestCode = A unique String used as
	 * an ID when regestring to SWAN currentUnit = The unit matching the current
	 * valuePath of the current Expression
	 */
	private String name, status, readings, expression, requestCode,
			currentUnit;
	/**
	 * The id of the sensor in Swan (e.g. used to call configuration activity)
	 */
	private int sensorId;
	/**
	 * The a list of units matching the current sensor
	 */
	private ArrayList<String> units;
	/**
	 * hasStatusTextVP = true of the current Sensor has a "status_text"
	 * valuePath registeredWithSwan = true if succesfully registered with Swan
	 * statusRegisteredWithSwan = true if "status_text" valuePath is also
	 * succesfully registered with Swan exportToFile = true if the user wants
	 * the sensor data to be exported to a file when recording exportToServer =
	 * true if the user wants the sensor data to be exported to a server when
	 * recording
	 */
	private boolean hasStatusTextVP, registeredWithSwan,
			statusRegisteredWithSwan, exportToFile, exportToServer;

	/**
	 * Used to export sensor data in uniform format.
	 */
	private static final String CSV = ".csv",
			firstLine = "sensorName;valuePath;value;timestamp";
	private FileExporter file = null;
}
