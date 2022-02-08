package com.pkasemer.kakebeshoplira.HelperClasses;

import com.pkasemer.kakebeshoplira.Models.FoodDBModel;

public interface CartItemHandlerListener {
    void increment(int qty, FoodDBModel foodDBModel);
    void decrement(int qty, FoodDBModel foodDBModel);
    void deletemenuitem(String foodMenu_id, FoodDBModel foodDBModel);
}
