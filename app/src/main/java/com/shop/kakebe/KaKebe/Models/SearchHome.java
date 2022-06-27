
package com.shop.kakebe.KaKebe.Models;

import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class SearchHome {

    @SerializedName("page")
    @Expose
    private Integer page;
    @SerializedName("searchCategoriees")
    @Expose
    private List<SearchCategoriee> searchCategoriees = null;
    @SerializedName("total_pages")
    @Expose
    private Integer totalPages;
    @SerializedName("total_results")
    @Expose
    private Integer totalResults;

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public List<SearchCategoriee> getSearchCategoriees() {
        return searchCategoriees;
    }

    public void setSearchCategoriees(List<SearchCategoriee> searchCategoriees) {
        this.searchCategoriees = searchCategoriees;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Integer getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(Integer totalResults) {
        this.totalResults = totalResults;
    }

}
