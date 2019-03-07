package pt.jpdinis;

import android.os.Environment;
import android.util.JsonReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.Buffer;
import java.nio.charset.Charset;

public class CloudDriveApi {

    public static BufferedInputStream GetFileData(String url) throws Exception{
        BufferedInputStream data = null;
        try {
            HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
            connection.setRequestMethod("get");
            connection.setConnectTimeout(10000);
            connection.connect();
            InputStream inputStream = connection.getInputStream();
            int lenghtOfFile = connection.getContentLength();

            data = new BufferedInputStream(inputStream,lenghtOfFile);

        } catch (IOException e) {
            e.printStackTrace();
        }

        if(data != null){
            return data;
        }else{
            throw new Exception("Failed to obtain json response.");
        }
    }

    private static String readFromInputStream(InputStream inputStream){
        StringBuilder stringBuilder = new StringBuilder();
        if(inputStream!=null){
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = null;
            try {
                while ((line = bufferedReader.readLine())!=null){
                    stringBuilder.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return stringBuilder.toString();
    }

    public static class PostJSONData implements Runnable{
        private String url;
        private JSONObject params;
        private JSONObject data = null;

        public PostJSONData(String url,JSONObject params) {
            this.url=url;
            this.params=params;
        }

        @Override
        public void run() {
            try  {
                HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();

                try {
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    connection.setRequestProperty("Accept","application/json");
                    connection.setConnectTimeout(10000);
                    connection.connect();

                    DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                    outputStream.writeBytes(params.toString());

                    outputStream.flush();
                    outputStream.close();

                    InputStream inputStream = connection.getInputStream();
                    data = new JSONObject(readFromInputStream(inputStream));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public JSONObject getData() {
            return data;
        }
    }

    public static class GetJSONData implements Runnable{
        private String url;
        private JSONObject params;
        private JSONObject data = null;

        public GetJSONData(String url,JSONObject params) {
            this.url=url;
            this.params=params;
        }

        @Override
        public void run() {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

                try {
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setConnectTimeout(10000);
                    connection.connect();

                    DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                    outputStream.writeBytes(params.toString());

                    outputStream.flush();
                    outputStream.close();

                    InputStream inputStream = connection.getInputStream();
                    data = new JSONObject(readFromInputStream(inputStream));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public JSONObject getData() {
            return data;
        }
    }
}


