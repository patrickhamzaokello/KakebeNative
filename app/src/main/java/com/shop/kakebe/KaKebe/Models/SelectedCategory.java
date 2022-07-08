
package com.shop.kakebe.KaKebe.Models;

import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class SelectedCategory {

    @SerializedName("page")
    @Expose
    private Integer page;
    @SerializedName("SelectedCategoryResult")
    @Expose
    private List<SelectedCategoryResult> selectedCategoryResult = null;
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

    public List<SelectedCategoryResult> getSelectedCategoryResult() {
        return selectedCategoryResult;
    }

    public void setSelectedCategoryResult(List<SelectedCategoryResult> selectedCategoryResult) {
        this.selectedCategoryResult = selectedCategoryResult;
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
