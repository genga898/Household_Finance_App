package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        Button registerBtn = findViewById(R.id.sign_up);
        Button loginBtn = findViewById(R.id.login);

        Intent dashboardIntent = new Intent(this, DashboardActivity.class);
        Intent registerIntent = new Intent(this, RegisterActivity.class);


        registerBtn.setOnClickListener(v -> {
            try {
                startActivity(registerIntent);
            } catch (Error e) {
                throw new RuntimeException(e);
            }
        });

        loginBtn.setOnClickListener(v -> {
            try {
                showToast("Login Successful\nWelcome Back");
                startActivity(dashboardIntent);
            }
            catch(Error e){
                throw new RuntimeException(e);
            }
        });
    }

    //Show toast message function
    private void showToast(CharSequence toastMessage){
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(this /* MyActivity */, toastMessage, duration);
        toast.show();
    }
}

