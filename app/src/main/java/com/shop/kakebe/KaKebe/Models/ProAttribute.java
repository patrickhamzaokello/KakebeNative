package com.shop.kakebe.KaKebe.Models;

import java.util.ArrayList;
import java.util.List;

public class ProAttribute {
    private List<String> text;
    private boolean isSelected = false;
    List<String> val_list = new ArrayList<>();

    public ProAttribute(List<String> text) {
        this.text = text;
    }

    public List<String> getText() {
        return text;
    }

    public List<String> getVal_list(){
        return val_list;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }


    public boolean isSelected() {
        return isSelected;
    }
}
