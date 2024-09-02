package com.example.wealthwave.user.budget;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.wealthwave.DashboardActivity;
import com.example.wealthwave.ProfileActivity;
import com.example.wealthwave.R;
import com.example.wealthwave.databinding.ActivityBudgetBinding;
import com.example.wealthwave.user.transactions.TransactionActivity;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import models.dtos.RemainingBudgetDto;

public class BudgetActivity extends AppCompatActivity {

		private ActivityBudgetBinding binding;
		private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
		private final FirebaseDatabase databaseReference = FirebaseDatabase.getInstance("https://wealthwave-c1cca-default-rtdb.europe-west1.firebasedatabase.app");
		private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
		private LocalDateTime dateTime = LocalDateTime.now();

		@Override
		protected void onCreate(Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);
				getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

				binding = ActivityBudgetBinding.inflate(getLayoutInflater());
				setContentView(binding.getRoot());

				VPFragmentAdapter viewPagerAdapter = new VPFragmentAdapter(this);
				binding.viewPager.setAdapter(viewPagerAdapter);
				binding.bottomNavigation.setSelectedItemId(R.id.page_3);

				binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
						@Override
						public boolean onNavigationItemSelected(@NonNull MenuItem item) {
								if (item.getItemId() == R.id.page_1) {
										Intent profileIntent = new Intent(BudgetActivity.this, DashboardActivity.class);
										startActivity(profileIntent);
										finish();
								}
								if (item.getItemId() == R.id.page_2) {
										Intent transactionIntent = new Intent(BudgetActivity.this, TransactionActivity.class);
										startActivity(transactionIntent);
										finish();
								}
								if (item.getItemId() == R.id.page_4) {
										Intent profileIntent = new Intent(BudgetActivity.this, ProfileActivity.class);
										startActivity(profileIntent);
										finish();
								}
								return false;
						}
				});

				binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
						@Override
						public void onTabSelected(TabLayout.Tab tab) {
								binding.viewPager.setCurrentItem(tab.getPosition());
						}

						@Override
						public void onTabUnselected(TabLayout.Tab tab) {
						}

						@Override
						public void onTabReselected(TabLayout.Tab tab) {
								binding.viewPager.setCurrentItem(tab.getPosition());
						}
				});

				binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
						@Override
						public void onPageSelected(int position) {
								super.onPageSelected(position);
								binding.tabLayout.getTabAt(position).select();
						}
				});

				//Get budget balance
				GetBudgetBalance();

		}

		private void GetBudgetBalance() {
				List<RemainingBudgetDto> budgetDtoList = new ArrayList<>();
				// Get remaining budgets from the db
				databaseReference.getReference("Remaining Budgets")
								.child(firebaseAuth.getCurrentUser().getUid())
								.child(String.valueOf(dateTime.getYear()))
								.child(String.valueOf(dateTime.getMonth()))
								.addValueEventListener(new ValueEventListener() {
										@Override
										public void onDataChange(@NonNull DataSnapshot snapshot) {
												if (snapshot.exists()) {
														for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
																RemainingBudgetDto remainingBudget = dataSnapshot.getValue(RemainingBudgetDto.class);
																budgetDtoList.add(remainingBudget);
																DecimalFormat format = new DecimalFormat("#,###,###,###.00");
																Double ammount = 0.00;

																for (RemainingBudgetDto budget : budgetDtoList) {
																		ammount += budget.getRemainingAmt();
																}
																binding.amount.setText(String.format("KES %s", format.format(ammount)));
														}

												}
										}

										@Override
										public void onCancelled(@NonNull DatabaseError error) {
												Log.w("Database Error", error.getDetails());
										}
								});
		}
}