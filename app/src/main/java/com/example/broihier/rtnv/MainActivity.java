package com.example.broihier.rtnv;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.ArrayList;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class MainActivity extends Activity {
    /* MainActivity - defines the main activity class of rtnv - Real Time Net Value                                  */
    /* ============================================================================================================= */
    /* ------------------------------------------------------------------------------------------------------------- */
    /*                                                                                                               */
    /* Description:                                                                                                  */
    /*     This class, when instantiated, creates the main activity for the Android APP rtnv (Real Time Net Value)   */
    /* This APP stores stock ticker values supplied by a user and, on demand, queries Yahoo to determine the net     */
    /* value of the portfolio.                                                                                       */
    /* ------------------------------------------------------------------------------------------------------------- */
    /* ------------------------------------------------------------------------------------------------------------- */
    /*                                                                                                               */
    /* Inputs:                                                                                                       */
    /*     Mnemonic      Parameter                      Source                                                       */
    /*   ___________    ___________                     ____________________________________________________________ */
    /*                                                                                                               */
    /* ------------------------------------------------------------------------------------------------------------- */
    /* ------------------------------------------------------------------------------------------------------------- */
    /*                                                                                                               */
    /* Processing:                                                                                                   */
    /*                                                                                                               */
    /* ------------------------------------------------------------------------------------------------------------- */
    /* ------------------------------------------------------------------------------------------------------------- */
    /*                                                                                                               */
    /* Outputs:                                                                                                      */
    /*     Mnemonic      Parameter                      Source                                                       */
    /*   ___________    ___________                     ____________________________________________________________ */
    /*                                                                                                               */
    /* ------------------------------------------------------------------------------------------------------------- */
    /* ============================================================================================================= */

    private TextView valueLabel, netValue;
    private EditText tickerLabel, sharesLabel;
    private Button addTicker, deleteTicker, nextButton, calculateNetValue;
    private Stock stock;
    private List<Stock> stocks;
    private int numberOfStocks = 0;
    private int stocksIndex = 0;
    private boolean autoWrite = false;
    private QueryThread query;
    private Map<String,Integer> retry = Collections.synchronizedMap(new HashMap<String,Integer>());

    StockDatabaseHelper databaseHelper;
    SQLiteDatabase db = null;

    /* Method onCreate - initialize objects used by the activity object                                              */
    /* ============================================================================================================= */
    /* ------------------------------------------------------------------------------------------------------------- */
    /*                                                                                                               */
    /* Description:                                                                                                  */
    /*     This method creates the objects accessed by this main activity object: an array of stocks, an SQLite      */
    /* database, and a helper for the database.                                                                      */
    /* ------------------------------------------------------------------------------------------------------------- */
    /* ------------------------------------------------------------------------------------------------------------- */
    /*                                                                                                               */
    /* Inputs:                                                                                                       */
    /*     Mnemonic      Parameter                      Source                                                       */
    /*   ___________    ___________                     ____________________________________________________________ */
    /*                  SQL database "stock.sqlite"     data storage area on device                                  */
    /*                                                                                                               */
    /* ------------------------------------------------------------------------------------------------------------- */
    /* ------------------------------------------------------------------------------------------------------------- */
    /*                                                                                                               */
    /* Processing:                                                                                                   */
    /*  create an array of stocks;                                                                                   */
    /*  create the views;                                                                                            */
    /*  create a database helper and use it to initialize the contents of the array of stocks;                       */
    /*                                                                                                               */
    /* ------------------------------------------------------------------------------------------------------------- */
    /* ------------------------------------------------------------------------------------------------------------- */
    /*                                                                                                               */
    /* Outputs:                                                                                                      */
    /*     Mnemonic      Parameter                      Destination                                                  */
    /*   ___________    ___________                     ____________________________________________________________ */
    /*     stocks       An array of Stock objects       Used internally to the activity object                       */
    /*     tickerLabel  Edit view that holds the ticker Used internally                                              */
    /*     sharesLabel  Edit view that holds the number Used internally                                              */
    /*                  of shares                                                                                    */
    /*     valueLabel   Edit view that holds the value  Used internally                                              */
    /*                  of a share                                                                                   */
    /*     netValue     Text view that holds the net    Used internally                                              */
    /*                  value of the portfolio                                                                       */
    /*     addTicker    Button that triggers the action Used internally                                              */
    /*                  to store a new stock                                                                         */
    /*     deleteTicker Button that triggers the action Used internally                                              */
    /*                  to delete a stock                                                                            */
    /*     nextButton   Button that triggers the action Used internally                                              */
    /*                  to display another stock in the                                                              */
    /*                  array                                                                                        */
    /*     calculateNetValue                                                                                         */
    /*                  Button that triggers the action Used internally                                              */
    /*                  to calcuate the net value of                                                                 */
    /*                  portfolio                                                                                    */
    /*                                                                                                               */
    /* ------------------------------------------------------------------------------------------------------------- */
    /* ============================================================================================================= */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        stocks = new ArrayList<Stock>();
        stock = new Stock("", 0.0, 0.0);
        initializeViews();
        Log.d("rtnv", "in onCreate of MainActivity");
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Log.d("rtnv", "starting QueryThread");
        query = new QueryThread(new Handler());
        query.setListener(new QueryThread.Listener<String>() {
            public void onQueryComplete(String ticker, String value) {
                Log.d("rtnv", "Listener called for ticker:" + ticker + ": " + value);
                for (int index = 0; index < numberOfStocks; index++) {
                    if (stocks.get(index).getTicker().contentEquals(ticker)) {
                        if (value.equals("Not found")) {
                            stocks.get(index).setValueOfEachShare(0.0);
                            if (retry.get(stocks.get(index).getTicker()) == null) {
                                Log.e("rtnv","No pending operation, this should never happen");
                            } else {
                                if (retry.get(stocks.get(index).getTicker()) == 0) {
                                    Log.e("rtnv","Retry attempted for: "+stocks.get(index).getTicker());
                                    long now = System.currentTimeMillis();
                                    long then = System.currentTimeMillis();

                                    while (now - then < 5000) {
                                        retry.put(stocks.get(index).getTicker(), 1);
                                        now = System.currentTimeMillis();
                                    }
                                    Stock retryStock = new Stock(stocks.get(index).getTicker(),0,0);
                                    Log.e("rtrv","requeuing "+retryStock.getTicker());
                                    query.queueQuery(retryStock.getTicker(),retryStock.getTicker());
                                } else {
                                    Log.e("rtnv","Retry of "+stocks.get(index).getTicker()+" failed");
                                    retry.remove(stocks.get(index).getTicker());
                                }
                            }
                        } else if (!value.equals("Got exception")) {
                            try {
                                stocks.get(index).setValueOfEachShare(Double.parseDouble(value));
                                retry.remove(stocks.get(index).getTicker());
                            }
                            catch (Exception e) {
                                Log.e("rtnv","error in double: "+value+", ticker was: "+ticker);
                            };
                        } else {
                            if (value.equals("Got exception")) {
                                retry.remove(stocks.get(index).getTicker());
                                Log.e("rtnv","removing retry entry after exception");
                            }
                        }
                    }
                }
                double totalNetValue = 0.0;
                for (int index = 0; index < numberOfStocks; index++) {
                    totalNetValue += stocks.get(index).getNetValue();
                }
                NumberFormat format = NumberFormat.getCurrencyInstance();
                String formattedNetValue = format.format(totalNetValue);
                netValue.setText(formattedNetValue);
            }

        });

        query.start();
        query.getLooper();
        databaseHelper = new StockDatabaseHelper(this.getApplicationContext());
        db = databaseHelper.getWritableDatabase();
        if (db == null) {
            Log.d("rtnv", " database is not initialized");
        } else {
            databaseHelper.reset();
            boolean done = false;
            do {
                Stock stock = databaseHelper.get();
                if (stock.getTicker().equals("")) {
                    done = true;
                } else {
                    stocks.add(stock);
                    retry.put(stock.getTicker(), 0);
                    numberOfStocks++;
                    query.queueQuery(stock.getTicker(), stock.getTicker());
                }
            } while (!done);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        query.quit();
    }


    /* Method initializeViews - initializes views of the activity object                                             */
    /* ============================================================================================================= */
    /* ------------------------------------------------------------------------------------------------------------- */
    /*                                                                                                               */
    /* Description:                                                                                                  */
    /*     This method creates the objects accessed by this main activity object: an array of stocks, an SQLite      */
    /* database, and a helper for the database.                                                                      */
    /* ------------------------------------------------------------------------------------------------------------- */
    /* ------------------------------------------------------------------------------------------------------------- */
    /*                                                                                                               */
    /* Inputs:                                                                                                       */
    /*     Mnemonic      Parameter                      Source                                                       */
    /*   ___________    ___________                     ____________________________________________________________ */
    /*                                                                                                               */
    /* ------------------------------------------------------------------------------------------------------------- */
    /* ------------------------------------------------------------------------------------------------------------- */
    /*                                                                                                               */
    /* Processing:                                                                                                   */
    /*  create a tickerLabel and add an edit listener to it (capital letters only);                                  */
    /*  create a sharesLabel and add an edit listener to it (numbers only);                                          */
    /*  create a valueLabel and add an edit listener to it (numbers only);                                           */
    /*  create a netValue text view to display the calcuated net value;                                              */
    /*  create a calculateNetValue button to induce the calculation and display of the portfolio's net value;        */
    /*  create an addTicker button to add a displayed stock to the portfolio;                                        */
    /*  create an deleteTicker button to remove a displayed stock from the portfolio;                                */
    /*  create an nextButton button to displayed a different stock from the portfolio;                               */
    /*                                                                                                               */
    /* ------------------------------------------------------------------------------------------------------------- */
    /* ------------------------------------------------------------------------------------------------------------- */
    /*                                                                                                               */
    /* Outputs:                                                                                                      */
    /*     Mnemonic      Parameter                      Destination                                                  */
    /*   ___________    ___________                     ____________________________________________________________ */
    /*     tickerLabel  Edit view that holds the ticker Used internally                                              */
    /*     sharesLabel  Edit view that holds the number Used internally                                              */
    /*                  of shares                                                                                    */
    /*     valueLabel   Edit view that holds the value  Used internally                                              */
    /*                  of a share                                                                                   */
    /*     netValue     Text view that holds the net    Used internally                                              */
    /*                  value of the portfolio                                                                       */
    /*     addTicker    Button that triggers the action Used internally                                              */
    /*                  to store a new stock                                                                         */
    /*     deleteTicker Button that triggers the action Used internally                                              */
    /*                  to delete a stock                                                                            */
    /*     nextButton   Button that triggers the action Used internally                                              */
    /*                  to display another stock in the                                                              */
    /*                  array                                                                                        */
    /*     calculateNetValue                                                                                         */
    /*                  Button that triggers the action Used internally                                              */
    /*                  to calculate the net value of                                                                */
    /*                  portfolio                                                                                    */
    /*                                                                                                               */
    /* ------------------------------------------------------------------------------------------------------------- */
    /* ============================================================================================================= */
    public void initializeViews() {
        tickerLabel = (EditText) findViewById(R.id.tickerLabel);
        tickerLabel.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(
                    CharSequence c, int start, int before, int count) {
                if (!autoWrite) {
                    stocksIndex = 0;
                    stock.setTicker((c.toString()));
                }
            }

            public void beforeTextChanged(
                    CharSequence c, int start, int count, int after) {
            }

            public void afterTextChanged(
                    Editable c) {
            }
        });

        sharesLabel = (EditText) findViewById(R.id.sharesLabel);
        sharesLabel.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(
                    CharSequence c, int start, int before, int count) {
                if (count > 0 && !autoWrite) {
                    stocksIndex = 0;
                    stock.setNumberOfShares(Double.parseDouble(c.toString()));
                }
            }

            public void beforeTextChanged(
                    CharSequence c, int start, int count, int after) {
            }

            public void afterTextChanged(
                    Editable c) {
            }
        });
        valueLabel = (TextView) findViewById(R.id.valueLabel);
        netValue = (TextView) findViewById(R.id.netValue);

        calculateNetValue = (Button) findViewById(R.id.calculateNetValue);
        calculateNetValue.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stocksIndex = 0;
                calculateNetValue.setClickable(false);
                databaseHelper.reset();
                boolean done = false;
                do {
                    Stock stock = databaseHelper.get();
                    if (stock.getTicker().equals("")) {
                        done = true;
                    } else {
                        if (retry.get(stock.getTicker())==null) {
                            retry.put(stock.getTicker(), 0);
                            query.queueQuery(stock.getTicker(), stock.getTicker());
                        } else {
                            Log.e("rtnv","query inhibited for "+stock.getTicker());
                        }
                    }
                } while (!done);
                calculateNetValue.setClickable(true);
            }
        });
        addTicker = (Button) findViewById(R.id.addTicker);
        addTicker.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stocksIndex = 0;
                if (!stock.getTicker().equals("")) {
                    query.queueQuery(stock.getTicker(), stock.getTicker());
                    String value = "0.0";
                    autoWrite = true;
                    stock.setValueOfEachShare(Double.parseDouble(value));
                    valueLabel.setText(Double.toString(stock.getValueOfEachShare()));
                    autoWrite = false;
                    for (int removeIndex = 0; removeIndex < numberOfStocks; removeIndex++) {
                        if (stock.getTicker().equals(stocks.get(removeIndex).getTicker())) {
                            stocks.remove(removeIndex);
                            numberOfStocks--;
                            break;
                        }
                    }
                    stocks.add(stock);
                    long result;
                    if ((result = databaseHelper.update(stock)) == -1) {
                        Log.d("rtnv", "updating of SQLite database failed");
                    } else {
                        Log.d("rtnv", "updating of SQLite database was successful, index: " + result);
                    }
                    databaseHelper.close();
                    numberOfStocks++;
                    stock = new Stock("", 0.0, 0.0);
                    Log.d("rtnv", "incrementing number of stocks: " + Double.toString(numberOfStocks));
                } else {
                    Log.e("rtnv", "attempting to add a null stock - inhibit");
                }
                autoWrite = true;
                tickerLabel.setText("");
                sharesLabel.setText("");
                valueLabel.setText("");
                autoWrite = false;
            }
        });
        deleteTicker = (Button) findViewById(R.id.deleteTicker);
        deleteTicker.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stocksIndex = 0;
                if (stock.getTicker().equals("")) {
                    stock.setTicker(tickerLabel.getText().toString());
                    Log.d("rtnv", "Setting ticker to: " + stock.getTicker());
                    if (stock.getTicker().equals("")) {
                        Log.d("rtnv", "nothing to do - returning");
                        return; // do nothing
                    }
                } else {
                    Log.d("rtnv", "Ticker to delete: " + stock.getTicker());
                }
                for (int index = 0; index < numberOfStocks; index++) {
                    if ((stocks.get(index).getTicker()).equals(stock.getTicker())) {
                        stocks.remove(index);
                        databaseHelper.delete(stock);
                        databaseHelper.close();
                        stock.setTicker("");
                        numberOfStocks--;
                        autoWrite = true;
                        tickerLabel.setText("");
                        sharesLabel.setText("");
                        valueLabel.setText("");
                        autoWrite = false;
                        retry.remove(stock.getTicker());
                        break;
                    }
                }
            }
        });
        nextButton = (Button) findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (numberOfStocks > 0) {
                    Log.d("rtnv", "in Next button handling - stocksIndex is " + stocksIndex + " number of stocks is " + numberOfStocks);
                    if (stocksIndex >= numberOfStocks) {
                        Log.d("rtnv", "blanking screen");
                        autoWrite = true;
                        tickerLabel.setText("");
                        sharesLabel.setText("");
                        valueLabel.setText("");
                        autoWrite = false;
                    } else {
                        Log.d("rtnv", "putting stock info on screen");
                        autoWrite = true;
                        tickerLabel.setText(stocks.get(stocksIndex).getTicker());
                        sharesLabel.setText(Double.toString(stocks.get(stocksIndex).getNumberOfShares()));
                        if (stocks.get(stocksIndex).getValueOfEachShare() == 0) {
                            valueLabel.setText("Not found");
                        } else {
                            valueLabel.setText(Double.toString(stocks.get(stocksIndex).getValueOfEachShare()));
                        }
                        autoWrite = false;
                        stocksIndex++;
                        Log.d("rtnv", "stocksIndex after increment " + stocksIndex);
                    }
                }
            }
        });


    }

}
