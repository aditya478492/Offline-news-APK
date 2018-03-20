package com.example.aditya.news_offline;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    ListView lst=null;
    ArrayList<String> arr=new ArrayList<>();
    ArrayList<String> url_cont=new ArrayList<>();
    ArrayAdapter<String> adp=null;
    SQLiteDatabase news_db;
    public class Download_topsories extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... params) {
           news_db.execSQL("DELETE FROM news2");
            try {
                String res="";
                URL url=new URL(params[0]);
                HttpsURLConnection uc= (HttpsURLConnection) url.openConnection();
                InputStream is=uc.getInputStream();
                InputStreamReader reader=new InputStreamReader(is);
                int data=reader.read();
                while (data!=-1){
                   char current=(char)data;
                    res+=current;
                    data=reader.read();
                }
                JSONArray jarr=new JSONArray(res);
                for(int i=0;i<jarr.length();i++){
                    //Log.i("each element",jarr.getString(i));
                    String res1="";
                    URL url1=new URL("https://hacker-news.firebaseio.com/v0/item/"+jarr.getString(i)+".json?print=pretty");
                    HttpsURLConnection urlConnection= (HttpsURLConnection) url1.openConnection();
                    InputStream is1=urlConnection.getInputStream();
                    InputStreamReader reader1=new InputStreamReader(is1);
                    int data1=reader1.read();
                    while (data1!=-1){
                        char current1=(char)data1;
                        res1+=current1;
                        data1=reader1.read();
                    }
                    JSONObject jobj=new JSONObject(res1);
                    String obt_url=jobj.getString("title");
                    String obt_url1=jobj.getString("url");
                   /* String sub_str=obt_url1.substring(0,4);
                    String htt_res="";
                    URL url2=new URL(obt_url1);
                    if(sub_str=="https"){
                        HttpsURLConnection httpsuc= (HttpsURLConnection) url2.openConnection();
                        InputStream http_is=httpsuc.getInputStream();
                        InputStreamReader httpsreader=new InputStreamReader(http_is);
                        int https_data=httpsreader.read();
                        while (https_data!=-1){
                            char https_current=(char)https_data;
                            htt_res+=https_current;
                            https_data=httpsreader.read();
                        }
                    }else{
                        HttpsURLConnection httpuc= (HttpsURLConnection) url2.openConnection();
                        InputStream http_is=httpuc.getInputStream();
                        InputStreamReader http_reader=new InputStreamReader(http_is);
                        int http_data=http_reader.read();
                        while (http_data!=-1){
                            char http_current=(char)http_data;
                            htt_res+=http_current;
                            http_data=http_reader.read();
                        }
                    }*/
                    String sql="INSERT INTO news2(title,content) VALUES(?,?)";
                    SQLiteStatement statement=news_db.compileStatement(sql);
                    statement.bindString(1,obt_url);
                    statement.bindString(2,obt_url1);
                    statement.execute();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Cursor c=news_db.rawQuery("SELECT * FROM news2",null);
            int title_index=c.getColumnIndex("title");
            int url_cont_index=c.getColumnIndex("content");
            if(c.moveToFirst() && c!=null){
                do{
                    arr.add(c.getString(title_index));
                    url_cont.add(c.getString(url_cont_index));
                    Log.i("eeeeeeeeeeeeee",c.getString(title_index));
                }while (c.moveToNext());
            }
            adp.notifyDataSetChanged();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lst=(ListView)findViewById(R.id.lst_vw);
        news_db=this.openOrCreateDatabase("news2",MODE_PRIVATE,null);
        news_db.execSQL("CREATE TABLE IF NOT EXISTS news2(title VRACHAR,content VARCHAR)");
        adp=new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,arr);
        Download_topsories dt=new Download_topsories();
        dt.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");
        adp=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,arr);
        lst.setAdapter(adp);
        lst.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(getApplicationContext(),Main2Activity.class);
                intent.putExtra("url_data",url_cont.get(position).toString());
                startActivity(intent);
            }
        });
    }
}
