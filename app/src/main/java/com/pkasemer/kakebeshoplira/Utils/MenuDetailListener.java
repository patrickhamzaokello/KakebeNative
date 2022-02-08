package com.pkasemer.kakebeshoplira.Utils;

import com.pkasemer.kakebeshoplira.Models.FoodDBModel;
import com.pkasemer.kakebeshoplira.Models.SelectedCategoryMenuItemResult;

public interface MenuDetailListener {
    void retryPageLoad();
    void incrementqtn(int qty, FoodDBModel foodDBModel);
    void decrementqtn(int qty, FoodDBModel foodDBModel);

    void addToCartbtn(SelectedCategoryMenuItemResult selectedCategoryMenuItemResult);
    void orderNowMenuBtn(SelectedCategoryMenuItemResult selectedCategoryMenuItemResult);

}
