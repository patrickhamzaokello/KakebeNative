package com.shop.kakebe.KaKebe.Utils;

import com.shop.kakebe.KaKebe.Models.UserAddress;

public interface SelectedAddressListener {
    void selectedAddress(UserAddress userAddress);
    void retryPageLoad();
    void requestfailed();
}
