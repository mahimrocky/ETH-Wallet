package com.skyhope.wallettest;

import android.Manifest;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.skyhope.wallettest.R;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.json.JSONException;
import org.json.JSONObject;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.WalletUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText password;
    private TextView testText;
    private Button create;
    private String pass;

    private String fileName;

    private static String FOLDER_NAME = "wallet";

    public static MainActivity sMainInstance = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sMainInstance = this;

        password = (EditText) findViewById(R.id.password);
        create = (Button) findViewById(R.id.create);
        testText = (TextView) findViewById(R.id.testText);

        setupBouncyCastle();

        create.setEnabled(false);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //createWallet();

                if (password.length() < 9) {
                    password.setError("Enter password at least 9 character");
                } else {
                    createWallet();
                }

            }
        });

        testText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, LoadActivity.class));
            }
        });

        Dexter.withActivity(MainActivity.this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {

                        create.setEnabled(true);

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }


    public void loadAddress(String fileName) {
        String js = loadJsonFile(fileName);
        String address;
        try {
            JSONObject jsonObject = new JSONObject(js);
            address = jsonObject.getString("address");

            Intent intent = new Intent(MainActivity.this, Homepage.class);
            intent.putExtra("ETH_Adress", address);
            startActivity(intent);
            Toast.makeText(MainActivity.this, "Wallet Created successfully", Toast.LENGTH_SHORT).show();
            Log.d("WalletTest", "Wallet created successfully");
            finish();

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void createWallet() {

        String folder_main = FOLDER_NAME;

        File f = new File(Environment.getExternalStorageDirectory(), folder_main);
        if (!f.exists()) {
            f.mkdir();
        }


        pass = password.getText().toString();
        try {

            String value = WalletUtils.generateLightNewWalletFile(pass, f);
            Log.d("WalletTest", "value: " + value);
            if (!TextUtils.isEmpty(value)) {

                loadAddress(value);

            } else {
                Log.d("WalletTest", "Wallet not created");
            }

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            Log.e("WalletTest", "Error: " + e.getMessage());
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
            Log.e("WalletTest", "Error: " + e.getMessage());
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
            Log.e("WalletTest", "Error: " + e.getMessage());
        } catch (CipherException e) {
            e.printStackTrace();
            Log.e("WalletTest", "Error: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("WalletTest", "Error: " + e.getMessage());
        }
    }

    public String readWalletFile() {

        File file = new File(Environment.getExternalStorageDirectory() + "/" + FOLDER_NAME);
        for (File f : file.listFiles()) {
            if (f.isFile()) {
                fileName = f.getName();
                //Toast.makeText(MainActivity.this,fileName,Toast.LENGTH_SHORT).show();
            }
        }
        return fileName;
    }

    public String loadJsonFile(String fileName) {
        String js = null;
        File file = new File(Environment.getExternalStorageDirectory() + "/" + FOLDER_NAME + "/" + fileName);

        try {
            FileInputStream fileInputStream = new FileInputStream(file);

            int size = fileInputStream.available();

            byte[] read = new byte[size];

            fileInputStream.read(read);
            fileInputStream.close();
            js = new String(read, "UTF-8");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return js;
    }

    public boolean isFileExist() {
        File file = new File(Environment.getExternalStorageDirectory() + "/" + FOLDER_NAME + "/" + readWalletFile());
        if (file.exists()) {
            return true;
        } else return false;
    }

    private void setupBouncyCastle() {
        final Provider provider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        if (provider == null) {
            // Web3j will set up the provider lazily when it's first used.
            return;
        }
        if (provider.getClass().equals(BouncyCastleProvider.class)) {
            // BC with same package name, shouldn't happen in real life.
            return;
        }
        // Android registers its own BC provider. As it might be outdated and might not include
        // all needed ciphers, we substitute it with a known BC bundled in the app.
        // Android's BC has its package rewritten to "com.android.org.bouncycastle" and because
        // of that it's possible to have another BC implementation loaded in VM.
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }
}
