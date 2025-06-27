package com.topflytech.lockActive.data;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.topflytech.lockActive.R;
import com.topflytech.lockActive.popmenu.MenuItem;

import java.util.List;

public class CustomPopMenu {
    private Context context;
    private PopupWindow popupWindow;
    private CustomPopupMenuAdapter.OnMenuItemClickListener itemClickListener;
    public CustomPopMenu(Context context,  CustomPopupMenuAdapter.OnMenuItemClickListener itemClickListener) {
        this.context = context;
        this.itemClickListener = itemClickListener;
    }

    public void show(View anchor, List<CustomMenuItem> menuItems) {
        // Inflate the layout for the pop-up menu
        View contentView = LayoutInflater.from(context).inflate(R.layout.custom_popup_window, null);



        // Create the PopupWindow
        popupWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);
        // Set up the ListView with the custom adapter
        ListView listView = contentView.findViewById(R.id.pop_menu_list);
        CustomPopupMenuAdapter adapter = new CustomPopupMenuAdapter(context, menuItems,itemClickListener,popupWindow);
        listView.setAdapter(adapter);
        // Show the PopupWindow at the anchor's position
        popupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY,
                (int) anchor.getX(), (int) anchor.getY() + anchor.getHeight());
    }
}
