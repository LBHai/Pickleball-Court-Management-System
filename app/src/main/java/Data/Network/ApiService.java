package Data.Network;

import java.util.List;

import Data.Model.CUser;
import Data.Model.ChangePasswordRequest;
import Data.Model.CheckInvalidSlotsRequest;
import Data.Model.CheckInvalidSlotsResponse;
import Data.Model.CourtImage;
import Data.Model.CourtPrice;
import Data.Model.Courts;
import Data.Model.CourtSlot;
import Data.Model.CreateOrderRegularRequest;
import Data.Model.CreateOrderRequest;
import Data.Model.CreateOrderResponse;
import Data.Model.ForgetPasswordRequest;
import Data.Model.GetToken;
import Data.Model.MyInfoResponse;
import Data.Model.NotificationRequest;
import Data.Model.NotificationResponse;
import Data.Model.Orders;
import Data.Model.QRPaymentRemaining;
import Data.Model.Service;
import Data.Model.ServiceOrderRequest;
import Data.Model.StudentRegistrationRequest;
import Data.Model.Transaction;
import Data.Model.UnreadResponse;
import Data.Model.User;
import Data.Model.UpdateMyInfor;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("identity/auth/token")
    Call<GetToken> getToken(@Body User user);

    @POST("identity/users/create_user")
    Call<GetToken> registerUser(@Body CUser cUser);

    @GET("court/public/getAll")
    Call<List<Courts>> getCourts();

    @GET("court/public/booking_slot")
    Call<List<CourtSlot>> getBookingSlots(
            @Query("courtId") String courtId,
            @Query("dateBooking") String dateBooking
    );

    @GET("identity/users/my-info")
    Call<MyInfoResponse> getMyInfo(@Header("Authorization") String authHeader);

    @GET("court/public/{clubId}")
    Call<Courts> getCourtById(@Path("clubId") String clubId);

    @POST("identity/public/create_order")
    Call<CreateOrderResponse> createOrder(@Body CreateOrderRequest request);

    @PUT("identity/users/update")
    Call<UpdateMyInfor> updateMyInfo(@Header("Authorization") String authHeader, @Body UpdateMyInfor updateUser);

    @GET("identity/public/getOrders")
    Call<List<Orders>> getOrders(@Query("value") String value);

    @POST("identity/public/notification/save-token") // Điều chỉnh endpoint theo API của bạn
    Call<Void> registerNotification(@Body NotificationRequest request);

    // GET danh sách thông báo


    @GET("court/public/court_price/getByCourtId/{courtId}")
    Call<CourtPrice> getCourtPriceByCourtId(@Path("courtId") String courtId);

    @GET("identity/public/getOrderById")
    Call<Orders> getOrderById(@Query("orderId") String orderId);

    @PUT("identity/public/cancelOrder")
    Call<Orders> cancelOrder(@Query("orderId") String orderId);

    @POST("identity/public/change_order")
    Call<CreateOrderResponse> changeOrder(@Query("orderId") String orderId, @Body CreateOrderRequest request);

    @GET("court/public/court-images/list")
    Call<List<CourtImage>> getCourtImages(
            @Query("courtId") String courtId,
            @Query("isMap") boolean isMap
    );


    @PUT("identity/users/change-password")
    Call<Void> changePassword(@Header("Authorization") String authHeader, @Body ChangePasswordRequest request);

    @POST("identity/public/check-invalid-slots")
    Call<CheckInvalidSlotsResponse> checkInvalidSlots(@Body CheckInvalidSlotsRequest request);

    @GET("identity/public/payment-value")
    Call<Double> getPaymentValue(
            @Query("courtId") String courtId,
            @Query("daysOfWeek") String daysOfWeek,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate,
            @Query("startTime") String startTime,
            @Query("endTime") String endTime
    );

    @POST("identity/public/order-fixed")
    Call<CreateOrderResponse> createFixedOrder(@Body CreateOrderRegularRequest request);
    @Multipart
    @POST("identity/users/upload-avatar")
    @Headers("Accept: text/plain")
    Call<String> uploadAvatar(
            @Header("Authorization") String token,
            @Part MultipartBody.Part file,
            @Part("oldPath") RequestBody oldPath
    );
    @GET("notifications/by-phone")
    Call<NotificationResponse> getNotificationsByPhone(@Query("phone") String phoneNumber);
    @GET("court/public/getServices")
    Call<List<Service>> getServices(@Query("courtId") String courtId);
    // Lấy danh sách thông báo
    @GET("identity/public/notification/getNotifications")
    Call<NotificationResponse> getNotifications(@Query("value") String value);

    // Lấy số lượng thông báo chưa đọc
    @GET("identity/public/notification/courtUnRead")
    Call<UnreadResponse> getUnreadCount(@Query("value") String value);

    // Đánh dấu thông báo đã đọc
    @PUT("identity/public/notification/read")
    Call<Void> markAsRead(@Query("id") String notificationId);
    @POST("identity/public/order/service")
    Call<CreateOrderResponse> createServiceOrder(@Body ServiceOrderRequest request);
    @GET("identity/public/getTransactionHistory")
    Call<List<Transaction>> getTransactionHistory(@Query("orderId") String orderId);
    @Headers("Content-Type: application/json")
    @POST("identity/auth/forgetPassword")
    Call<Void> forgetPassword(@Body ForgetPasswordRequest body);
    @POST("identity/users/registerForStudent")
    Call<Void> registerStudent(@Body StudentRegistrationRequest req);

    @POST("identity/public/paymentOrder")
    Call<String> createPaymentForRemaining(@Query("orderId") String orderId);

}
