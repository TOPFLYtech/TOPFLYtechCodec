package com.topflytech.bleAntiLost.data;

import android.text.InputFilter;
import android.text.Spanned;

public class PositiveIntegerFilter implements InputFilter {

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        for (int i = start; i < end; i++) {
            char c = source.charAt(i);
            if (c >= '0' && c <= '9') { // 允许正负号仅出现在开头
//                result.append(c);
            }else{
                return "";
            }
        }

        String filteredInput = source.toString().trim();
        try {
            long value = Long.parseLong(filteredInput);
            if (value >= 0) {
                return source; // 返回过滤后的合法字符串
            }
        } catch (NumberFormatException ignored) {
        }

        // 若不符合正整数要求，返回空字符串表示拒绝此次插入或替换
        return "";
    }
}