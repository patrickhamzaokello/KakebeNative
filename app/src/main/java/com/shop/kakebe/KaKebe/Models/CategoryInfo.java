
package com.shop.kakebe.KaKebe.Models;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class CategoryInfo {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("meta_description")
    @Expose
    private Object metaDescription;
    @SerializedName("banner")
    @Expose
    private String banner;
    @SerializedName("created")
    @Expose
    private String created;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getMetaDescription() {
        return metaDescription;
    }

    public void setMetaDescription(Object metaDescription) {
        this.metaDescription = metaDescription;
    }

    public String getBanner() {
        return banner;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

}
