package com.pkasemer.kakebeshoplira.Apis;

import com.pkasemer.kakebeshoplira.Models.HomeBannerModel;
import com.pkasemer.kakebeshoplira.Models.HomeCategories;
import com.pkasemer.kakebeshoplira.Models.HomeMenuCategoryModel;
import com.pkasemer.kakebeshoplira.Models.OrderRequest;
import com.pkasemer.kakebeshoplira.Models.OrderResponse;
import com.pkasemer.kakebeshoplira.Models.SearchHome;
import com.pkasemer.kakebeshoplira.Models.SearchResult;
import com.pkasemer.kakebeshoplira.Models.SelectedCategoryMenuItem;
import com.pkasemer.kakebeshoplira.Models.UserOrders;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;


public interface MovieService {

    @GET("menus/menupages.php")
    Call<SelectedCategoryMenuItem> getTopRatedMovies(
            @Query("category") int menu_category_id,
            @Query("page") int pageIndex
    );

    @GET("menus/topmenuitems.php")
    Call<SelectedCategoryMenuItem> getTopMenuItems(
            @Query("page") int pageIndex
    );


    @GET("category/allcombined.php")
    Call<HomeCategories> getMenuCategoriesSection(
            @Query("page") int pageIndex
    );

    //    http://192.168.0.199:8080/projects/KakebeAPI/Requests/category/search.php?query=rings& page=1
    @GET("category/search.php")
    Call<SearchResult> getSearch(
            @Query("query") String queryString,
            @Query("page") int pageIndex
    );

    //    http://192.168.0.199:8080/projects/KakebeAPI/Requests/category/searchhomepage.php?page=1
    @GET("category/searchhomepage.php")
    Call<SearchHome> getMostSearched(
            @Query("page") int pageIndex
    );


    //    menus/menudetails.php?menuId=9&category=3&page=1
    @GET("menus/menudetails.php")
    Call<SelectedCategoryMenuItem> getMenuDetails(
            @Query("menuId") int menu_id,
            @Query("category") int menu_category_id,
            @Query("page") int pageIndex
    );


    //post all Orders
    @POST("orders/create_order.php")
    Call<OrderResponse> postCartOrder(
            @Body OrderRequest orderRequest
    );


    //fetch past orders
    @GET("orders/userOrders.php")
    Call<UserOrders> getUserOrders(
            @Query("customerId") int customerID,
            @Query("page") int pageIndex
    );


}