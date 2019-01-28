package com.skyhope.wallettest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.skyhope.wallettest.R;

public class Homepage extends AppCompatActivity {

    TextView textViewAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        textViewAddress = findViewById(R.id.text_view_address);

        parseIntent();
    }

    private void parseIntent() {
        Intent intent = getIntent();

        if (intent.hasExtra("ETH_Adress")) {
            String address = intent.getStringExtra("ETH_Adress");
            textViewAddress.setText(address);
        }
    }
}
