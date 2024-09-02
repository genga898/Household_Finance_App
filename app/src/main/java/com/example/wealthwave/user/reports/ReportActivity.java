package com.example.wealthwave.user.reports;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.wealthwave.ProfileActivity;
import com.example.wealthwave.R;
import com.example.wealthwave.databinding.ActivityReportBinding;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.dtos.TransactionDto;

public class ReportActivity extends AppCompatActivity {

		private ActivityReportBinding binding;
		private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
		private final FirebaseDatabase databaseReference = FirebaseDatabase.getInstance("https://wealthwave-c1cca-default-rtdb.europe-west1.firebasedatabase.app");
		private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
		private final LocalDateTime dateTime = LocalDateTime.now();

		@Override
		protected void onCreate(Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);
				binding = ActivityReportBinding.inflate(getLayoutInflater());
				setContentView(binding.getRoot());
				ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
						Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
						v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
						return insets;
				});

				binding.backButton.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
								Intent profileIntent = new Intent(ReportActivity.this, ProfileActivity.class);
								startActivity(profileIntent);
								finish();
						}
				});

				CreateDataAnalyticsChart7Days();
				CreateDataAnalyticsChart30Days();
		}

		public void CreateDataAnalyticsChart30Days() {
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
		public void CreateDataAnalyticsChart7Days() {
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

												binding.barChart1.setBackgroundColor(getColor(R.color.md_theme_light_onPrimary));
												binding.barChart1.setGridBackgroundColor(getColor(R.color.md_theme_light_primary));
												binding.barChart1.setViewPortOffsets(0, 0, 0, 0);
												binding.barChart1.getDescription().setEnabled(false);
												binding.barChart1.animateX(300);

												binding.barChart1.setTouchEnabled(true);

												binding.barChart1.setScaleEnabled(true);

												binding.barChart1.setPinchZoom(false);

												binding.barChart1.setDrawGridBackground(false);

												XAxis x = binding.barChart1.getXAxis();
												x.setEnabled(true);
												x.setDrawGridLines(false);
												x.setPosition(XAxis.XAxisPosition.BOTTOM);
												x.setGranularity(1f);
												x.setSpaceMin(2f);
												x.setSpaceMax(1.5f);
												x.setLabelCount(10);


												YAxis y = binding.barChart1.getAxisLeft();
												y.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
												y.setDrawGridLines(false);
												y.setSpaceTop(20f);
												y.setAxisLineColor(R.color.md_theme_dark_onPrimary);
												y.setLabelCount(8);

												binding.barChart1.getAxisRight().setEnabled(false);


												binding.barChart1.getLegend().setEnabled(false);
												binding.barChart1.animateXY(200, 200);

												List<BarEntry> entries = new ArrayList<>();

												int index = 0;
												LocalDate startDate = LocalDate.now().minusDays(7);
												LocalDate endDate = LocalDate.now().plusDays(1);


												for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
														double amount = dailySpendMap.getOrDefault(date, 0.0);
														entries.add(new BarEntry(index++, (float) amount));
												}


												BarDataSet dataSet = new BarDataSet(entries, "Daily Spend");

												BarData barData = new BarData(dataSet);
												barData.setValueTextSize(Utils.convertDpToPixel(6f));

												binding.barChart1.setData(barData);
												binding.barChart1.invalidate();
										}

										@Override
										public void onCancelled(@NonNull DatabaseError error) {
												Log.w("Database Error", error.getDetails());
										}
								});
		}
		}