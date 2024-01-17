package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_register);

        Button loginBtn = findViewById(R.id.login_page);
        Button registerBtn = findViewById(R.id.register);

        Intent loginIntent = new Intent(this, LoginActivity.class);
        Intent registerIntent = new Intent(this, LoginActivity.class);

        //
        String emailRegex = "/^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$/";



        loginBtn.setOnClickListener(v -> {
            try {
                startActivity(loginIntent);
            } catch (Error e) {
                throw new RuntimeException(e);
            }
        });
        registerBtn.setOnClickListener(v->{
            try {
                showToast("Registration Successful. Login to Continue");
                startActivity(registerIntent);
            } catch (Error error) {
                throw new RuntimeException(error);
            }
        });
    }

    private void showToast(CharSequence toastMessage){
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(this /* MyActivity */, toastMessage, duration);
        toast.show();
    }
}