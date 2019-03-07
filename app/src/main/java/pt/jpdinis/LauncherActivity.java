package pt.jpdinis;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class LauncherActivity extends AppCompatActivity {
    JsonElement response;
    LauncherActivity activity;
    Intent activityIntent = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(CloudDriveApi.ApiURL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        activity = this;

        CloudDriveApi service = retrofit.create(CloudDriveApi.class);
        User user = new Preferences(this).getUser();
        Intent activityIntent = null;

        if(user.getUsername()!=null) {
            Call<JsonElement> isLogged = service.isLogged(user.Username);

            isLogged.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    try {
                            if (response.body().getAsJsonObject().get("isLoggedIn").getAsBoolean()) {
                                startActivity(new Intent(activity, MainActivity.class));
                                finish();
                            } else {
                                startActivity(new Intent(activity, LoginActivity.class));
                                finish();
                            }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<JsonElement> call, Throwable t) {
                    Log.e("[JSON]", t.getMessage());
                }
            });
        }else{
            startActivity(new Intent(activity, LoginActivity.class));
            finish();
        }


        /*Intent activityIntent = null;
        User.GetUserData(this);
        JSONObject data = null;

        if (User.Username != null) {
            try {
                JSONObject object = new JSONObject();
                object.put("Username",User.Username);

                CloudDriveApi.PostJSONData postJSONRunnable = new CloudDriveApi.PostJSONData("https://clouddriveserver.azurewebsites.net/islogged",object);
                Thread t = new Thread(postJSONRunnable);
                t.start();

                while ((data=postJSONRunnable.getData())==null);

            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (data.getBoolean("isLoggedIn")) {
                    activityIntent = new Intent(this, MainActivity.class);
                } else {
                    activityIntent = new Intent(this, LoginActivity.class);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            activityIntent = new Intent(this, LoginActivity.class);
        }


        finish();*/
    }

    public void setActivityIntent(Intent intent) {
       this.activityIntent = intent;
    }
}
