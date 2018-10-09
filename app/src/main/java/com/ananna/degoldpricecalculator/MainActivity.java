package com.ananna.degoldpricecalculator;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import android.widget.TextView;
import android.widget.Toast;

import com.ananna.degoldpricecalculator.helper.EndPoints;
import com.ananna.degoldpricecalculator.model.Currency;
import com.ananna.degoldpricecalculator.utills.CustomOnItemSelectedListener;
import com.ananna.degoldpricecalculator.utills.NetworkStateReceiver;
import com.android.degoldpricecalculator.BuildConfig;
import com.android.degoldpricecalculator.R;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.text.NumberFormat;

public class MainActivity extends AppCompatActivity implements NetworkStateReceiver.NetworkStateReceiverListener {
    private Spinner spn_weightUnit, spn_karat;
    private Button btnCalculate;
    int karats = 100;
    private double pricePerGram;
    private double pricePerUnit;

    private EditText etPricepergram;
    private EditText etWeight;
    private double weightGram;
    private double resultTOz;
    private double resultOz;
    private double resultKG;
    private double resultGram;

    private TextView tvResultTOz;
    private double liveGOldPrice;
    private double currencyBDT;
    private Currency currencyObject;
    private double pricePerTOZ;
    private double pricePerkarat = 0.00;


    private final double TROYOZ_TO_GRAM = 31.1035;
    private final double GRAM_TO_TROYOZ = 0.032151;

    private final double TROYOZ_TO_OZ = 1.09714;
    private final double TROYOZ_TO_TOLA = 28.3495;
    private final double TROYOZ_TO_KG = 0.0311035;

    private TextView tvusdRate;
    private TextView tvusd_rate_gram;

    private double pricePerUnitGram;
    private double pricePerUnitTOz;
    private double pricePerUnitOz;
    private double pricePerUnitKG;
    private double result;
    private TextView tvResultOz;
    private TextView tvResultKG;
    private TextView tvResultGram;
    private NumberFormat nf;
    private AdView mAdView;
    private NetworkStateReceiver networkStateReceiver;
    private TextView tvtitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goldapp);

        nf = NumberFormat.getNumberInstance();
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);

        addItemsOnSpnner();
        addListenerOnBtnCalculate();
        addListenerOnSpinnerItemSelection();


        //Ads initialization
        if (BuildConfig.BUILD_TYPE.contentEquals("release"))
            adsInitialization();

        //Network Change Detection Receiver
        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(MainActivity.this);
        this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));

    }

    private void adsInitialization() {
        MobileAds.initialize(this, getString(R.string.admob_app_id));
        mAdView = (AdView) findViewById(R.id.adView);
        //mAdView.setAdSize(AdSize.BANNER);
        //mAdView.setAdUnitId(getString(R.string.banner_home_footer));
        //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
        AdRequest adRequest = new AdRequest.Builder()
                .build();

        mAdView.loadAd(adRequest);

        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
            }

            @Override
            public void onAdClosed() {
                Log.d("Ads", "Ad is closed!!");
                mAdView.loadAd(new AdRequest.Builder().build());
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                Log.d("Ads", "Ad failed to load! error code: " + errorCode);
            }

            @Override
            public void onAdLeftApplication() {
                Log.d("Ads", "Ad left application!");
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
            }
        });
    }


    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        networkStateReceiver.removeListener(this);
        this.unregisterReceiver(networkStateReceiver);

        if (mAdView != null) {
            mAdView.destroy();
        }
    }

    @Override
    public void networkAvailable() {
        Log.d("Network", "Available!");
        if (mAdView != null)
            mAdView.setVisibility(View.VISIBLE);
    /* TODO: Your connection-oriented stuff here */
    }

    @Override
    public void networkUnavailable() {
        Log.d("Network", "Unavailable");
        if (mAdView != null)
            mAdView.setVisibility(View.GONE);
    /* TODO: Your disconnection-oriented stuff here */
    }

    // add items into spinner dynamically
    public void addItemsOnSpnner() {
        spnGoldCaratItems();
        spnweightUnitItems();

    }

    private void spnGoldCaratItems() {
        spn_karat = (Spinner) findViewById(R.id.spn_karat);
        ArrayAdapter<CharSequence> dataAdapter = ArrayAdapter.createFromResource(this,
                R.array.gold_Karat, android.R.layout.simple_spinner_item);


        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spn_karat.setAdapter(dataAdapter);
    }

    private void spnweightUnitItems() {
        spn_weightUnit = (Spinner) findViewById(R.id.spn_weightUnit);
        ArrayAdapter<CharSequence> dataWAdapter = ArrayAdapter.createFromResource(this,
                R.array.weight_unit, android.R.layout.simple_spinner_item);


        dataWAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spn_weightUnit.setAdapter(dataWAdapter);
    }

    public void addListenerOnSpinnerItemSelection() {
        spn_weightUnit = (Spinner) findViewById(R.id.spn_weightUnit);
        spn_weightUnit.setOnItemSelectedListener(new CustomOnItemSelectedListener());
    }

    // get the selected dropdown list value
    public void addListenerOnBtnCalculate() {

        spn_weightUnit = (Spinner) findViewById(R.id.spn_weightUnit);
        spn_karat = (Spinner) findViewById(R.id.spn_karat);
        etPricepergram = (EditText) findViewById(R.id.etPricepergram);
        etWeight = (EditText) findViewById(R.id.etWeight);
        tvResultTOz = (TextView) findViewById(R.id.tvResult);
        tvResultOz = (TextView) findViewById(R.id.tvResultOz);
        tvResultKG = (TextView) findViewById(R.id.tvResultKG);
        tvResultGram = (TextView) findViewById(R.id.tvResultGM);
        tvusdRate = (TextView) findViewById(R.id.tvusd_rate);
        tvtitle = (TextView) findViewById(R.id.tvtitle);
       /* Typeface font = Typeface.createFromAsset(getAssets(), "fonts/roboto_regular.ttf");
        tvtitle.setTypeface(font);*/


        tvusd_rate_gram = (TextView) findViewById(R.id.tvusd_rate_gram);

        btnCalculate = (Button) findViewById(R.id.btnCalculate);

        requestToWeb();
        currencyRequest();

        btnCalculate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(etWeight.getText())) {
                    Toast.makeText(MainActivity.this, "Please input some weight to calculate", Toast.LENGTH_SHORT).show();
                    return;
                }

                weightGram = Double.parseDouble(etWeight.getText().toString());
                String name = String.valueOf(spn_weightUnit.getSelectedItem());
                Log.v("MainActivity.this",
                        "OnClickListener : " +
                                "\nWeight Unit: " + String.valueOf(spn_weightUnit.getSelectedItem()) +
                                "\nGold Carat: " + String.valueOf(spn_karat.getSelectedItem()));
                karats = Integer.parseInt(String.valueOf((spn_karat.getSelectedItemId())));

                goldPriceCalculation();

            }

        });
    }

    public double convertToGram(double pricePerUnitTOz) {

        pricePerGram = pricePerUnitTOz / TROYOZ_TO_GRAM;
        return pricePerGram;
    }

    public double convertToOz(double pricePerUnitTOz) {
        pricePerGram = pricePerUnitTOz / TROYOZ_TO_OZ;
        return pricePerGram;
    }

    public double convertToKG(double pricePerUnitTOz) {
        pricePerGram = (pricePerUnitTOz / TROYOZ_TO_KG);
        return pricePerGram;
    }

    public void goldPriceCalculation() {

        /*if (!TextUtils.isEmpty(etPricepergram.getText().toString())) {
            pricePerUnitGram = Double.parseDouble(etPricepergram.getText().toString());
            pricePerUnitTOz = pricePerUnitGram * GRAM_TO_TROYOZ;
        } else {
            pricePerUnitTOz = liveGOldPrice;
        }
*/
        pricePerUnitTOz = liveGOldPrice;
        resultTOz = pricePerUnitBypurity(pricePerUnitTOz);
        tvResultTOz.setText(String.format("%s (in TOz)", nf.format(resultTOz)));

        pricePerUnitOz = convertToOz(pricePerUnitTOz);
        resultOz = pricePerUnitBypurity(pricePerUnitOz);
        tvResultOz.setText(String.format("%s (in Oz)", nf.format(resultOz)));

        pricePerUnitGram = convertToGram(pricePerUnitTOz);
        resultGram = pricePerUnitBypurity(pricePerUnitGram);
        tvResultGram.setText(String.format("%s (in Gram)", nf.format(resultGram)));

        pricePerUnitKG = convertToKG(pricePerUnitTOz);
        resultKG = pricePerUnitBypurity(pricePerUnitKG);
        tvResultKG.setText(String.format("%s (in KG)", nf.format(resultKG)));

    }

    public double pricePerUnitBypurity(double pricePerUnit) {
        switch (karats) {
            case 1: /** Start a new Activity MyCards.java 24 K */
                result = pricePerUnit * weightGram;

                break;

            case 2: /** AlerDialog when click on Exit 22K */
                pricePerkarat = (double) 22 / 24;
                result = pricePerUnit * weightGram * pricePerkarat;
                break;
            case 3: /** Start a new Activity MyCards.java 21 K*/
                pricePerkarat = (double) 21 / 24;

                result = pricePerUnit * weightGram * pricePerkarat;

                break;

            case 4: /** AlerDialog when click on Exit 18k*/
                pricePerkarat = (double) 18 / 24;
                result = pricePerUnit * weightGram * pricePerkarat;

                break;
            case 5: /** Start a new Activity MyCards.java 14 K*/
                pricePerkarat = (double) 14 / 24;
                result = pricePerUnit * weightGram * pricePerkarat;

                break;

            case 6: /** AlerDialog when click on Exit 10 K */
                pricePerkarat = (double) 10 / 24;
                result = pricePerUnit * weightGram * pricePerkarat;

                break;
            case 7: /** Start a new Activity MyCards.java 6 K*/
                pricePerkarat = (double) 6 / 24;
                result = pricePerUnit * weightGram * pricePerkarat;
                break;
        }

        return result;
    }

    public void requestToWeb() {
        RequestQueue queue = Volley.newRequestQueue(this);
        if (isNetworkAvailable()) {
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, EndPoints.GETDATA, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if (response.length() == 0) {
                            Toast.makeText(MainActivity.this, "Not found", Toast.LENGTH_LONG).show();
                            return;

                        }
                        // System.out.println(response.getJSONObject("dataset").getJSONArray("data").getJSONArray(0));
                        //Toast.makeText(MainActivity.this, response.getJSONObject("dataset").getJSONArray("data").getJSONArray(0).get(1).toString(), Toast.LENGTH_SHORT).show();
                        liveGOldPrice = Double.parseDouble(response.getJSONObject("dataset").getJSONArray("data").getJSONArray(0).get(1).toString());
                        tvusdRate.setText(String.format("%s (per TOz)", nf.format(liveGOldPrice)));
                        tvusd_rate_gram.setText(String.format("%s (per Gram)", nf.format(convertToGram(liveGOldPrice))));

                    } catch (final Exception error) {
                        error.printStackTrace();
                        Log.e("ERROR", error.toString());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(final VolleyError error) {
                    Log.e("ERROR", error.toString());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            });
            queue.add(jsonObjectRequest);
        }
    }

    public void currencyRequest() {
        RequestQueue queueCurrency = Volley.newRequestQueue(this);
        if (isNetworkAvailable()) {
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, EndPoints.GETCURRENCY, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if (response.length() == 0) {
                            Toast.makeText(MainActivity.this, "Not found", Toast.LENGTH_LONG).show();
                            return;

                        }

                        Gson gson = new Gson();
                        // String jsonKey = "quotes" ;
                        currencyObject = gson.fromJson(response.toString(), Currency.class);
                        Log.i("Error", currencyObject.getQuotes().getUSDBDT().toString());
                        currencyBDT = Double.parseDouble(currencyObject.getQuotes().getUSDBDT().toString());

                        // Toast.makeText(MainActivity.this, currencyObject.getQuotes().getUSDBDT().toString(), Toast.LENGTH_SHORT).show();

                    } catch (final Exception error) {
                        error.printStackTrace();
                        Log.e("ERROR", error.toString());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(final VolleyError error) {
                    Log.e("ERROR", error.toString());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            });
            queueCurrency.add(jsonObjectRequest);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void clearItems(View v) {
        etWeight.setText("");
        etWeight.requestFocus();
        etPricepergram.setText("");
        tvResultOz.setText("Oz");
        tvResultKG.setText("KG");
        tvResultGram.setText("Gram");
        tvResultTOz.setText("TOz");
    }


}
