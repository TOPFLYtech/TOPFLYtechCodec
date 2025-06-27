package com.topflytech.lockActive.data;

public class CustomMenuItem {
    private String title;
    private int iconResId;

    public CustomMenuItem(String title, int iconResId) {
        this.title = title;
        this.iconResId = iconResId;
    }

    public String getTitle() {
        return title;
    }

    public int getIconResId() {
        return iconResId;
    }
}