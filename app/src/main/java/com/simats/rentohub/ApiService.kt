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
    @POST("delete.php")
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
    @POST("booking.php") // Check if your file is named booking.php or add_booking.php
    fun placeBooking(
        @Field("user_id") userId: String,
        @Field("equipment_id") equipmentId: String,
        @Field("days") days: String,
        @Field("location") location: String,
        @Field("status") status: String,
        @Field("payment_id") paymentId: String?
    ): Call<BookingResponse>

    // ================= GET USER BOOKINGS =================
    @FormUrlEncoded
    @POST("view_bookings.php")
    fun getUserBookings(
        @Field("user_id") userId: String
    ): Call<UserBookingsResponse>

    // ================= UPDATE BOOKING STATUS =================
    @FormUrlEncoded
    @POST("update_booking_status.php")
    fun updateBookingStatus(
        @Field("booking_id") bookingId: String,
        @Field("status") status: String
    ): Call<UpdateProductResponse> // Reusing UpdateProductResponse if it has status/message, or create a new one.

    // ================= GET ALL BOOKINGS (ADMIN) =================
    @GET("view_all_bookings.php")
    fun getAllBookings(): Call<UserBookingsResponse>

    @FormUrlEncoded
    @POST("get_booking_details.php")
    fun getBookingDetails(
        @Field("booking_id") bookingId: String
    ): Call<BookingDetailsResponse>

    // ================= GOOGLE SYNC =================
    @FormUrlEncoded
    @POST("google_sync.php")
    fun syncGoogleUser(
        @Field("name") name: String,
        @Field("email") email: String
    ): Call<LoginResponse>
}
