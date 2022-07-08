
package com.shop.kakebe.KaKebe.Models;

import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class SearchCategoriee {

    @SerializedName("popularSearch")
    @Expose
    private List<PopularSearch> popularSearch = null;
    @SerializedName("featuredCategories")
    @Expose
    private List<FeaturedCategory> featuredCategories = null;

    public List<PopularSearch> getPopularSearch() {
        return popularSearch;
    }

    public void setPopularSearch(List<PopularSearch> popularSearch) {
        this.popularSearch = popularSearch;
    }

    public List<FeaturedCategory> getFeaturedCategories() {
        return featuredCategories;
    }

    public void setFeaturedCategories(List<FeaturedCategory> featuredCategories) {
        this.featuredCategories = featuredCategories;
    }

}
