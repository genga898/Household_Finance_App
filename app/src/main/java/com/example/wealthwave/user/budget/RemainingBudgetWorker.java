package com.example.wealthwave.user.budget;

import android.content.Context;
import android.util.ArraySet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

import models.Budget;
import models.RemainingBudget;

public class RemainingBudgetWorker extends Worker {

		private final FirebaseDatabase firebaseDatabase =  FirebaseDatabase.getInstance("https://wealthwave-c1cca-default-rtdb.europe-west1.firebasedatabase.app");
		private DatabaseReference databaseReference;
		private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
		private final LocalDateTime localDateTime = LocalDateTime.now();

		public RemainingBudgetWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
				super(context, workerParams);
		}

		@NonNull
		@Override
		public Result doWork() {
				//{TODO}: Fix issue with atomic reference to show correct result
				AtomicReference<Result> result = new AtomicReference<>(Result.success());
				// Create remaining budget if it does not exist
				databaseReference = firebaseDatabase.getReference("budgets");

				databaseReference.child(firebaseAuth.getCurrentUser().getUid())
								.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
										@Override
										public void onSuccess(DataSnapshot dataSnapshot) {
												dataSnapshot.getChildren().forEach(dataSnapshot1 -> {
														Budget budget = dataSnapshot1.getValue(Budget.class);

														if (budget != null){
																RemainingBudget remainingBudget = new RemainingBudget(budget);
																CreateNewRemainingBudget(remainingBudget, budget);
																result.set(Result.success());
														}
														else {
																result.set(Result.failure());
														}
												});
										}
								});

				return result.get();
		}

		//Creates a new remaining budget for each month
		private void CreateNewRemainingBudget(RemainingBudget remainingBudget, Budget budget){

				ArraySet<String> remainingBudgets = new ArraySet<>();

				databaseReference = firebaseDatabase.getReference("Remaining Budgets");

				databaseReference.child(firebaseAuth.getCurrentUser().getUid())
								.child(String.valueOf(localDateTime.getYear()))
								.child(String.valueOf(localDateTime.getMonth()))
								.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
										@Override
										public void onComplete(@NonNull Task<DataSnapshot> task) {
												remainingBudgets.clear();
												task.getResult().getChildren().forEach(dataSnapshot2 -> {
														remainingBudgets.add(dataSnapshot2.getKey());
												});

												if (!remainingBudgets.contains(budget.getBudgetName())){
														databaseReference.child(firebaseAuth.getCurrentUser().getUid())
																		.child(String.valueOf(localDateTime.getYear()))
																		.child(String.valueOf(localDateTime.getMonth()))
																		.child(budget.getBudgetName())
																		.setValue(remainingBudget).addOnSuccessListener(new OnSuccessListener<Void>() {
																				@Override
																				public void onSuccess(Void unused) {
																						Log.d("Success", "Successful");
																				}
																		}).addOnFailureListener(new OnFailureListener() {
																				@Override
																				public void onFailure(@NonNull Exception e) {
																						e.printStackTrace();
																				}
																		});
												}else {
														Log.d("Value Present", "Value already exists in the database");
												}
										}
								});
		}
}
