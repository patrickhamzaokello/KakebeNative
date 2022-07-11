package com.shop.kakebe.KaKebe.Apis;

import com.shop.kakebe.KaKebe.Models.Address;
import com.shop.kakebe.KaKebe.Models.CreateAddressModel;
import com.shop.kakebe.KaKebe.Models.CreateAddressResponse;
import com.shop.kakebe.KaKebe.Models.HomeCategories;
import com.shop.kakebe.KaKebe.Models.OrderRequest;
import com.shop.kakebe.KaKebe.Models.OrderResponse;
import com.shop.kakebe.KaKebe.Models.ProductDetail;
import com.shop.kakebe.KaKebe.Models.SearchHome;
import com.shop.kakebe.KaKebe.Models.SearchResult;
import com.shop.kakebe.KaKebe.Models.SelectedCategory;
import com.shop.kakebe.KaKebe.Models.UserOrders;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;


public interface ShopApiEndPoints {

//    http://localhost:8080/projects/KakebeAPI/Requests/menus/featuredcategoryProducts.php?page=1&category=4
    @GET("menus/featuredcategoryProducts.php")
    Call<SelectedCategory> getSelectedCategory(
            @Query("category") int menu_category_id,
            @Query("page") int pageIndex
    );

    //  http://localhost:8080/projects/KakebeAPI/Requests/menus/selectedProduct.php?page=2&category=41&productID=45
    @GET("menus/selectedProduct.php")
    Call<ProductDetail> getMenuDetails(
            @Query("productID") int menu_id,
            @Query("category") int menu_category_id,
            @Query("page") int pageIndex
    );



    @GET("category/allcombined.php")
    Call<HomeCategories> getMenuCategoriesSection(
            @Query("page") int pageIndex
    );

    // http://localhost:8080/projects/KakebeAPI/Requests/category/search.php?query=rings& page=1
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


    //fetch past orders
    @GET("orders/userOrders.php")
    @Headers("Cache-Control: no-cache")
    Call<UserOrders> getUserOrders(
            @Query("customerId") int customerID,
            @Query("page") int pageIndex
    );


    //post all Orders
    @POST("orders/create_order.php")
    Call<OrderResponse> postCartOrder(
            @Body OrderRequest orderRequest
    );



    // http://localhost:8080/projects/KakebeAPI/Requests/Address/userAddress.php?userId=49&page=1
    @GET("Address/userAddress.php")
    @Headers("Cache-Control: no-cache")
    Call<Address> getAddresses(
            @Query("userId") int userId,
            @Query("page") int page
    );


    //post Address
    @POST("Address/create_address.php")
    @Headers("Cache-Control: no-cache")
    Call<CreateAddressResponse> postCreateAddress(
            @Body CreateAddressModel createAddressModel
    );


    //http://localhost:8080/projects/KakebeAPI/Requests/City/cities.php
    @GET("City/cities.php")
    Call<String[]> getCities();


}