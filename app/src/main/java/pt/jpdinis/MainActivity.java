package pt.jpdinis;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.os.Environment.getExternalStorageDirectory;

public class MainActivity extends AppCompatActivity
       implements NavigationView.OnNavigationItemSelectedListener {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private MainActivity mainActivity;
    boolean download = true;


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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        recyclerView = findViewById(R.id.fileList);
        recyclerView.setHasFixedSize(false);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        mainActivity=this;

        Call<JsonElement> files =  CloudDriveApi.service.listFiles();

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
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void logout(View view){
        Call<JsonElement> logout = CloudDriveApi.service.logout();

        logout.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if(response.body().getAsJsonObject().get("success").getAsBoolean()){
                    SharedPreferences sharedPreferences = getSharedPreferences( getString(R.string.appName),0);
                    sharedPreferences.edit().clear().commit();

                    Intent intent = new Intent(mainActivity, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Toast.makeText(mainActivity,getString(R.string.logoutFailed),Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        TextView navTitle = findViewById(R.id.nav_title);
        TextView navSubtitle = findViewById(R.id.nav_subtitle);
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

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_local_files) {
            verifyStoragePermissions(mainActivity);
            requestPermission(mainActivity);

            File file = new File(getExternalStorageDirectory().getAbsolutePath() +File.separator+ getString(R.string.appName));

            mAdapter = new FileAdapter(file.list());
            recyclerView.setAdapter(mAdapter);
            download=false;
        } else if (id == R.id.nav_online_files) {
            download=true;
            Call<JsonElement> files =  CloudDriveApi.service.listFiles();

            files.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    JsonArray files = response.body().getAsJsonObject().getAsJsonArray("files");

                    if (files != null) {
                        ArrayList<String> filenames = new ArrayList<>();

                        for (int i = 0; i < files.size(); i++) {
                            filenames.add(files.get(i).getAsString());
                        }

                        String[] filex = filenames.toArray(new String[0]);
                        mAdapter = new FileAdapter(filex);
                        recyclerView.setAdapter(mAdapter);
                    }
                }

                @Override
                public void onFailure(Call<JsonElement> call, Throwable t) {

                }
            });
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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

    private Boolean writeResponseBodyToDisk(ResponseBody body,String filename) {
        WriteToDiskTask asyncTask = new WriteToDiskTask();
        asyncTask.execute(new WriteToDiskParams(body,filename));

        try {
            return asyncTask.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void download(View view) {
        if(download) {
            final String filename = ((TextView) view.findViewById(R.id.textView)).getText().toString();
            Call<ResponseBody> files =  CloudDriveApi.service.downloadFile(filename);

            files.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(mainActivity, getString(R.string.fileExistsServer), Toast.LENGTH_SHORT).show();

                        boolean writtenToDisk = writeResponseBodyToDisk(response.body(), filename);

                        Toast.makeText(mainActivity, getString(R.string.downloadSuccess) + writtenToDisk, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(mainActivity, getString(R.string.connectionFailed), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(mainActivity, getString(R.string.downloadFailed) + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }else{
            String filename = getExternalStorageDirectory().getAbsolutePath() + File.separator +  getString(R.string.appName)+File.separator+((TextView) view.findViewById(R.id.textView)).getText().toString();
            File dir = new File(filename);
            RequestBody file = RequestBody.create(MediaType.parse(getMimeType(filename)),dir);
            MultipartBody.Part part = MultipartBody.Part.createFormData(
                    "files",
                    dir.getName(),
                    file);
            Call<JsonElement> files =  CloudDriveApi.service.uploadFile(part);

            files.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    if (response.body().getAsJsonObject().get("success").getAsBoolean()) {
                        Toast.makeText(mainActivity, getString(R.string.uploadSuccess), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mainActivity, getString(R.string.uploadFailed), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<JsonElement> call, Throwable t) {
                    Toast.makeText(mainActivity, getString(R.string.uploadFailed)+ ": " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = url.substring(url.lastIndexOf(".")+1);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    private static class WriteToDiskParams {
        ResponseBody body;
        String filename;

        WriteToDiskParams(ResponseBody body,String filename) {
            this.body = body;
            this.filename = filename;
        }
    }


    private class WriteToDiskTask extends AsyncTask<WriteToDiskParams,Void,Boolean>{
        @Override
        protected Boolean doInBackground(WriteToDiskParams... params) {
            verifyStoragePermissions(mainActivity);
            requestPermission(mainActivity);

            if (!checkExternalMedia()[0] && !checkExternalMedia()[1]) {
                return false;
            }

            try {
                // todo change the file location/name according to your needs
                File dir = new File(getExternalStorageDirectory().getAbsolutePath() + File.separator + getString(R.string.appName));
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                File file = new File(getExternalStorageDirectory().getAbsolutePath() + File.separator + getString(R.string.appName) + File.separator + params[0].filename);
                if (!file.exists()) {
                    file.createNewFile();
                }

                InputStream inputStream = null;
                OutputStream outputStream = null;

                try {
                    byte[] fileReader = new byte[1024];

                    inputStream = params[0].body.byteStream();
                    outputStream = new FileOutputStream(file);
                    int read = 0;

                    while ((read = inputStream.read(fileReader)) != -1) {
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
    }
}
