package pt.jpdinis;

import android.app.Activity;
import android.content.SharedPreferences;

public class CloudPreferences {
    SharedPreferences Preferences;

    public CloudPreferences(Activity activity){
        this.Preferences = activity.getSharedPreferences(activity.getString(R.string.appName),0);
    }

    User getUser(){
        return new User(Preferences.getString("firstName", null),Preferences.getString("lastName", null),Preferences.getString("username", null),Preferences.getString("password", null),Preferences.getString("email", null),Preferences.getString("profilepic", null),Preferences.getBoolean("admin", false));
    }

    void setUser(String firstName,String lastName,String username,String password,String email,String profilepic,Boolean admin){
        Preferences.edit().putString("firstName", firstName).commit();
        Preferences.edit().putString("lastName", lastName).commit();
        Preferences.edit().putString("username", username).commit();
        Preferences.edit().putString("password", password).commit();
        Preferences.edit().putString("email", email).commit();
        Preferences.edit().putString("profilepic", profilepic).commit();
        Preferences.edit().putBoolean("admin", admin).commit();
    }

    void setUser(User user){
        Preferences.edit().putString("firstName", user.getFirstName()).commit();
        Preferences.edit().putString("lastName", user.getLastName()).commit();
        Preferences.edit().putString("username", user.getUsername()).commit();
        Preferences.edit().putString("password", user.getPassword()).commit();
        Preferences.edit().putString("email", user.getEmail()).commit();
        Preferences.edit().putString("profilepic", user.getProfilePic()).commit();
        Preferences.edit().putBoolean("admin", user.isAdmin()).commit();
    }
}
