package com.example.wealthwave;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.example.wealthwave.databinding.ActivityProfileBinding;
import com.example.wealthwave.user.budget.BudgetActivity;
import com.example.wealthwave.user.reports.ReportActivity;
import com.example.wealthwave.user.transactions.TransactionActivity;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.Utils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.dtos.TransactionDto;

public class ProfileActivity extends AppCompatActivity {

		private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
		private final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
		private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
		private final FirebaseDatabase database = FirebaseDatabase.getInstance("https://wealthwave-c1cca-default-rtdb.europe-west1.firebasedatabase.app");
		private final LocalDateTime dateTime = LocalDateTime.now();

		private enum SettingsList {
				Weekly_report("Weekly Report");

				private final String label;

				SettingsList(String s) {
						this.label = s;
				}

				@NonNull
				@Override
				public String toString() {
						return label;
				}

				public static ArrayList<String> getEnumValues() {
						SettingsList[] settings = SettingsList.values();
						List<String> labelsList = Arrays.asList(Arrays.stream(settings)
										.map(SettingsList::toString)
										.toArray(String[]::new));
						return new ArrayList<>(labelsList);
				}
		}

		private ActivityProfileBinding binding;

		@Override
		protected void onCreate(Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);
				getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

				binding = ActivityProfileBinding.inflate(getLayoutInflater());
				setContentView(binding.getRoot());


				binding.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
								if (position == 0) {
										Intent reportIntent = new Intent(ProfileActivity.this, ReportActivity.class);
										startActivity(reportIntent);
										finish();
								}
						}
				});


				binding.bottomNavigation.setSelectedItemId(R.id.page_4);

				binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
						@Override
						public boolean onNavigationItemSelected(@NonNull MenuItem item) {
								if (item.getItemId() == R.id.page_1) {
										Intent dashboardIntent = new Intent(ProfileActivity.this, DashboardActivity.class);
										startActivity(dashboardIntent);
										finish();
								}
								if (item.getItemId() == R.id.page_2) {
										Intent transactionIntent = new Intent(ProfileActivity.this, TransactionActivity.class);
										startActivity(transactionIntent);
										finish();
								}
								if (item.getItemId() == R.id.page_3) {
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
				if (firebaseUser != null) {
						binding.emailAddress.setText(firebaseUser.getEmail());
						// Retrieve user info from the firestore database
						DocumentReference document = firestore.collection("user_details").document(firebaseUser.getUid());
						document.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
								@Override
								public void onSuccess(DocumentSnapshot documentSnapshot) {
										if (documentSnapshot.exists()) {
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

		private void LogoutUser() {
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

		public void CreateDataAnalyticsChart(View view) {
				List<TransactionDto> transactionDtoList = new ArrayList<>();
				Map<LocalDate, Double> dailySpendMap = new HashMap<>();
				PieChart pieChart = view.findViewById(R.id.pie_chart);


				database.getReference("transactions")
								.child(firebaseAuth.getCurrentUser().getUid())
								.child(String.valueOf(dateTime.getYear()))
								.child(String.valueOf(dateTime.getMonth())).addValueEventListener(new ValueEventListener() {
										@Override
										public void onDataChange(@NonNull DataSnapshot snapshot) {
												for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
														TransactionDto transaction = dataSnapshot.getValue(TransactionDto.class);
														DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
														if (transaction != null) {
																LocalDateTime dateTime1 = LocalDateTime.parse(transaction.getCreatedAt(), formatter);
																LocalDate transactionDate = dateTime1.toLocalDate();
																if (transaction.getTransactionName().equals("Expense")) {
																		transactionDtoList.add(transaction);
																		dailySpendMap.put(transactionDate, dailySpendMap.getOrDefault(transactionDate, 0.0) + transaction.getAmount());
																}
														}
												}

												pieChart.setBackgroundColor(getColor(R.color.md_theme_light_onPrimary));
												pieChart.getDescription().setEnabled(false);
												pieChart.animateX(300);

												pieChart.setTouchEnabled(true);


												pieChart.getLegend().setEnabled(false);
												pieChart.animateXY(200, 200);

												List<PieEntry> entries = new ArrayList<>();

												int index = 0;
												LocalDate startDate = LocalDate.now().minusMonths(1).plusDays(1);
												LocalDate endDate = LocalDate.now().plusDays(1);


												for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
														double amount = dailySpendMap.getOrDefault(date, 0.0);
														entries.add(new PieEntry(index++, (float) amount));
												}

												PieDataSet dataSet = new PieDataSet(entries, "Daily Spend");

												PieData pieData = new PieData(dataSet);
												pieData.setValueTextSize(Utils.convertDpToPixel(6f));

												pieChart.setData(pieData);
												pieChart.invalidate();

										}

										@Override
										public void onCancelled(@NonNull DatabaseError error) {
												Log.w("Database Error", error.getDetails());
										}
								});
		}
}