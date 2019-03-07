package pt.jpdinis;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class LauncherActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent activityIntent = null;
        User.GetUserData(this);
        JSONObject data = null;

        if (User.Username != null) {
            try {
                JSONObject object = new JSONObject();
                object.put("username",User.Username);

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

        if(activityIntent!=null) {
            startActivity(activityIntent);
        }

        finish();
    }
}
