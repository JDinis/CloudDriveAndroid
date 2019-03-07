package pt.jpdinis;

import android.os.Environment;
import android.util.JsonReader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.Buffer;
import java.nio.charset.Charset;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface CloudDriveApi {
    static String ApiURL = "https://clouddriveserver.azurewebsites.net/";

    @GET("files")
    Call<JsonElement> listFiles();

    @FormUrlEncoded
    @POST("islogged")
    Call<JsonElement> isLogged(@Field("username") String username);

    @FormUrlEncoded
    @POST("login")
    Call<JsonElement> login(@Field("username") String username,@Field("password") String password);

    @Streaming
    @GET("/search/{filename}")
    Call<ResponseBody> downloadFile(@Path("filename") String filename);
}


