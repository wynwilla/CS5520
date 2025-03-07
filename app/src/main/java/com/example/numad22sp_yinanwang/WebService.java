package com.example.numad22sp_yinanwang;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;


public class WebService extends AppCompatActivity {
    //Creating the essential parts needed for a Recycler view to work: RecyclerView, Adapter, LayoutManager
    private ArrayList<ServiceCard> serviceList = new ArrayList<>();
    ;
    private static final String TAG = "WebServiceActivity";
    private RecyclerView recyclerView;
    private SviewAdapter sviewAdapter;
    private RecyclerView.LayoutManager rLayoutManger;
    private static final String KEY_OF_INSTANCE = "KEY_OF_INSTANCE";
    private static final String NUMBER_OF_ITEMS = "NUMBER_OF_ITEMS";

    private Handler handler=new Handler();
    private int mProgress = 0;
    private ProgressBar progressBar;

    private class MultiThread extends Thread{

        @Override
        public void run(){
            while(mProgress<100){
                mProgress++;
                android.os.SystemClock.sleep(10);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setProgress(mProgress);//refresh UI
                    }
                });
            }
            try {
                String mealDB="https://www.themealdb.com/api/json/v1/1/filter.php?a=Canadian";
                URL url = new URL(mealDB);
                // Get String response from the url address
                String resp = NetworkUtil.httpResponse(url);
                //Log.i("resp",resp);
                JSONObject jObject = new JSONObject();
                // Transform String into JSONObject
                jObject = new JSONObject(resp);
                JSONArray jsonArray = jObject.getJSONArray("meals");
                //Log.i("resp",jsonArray.getString(0));
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject body = jsonArray.getJSONObject(i);
                    String serviceName=body.getString("strMeal");
                    String serviceImg = body.getString("strMealThumb");
                    String serviceID = body.getString("idMeal");
                    Bitmap bm = getInternetPicture(serviceImg);
                    ServiceCard serviceCard = new ServiceCard(bm,serviceID,serviceName);
                    serviceList.add(serviceCard);
                }

                handler.post(new Runnable() {
                                 @Override
                                 public void run() {
                                     progressBar.setVisibility(View.INVISIBLE);
                                     createRecyclerView();//更新UI
                                 }
                             });
                //Log.i("jTitle",jObject.getString("title"));
                //Log.i("jBody",jObject.getString("body"));

            } catch (MalformedURLException e) {
                Log.e(TAG,"MalformedURLException");
                e.printStackTrace();
            } catch (ProtocolException e) {
                Log.e(TAG,"ProtocolException");
                e.printStackTrace();
            } catch (IOException e) {
                Log.e(TAG,"IOException");
                e.printStackTrace();
            } catch (JSONException e) {
                Log.e(TAG,"JSONException");
                e.printStackTrace();
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webservice);
        init(savedInstanceState);

        Button webservice = (Button) findViewById(R.id.ping);
        webservice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar=(ProgressBar) findViewById(R.id.progress);
                progressBar.setVisibility(View.VISIBLE);
                MultiThread mt = new MultiThread();
                Thread mt1 = new Thread(mt, "Service");
                mt.start();
                createRecyclerView();
                //sviewAdapter = new SviewAdapter(serviceList);
                //recyclerView.setAdapter(sviewAdapter);
            }
        });

    }


    private void init(Bundle savedInstanceState) {
        initialItemData(savedInstanceState);
        createRecyclerView();
    }


    private void initialItemData(Bundle savedInstanceState) {

        // Not the first time to open this Activity
        if (savedInstanceState != null && savedInstanceState.containsKey(NUMBER_OF_ITEMS)) {
            if (serviceList == null || serviceList.size() == 0) {

                int size = savedInstanceState.getInt(NUMBER_OF_ITEMS);

                // Retrieve keys we stored in the instance
                for (int i = 0; i < size; i++) {
                    String serviceImg = savedInstanceState.getString(KEY_OF_INSTANCE + i + "0");
                    String serviceID = savedInstanceState.getString(KEY_OF_INSTANCE + i + "1");
                    String serviceName = savedInstanceState.getString(KEY_OF_INSTANCE + i + "2");
                    Bitmap bm = getInternetPicture(serviceImg);


                    ServiceCard serviceCard = new ServiceCard(bm,serviceID,serviceName);

                    serviceList.add(serviceCard);
                }
            }
        }
        // The first time to opne this Activity
        //else {
        //ItemCard item1 = new ItemCard(R.drawable.pic_gmail_01, "Gmail", "Example description", false);
        //ItemCard item2 = new ItemCard(R.drawable.pic_google_01, "Google", "Example description", false);
        //ItemCard item3 = new ItemCard(R.drawable.pic_youtube_01, "Youtube", "Example description", false);
        //itemList.add(item1);
        //itemList.add(item2);
        //itemList.add(item3);
        //}

    }


    private void createRecyclerView() {
        rLayoutManger = new LinearLayoutManager(this);
        recyclerView = findViewById(R.id.service_view);
        recyclerView.setHasFixedSize(true);

        sviewAdapter = new SviewAdapter(serviceList);
        recyclerView.setAdapter(sviewAdapter);
        recyclerView.setLayoutManager(rLayoutManger);
    }


    static class SviewAdapter extends RecyclerView.Adapter<SviewHolder> {

        private final ArrayList<ServiceCard> itemList;
        private LinkClickListener listener;

        //Constructor
        public SviewAdapter(ArrayList<ServiceCard> itemList) {
            this.itemList = itemList;
        }

        @Override
        public SviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.service_card, parent, false);
            return new SviewHolder(view, listener);
        }

        @Override
        public void onBindViewHolder(SviewHolder holder, int position) {
            ServiceCard currentItem = itemList.get(position);

            holder.itemImg.setImageBitmap(currentItem.getServiceImg());
            holder.itemID.setText(currentItem.getServiceID());
            holder.itemName.setText(currentItem.getServiceName());

        }

        @Override
        public int getItemCount() {
            return itemList.size();
        }
    }


    static class SviewHolder extends RecyclerView.ViewHolder {
        public ImageView itemImg;
        public TextView itemID;
        public TextView itemName;

        public SviewHolder(View itemView, final LinkClickListener listener) {
            super(itemView);
            itemImg = itemView.findViewById(R.id.item_icon);
            itemID = itemView.findViewById(R.id.item_id);
            itemName = itemView.findViewById(R.id.item_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getLayoutPosition();
                        if (position != RecyclerView.NO_POSITION) {

                            listener.onLinkClick(position);
                        }
                    }
                }
            });
        }
    }


    public Bitmap getInternetPicture(String UrlPath) {
        Bitmap bm = null;
        String urlpath = UrlPath;

        try {
            URL uri = new URL(urlpath);

            HttpURLConnection connection = (HttpURLConnection) uri.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(5000);
            connection.setConnectTimeout(5000);
            connection.connect();

            if (connection.getResponseCode() == 200) {
                InputStream is = connection.getInputStream();
                bm = BitmapFactory.decodeStream(is);
            } else {
                bm = null;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bm;
    }


}
