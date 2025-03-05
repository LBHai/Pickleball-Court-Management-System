package Api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.List;
import Model.CUser;
import Model.Courts;
import Model.CourtSlot;
import Model.CreateOrderRequest;
import Model.CreateOrderResponse;
import Model.GetToken;
import Model.MyInfoResponse;
import Model.User;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // link API: http://203.145.46.242:8080/api/
    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();

    ApiService apiService = new Retrofit.Builder()
            .baseUrl("http://203.145.46.242:8080/api/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService.class);

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

    // --- Endpoint tạo đơn hàng ---
    @POST("identity/public/create_order")
    Call<CreateOrderResponse> createOrder(@Body CreateOrderRequest request);
}
