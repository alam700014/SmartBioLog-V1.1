package com.android.fortunaattendancesystem.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.android.fortunaattendancesystem.R;
import com.android.fortunaattendancesystem.model.BasicEmployeeInfo;

import java.util.ArrayList;

/**
 * Created by fortuna on 14/2/18.
 */

public class CustomEnrollListAdapter extends BaseAdapter implements Filterable {

    private Activity activity;
    private ArrayList <BasicEmployeeInfo> employeeDetailsList;
    private ArrayList <BasicEmployeeInfo> employeeDetailsFilter;
    private EmployeeDataFilter employeeDataFilter;

    public CustomEnrollListAdapter(Activity activity, ArrayList <BasicEmployeeInfo> employeeDetailsList) {
        this.activity = activity;
        this.employeeDetailsList = employeeDetailsList;
        this.employeeDetailsFilter = employeeDetailsList;
    }

    @Override
    public int getCount() {
        return employeeDetailsList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row_view = convertView;
        if (row_view == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            row_view = inflater.inflate(R.layout.emp_list_item, null, true);
        }

        TextView emp_Id = (TextView) row_view.findViewById(R.id.EmpID_list);
        TextView card_Id = (TextView) row_view.findViewById(R.id.CardID_list);
        TextView emp_Name = (TextView) row_view.findViewById(R.id.EmpName_list);
        TextView enolledIcon1 = (TextView) row_view.findViewById(R.id.EnrolledIcon1);
        TextView enolledIcon2 = (TextView) row_view.findViewById(R.id.EnrolledIcon2);

        BasicEmployeeInfo employeeDetails = employeeDetailsList.get(position);

        emp_Id.setText(employeeDetails.getEmployeeID().trim());
        card_Id.setText(employeeDetails.getCardID().replaceAll("\\G0", " ").trim());
        emp_Name.setText(employeeDetails.getEmployeeName());

        enolledIcon1.setVisibility(View.INVISIBLE);
        enolledIcon2.setVisibility(View.INVISIBLE);

        //Assign Finger Icon for showing finger enrolled status
        //Assign Finger Icon for showing finger enrolled status
        String fingerEnrollStatus = employeeDetails.getEnrolledStatus().trim();
       // Log.d("TEST","FingerEnrollStatus:"+fingerEnrollStatus);
        if (fingerEnrollStatus.length() > 0 && fingerEnrollStatus.equals("Y")) {
            String nosFinger = employeeDetails.getNosFinger();
          //  Log.d("TEST","NosFinger:"+nosFinger);
            if (nosFinger != null && nosFinger.trim().length() > 0 && nosFinger.equals("1")) {
                enolledIcon1.setVisibility(View.VISIBLE);
            } else if (nosFinger != null && nosFinger.trim().length() > 0 && nosFinger.equals("2")) {
                enolledIcon1.setVisibility(View.VISIBLE);
                enolledIcon2.setVisibility(View.VISIBLE);
            }
        }
        return row_view;
    }

    @Override
    public Filter getFilter() {
        if (employeeDataFilter == null) {
            employeeDataFilter = new EmployeeDataFilter();
        }
        return employeeDataFilter;
    }

    private class EmployeeDataFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            FilterResults filterResults = new FilterResults();

            if (charSequence != null && charSequence.toString().length() > 0) {
                ArrayList <BasicEmployeeInfo> employeeDetails_result = new ArrayList <BasicEmployeeInfo>();
                for (int i = 0, l = employeeDetailsFilter.size(); i < l; i++) {
                    BasicEmployeeInfo employeeDetails_obj = employeeDetailsFilter.get(i);

                    if (employeeDetails_obj.getEmployeeID().toUpperCase().contains(charSequence.toString().toUpperCase()) ||
                            employeeDetails_obj.getCardID().toUpperCase().contains(charSequence.toString().toUpperCase()) ||
                            employeeDetails_obj.getEmployeeName().toUpperCase().contains(charSequence.toString().toUpperCase())) {
                        employeeDetails_result.add(employeeDetails_obj);
                    }
                }
                filterResults.count = employeeDetails_result.size();
                filterResults.values = employeeDetails_result;
            } else {
                filterResults.count = employeeDetailsFilter.size();
                filterResults.values = employeeDetailsFilter;
            }
            return filterResults;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            employeeDetailsList = (ArrayList <BasicEmployeeInfo>) results.values;
            notifyDataSetChanged();
        }
    }
}
