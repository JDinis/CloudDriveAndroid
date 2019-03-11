package pt.jpdinis;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.gson.JsonElement;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LauncherActivity extends AppCompatActivity {
    JsonElement response;
    LauncherActivity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_launcher);

        activity = this;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

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
    }
}
