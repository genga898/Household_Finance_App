package com.example.wealthwave;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.WindowManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class Splash_Screen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);
        try {

            ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();

            if (nInfo!=null && nInfo.isConnectedOrConnecting() && nInfo.isAvailable()){
                Thread.sleep(3000);
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
            }else{
                MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(Splash_Screen.this)
                        .setMessage("Network connection failed")
                        .setPositiveButton("Try again", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                dialogBuilder.create();
                dialogBuilder.show();
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}