package interdroid.swan.swanmonitor;

import interdroid.swan.SensorInfo;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SensorSelectSpinnerAdapter extends ArrayAdapter<SensorInfo> {

	Context context;
	List<SensorInfo> swanSensorList;

	public SensorSelectSpinnerAdapter(Context context, int textViewResourceId,
			List<SensorInfo> swanSensorList) {
		super(context, textViewResourceId, swanSensorList);
		this.context = context;
		this.swanSensorList = swanSensorList;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return getCustomView(position, convertView, parent);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return getCustomView(position, convertView, parent);
	}

	public View getCustomView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.spinner_row, null);
		}
		TextView label = (TextView) convertView.findViewById(R.id.sensors);
		label.setText(swanSensorList.get(position).getEntity());

		TextView sub = (TextView) convertView.findViewById(R.id.valuepaths);
		sub.setText(swanSensorList.get(position).getValuePaths().toString());

		ImageView icon = (ImageView) convertView.findViewById(R.id.image);
		icon.setImageDrawable(swanSensorList.get(position).getIcon());

		return convertView;
	}
}