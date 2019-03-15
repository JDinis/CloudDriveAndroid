package pt.jpdinis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Streaming;

public interface CloudDriveApi {
    String ApiURL = "https://clouddrive.azurewebsites.net:443/";
    String LocalApiURL = "http://192.168.1.5:3001/";

    Gson gson = new GsonBuilder()
            .setLenient()
            .create();

    OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .readTimeout(1, TimeUnit.MINUTES)
            .connectTimeout(1, TimeUnit.MINUTES)
            .build();

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(CloudDriveApi.ApiURL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();

    CloudDriveApi service = retrofit.create(CloudDriveApi.class);

    @GET("files")
    Call<JsonElement> listFiles();

    @FormUrlEncoded
    @POST("islogged")
    Call<JsonElement> isLogged(@Field("username") String username);

    @POST("smartlogout")
    Call<JsonElement> logout();

    @FormUrlEncoded
    @POST("login")
    Call<JsonElement> login(@Field("username") String username, @Field("password") String password);

    @Streaming
    @GET("/search/{filename}")
    Call<ResponseBody> downloadFile(@Path("filename") String filename);

    @Multipart
    @POST("/files/uploadsmart/{username}")
    Call<JsonElement> uploadFile(@Part MultipartBody.Part files,@Path("username") String username);

    @Multipart
    @POST("/files/uploadsmart/{username}")
    Call<JsonElement> uploadFiles(@Part List<MultipartBody.Part> files,@Path("username") String username);

    @DELETE("/files/{filename}")
    Call<JsonElement> deleteFile(@Path("filename") String filename);
}


