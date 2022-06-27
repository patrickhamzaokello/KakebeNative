package com.shop.kakebe.KaKebe.HelperClasses;

import com.shop.kakebe.KaKebe.Models.FoodDBModel;

public interface CartItemHandlerListener {
    void increment(int qty, FoodDBModel foodDBModel);
    void decrement(int qty, FoodDBModel foodDBModel);
    void deletemenuitem(String foodMenu_id, FoodDBModel foodDBModel);
}
