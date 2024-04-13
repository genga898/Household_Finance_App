package com.example.wealthwave;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.wealthwave.databinding.ActivityProfileBinding;
import com.example.wealthwave.user.budget.BudgetActivity;
import com.example.wealthwave.user.transactions.TransactionActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    private enum SettingsList{
        Account_information("Account Information"),
        Weekly_report("Weekly Report"),
        Usage_statistics("Usage Statistics");
        private final String label;
        SettingsList(String s) {
            this.label = s;
        }
    }
    private ActivityProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.bottomNavigation.setSelectedItemId(R.id.page_4);

        binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId() == R.id.page_1){
                    Intent dashboardIntent = new Intent(ProfileActivity.this, DashboardActivity.class);
                    startActivity(dashboardIntent);
                    finish();
                }
                if(item.getItemId() == R.id.page_2){
                    Intent transactionIntent = new Intent(ProfileActivity.this, TransactionActivity.class);
                    startActivity(transactionIntent);
                    finish();
                }
                if(item.getItemId() == R.id.page_3){
                    Intent dashboardIntent = new Intent(ProfileActivity.this, BudgetActivity.class);
                    startActivity(dashboardIntent);
                    finish();
                }
                return false;
            }
        });


        // Create a list for the settings
        ArrayAdapter<SettingsList> settingsList = new ArrayAdapter<>(this, R.layout.list_view, R.id.textview, SettingsList.values());
        binding.listView.setAdapter(settingsList);



        //Get and display user data
        if(firebaseUser != null){
            binding.emailAddress.setText(firebaseUser.getEmail());
            // Retrieve user info from the firestore database
            DocumentReference document = firestore.collection("user_details").document(firebaseUser.getUid());
            document.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()){
                        //Create user object to map to
                        String users_name = documentSnapshot.get("name").toString();
                        binding.fullName.setText(users_name);
                    }
                }
            });
        }

        //Logout the user
        LogoutUser();
    }

    private void LogoutUser(){
        binding.logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Snackbar.make(binding.logoutButton, "You have been logged out successfully", Snackbar.LENGTH_SHORT).setAnchorView(R.id.bottom_navigation).show();
                //Redirect to login page
                Intent logoutIntent = new Intent(ProfileActivity.this, LoginActivity.class);
                startActivity(logoutIntent);
                finish();
            }
        });
    }
}