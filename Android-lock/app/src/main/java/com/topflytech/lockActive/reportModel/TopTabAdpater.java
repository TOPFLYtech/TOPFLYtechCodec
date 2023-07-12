package com.topflytech.lockActive.reportModel;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.topflytech.lockActive.R;
import com.topflytech.lockActive.data.MyByteUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 头部列表
 */
public class TopTabAdpater extends RecyclerView.Adapter<TopTabAdpater.TabViewHolder> {

    private Context context;
    private List<String> datas;
    private List<Integer> columnWidth = MyByteUtils.columnWidth;
    public TopTabAdpater(Context context) {
        this.context = context;
    }

    public void setDatas(List<String> datas) {
        this.datas = datas;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TabViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_item_scroll, viewGroup, false);
        return new TabViewHolder(view,i);
    }

    @Override
    public void onBindViewHolder(@NonNull TabViewHolder tabViewHolder, int i) {
        int width = 240;
        if(columnWidth.size() > i){
            width = columnWidth.get(i);
        }
        tabViewHolder.mTabTv.setWidth(width);
        tabViewHolder.mTabTv.setText(datas.get(i));
    }

    @Override
    public int getItemCount() {
        return null == datas ? 0 : datas.size();
    }

    class TabViewHolder extends RecyclerView.ViewHolder {

        TextView mTabTv;

        public TabViewHolder(@NonNull View itemView,int index) {
            super(itemView);
            int width = 240;
            if(columnWidth.size() > index){
                width = columnWidth.get(index);
            }

            mTabTv = itemView.findViewById(R.id.tv_right_scroll);
            mTabTv.setWidth(width);
        }
    }

}
