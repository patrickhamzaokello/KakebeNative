package com.shop.kakebe.KaKebe.Models;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class ResetPassword {

    @SerializedName("email_or_phone")
    @Expose
    private String emailOrPhone;
    @SerializedName("send_code_by")
    @Expose
    private String sendCodeBy;

    public String getEmailOrPhone() {
        return emailOrPhone;
    }

    public void setEmailOrPhone(String emailOrPhone) {
        this.emailOrPhone = emailOrPhone;
    }

    public String getSendCodeBy() {
        return sendCodeBy;
    }

    public void setSendCodeBy(String sendCodeBy) {
        this.sendCodeBy = sendCodeBy;
    }

}