package com.topflytech.lockActive.data;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.topflytech.lockActive.R;

import java.util.List;
import android.graphics.drawable.Drawable;

public class CustomPopupMenuAdapter extends BaseAdapter {
    private Context context;
    private List<CustomMenuItem> items;
    private PopupWindow popupWindow;
    public CustomPopupMenuAdapter(Context context, List<CustomMenuItem> items, OnMenuItemClickListener listener, PopupWindow popupWindow) {
        this.context = context;
        this.items = items;
        this.listener = listener;
        this.popupWindow = popupWindow;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    private OnMenuItemClickListener listener;
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.popup_menu_item, parent, false);
            holder = new ViewHolder();
            holder.icon = convertView.findViewById(R.id.icon);
            holder.title = convertView.findViewById(R.id.title);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        CustomMenuItem item = items.get(position);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onMenuItemClick(item);  // 回调点击事件
                }
                popupWindow.dismiss();
            }
        });
        holder.title.setText(item.getTitle());
        Drawable icon = context.getResources().getDrawable(item.getIconResId(), context.getTheme());
        if (icon != null) {
            holder.icon.setImageDrawable(icon);
        } else {
            holder.icon.setVisibility(View.GONE); // Hide the icon if there is none
        }

        return convertView;
    }

    static class ViewHolder {
        ImageView icon;
        TextView title;
    }
    public interface OnMenuItemClickListener {
        void onMenuItemClick(CustomMenuItem menuItem);
    }

}