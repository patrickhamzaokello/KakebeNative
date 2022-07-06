package com.shop.kakebe.KaKebe.Models;

import java.util.List;

public class ProAttribute {
    private List<String> text;
    private boolean isSelected = false;

    public ProAttribute(List<String> text) {
        this.text = text;
    }

    public List<String> getText() {
        return text;
    }


    public void setSelected(boolean selected) {
        isSelected = selected;
    }


    public boolean isSelected() {
        return isSelected;
    }
}
