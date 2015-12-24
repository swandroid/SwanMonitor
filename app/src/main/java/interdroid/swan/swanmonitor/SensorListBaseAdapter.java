package interdroid.swan.swanmonitor;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SensorListBaseAdapter extends BaseAdapter {
	private static ArrayList<SensorObject> itemDetailsArrayList;
	private Context context;
	private LayoutInflater l_Inflater;

	public SensorListBaseAdapter(Context context, ArrayList<SensorObject> results) {
		this.context = context;
		itemDetailsArrayList = results;
		l_Inflater = LayoutInflater.from(context);
	}

	public int getCount() {
		return itemDetailsArrayList.size();
	}

	public Object getItem(int position) {
		return itemDetailsArrayList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = l_Inflater.inflate(R.layout.item_details_view, null);
			holder = new ViewHolder();
			holder.txt_itemName = (TextView) convertView.findViewById(R.id.sensorName_textView);
			holder.txt_sensorExpression = (TextView) convertView.findViewById(R.id.expression_textView);
			holder.txt_itemReadings = (TextView) convertView.findViewById(R.id.onReading_textView);
			holder.itemImage = (ImageView) convertView.findViewById(R.id.sensorIcon_imageView);
			holder.txt_export = (TextView) convertView.findViewById(R.id.export_textView);
		    holder.txt_export_file = (TextView) convertView.findViewById(R.id.export_file_textView);
		    holder.txt_export_server = (TextView) convertView.findViewById(R.id.export_server_textView);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.txt_itemName.setText(itemDetailsArrayList.get(position).getNameWithStatus());
		holder.txt_sensorExpression.setText(itemDetailsArrayList.get(position).getExpression());
		holder.txt_itemReadings.setText(itemDetailsArrayList.get(position).getReadings());
		holder.itemImage.setBackgroundDrawable(itemDetailsArrayList.get(position).getIcon());
		//holder.itemImage.setBackground(itemDetailsArrayList.get(position).getIcon());
		
		// Set colors to match content
		checkStatusColor(holder, position);
		
		return convertView;
	}
	
	/**
	 * Sets the color of the expression to green if registered, red if register failed
	 * And sets active export options (file or server) to black, inactive to light grey)
	 * @param holder
	 * @param position
	 */
	private void checkStatusColor(ViewHolder holder, int position){
		// Check and set expression color (not registered == red)
		if(itemDetailsArrayList.get(position).registeredWithSwan()){
			holder.txt_sensorExpression.setTextColor(context.getResources().getColor(R.color.dark_green));
		}
		else{
			holder.txt_sensorExpression.setTextColor(context.getResources().getColor(R.color.red));
		}
		
		// Check and set file export color
		if(itemDetailsArrayList.get(position).exportToFile()){
			holder.txt_export_file.setTextColor(context.getResources().getColor(R.color.black));
		}
		else{
			holder.txt_export_file.setTextColor(context.getResources().getColor(R.color.light_gray));
		}
		
		//Check and set server export color
		if(itemDetailsArrayList.get(position).exportToServer()){
			holder.txt_export_server.setTextColor(context.getResources().getColor(R.color.black));
		}
		else{
			holder.txt_export_server.setTextColor(context.getResources().getColor(R.color.light_gray));
		}
	}

	static class ViewHolder {
		TextView txt_itemName;
		TextView txt_sensorExpression;
		TextView txt_itemReadings;
		TextView txt_export;
		TextView txt_export_file;
		TextView txt_export_server;
		ImageView itemImage;
	}
}