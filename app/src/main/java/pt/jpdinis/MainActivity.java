package pt.jpdinis;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.os.Environment.getExternalStorageDirectory;

public class MainActivity extends AppCompatActivity
       implements NavigationView.OnNavigationItemSelectedListener {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private MainActivity mainActivity;


    public static final int REQUEST_WRITE_STORAGE = 112;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        recyclerView = (RecyclerView) findViewById(R.id.fileList);
        recyclerView.setHasFixedSize(false);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        mainActivity=this;

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(CloudDriveApi.ApiURL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        CloudDriveApi service = retrofit.create(CloudDriveApi.class);
        Call<JsonElement> files = service.listFiles();

        files.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
               JsonArray files = response.body().getAsJsonObject().getAsJsonArray("files");
               ArrayList<String> filenames = new ArrayList<>();

               for(int i = 0; i<files.size();i++){
                   filenames.add(files.get(i).getAsString());
               }

                String[] filex = filenames.toArray(new String[0]);
                mAdapter = new FileAdapter(filex);
                recyclerView.setAdapter(mAdapter);
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void logout(View view){
        JSONObject object = new JSONObject();
        /*CloudDriveApi.PostJSONData postJSONRunnable = new CloudDriveApi.PostJSONData("https://clouddriveserver.azurewebsites.net/smartlogout",object);
        Thread t = new Thread(postJSONRunnable);
        t.start();
        JSONObject data;

        while ((data=postJSONRunnable.getData())==null);

        SharedPreferences sharedPreferences = getSharedPreferences("CloudDrive",0);
        sharedPreferences.edit().clear().commit();

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        TextView navTitle = (TextView) findViewById(R.id.nav_title);
        TextView navSubtitle = (TextView) findViewById(R.id.nav_subtitle);
        User user = new CloudPreferences(mainActivity).getUser();
        navTitle.setText(user.getUsername());
        navSubtitle.setText(user.getEmail());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_local_files) {
            verifyStoragePermissions(mainActivity);
            requestPermission(mainActivity);

            File file = new File(getExternalStorageDirectory().getAbsolutePath() + "/CloudDrive");

            mAdapter = new FileAdapter(file.list());
            recyclerView.setAdapter(mAdapter);
        } else if (id == R.id.nav_online_files) {
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(CloudDriveApi.ApiURL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

            CloudDriveApi service = retrofit.create(CloudDriveApi.class);
            Call<JsonElement> files = service.listFiles();

            files.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    JsonArray files = response.body().getAsJsonObject().getAsJsonArray("files");
                    ArrayList<String> filenames = new ArrayList<>();

                    for(int i = 0; i<files.size();i++){
                        filenames.add(files.get(i).getAsString());
                    }

                    String[] filex = filenames.toArray(new String[0]);
                    mAdapter = new FileAdapter(filex);
                    recyclerView.setAdapter(mAdapter);
                }

                @Override
                public void onFailure(Call<JsonElement> call, Throwable t) {

                }
            });
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void requestPermission(Activity context) {
        boolean hasPermission = (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(context,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        }
    }

    private boolean[] checkExternalMedia(){
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // Can read and write the media
            return new boolean[]{  true, true};
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // Can only read the media
            return new boolean[]{  true, false};
        } else {
            // Can't read or write
            return new boolean[]{  false, false};
        }
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private boolean writeResponseBodyToDisk(ResponseBody body,String filename) {
        verifyStoragePermissions(mainActivity);
        requestPermission(mainActivity);

        if(!checkExternalMedia()[0] && !checkExternalMedia()[1]){
            return false;
        }

        try {
            // todo change the file location/name according to your needs
            File dir = new File(getExternalStorageDirectory().getAbsolutePath() + "/CloudDrive");
            if(!dir.exists()) {
                dir.mkdirs();
            }

            File file = new File(getExternalStorageDirectory().getAbsolutePath() + File.separator + "CloudDrive" + File.separator + filename);
            if(!file.exists()){
                file.createNewFile();
            }

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[1024];

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(file);
                int read =0;

                while ((read = inputStream.read(fileReader))!=-1) {
                    outputStream.write(fileReader, 0, read);
                }

                outputStream.flush();

                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }

    public void download(View view) {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(CloudDriveApi.ApiURL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        CloudDriveApi service = retrofit.create(CloudDriveApi.class);
        Call<ResponseBody> files = service.downloadFile(((TextView)view.findViewById(R.id.textView)).getText().toString());
        final String filename = ((TextView)view.findViewById(R.id.textView)).getText().toString();

        files.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(mainActivity, "Server contacted and has file", Toast.LENGTH_SHORT);

                    boolean writtenToDisk = writeResponseBodyToDisk(response.body(),filename);

                    Toast.makeText(mainActivity, "Download successful? " + writtenToDisk, Toast.LENGTH_LONG);
                }else{
                    Toast.makeText(mainActivity, "Server contact failed",Toast.LENGTH_SHORT);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(mainActivity, "Download failed: "+t.getMessage(),Toast.LENGTH_LONG);
            }
        });

    }
}
