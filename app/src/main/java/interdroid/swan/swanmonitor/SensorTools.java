package interdroid.swan.swanmonitor;

import android.util.Log;
import interdroid.swan.swansong.ExpressionFactory;
import interdroid.swan.swansong.ExpressionParseException;
import interdroid.swan.swansong.SensorValueExpression;

public class SensorTools {
	
	public static String FILENAME = "filename";
	public static String FILEPATH = "filepath";
	
	
	/**
	 * Used to parse a basic expression
	 * 
	 * @param expression
	 * @return a string array containing
	 *         [sensorId][valuePath][reductionMode][historyWindow]
	 */
	public static String[] parseExpression(String expression) {
		String[] values = new String[4];
		SensorValueExpression exp = null;
		try {
			exp = (SensorValueExpression) ExpressionFactory.parse(expression);
		} catch (ExpressionParseException e) {
			Log.e("SwanMonitor_SensorTools", "Failed to parse expression");
			e.printStackTrace();
		}

		values[0] = exp.getEntity();
		values[1] = exp.getValuePath();
		values[2] = exp.getHistoryReductionMode().toString();
		values[3] = "" + exp.getHistoryLength();
		return values;
	}
}
