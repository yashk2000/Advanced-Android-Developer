package com.example.android.localetext;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private int mInputQuantity = 1;
    private NumberFormat mNumberFormat = NumberFormat.getInstance();
    private static final String TAG = MainActivity.class.getSimpleName();

    private double mPrice = 0.10;

    double mFrExchangeRate = 0.93;
    double mIwExchangeRate = 3.61;

    private NumberFormat mCurrencyFormat = NumberFormat.getCurrencyInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHelp();
            }
        });

        final Date myDate = new Date();
        final long expirationDate = myDate.getTime() + TimeUnit.DAYS.toMillis(5);
        myDate.setTime(expirationDate);

        String myFormattedDate =
                DateFormat.getDateInstance().format(myDate);
        TextView expirationDateView = findViewById(R.id.date);
        expirationDateView.setText(myFormattedDate);

        String myFormattedPrice;
        String deviceLocale = Locale.getDefault().getCountry();
        if (deviceLocale.equals("FR") || deviceLocale.equals("IL")) {
            if (deviceLocale.equals("FR")) {
                mPrice *= mFrExchangeRate;
            } else {
                mPrice *= mIwExchangeRate;
            }
            myFormattedPrice = mCurrencyFormat.format(mPrice);
        } else {
            mCurrencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
            myFormattedPrice = mCurrencyFormat.format(mPrice);
        }
        TextView localePrice = findViewById(R.id.price);
        localePrice.setText(myFormattedPrice);
        final EditText enteredQuantity = findViewById(R.id.quantity);
        enteredQuantity.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService
                            (Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    if (v.toString().equals("")) {
                    } else {

                        try {
                            mInputQuantity = mNumberFormat.parse(v.getText()
                                    .toString()).intValue();
                            v.setError(null);
                        } catch (ParseException e) {
                            Log.e(TAG, Log.getStackTraceString(e));
                            v.setError(getText(R.string.enter_number));
                            return false;
                        }

                        String myFormattedQuantity = mNumberFormat.format(mInputQuantity);
                        v.setText(myFormattedQuantity);

                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void showHelp() {
        Intent helpIntent = new Intent(this, HelpActivity.class);
        startActivity(helpIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((EditText) findViewById(R.id.quantity)).getText().clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_help:
                Intent helpIntent = new Intent(this, HelpActivity.class);
                startActivity(helpIntent);
                return true;
            case R.id.action_language:
                Intent languageIntent = new Intent(Settings.ACTION_LOCALE_SETTINGS);
                startActivity(languageIntent);
                return true;
            default:
        }
        return super.onOptionsItemSelected(item);
    }
}
