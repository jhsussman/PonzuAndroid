package com.sussman.ponzu;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Filterable;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CheckboxAdapter extends ArrayAdapter implements Filterable {
        Checks[] modelItems = null;
        Context context;
        TextView name;
        CheckBox cb;
    private ArrayList<String>originalData = null;
    private ArrayList<String>filteredData = null;

public CheckboxAdapter(Context context, Checks[] resource, ArrayList<String> data) {
        super(context,R.layout.checkmark_row,resource);
        // TODO Auto-generated constructor stub
        this.context = context;
        this.modelItems = resource;
        this.filteredData = data ;
        this.originalData = data ;
        }
@Override
public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        convertView = inflater.inflate(R.layout.checkmark_row, parent, false);
         name = (TextView) convertView.findViewById(R.id.specificClassText);
         cb = (CheckBox) convertView.findViewById(R.id.checkBox1);
        name.setText(modelItems[position].getName());
        if(modelItems[position].getValue() == 1)
        cb.setChecked(true);
        else
        cb.setChecked(false);
        return convertView;
        }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint,FilterResults results) {

                filteredData = (ArrayList<String>) results.values;
                notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                ArrayList<String> FilteredArrList = new ArrayList<String>();

                if (originalData == null) {
                    originalData = new ArrayList<String>(filteredData);
                }

                if (constraint == null || constraint.length() == 0) {
                    results.count = originalData.size();
                    results.values = originalData;
                } else {
                    constraint = constraint.toString().toLowerCase();
                    for (int i = 0; i < originalData.size(); i++) {
                        String data0 = originalData.get(i);
                        if (data0.toLowerCase().contains(constraint.toString())) {
                            FilteredArrList.add(originalData.get(i));
                        }
                    }
                    results.count = FilteredArrList.size();
                    results.values = FilteredArrList;
                }
                return results;
            }
        };
        return filter;
    }
}
