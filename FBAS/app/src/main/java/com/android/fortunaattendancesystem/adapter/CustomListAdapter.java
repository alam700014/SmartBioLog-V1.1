package com.android.fortunaattendancesystem.adapter;


import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.fortunaattendancesystem.R;

import java.util.ArrayList;

public class CustomListAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final ArrayList<String> itemname;
    private final Integer[] imgid;

    public CustomListAdapter(Activity context, ArrayList<String> itemname, Integer[] imgid) {

        super(context, R.layout.list_item, itemname);

        // TODO Auto-generated constructor stub

        this.context=context;
        this.itemname=itemname;
        this.imgid=imgid;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    public View getView(int position,View convertView,ViewGroup parent) {

        // assign the view we are converting to a local variable
        View rowView = convertView;

        // first check to see if the view is null. if so, we have to inflate it.
        // to inflate it basically means to render, or show, the view.
        if (rowView == null) {
            LayoutInflater inflater=context.getLayoutInflater();
            rowView=inflater.inflate(R.layout.list_item, null, true);
        }

        TextView empName = (TextView) rowView.findViewById(R.id.EmpName);
        ImageView empIcon = (ImageView) rowView.findViewById(R.id.EmpIcon);

        empName.setText(itemname.get(position));
        empIcon.setImageResource(imgid[position]);

        return rowView;

    }
}