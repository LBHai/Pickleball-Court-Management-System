package Api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import Model.CUser;
import Model.GetToken;
import Model.User;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {


    //link API: http://203.145.46.242:8080/api/
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
    Call<GetToken> getToken(@Body CUser cUser);
}
