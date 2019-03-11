package pt.jpdinis;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;

import android.support.v7.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A login screen that offers login via Email/Password.
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private LoginActivity loginActivity=null;

    // UI references.
    private EditText mUserView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    boolean cancel = false;
    View focusView = null;
    CloudDriveApi service;
    Retrofit retrofit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_login);
        loginActivity=this;

        // Set up the login form.
        mUserView = (EditText) findViewById(R.id.userEditText);

        mPasswordView = (EditText) findViewById(R.id.passEditText);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    showProgress(true);
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_page);
        mProgressView = findViewById(R.id.loginProgress);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid Email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        mUserView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String Username = mUserView.getText().toString();
        String Password = mPasswordView.getText().toString();

        Call<JsonElement> login =  CloudDriveApi.service.login(Username,Password);

        login.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if(response.body().getAsJsonObject().has("User")){
                    User user = new Gson().fromJson(response.body().getAsJsonObject().get("User").getAsJsonObject(),User.class);

                    if(response.body().getAsJsonObject().get("Error").getAsJsonObject().has("msg")){
                        if(response.body().getAsJsonObject().get("Error").getAsJsonObject().get("msg").getAsJsonNull() == JsonNull.INSTANCE){
                            new CloudPreferences(loginActivity).setUser(user);
                        }else {

                            if (response.body().getAsJsonObject().get("Error").getAsJsonObject().get("msg").getAsString().contains("Password")) {
                                mPasswordView.setError(getString(R.string.errorIncorrectPassword));
                                focusView = mPasswordView;
                                cancel = true;
                            }

                            if (response.body().getAsJsonObject().get("Error").getAsJsonObject().get("msg").getAsString().contains("User")) {
                                mUserView.setError(getString(R.string.errorInvalidUsername));
                                focusView = mUserView;
                                cancel = true;
                            }
                        }
                    }else{
                        new CloudPreferences(loginActivity).setUser(user);
                    }
                }else{
                    mUserView.setError(getString(R.string.errorInvalidUsername));
                    focusView = mUserView;
                    cancel = true;
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                finish();
            }
        });

        try {


            if (cancel) {
                // There was an error; don't attempt login and focus the first
                // form field with an error.
                focusView.requestFocus();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        Intent activityIntent = new Intent(loginActivity, MainActivity.class);
        startActivity(activityIntent);
        finish();
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}

