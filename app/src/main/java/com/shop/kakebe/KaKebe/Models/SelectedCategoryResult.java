
package com.shop.kakebe.KaKebe.Models;

import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class SelectedCategoryResult {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("meta_description")
    @Expose
    private String metaDescription;
    @SerializedName("banner")
    @Expose
    private String banner;
    @SerializedName("order_level")
    @Expose
    private Integer orderLevel;
    @SerializedName("created")
    @Expose
    private String created;
    @SerializedName("category_products")
    @Expose
    private List<CategoryProduct> categoryProducts = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getMetaDescription() {
        return metaDescription;
    }

    public void setMetaDescription(String metaDescription) {
        this.metaDescription = metaDescription;
    }

    public String getBanner() {
        return banner;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public Integer getOrderLevel() {
        return orderLevel;
    }

    public void setOrderLevel(Integer orderLevel) {
        this.orderLevel = orderLevel;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public List<CategoryProduct> getCategoryProducts() {
        return categoryProducts;
    }

    public void setCategoryProducts(List<CategoryProduct> categoryProducts) {
        this.categoryProducts = categoryProducts;
    }

}
