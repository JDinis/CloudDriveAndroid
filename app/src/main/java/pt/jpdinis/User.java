package pt.jpdinis;


import android.app.Activity;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Context.MODE_PRIVATE;

public class User {
    static String Username,FirstName,LastName,Email;

    public static void SetUserData(JSONObject data, Activity activity){
        try {
            SharedPreferences settings = activity.getSharedPreferences("CloudDrive", MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            JSONObject user = data.getJSONObject("User");
            Username = user.getString("username");
            FirstName = user.has("firstName") ? user.getString("firstName") : "";
            LastName = user.has("lastName") ? user.getString("lastName") : "";
            Email = user.has("email") ? user.getString("email") : "";
            editor.putString("username",Username);
            editor.putString("firstName",FirstName);
            editor.putString("lastName",LastName);
            editor.putString("email",Email);
            editor.commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void GetUserData(Activity activity){
        SharedPreferences settings = activity.getSharedPreferences("CloudDrive", MODE_PRIVATE);
        Username = settings.getString("username",null);
        FirstName = settings.getString("firstName",null);
        LastName = settings.getString("lastName",null);
        Email = settings.getString("email",null);
    }
}
