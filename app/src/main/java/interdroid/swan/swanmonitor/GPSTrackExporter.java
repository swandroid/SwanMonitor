package interdroid.swan.swanmonitor;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Environment;

public class GPSTrackExporter {
	private String filename;
	private FileWriter f;
	final private boolean TCX;

	/**
	 * Sets up the gpx or tcx file
	 * 
	 * @param if asTCX is true, file will be saved as .tcx in corresponding
	 *        format, if false it will be .gpx
	 */
	GPSTrackExporter(boolean asTCX) {
		this.TCX = asTCX;

		try {
			if (TCX) {
				createTCXFile();
			} else {
				createGPXFile();
			}
		} catch (IOException e) {
			System.out.println("Failed to create file: " + filename);
			e.printStackTrace();
		}
	}

	public void addTrackPoint(Location location) {
		try {
			if (TCX) {
				addTrackPointTCX(location);
			} else {
				addTrackPointGPX(location);
			}
		} catch (IOException e) {
			System.out.println("Failed to write trackpoint");
			e.printStackTrace();
		}
	}

	public void closeFile() {
		try {
			if (TCX) {
				writeTCXFooters();
			} else {
				writeGPXFooters();
			}
			f.close();
		} catch (IOException e) {
			System.out.println("Failed to close file: " + filename);
			e.printStackTrace();
		}
	}

	@SuppressLint("SimpleDateFormat")
	private String getTimeInCorrectFormat(long timeInMillis) {
		Date date = new Date(timeInMillis);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(date).replace(" ", "T") + "Z";
	}

	private void createGPXFile() throws IOException {
		filename = getTimeInCorrectFormat(System.currentTimeMillis()) + "_GPS_TRACK_LOG.gpx";
		f = new FileWriter(Environment.getExternalStorageDirectory().toString() + "/SwanMonitor/" + filename, true);
		writeGPXHeaders();
	}

	private void writeGPXHeaders() throws IOException {
		f.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		f.write("\n<gpx version=\"1.1\" creator=\"SwanMonitor\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 "
				+ "http://www.topografix.com/GPX/1/1/gpx.xsd http://www.garmin.com/xmlschemas/GpxExtensions/v3 "
				+ "http://www.garmin.com/xmlschemas/GpxExtensionsv3.xsd http://www.garmin.com/xmlschemas/TrackPointExtension/v1 "
				+ "http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd\" xmlns=\"http://www.topografix.com/GPX/1/1\" "
				+ "xmlns:gpxtpx=\"http://www.garmin.com/xmlschemas/TrackPointExtension/v1\" "
				+ "xmlns:gpxx=\"http://www.garmin.com/xmlschemas/GpxExtensions/v3\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
		f.write("\n<metadata>");
		f.write("\n<author>");
		f.write("\n<name>Swan Monitor</name>");
		f.write("\n</author>");
		f.write("\n<link href=\"https://github.com/interdroid\">");
		f.write("\n<text>Swan Monitor</text>");
		f.write("\n</link>");
		f.write("\n<time>" + getTimeInCorrectFormat(System.currentTimeMillis()) + "</time>");
		f.write("\n</metadata>");
		f.write("\n<trk>");
		f.write("\n<src>https://github.com/interdroid</src>");
		f.write("\n<type>MONITORING</type>");
		f.write("\n<trkseg>");
	}

	private void addTrackPointGPX(Location location) throws IOException {
		f.write("\n<trkpt lat=\"" + location.getLatitude() + "\" lon=\"" + location.getLongitude() + "\">");
		f.write("\n<ele>" + location.getAltitude() + "</ele>");
		f.write("\n<time>" + getTimeInCorrectFormat(location.getTime()) + "</time>");
		f.write("\n</trkpt>");

	}

	private void writeGPXFooters() throws IOException {
		f.write("\n</trkseg>");
		f.write("\n</trk>");
		f.write("\n</gpx>");
	}

	private void createTCXFile() throws IOException {
		filename = getTimeInCorrectFormat(System.currentTimeMillis()) + "_GPS_TRACK_LOG.tcx";
		f = new FileWriter(Environment.getExternalStorageDirectory().toString() + "/SwanMonitor/" + filename, true);
		writeTCXHeaders();
	}

	private void writeTCXHeaders() throws IOException {
		f.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		f.write("\n<TrainingCenterDatabase xmlns=\"http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2\" "
				+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
				+ "xsi:schemaLocation=\"http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2 "
				+ "http://www.garmin.com/xmlschemas/TrainingCenterDatabasev2.xsd\">");
		f.write("\n<Activities>");
		f.write("\n<Activity Sport=\"Monitoring\">");
		f.write("\n<Id>" + getTimeInCorrectFormat(System.currentTimeMillis()) + "</Id>");
		f.write("\n<Lap StartTime=\"" + getTimeInCorrectFormat(System.currentTimeMillis()) + "\">");
		// f.write("\n<TotalTimeSeconds>1401.0</TotalTimeSeconds>"); //Not
		// required
		f.write("\n<Intensity>Active</Intensity>");
		f.write("\n<TriggerMethod>Manual</TriggerMethod>");
		f.write("\n<Track>");
	}

	private void addTrackPointTCX(Location location) throws IOException {
		f.write("\n<Trackpoint>");
		f.write("\n<Time>" + getTimeInCorrectFormat(location.getTime()) + "</Time>");
		f.write("\n<Position>");
		f.write("\n<LatitudeDegrees>" + location.getLatitude() + "</LatitudeDegrees>");
		f.write("\n<LongitudeDegrees>" + location.getLongitude() + "</LongitudeDegrees>");
		f.write("\n</Position>");
		f.write("\n</Trackpoint>");

	}

	private void writeTCXFooters() throws IOException {
		f.write("\n</Track>");
		f.write("\n</Lap>");
		f.write("\n</Activity>");
		f.write("\n</Activities>");
		f.write("\n</TrainingCenterDatabase>");
	}
}
