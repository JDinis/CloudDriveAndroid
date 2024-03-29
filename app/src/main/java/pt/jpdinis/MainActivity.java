package pt.jpdinis;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
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
    public static final int REQUEST_WRITE_STORAGE = 112;
    public static String Channel_ID = "CloudDrive";
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    static boolean download = true;
    private static RecyclerView recyclerView;
    private static MainActivity mainActivity;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    NotificationManagerCompat notificationManagerCompat;
    NotificationManager notificationManager;
    Intent notificationIntent;
    PendingIntent notificationPendingIntent;

    NotificationCompat.Builder builderCompat;
    Notification.Builder builder;

    private static void requestPermission(Activity context) {
        boolean hasPermission = (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(context,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
            CharSequence name = getString(R.string.appName);
            String description = getString(R.string.appName);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(name.toString(), name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
    }

    private static boolean[] checkExternalMedia() {
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // Can read and write the media
            return new boolean[]{true, true};
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // Can only read the media
            return new boolean[]{true, false};
        } else {
            // Can't read or write
            return new boolean[]{false, false};
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

    private static Boolean writeResponseBodyToDisk(ResponseBody body, String filename) {
        WriteToDiskTask asyncTask = new WriteToDiskTask();
        asyncTask.execute(new WriteToDiskParams(body, filename));

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

    public static void download(View view) {
        final String filename = ((TextView) view.findViewById(R.id.textView)).getText().toString();
        Call<ResponseBody> files = CloudDriveApi.service.downloadFile(filename);

        files.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(mainActivity, mainActivity.getString(R.string.fileExistsServer), Toast.LENGTH_SHORT).show();

                    boolean writtenToDisk = writeResponseBodyToDisk(response.body(), filename);

                    Toast.makeText(mainActivity, mainActivity.getString(R.string.downloadSuccess) + writtenToDisk, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mainActivity, mainActivity.getString(R.string.connectionFailed), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(mainActivity, mainActivity.getString(R.string.downloadFailed) + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public static void upload(View view) {
        final String filename = getExternalStorageDirectory().getAbsolutePath() + File.separator + mainActivity.getString(R.string.appName) + File.separator + ((TextView) view.findViewById(R.id.textView)).getText().toString();
        File dir = new File(filename);
        RequestBody file = RequestBody.create(MediaType.parse(getMimeType(filename)), dir);
        MultipartBody.Part part = MultipartBody.Part.createFormData(
                "files",
                dir.getName(),
                file);
        Call<JsonElement> files = CloudDriveApi.service.uploadFile(part,new CloudPreferences(mainActivity).getUser().Username);

        files.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.body().getAsJsonObject().get("success").getAsBoolean()) {
                    Toast.makeText(mainActivity, mainActivity.getString(R.string.uploadSuccess), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mainActivity, mainActivity.getString(R.string.uploadFailed), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Toast.makeText(mainActivity, mainActivity.getString(R.string.uploadFailed) + ": " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public static void uploadSingle(Intent intent) {
        Uri fileUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        File dir = null;

        if (fileUri == null || !new File(fileUri.getPath()).getName().contains(".") && MediaType.parse(intent.getType())==null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mainActivity.finishAffinity();
                System.exit(0);
            } else {
                intent = mainActivity.getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage(mainActivity.getBaseContext().getPackageName());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mainActivity.startActivity(intent);
            }

        if (fileUri.getScheme().equals("content")) {
            try {
                dir = mainActivity.getFileFromContentUri(mainActivity.getContentResolver().openInputStream(fileUri), new File(fileUri.getPath()).getName());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else if (fileUri.getScheme().equals("file")) {
            dir = new File(fileUri.getPath());
        }

        MediaType mediaType = MediaType.parse("text/plain");

        if(MediaType.parse(intent.getType())!=null){
            mediaType=MediaType.parse(intent.getType());
        }else{
            String fn = dir.getName();
            mediaType = MediaType.parse(getMimeType(fn));
        }

        final String filename = dir.getName().contains(".") ? dir.getName() :  dir.getName()+"."+mediaType.subtype();

        RequestBody file = RequestBody.create(mediaType, dir);
        MultipartBody.Part part = MultipartBody.Part.createFormData(
                "files",
                filename,
                file);
        Call<JsonElement> files = CloudDriveApi.service.uploadFile(part,new CloudPreferences(mainActivity).getUser().Username);

        files.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.body().getAsJsonObject().get("success").getAsBoolean()) {
                    Toast.makeText(mainActivity, mainActivity.getString(R.string.uploadSuccess), Toast.LENGTH_SHORT).show();
                    if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O) {
                        mainActivity.builder.setContentIntent(mainActivity.notificationPendingIntent).setContentText(mainActivity.getString(R.string.uploadSuccess));
                        mainActivity.notificationManager.notify(88591, mainActivity.builder.build());
                    }else {
                        mainActivity.builderCompat.setContentIntent(mainActivity.notificationPendingIntent).setContentText(mainActivity.getString(R.string.uploadSuccess));
                        mainActivity.builderCompat.build();
                    }


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        mainActivity.finishAffinity();
                        System.exit(0);
                    } else {
                        Intent intent = mainActivity.getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(mainActivity.getBaseContext().getPackageName());
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        mainActivity.startActivity(intent);
                    }

                } else {
                    if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O) {
                        mainActivity.builder.setContentIntent(mainActivity.notificationPendingIntent).setContentText(mainActivity.getString(R.string.uploadFailed));
                        mainActivity.notificationManager.notify(88591, mainActivity.builder.build());
                    }else {
                        mainActivity.builderCompat.setContentIntent(mainActivity.notificationPendingIntent).setContentText(mainActivity.getString(R.string.uploadFailed));
                        mainActivity.builderCompat.build();
                    }
                    Toast.makeText(mainActivity, mainActivity.getString(R.string.uploadFailed), Toast.LENGTH_SHORT).show();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        mainActivity.finishAffinity();
                        System.exit(0);
                    } else {
                        Intent intent = mainActivity.getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(mainActivity.getBaseContext().getPackageName());
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        mainActivity.startActivity(intent);
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Toast.makeText(mainActivity, mainActivity.getString(R.string.uploadFailed) + ": " + t.getMessage(), Toast.LENGTH_LONG).show();

                if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O) {
                    mainActivity.builder.setContentIntent(mainActivity.notificationPendingIntent).setContentText(mainActivity.getString(R.string.uploadFailed)+": "+t.getMessage());
                    mainActivity.notificationManager.notify(88591, mainActivity.builder.build());
                }else {
                    mainActivity.builderCompat.setContentIntent(mainActivity.notificationPendingIntent).setContentText(mainActivity.getString(R.string.uploadFailed)+": "+t.getMessage());
                    mainActivity.builderCompat.build();
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mainActivity.finishAffinity();
                    System.exit(0);
                } else {
                    Intent intent = mainActivity.getBaseContext().getPackageManager()
                            .getLaunchIntentForPackage(mainActivity.getBaseContext().getPackageName());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    mainActivity.startActivity(intent);
                }
            }
        });
    }

    public static void uploadMultiple(Intent intent) {
        ArrayList<Uri> fileUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        List<MultipartBody.Part> parts = new ArrayList<>();

        for (Uri fileUri :
                fileUris) {
            File dir = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                if (fileUri != null && new File(fileUri.getPath()).getName().contains(".") || intent.getClipData().getDescription().getMimeTypeCount()>0) {
                    if (fileUri.getScheme().equals("content")) {
                        try {
                            dir = mainActivity.getFileFromContentUri(mainActivity.getContentResolver().openInputStream(fileUri), new File(fileUri.getPath()).getName());
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    } else if (fileUri.getScheme().equals("file")) {
                        dir = new File(fileUri.getPath());
                    }

                    MediaType mediaType = MediaType.parse(intent.getClipData().getDescription().getMimeType(0));

                    final String filename = dir.getName().contains(".") ? dir.getName() :  dir.getName()+"."+mediaType.subtype();
                    RequestBody file = RequestBody.create(MediaType.parse(getMimeType(filename)), dir);
                    MultipartBody.Part part = MultipartBody.Part.createFormData(
                            "files",
                            filename,
                            file);
                    parts.add(part);
                }
            }
        }

        Call<JsonElement> files = CloudDriveApi.service.uploadFiles(parts,new CloudPreferences(mainActivity).getUser().Username);

        files.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.body().getAsJsonObject().get("success").getAsBoolean()) {
                    Toast.makeText(mainActivity, mainActivity.getString(R.string.uploadsSuccess), Toast.LENGTH_SHORT).show();

                    if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O) {
                        mainActivity.builder.setContentIntent(mainActivity.notificationPendingIntent).setContentText(mainActivity.getString(R.string.uploadsSuccess));
                        mainActivity.notificationManager.notify(88591, mainActivity.builder.build());
                    }else {
                        mainActivity.builderCompat.setContentIntent(mainActivity.notificationPendingIntent).setContentText(mainActivity.getString(R.string.uploadsSuccess));
                        mainActivity.builderCompat.build();
                    }
                } else {
                    Toast.makeText(mainActivity, mainActivity.getString(R.string.uploadsFailed), Toast.LENGTH_SHORT).show();

                    if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O) {
                        mainActivity.builder.setContentIntent(mainActivity.notificationPendingIntent).setContentText(mainActivity.getString(R.string.uploadsFailed));
                        mainActivity.notificationManager.notify(88591, mainActivity.builder.build());
                    }else {
                        mainActivity.builderCompat.setContentIntent(mainActivity.notificationPendingIntent).setContentText(mainActivity.getString(R.string.uploadsFailed));
                        mainActivity.builderCompat.build();
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mainActivity.finishAffinity();
                    System.exit(0);
                } else {
                    Intent intent = mainActivity.getBaseContext().getPackageManager()
                            .getLaunchIntentForPackage(mainActivity.getBaseContext().getPackageName());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    mainActivity.startActivity(intent);
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Toast.makeText(mainActivity, mainActivity.getString(R.string.uploadsFailed) + ": " + t.getMessage(), Toast.LENGTH_LONG).show();

                if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O) {
                    mainActivity.builder.setContentIntent(mainActivity.notificationPendingIntent).setContentText(mainActivity.getString(R.string.uploadsFailed)+": "+t.getMessage());
                    mainActivity.notificationManager.notify(88591, mainActivity.builder.build());
                }else {
                    mainActivity.builderCompat.setContentIntent(mainActivity.notificationPendingIntent).setContentText(mainActivity.getString(R.string.uploadsFailed)+": "+t.getMessage());
                    mainActivity.builderCompat.build();
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mainActivity.finishAffinity();
                    System.exit(0);
                } else {
                    Intent intent = mainActivity.getBaseContext().getPackageManager()
                            .getLaunchIntentForPackage(mainActivity.getBaseContext().getPackageName());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    mainActivity.startActivity(intent);
                }
            }
        });
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = url.substring(url.lastIndexOf(".") + 1);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }

        if (type == null) {
            type = "application/octet-stream";
        }

        return type;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        notificationIntent = new Intent(this, MainActivity.class);
        notificationPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, getString(R.string.appName))
                    .setSmallIcon(R.drawable.upload)
                    .setContentTitle(getString(R.string.appName))
                    .setContentText("")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setChannelId(Channel_ID);
        }else{
            builderCompat = new NotificationCompat.Builder(this, getString(R.string.appName))
                    .setSmallIcon(R.drawable.upload)
                    .setContentTitle(getString(R.string.appName))
                    .setContentText("")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setChannelId(Channel_ID);
        }
        createNotificationChannel();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            uploadSingle(intent);
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            uploadMultiple(intent);
        } else {

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

            mainActivity = this;
            registerForContextMenu(recyclerView);

            Call<JsonElement> files = CloudDriveApi.service.listFiles();

            files.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    JsonArray files = response.body().getAsJsonObject().getAsJsonArray("files");
                    ArrayList<String> filenames = new ArrayList<>();

                    for (int i = 0; i < files.size(); i++) {
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
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int position = -1;
        try {
            position = ((FileAdapter) recyclerView.getAdapter()).getPosition();
        } catch (Exception e) {
            Log.d("[MyViewHolder]", e.getLocalizedMessage(), e);
            return super.onContextItemSelected(item);
        }
        switch (item.getItemId()) {
            case R.id.downloadMenuOption:
                download(recyclerView.getChildAt(position));
                break;
            case R.id.uploadMenuOption:
                upload(recyclerView.getChildAt(position));
                break;
            case R.id.deleteMenuOption:
                deleteFile(recyclerView.getChildAt(position));
                break;
        }
        return super.onContextItemSelected(item);
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

    public void logout(View view) {
        Call<JsonElement> logout = CloudDriveApi.service.logout();

        logout.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.body().getAsJsonObject().get("success").getAsBoolean()) {
                    SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.appName), 0);
                    sharedPreferences.edit().clear().commit();

                    Intent intent = new Intent(mainActivity, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Toast.makeText(mainActivity, getString(R.string.logoutFailed), Toast.LENGTH_LONG).show();
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

        switch (id) {
            case R.id.nav_local_files:
                verifyStoragePermissions(mainActivity);
                requestPermission(mainActivity);

                File file = new File(getExternalStorageDirectory().getAbsolutePath() + File.separator + getString(R.string.appName));

                mAdapter = new FileAdapter(file.list());
                recyclerView.setAdapter(mAdapter);
                download = false;
                break;
            case R.id.nav_online_files:
                download = true;
                Call<JsonElement> files = CloudDriveApi.service.listFiles();

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
                break;
            case R.id.nav_search_files:
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

                intent.addCategory(Intent.CATEGORY_OPENABLE);

                intent.setType("*/*");

                startActivityForResult(intent, 42);
                break;
            default:
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public File getFileFromContentUri(InputStream input, String filename) {
        File file = new File(getCacheDir(), filename);
        try {
            OutputStream output = new FileOutputStream(file);
            try {
                byte[] buffer = new byte[4 * 1024]; // or other buffer size
                int read;

                while ((read = input.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }

                output.flush();
            } finally {
                input.close();
                output.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        if (requestCode == 42 && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            File dir = null;

            if (resultData != null) {
                uri = resultData.getData();

                if (uri.getScheme().equals("content")) {
                    try {
                        dir = getFileFromContentUri(getContentResolver().openInputStream(uri), new File(uri.getPath()).getName());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                } else if (uri.getScheme().equals("file")) {
                    dir = new File(uri.getPath());
                }

                final String filename = dir.getName();
                RequestBody file = RequestBody.create(MediaType.parse(getMimeType(filename)), dir);
                MultipartBody.Part part = MultipartBody.Part.createFormData(
                        "files",
                        dir.getName(),
                        file);
                Call<JsonElement> files = CloudDriveApi.service.uploadFile(part,new CloudPreferences(mainActivity).getUser().Username);

                files.enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                        if (response.body().getAsJsonObject().get("success").getAsBoolean()) {
                            Toast.makeText(mainActivity, getString(R.string.uploadSuccess), Toast.LENGTH_SHORT).show();
                            ((FileAdapter) recyclerView.getAdapter()).addItem(filename);
                        } else {
                            Toast.makeText(mainActivity, getString(R.string.uploadFailed), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonElement> call, Throwable t) {
                        Toast.makeText(mainActivity, getString(R.string.uploadFailed) + ": " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }

    public void deleteFile(View view) {
        final String filename = ((TextView) view.findViewById(R.id.textView)).getText().toString();
        if (download) {
            Call<JsonElement> files = CloudDriveApi.service.deleteFile(filename);
            files.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    if (response.body().getAsJsonObject().get("success").getAsBoolean()) {
                        Toast.makeText(mainActivity, getString(R.string.fileDeleteSuccess), Toast.LENGTH_LONG).show();
                        ((FileAdapter) recyclerView.getAdapter()).removeItem(filename);
                    } else {
                        Toast.makeText(mainActivity, mainActivity.getString(R.string.fileDeleteFailed), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<JsonElement> call, Throwable t) {
                    Toast.makeText(mainActivity, mainActivity.getString(R.string.fileDeleteFailed) + ": " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            File dir = new File(getExternalStorageDirectory().getAbsolutePath() + File.separator + mainActivity.getString(R.string.appName) + File.separator + filename);
            if (dir.delete()) {
                Toast.makeText(mainActivity, getString(R.string.fileDeleteSuccess), Toast.LENGTH_LONG).show();
                ((FileAdapter) recyclerView.getAdapter()).removeItem(filename);
            } else {
                Toast.makeText(mainActivity, getString(R.string.fileDeleteFailed), Toast.LENGTH_LONG).show();
            }
        }
    }

    private static class WriteToDiskParams {
        ResponseBody body;
        String filename;

        WriteToDiskParams(ResponseBody body, String filename) {
            this.body = body;
            this.filename = filename;
        }
    }


    private static class WriteToDiskTask extends AsyncTask<WriteToDiskParams, Void, Boolean> {
        @Override
        protected Boolean doInBackground(WriteToDiskParams... params) {
            verifyStoragePermissions(mainActivity);
            requestPermission(mainActivity);

            if (!checkExternalMedia()[0] && !checkExternalMedia()[1]) {
                return false;
            }

            try {
                // todo change the file location/name according to your needs
                File dir = new File(getExternalStorageDirectory().getAbsolutePath() + File.separator + mainActivity.getString(R.string.appName));
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                File file = new File(getExternalStorageDirectory().getAbsolutePath() + File.separator + mainActivity.getString(R.string.appName) + File.separator + params[0].filename);
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
