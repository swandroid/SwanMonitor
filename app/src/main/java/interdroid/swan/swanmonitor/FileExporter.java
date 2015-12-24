package interdroid.swan.swanmonitor;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.os.Environment;

public class FileExporter {
	private String filename;
	private FileWriter f;
		
	
	/**
	 * Creates the file on ExternalStorage in the folder "SwanMonitor"
	 * @param filename The name of the file to be created (excluding the extension)
	 * @param extension The extension the file should have (include the "." so eg: ".txt")
	 */
	FileExporter(String filename, String extension, String firstLine) {
		this.filename = getTimeInCorrectFormat(System.currentTimeMillis()) + "_" + filename;
		try {
			f = new FileWriter(Environment.getExternalStorageDirectory().toString() + "/SwanMonitor/" + this.filename + extension, true);
			f.write(firstLine);
		} catch (IOException e) {
			System.out.println("Failed to create file: " + filename);
			e.printStackTrace();
		}
	}
	
	/**
	 * Add a new line to the text file
	 * @param newLine the line to be added at the bottom of the file
	 */
	public void addLine(String newLine) {
		try {
			f.write("\n" + newLine);
		} catch (IOException e) {
			System.out.println("Failed writing [" + newLine + "] to file: " + filename);
			e.printStackTrace();
		}
	}
	
	/**
	 * call to close the File
	 */
	public void closeFile() {
		try {
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
		return sdf.format(date);
	}
	
}
