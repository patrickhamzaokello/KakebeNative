package com.shop.kakebe.KaKebe.Models;


public class FoodDBModel {

    private Integer menuId;

    private String menuName;

    private Integer price;

    private Integer menuTypeId;

    private String menuImage;


    private String created;


    private Integer rating;

    private Integer quantity;

    private Integer orderstatus;

    public FoodDBModel(Integer menuId, String menuName, Integer price, Integer menuTypeId, String menuImage, Integer quantity, Integer orderstatus) {
        this.menuId = menuId;
        this.menuName = menuName;
        this.price = price;
        this.menuTypeId = menuTypeId;
        this.menuImage = menuImage;
        this.quantity = quantity;
        this.orderstatus = orderstatus;
    }


    public Integer getMenuId() {
        return menuId;
    }

    public void setMenuId(Integer menuId) {
        this.menuId = menuId;
    }

    public String getMenuName() {
        return menuName;
    }


    public Integer getPrice() {
        return price;
    }



    public Integer getMenuTypeId() {
        return menuTypeId;
    }


    public String getMenuImage() {
        return menuImage;
    }



    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }



    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }


    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getOrderstatus() {
        return orderstatus;
    }

    public void setOrderstatus(Integer orderstatus) {
        this.orderstatus = orderstatus;
    }

}
