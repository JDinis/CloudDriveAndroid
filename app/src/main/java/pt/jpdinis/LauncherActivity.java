package pt.jpdinis;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
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

        setContentView(R.layout.activity_launcher);

        activity = this;

        User user = new CloudPreferences(this).getUser();
        Intent activityIntent = null;

        if(user.getUsername()!=null) {
            Call<JsonElement> isLogged = CloudDriveApi.service.isLogged(user.Username);

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
                    recreate();
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
