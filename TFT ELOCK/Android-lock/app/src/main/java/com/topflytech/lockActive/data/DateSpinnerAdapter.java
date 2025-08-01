package com.topflytech.lockActive.data;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.topflytech.lockActive.R;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by admin on 2016/10/9.
 */

public class DateSpinnerAdapter extends BaseAdapter implements SpinnerAdapter {
//
//    public static String[] items = new String[]{"Today","Yesterday","Last 3 Days","Last 7 Days","Last Week","This Month",
//            "Previous Month","Custom"};
    public static int[] itemsStrKey = new int[]{R.string.str_today,R.string.str_yesterday,R.string.str_last_3_day,
//        R.string.str_last_7_day,
//                R.string.str_last_week,R.string.str_this_month,R.string.str_pre_month,
        R.string.str_custom};
    private Context mContext;

    public DateSpinnerAdapter(Context mContext) {
        this.mContext = mContext;
    }
    @Override
    public int getCount() {
        return itemsStrKey.length;
    }

    @Override
    public Object getItem(int position) {
        return mContext.getResources().getString(itemsStrKey[position]);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (null == convertView) {
            convertView = View.inflate(mContext, R.layout.spinner_daropdown_item, null);
        }
        String value = (String)getItem(position);
        ((TextView) convertView.findViewById(R.id.main_title)).setText(value);
        convertView.findViewById(R.id.sub_title).setVisibility(View.GONE);
        return convertView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (null == convertView) {
            convertView = View.inflate(mContext, R.layout.spinner_daropdown_item, null);
        }
        String value = (String)getItem(position);
        ((TextView) convertView.findViewById(R.id.main_title)).setText(value);
        convertView.findViewById(R.id.sub_title).setVisibility(View.GONE);
        return convertView;
    }

    public static Date getStartDate(int selectedIndex){
        Date startDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        if (selectedIndex == 0){ // today
            return cal.getTime();
        }else if (selectedIndex == 1){ // Yesterday
            cal.add(Calendar.DAY_OF_MONTH, -1);
            return cal.getTime();
        }else if (selectedIndex == 2){ // Last 3 days
            cal.add(Calendar.DAY_OF_MONTH, -2);
            return cal.getTime();
        }
//        else if (selectedIndex == 3){ // last 7 days
//            cal.add(Calendar.DAY_OF_MONTH, -6);
//            return cal.getTime();
//        }else if (selectedIndex == 4){ // last week
//            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
//            cal.add(Calendar.DAY_OF_MONTH,-6-dayOfWeek);
//            return cal.getTime();
//        }else if (selectedIndex == 5){ // this month
//            int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
//            cal.add(Calendar.DAY_OF_MONTH,-dayOfMonth + 1);
//            return cal.getTime();
//        }else if (selectedIndex == 6){ // previous month
//            cal.set(Calendar.MONTH,cal.get(Calendar.MONTH) - 1);
//            int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
//            cal.add(Calendar.DAY_OF_MONTH,-dayOfMonth + 1);
//            return cal.getTime();
//        }
        else {
            return cal.getTime();
        }
    }

    public static Date getEndDate(int selectedIndex){
        Date startDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        cal.set(Calendar.HOUR_OF_DAY,23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 0);
        if (selectedIndex == 0){ // today
            return cal.getTime();
        }else if (selectedIndex == 1){ // Yesterday
            cal.add(Calendar.DAY_OF_MONTH, -1);
            return cal.getTime();
        }else if (selectedIndex == 2){ // Last 3 days
            return cal.getTime();
        }
//        else if (selectedIndex == 3){ // last 7 days
//            return cal.getTime();
//        }else if (selectedIndex == 4){ // last week
//            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
//            cal.add(Calendar.DAY_OF_MONTH,-dayOfWeek);
//            return cal.getTime();
//        }else if (selectedIndex == 5){ // this month
//            return cal.getTime();
//        }else if (selectedIndex == 6){ // previous month
//            int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
//            cal.add(Calendar.DAY_OF_MONTH,-dayOfMonth + 1);
//            cal.set(Calendar.HOUR_OF_DAY,0);
//            cal.set(Calendar.MINUTE, 0);
//            cal.set(Calendar.SECOND, 0);
//            cal.set(Calendar.MILLISECOND, 0);
//            cal.add(Calendar.SECOND,-1);
//            return cal.getTime();
//        }
        else {
            return cal.getTime();
        }
    }
}
