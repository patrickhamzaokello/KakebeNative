
package com.shop.kakebe.KaKebe.Models;

import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class ProductDetail {

    @SerializedName("page")
    @Expose
    private Integer page;
    @SerializedName("selectedProduct")
    @Expose
    private List<SelectedProduct> selectedProduct = null;
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

    public List<SelectedProduct> getSelectedProduct() {
        return selectedProduct;
    }

    public void setSelectedProduct(List<SelectedProduct> selectedProduct) {
        this.selectedProduct = selectedProduct;
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
