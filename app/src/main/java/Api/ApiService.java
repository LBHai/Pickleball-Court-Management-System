package Api;

import java.util.List;

import Model.CUser;
import Model.Courts;
import Model.CourtSlot;
import Model.CreateOrderRequest;
import Model.CreateOrderResponse;
import Model.GetToken;
import Model.MyInfoResponse;
import Model.User;
import Model.UpdateMyInfor;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
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

}
