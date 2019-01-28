package com.skyhope.wallettest;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.Nullable;
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

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class LoadActivity extends AppCompatActivity {

    TextView textViewLoadWallet;
    EditText editTextPassword;
    Button buttonLoad;

    private Uri mainUri;

    private static final int REQUEST_CODE = 202;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load);
        textViewLoadWallet = findViewById(R.id.text_view_wallet);
        editTextPassword = findViewById(R.id.edit_text_password);
        buttonLoad = findViewById(R.id.button_load);


        buttonLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Load wallet

                String password = editTextPassword.getText().toString();

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(LoadActivity.this, "Password cannot be empty!", Toast.LENGTH_SHORT).show();
                } else if (password.length() < 9) {
                    Toast.makeText(LoadActivity.this, "Password length at least 9 character", Toast.LENGTH_SHORT).show();
                } else {
                    if (mainUri != null) {
                        readKeyStoreFile(mainUri, password);
                    } else {
                        Toast.makeText(LoadActivity.this, "Please select your wallet", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


        textViewLoadWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // import wallet
                openFileSelector();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && data != null) {


            if (data.getData() != null) {
                File keyStoreFile = new File(data.getData().getPath());
                textViewLoadWallet.setText(keyStoreFile.getName());
                textViewLoadWallet.setEllipsize(TextUtils.TruncateAt.END);

                mainUri = data.getData();
            }

        }
    }

    /**
     * Purpose: To select a file from storage
     */
    private void openFileSelector() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/*");
        startActivityForResult(intent, REQUEST_CODE);
    }

    public void readKeyStoreFile(Uri path, String password) {

        Dexter.withActivity(LoadActivity.this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(path);
                            if (inputStream != null) {
                                String ret = "";
                                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                                String receiveString = "";
                                StringBuilder stringBuilder = new StringBuilder();

                                while ((receiveString = bufferedReader.readLine()) != null) {
                                    stringBuilder.append(receiveString);
                                }
                                inputStream.close();
                                ret = stringBuilder.toString();

                                File mainFile = new File(path.getPath());

                                File outputDir = new File(Environment.getExternalStorageDirectory().getPath() + "/wallet/");
                                if (!outputDir.exists()) {
                                    outputDir.mkdir();
                                }
                                File outputFile = File.createTempFile("tempFile", ".json", outputDir);

                                FileOutputStream fos = new FileOutputStream(outputFile);
                                fos.write(ret.getBytes());
                                fos.close();

                                loadWallet(outputFile, password);

                            }

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                    }
                }).check();

    }

    private void loadWallet(File file, String password) {
        try {
            Credentials credentials = WalletUtils.loadCredentials(password, file);
            Intent intent = new Intent(LoadActivity.this, Homepage.class);
            intent.putExtra("ETH_Adress", credentials.getAddress());
            startActivity(intent);
            Log.d("WalletTest", "Success: " + credentials.getAddress());
            file.delete();
            finish();
            if (MainActivity.sMainInstance != null) {
                MainActivity.sMainInstance.finish();
                MainActivity.sMainInstance = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("WalletTest", "Error: " + e.getMessage());
        } catch (CipherException e) {
            e.printStackTrace();
        }
    }
}
