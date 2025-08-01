package com.topflytech.lockActive.reportModel;

import java.util.ArrayList;
import java.util.List;

/**
 * 下方数据
 */
public class Entity {
    private List<String> rightDatas;


    public List<String> getRightDatas() {
        if (rightDatas == null) {
            return new ArrayList<>();
        }
        return rightDatas;
    }

    public void setRightDatas(List<String> rightDatas) {
        this.rightDatas = rightDatas;
    }
}
