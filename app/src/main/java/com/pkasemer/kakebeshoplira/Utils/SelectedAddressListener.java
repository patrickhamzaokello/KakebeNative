package com.pkasemer.kakebeshoplira.Utils;

import com.pkasemer.kakebeshoplira.Models.UserAddress;

public interface SelectedAddressListener {
    void selectedAddress(UserAddress userAddress);
    void retryPageLoad();
    void requestfailed();
}
