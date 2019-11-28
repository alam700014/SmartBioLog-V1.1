package com.android.fortunaattendancesystem.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.fortunaattendancesystem.R;
import com.android.fortunaattendancesystem.model.BasicEmployeeInfo;

import java.util.List;

/**
 * Created by fortuna on 31/1/19.
 */

public class EmployeeListAdapter extends RecyclerView.Adapter <EmployeeListAdapter.EmployeeViewHolder> {


    private Context mCtx;
    private List <BasicEmployeeInfo> employeeList;


    public EmployeeListAdapter(Context mCtx, List <BasicEmployeeInfo> employeeList) {
        this.mCtx = mCtx;
        this.employeeList = employeeList;
    }

    @Override
    public EmployeeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.employee_details_cardview, null);
        return new EmployeeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EmployeeViewHolder holder, int position) {

        if (position % 2 == 0) {
            holder.llRoot.setBackgroundColor(Color.parseColor("#9e9797"));
        } else {
            holder.llRoot.setBackgroundColor(Color.parseColor("#ffffff"));
        }

        BasicEmployeeInfo empInfo = employeeList.get(position);
        holder.etEmployeeId.setText(empInfo.getEmployeeID());
        holder.etCardId.setText(empInfo.getCardID());
        holder.etEmployeeName.setText(empInfo.getEmployeeName());


        //holder.ivFingerEnrollStatus.setImageDrawable(mCtx.getResources().getDrawable(product.getImage()));

    }


    @Override
    public int getItemCount() {
        return employeeList.size();
    }


    class EmployeeViewHolder extends RecyclerView.ViewHolder {

        LinearLayout llRoot;
        EditText etEmployeeId, etCardId, etEmployeeName;
        ImageView ivFingerEnrollStatus;

        public EmployeeViewHolder(View itemView) {
            super(itemView);
            llRoot = itemView.findViewById(R.id.llRoot);
            etEmployeeId = itemView.findViewById(R.id.etEmployeeId);
            etCardId = itemView.findViewById(R.id.etCardId);
            etEmployeeName = itemView.findViewById(R.id.etEmployeeName);
            ivFingerEnrollStatus = itemView.findViewById(R.id.ivFingerEnrollStatus);
        }
    }
}
