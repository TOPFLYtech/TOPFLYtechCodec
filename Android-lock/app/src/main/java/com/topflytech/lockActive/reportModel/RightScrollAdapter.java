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

import java.util.ArrayList;
import java.util.List;


/**
 * 底部右边列表
 */
public class RightScrollAdapter extends RecyclerView.Adapter<RightScrollAdapter.ScrollViewHolder> {


    private Context context;
    private List<String> rightDatas;
    private List<Integer> columnWidth;
    public RightScrollAdapter(Context context,List<Integer> columnWidthList) {
        this.context = context;
        columnWidth = columnWidthList;
    }

    public void setDatas(List<String> rightDatas) {
        this.rightDatas = rightDatas;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public ScrollViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_item_scroll, viewGroup, false);
        return new ScrollViewHolder(view,i);
    }

    @Override
    public void onBindViewHolder(@NonNull ScrollViewHolder scrollViewHolder, int i) {
        int width = 240;
        if(columnWidth.size() > i){
            width = columnWidth.get(i);
        }
        scrollViewHolder.mTvScrollItem.setWidth(width);
        scrollViewHolder.mTvScrollItem.setText(rightDatas.get(i));
    }

    @Override
    public int getItemCount() {
        return null == rightDatas ? 0 : rightDatas.size();
    }

    class ScrollViewHolder extends RecyclerView.ViewHolder {

        TextView mTvScrollItem;

        public ScrollViewHolder(@NonNull View itemView,int index) {
            super(itemView);
            int width = 240;
            if(columnWidth.size() > index){
                width = columnWidth.get(index);
            }
            mTvScrollItem = itemView.findViewById(R.id.tv_right_scroll);
            mTvScrollItem.setWidth(width);
        }
    }
}
