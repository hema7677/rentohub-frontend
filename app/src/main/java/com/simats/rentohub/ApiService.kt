package com.simats.rentohub

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    // ================= REGISTER =================
    @FormUrlEncoded
    @POST("register.php")
    fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("usertype") usertype: String
    ): Call<RegisterResponse>


    // ================= LOGIN =================
    @FormUrlEncoded
    @POST("login.php")
    fun loginUser(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<LoginResponse>


    // ================= ADD EQUIPMENT =================
    @Multipart
    @POST("add_equipment.php")
    fun addProduct(
        @Part("name") name: RequestBody,
        @Part("brand") brand: RequestBody,
        @Part("category") category: RequestBody,
        @Part("daily_rate") dailyRate: RequestBody,
        @Part("deposit") deposit: RequestBody,
        @Part("description") description: RequestBody,
        @Part image: MultipartBody.Part
    ): Call<AddProductResponse>


    // ================= GET ALL PRODUCTS =================
    @GET("view_equipment.php")
    fun getProducts(): Call<ProductResponse>


    // ================= DELETE PRODUCT =================
    @FormUrlEncoded
    @POST("delet.php")
    fun deleteProduct(@Field("id") id: Int): Call<DeleteResponse>


    @Multipart
    @POST("update.php")
    fun updateProduct(
        @Part("id") id: RequestBody,
        @Part("name") name: RequestBody,
        @Part("brand") brand: RequestBody,
        @Part("category") category: RequestBody,
        @Part("daily_rate") dailyRate: RequestBody,
        @Part("deposit") deposit: RequestBody,
        @Part("description") description: RequestBody,
        @Part image: MultipartBody.Part?
    ): Call<UpdateProductResponse>

    @GET("view_equipment.php")
    fun getEquipment(): Call<EquipmentResponse>

    // ================= GET EQUIPMENT DETAILS =================
    @GET("view_equipment_id.php")
    fun getEquipmentDetails(@Query("id") id: Int): Call<EquipmentDetailResponse>

    // ================= PLACE BOOKING =================
    @FormUrlEncoded
    @POST("book_equipment.php") // Assumed filename based on context
    fun placeBooking(
        @Field("user_id") userId: String,
        @Field("equipment_id") equipmentId: String,
        @Field("days") days: String
    ): Call<BookingResponse>

}
