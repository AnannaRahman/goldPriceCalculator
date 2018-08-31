package com.ananna.goldcalculator;

import android.content.Context;
import android.content.Intent;
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

import com.ananna.goldcalculator.helper.EndPoints;
import com.ananna.goldcalculator.model.Currency;
import com.ananna.goldcalculator.utills.CustomOnItemSelectedListener;
import com.ananna.goldcalculator.utills.NetworkStateReceiver;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdView;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.text.NumberFormat;

import mehdi.sakout.fancybuttons.FancyButton;

public class GoldCalculatorActivity extends AppCompatActivity {
    private Spinner spn_karat;
    private FancyButton btnCalculate;
    private double pricePerkarat = 0.00;
    int karats = 100;
    private double pricePerGram;
    private double pricePerUnit;

    private EditText etPricepergram;
    private EditText etWeightGram;
    private double weightGram;
    private double weightKiloGram;

    private double resultTOz;
    private double resultOz;
    private double resultKG;
    private double resultGram;

    private TextView tvResultTOz;
    private double liveGOldPrice;
    private double currencyBDT;
    private Currency currencyObject;
    private double pricePerTOZ;


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

    private NetworkStateReceiver networkStateReceiver;
    private TextView tvtitle;
    private EditText etWeightKiloGram;
    private TextView tvGoldPurity;
    private TextView tvLiveGoldPrice;
    private TextView tvTotalValue;
    private FancyButton btnReset;
    private FancyButton btnCheckWeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculate_price);

        nf = NumberFormat.getNumberInstance();
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);


        etPricepergram = (EditText) findViewById(R.id.etPricepergram);
        etWeightKiloGram = (EditText) findViewById(R.id.et_weightKiloGram);
        etWeightGram = (EditText) findViewById(R.id.et_weightGram);
        spn_karat = (Spinner) findViewById(R.id.spn_karat);
        //tvGoldPurity = (TextView) findViewById(R.id.tv_gold_purity);

        tvLiveGoldPrice = (TextView) findViewById(R.id.tvLiveGoldPrice);
        tvTotalValue = (TextView) findViewById(R.id.tvTotal_value);

        btnCalculate = (FancyButton) findViewById(R.id.btn_calculate);
        btnReset = (FancyButton) findViewById(R.id.btn_reset);


        btnCheckWeight = (FancyButton) findViewById(R.id.btn_check_weight);


        addListenerOnBtnCalculate();
        addItemsOnSpnner();
        requestToWeb();
        currencyRequest();

        btnCheckWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GoldCalculatorActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });


    }

    public void addItemsOnSpnner() {
        spnGoldCaratItems();


    }

    private void spnGoldCaratItems() {
        spn_karat = (Spinner) findViewById(R.id.spn_karat);
        ArrayAdapter<CharSequence> dataAdapter = ArrayAdapter.createFromResource(this,
                R.array.gold_Karat, android.R.layout.simple_spinner_item);


        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spn_karat.setAdapter(dataAdapter);
    }

    public double pricePerUnitBypurity(double pricePerUnit) {
        switch (karats) {
            case 1: /** Start a new Activity MyCards.java 24 K */
                result = pricePerUnit * weightGram;
                //tvGoldPurity.setText("99.95");
                break;

            case 2: /** AlerDialog when click on Exit 22K */
                pricePerkarat = (double) 22 / 24;
                //tvGoldPurity.setText("91.6");
                result = pricePerUnit * weightGram * pricePerkarat;
                break;
            case 3: /** Start a new Activity MyCards.java 21 K*/
                pricePerkarat = (double) 21 / 24;
                //tvGoldPurity.setText("87.5");
                result = pricePerUnit * weightGram * pricePerkarat;

                break;

            case 4: /** AlerDialog when click on Exit 18k*/
                pricePerkarat = (double) 18 / 24;
               // tvGoldPurity.setText("75.0");
                result = pricePerUnit * weightGram * pricePerkarat;

                break;
            case 5: /** Start a new Activity MyCards.java 14 K*/
                pricePerkarat = (double) 14 / 24;
               // tvGoldPurity.setText("58.5");
                result = pricePerUnit * weightGram * pricePerkarat;

                break;

            case 6: /** AlerDialog when click on Exit 10 K */
                pricePerkarat = (double) 10 / 24;
                //tvGoldPurity.setText("41.7");
                result = pricePerUnit * weightGram * pricePerkarat;

                break;
            case 7: /** Start a new Activity MyCards.java 6 K*/
                pricePerkarat = (double) 6 / 24;
                //tvGoldPurity.setText("25.0");
                result = pricePerUnit * weightGram * pricePerkarat;
                break;
        }

        return result;
    }

    public void addListenerOnBtnCalculate() {

        btnCalculate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(etWeightGram.getText()) && TextUtils.isEmpty(etWeightKiloGram.getText())) {
                    Toast.makeText(GoldCalculatorActivity.this, "Please input some weight to calculate", Toast.LENGTH_SHORT).show();
                    return;
                } else if((!TextUtils.isEmpty(etWeightGram.getText()) && !TextUtils.isEmpty(etWeightKiloGram.getText()))){
                    weightGram = Double.parseDouble(etWeightGram.getText().toString());
                    weightKiloGram= Double.parseDouble(etWeightKiloGram.getText().toString());
                    weightGram=weightGram+weightKiloGram*1000;
                    karats = Integer.parseInt(String.valueOf((spn_karat.getSelectedItemId())));
                }
                else if ((TextUtils.isEmpty(etWeightGram.getText()) && !TextUtils.isEmpty(etWeightKiloGram.getText()))){
                    weightKiloGram= Double.parseDouble(etWeightKiloGram.getText().toString());
                    weightGram=weightKiloGram*1000;
                }
                else
                weightGram = Double.parseDouble(etWeightGram.getText().toString());
                karats = Integer.parseInt(String.valueOf((spn_karat.getSelectedItemId())));

                goldPriceCalculation();

            }

        });
    }


    public void addListenerOnBtnAddToList() {

        btnReset.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(etWeightGram.getText()) && TextUtils.isEmpty(etWeightKiloGram.getText())) {
                    Toast.makeText(GoldCalculatorActivity.this, "Please input some weight to calculate", Toast.LENGTH_SHORT).show();
                    return;
                }

                weightGram = Double.parseDouble(etWeightGram.getText().toString());
                karats = Integer.parseInt(String.valueOf((spn_karat.getSelectedItemId())));

                goldPriceCalculation();

            }

        });
    }

    public void goldPriceCalculation() {


        if (!TextUtils.isEmpty(etPricepergram.getText().toString())) {
            pricePerUnitGram = Double.parseDouble(etPricepergram.getText().toString());
            pricePerUnitTOz = pricePerUnitGram * GRAM_TO_TROYOZ;
        } else {
            pricePerUnitTOz = liveGOldPrice*currencyBDT;
        }



        pricePerUnitGram = convertToGram(pricePerUnitTOz);
        resultGram = pricePerUnitBypurity(pricePerUnitGram);
        tvTotalValue.setText(String.format("%s ", nf.format(resultGram)));



    }


    public void requestToWeb() {
        RequestQueue queue = Volley.newRequestQueue(this);
        if (isNetworkAvailable()) {
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, EndPoints.GETDATA, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if (response.length() == 0) {
                            Toast.makeText(GoldCalculatorActivity.this, "No book found", Toast.LENGTH_LONG).show();
                            return;

                        }

                        liveGOldPrice = Double.parseDouble(response.getJSONObject("dataset").getJSONArray("data").getJSONArray(0).get(1).toString());
                        tvLiveGoldPrice.setText(String.format("%s ", nf.format(liveGOldPrice)));


                    } catch (final Exception error) {
                        error.printStackTrace();
                        Log.e("ERROR", error.toString());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(GoldCalculatorActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(GoldCalculatorActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            });
            queue.add(jsonObjectRequest);
        }
    }

    public double convertToGram(double pricePerUnitTOz) {

        pricePerGram = pricePerUnitTOz / TROYOZ_TO_GRAM;
        return pricePerGram;
    }

    public void currencyRequest() {
        RequestQueue queueCurrency = Volley.newRequestQueue(this);
        if (isNetworkAvailable()) {
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, EndPoints.GETCURRENCY, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if (response.length() == 0) {
                            Toast.makeText(GoldCalculatorActivity.this, "Not found", Toast.LENGTH_LONG).show();
                            return;

                        }

                        Gson gson = new Gson();
                        // String jsonKey = "quotes" ;
                        currencyObject = gson.fromJson(response.toString(), Currency.class);
                        Log.i("Error", currencyObject.getQuotes().getUSDBDT().toString());
                        currencyBDT = Double.parseDouble(currencyObject.getQuotes().getUSDBDT().toString());

                        // Toast.makeText(GoldCalculatorActivity.this, currencyObject.getQuotes().getUSDBDT().toString(), Toast.LENGTH_SHORT).show();

                    } catch (final Exception error) {
                        error.printStackTrace();
                        Log.e("ERROR", error.toString());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(GoldCalculatorActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(GoldCalculatorActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
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
        etWeightKiloGram.setText("");
        etWeightGram.requestFocus();
        etWeightGram.setText("");
        etPricepergram.setText("");
        spn_karat.setSelection(0);
        tvTotalValue.setText("0000.00");

    }
}
