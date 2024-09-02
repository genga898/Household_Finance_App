package com.example.wealthwave;

import android.content.Intent;
import android.icu.lang.UCharacter;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.example.wealthwave.databinding.ActivityDashboardBinding;
import com.example.wealthwave.user.budget.BudgetActivity;
import com.example.wealthwave.user.budget.RemainingBudgetWorker;
import com.example.wealthwave.user.transactions.TransactionActivity;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.Utils;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import models.dtos.RemainingBudgetDto;
import models.dtos.TransactionDto;

public class DashboardActivity extends AppCompatActivity {

		private ActivityDashboardBinding binding;
		private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
		private final FirebaseDatabase databaseReference = FirebaseDatabase.getInstance("https://wealthwave-c1cca-default-rtdb.europe-west1.firebasedatabase.app");
		private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
		private final LocalDateTime dateTime = LocalDateTime.now();

		@Override
		protected void onCreate(Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);
				getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

				binding = ActivityDashboardBinding.inflate(getLayoutInflater());
				setContentView(binding.getRoot());

				//Create a worker to make new remaining budgets in the background
				WorkRequest createNewRemainingBudget = new PeriodicWorkRequest.Builder(RemainingBudgetWorker.class, 10, TimeUnit.DAYS).build();
				WorkManager.getInstance(getApplicationContext()).enqueue(createNewRemainingBudget);

				binding.bottomNavigation.setSelectedItemId(R.id.page_1);

				binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
						@Override
						public boolean onNavigationItemSelected(@NonNull MenuItem item) {
								if (item.getItemId() == R.id.page_3) {
										Intent profileIntent = new Intent(DashboardActivity.this, BudgetActivity.class);
										startActivity(profileIntent);
								}
								if (item.getItemId() == R.id.page_2) {
										Intent transactionIntent = new Intent(DashboardActivity.this, TransactionActivity.class);
										startActivity(transactionIntent);
										finish();
								}
								if (item.getItemId() == R.id.page_4) {
										Intent profileIntent = new Intent(DashboardActivity.this, ProfileActivity.class);
										startActivity(profileIntent);
								}
								return false;
						}
				});


				//Get and assign values from the database
				GetBudgetRemainder();

				//Get and display the total expenditure of the previous week
				LastWeekSpend();

				//Get and display the total expenditure of the previous month
				LastMonthSpend();

				//Get and display data analytics on the amount spent in the past 30 days
				CreateDataAnalyticsChart();

		}

		public void GetBudgetRemainder() {
				List<RemainingBudgetDto> budgetDtoList = new ArrayList<>();
				// Get remaining budgets from the db
				databaseReference.getReference("Remaining Budgets")
								.child(firebaseAuth.getCurrentUser().getUid())
								.child(String.valueOf(dateTime.getYear()))
								.child(String.valueOf(dateTime.getMonth()))
								.addValueEventListener(new ValueEventListener() {
										@Override
										public void onDataChange(@NonNull DataSnapshot snapshot) {
												budgetDtoList.clear();
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
																binding.balanceAmount.setText(String.format("KES %s", format.format(ammount)));
														}

												}
										}

										@Override
										public void onCancelled(@NonNull DatabaseError error) {
												Log.w("Database Error", error.getDetails());
										}
								});
		}


		// Get the transactions that took place in the previous week
		public void LastWeekSpend() {
				List<TransactionDto> transactionDtoList = new ArrayList<>();
				Map<String, LocalDateTime> dateTimeMap = new HashMap<>();

				databaseReference.getReference("transactions")
								.child(firebaseAuth.getCurrentUser().getUid())
								.child(String.valueOf(dateTime.getYear()))
								.child(String.valueOf(dateTime.getMonth())).addValueEventListener(new ValueEventListener() {
										@Override
										public void onDataChange(@NonNull DataSnapshot snapshot) {
												transactionDtoList.clear();
												for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
														TransactionDto transaction = dataSnapshot.getValue(TransactionDto.class);
														DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
														if (transaction != null) {
																if (!transaction.getTransactionName().equals("Income")) {
																		LocalDateTime dateTime1 = LocalDateTime.parse(transaction.getCreatedAt(), formatter);

																		dateTimeMap.put(transaction.getTransactionID(), dateTime1);
																		transactionDtoList.add(transaction);
																}
														}
														Double totalSpend = 0.00;

														System.out.println(dateTimeMap);
														for (TransactionDto transactionDto : transactionDtoList) {
																LocalDateTime localDateTime = dateTimeMap.get(transactionDto.getTransactionID());
																LocalDateTime pastWeek = LocalDateTime.now().minusDays(7);
																if (localDateTime.isAfter(pastWeek) && localDateTime.isBefore(LocalDateTime.now())) {
																		totalSpend += transactionDto.getAmount();
																}
														}

														DecimalFormat format = new DecimalFormat("#,###,###,###.00");
														String amount = format.format(totalSpend);

														binding.recentSpendAmount.setText(String.format("KES %s", amount));
												}
										}

										@Override
										public void onCancelled(@NonNull DatabaseError error) {
												Log.w("Database Error", error.getDetails());
										}
								});
		}

		//Get previous month spend
		public void LastMonthSpend() {
				List<TransactionDto> transactionDtoList = new ArrayList<>();
				Map<String, LocalDateTime> dateTimeMap = new HashMap<>();

				databaseReference.getReference("transactions")
								.child(firebaseAuth.getCurrentUser().getUid())
								.child(String.valueOf(dateTime.getYear()))
								.addValueEventListener(new ValueEventListener() {
										@Override
										public void onDataChange(@NonNull DataSnapshot snapshot) {
												transactionDtoList.clear();
												for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
														LocalDateTime previousMonth = LocalDateTime.now().minusMonths(1);
														if (previousMonth.getMonth().toString().equals(dataSnapshot.getKey())) {
																dataSnapshot.getChildren().forEach(snapshot1 -> {
																		TransactionDto transaction = snapshot1.getValue(TransactionDto.class);
																		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
																		if (transaction != null) {
																				LocalDateTime dateTime1 = LocalDateTime.parse(transaction.getCreatedAt(), formatter);

																				dateTimeMap.put(transaction.getTransactionID(), dateTime1);
																				transactionDtoList.add(transaction);
																		}
																});
														}

														Double totalSpend = 0.00;

														for (TransactionDto transactionDto : transactionDtoList) {
																LocalDateTime localDateTime = dateTimeMap.get(transactionDto.getTransactionID());
																LocalDateTime pastMonth = LocalDateTime.now().minusMonths(1);
																if (localDateTime != null && localDateTime.isEqual(pastMonth)) {
																		totalSpend += transactionDto.getAmount();
																}
														}
														DecimalFormat format = new DecimalFormat("#,###,###,###.00");
														String amount = format.format(totalSpend);

														binding.spendText.setText(String.format("%s Spending", UCharacter.toTitleCase(Locale.US, LocalDateTime.now().minusMonths(1).getMonth().toString(), null)));
														binding.amountSpent.setText(String.format("KES %s", amount));

												}
										}

										@Override
										public void onCancelled(@NonNull DatabaseError error) {
												Log.w("Database Error", error.getDetails());
										}
								});
		}

		public void CreateDataAnalyticsChart() {
				List<TransactionDto> transactionDtoList = new ArrayList<>();
				Map<LocalDate, Double> dailySpendMap = new HashMap<>();


				databaseReference.getReference("transactions")
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

												binding.barChart.setBackgroundColor(getColor(R.color.md_theme_light_onPrimary));
												binding.barChart.setGridBackgroundColor(getColor(R.color.md_theme_light_primary));
												binding.barChart.setViewPortOffsets(0, 0, 0, 0);
												binding.barChart.getDescription().setEnabled(false);
												binding.barChart.animateX(300);

												binding.barChart.setTouchEnabled(true);

												binding.barChart.setScaleEnabled(true);

												binding.barChart.setPinchZoom(false);

												binding.barChart.setDrawGridBackground(false);

												XAxis x = binding.barChart.getXAxis();
												x.setEnabled(true);
												x.setDrawGridLines(false);
												x.setPosition(XAxis.XAxisPosition.BOTTOM);
												x.setGranularity(1f);
												x.setSpaceMin(2f);
												x.setSpaceMax(1.5f);
												x.setLabelCount(10);


												YAxis y = binding.barChart.getAxisLeft();
												y.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
												y.setDrawGridLines(false);
												y.setSpaceTop(20f);
												y.setAxisLineColor(R.color.md_theme_dark_onPrimary);
												y.setLabelCount(8);

												binding.barChart.getAxisRight().setEnabled(false);


												binding.barChart.getLegend().setEnabled(false);
												binding.barChart.animateXY(200, 200);

												List<BarEntry> entries = new ArrayList<>();

												int index = 0;
												LocalDate startDate = LocalDate.now().minusMonths(1).plusDays(1);
												LocalDate endDate = LocalDate.now().plusDays(1);


												for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
														double amount = dailySpendMap.getOrDefault(date, 0.0);
														entries.add(new BarEntry(index++, (float) amount));
												}


												BarDataSet dataSet = new BarDataSet(entries, "Daily Spend");

												BarData barData = new BarData(dataSet);
												barData.setValueTextSize(Utils.convertDpToPixel(6f));

												binding.barChart.setData(barData);
												binding.barChart.invalidate();
										}

										@Override
										public void onCancelled(@NonNull DatabaseError error) {
												Log.w("Database Error", error.getDetails());
										}
								});
		}
}