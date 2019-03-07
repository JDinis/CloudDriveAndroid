package pt.jpdinis;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

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


        CloudDriveApi.GetJSONData getJSONData = new CloudDriveApi.GetJSONData("https://clouddriveserver.azurewebsites.net/files",new JSONObject());
        Thread t = new Thread(getJSONData);
        t.start();
        JSONObject response;

        while ((response=getJSONData.getData())==null);

        String[] dataset = null;

        try {
            JSONArray files = response.getJSONArray("files");
            dataset = new String[files.length()];
            for(int i = 0;i<files.length();i++){
                dataset[i]=files.getString(i);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(dataset!=null) {
            mAdapter = new FileAdapter(dataset);
            recyclerView.setAdapter(mAdapter);
        }
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
        CloudDriveApi.PostJSONData postJSONRunnable = new CloudDriveApi.PostJSONData("https://clouddriveserver.azurewebsites.net/smartlogout",object);
        Thread t = new Thread(postJSONRunnable);
        t.start();
        JSONObject data;

        while ((data=postJSONRunnable.getData())==null);

        SharedPreferences sharedPreferences = getSharedPreferences("CloudDrive",0);
        sharedPreferences.edit().clear().commit();

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        TextView navTitle = (TextView) findViewById(R.id.nav_title);
        TextView navSubtitle = (TextView) findViewById(R.id.nav_subtitle);
        navTitle.setText(User.Username);
        navSubtitle.setText(User.Email);
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
            // Handle the camera action
        } else if (id == R.id.nav_online_files) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
