package com.example.wealthwave.user.transactions;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.wealthwave.DashboardActivity;
import com.example.wealthwave.PostHogAnalytics;
import com.example.wealthwave.ProfileActivity;
import com.example.wealthwave.R;
import com.example.wealthwave.databinding.ActivityTransactionBinding;
import com.example.wealthwave.user.budget.BudgetActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.divider.MaterialDividerItemDecoration;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.Budget;
import models.Expense;
import models.Income;
import models.RemainingBudget;
import models.Transaction;
import models.dtos.BudgetDto;
import models.dtos.TransactionDto;

public class TransactionActivity extends AppCompatActivity {

		private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
		private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
		private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://wealthwave-c1cca-default-rtdb.europe-west1.firebasedatabase.app");
		DatabaseReference databaseReference;
		private ActivityTransactionBinding binding;
		private boolean isExpanded = false;

		enum TransactionType {
				Expense,
				Income
		}

		enum Categories {
				Food_and_Groceries,
				Shopping,
				Transport,
				Entertainment,
				Bills_and_fees,
				Income
		}

		@Override
		protected void onCreate(Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);
				getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
				/* Binding the animations to the buttons in the activity
				 *  */
				binding = ActivityTransactionBinding.inflate(getLayoutInflater());
				setContentView(binding.getRoot());

				binding.bottomNavigation.setSelectedItemId(R.id.page_2);

				binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
						@Override
						public boolean onNavigationItemSelected(@NonNull MenuItem item) {
								if (item.getItemId() == R.id.page_1) {
										Intent dashboardIntent = new Intent(TransactionActivity.this, DashboardActivity.class);
										startActivity(dashboardIntent);
										finish();
								}
								if (item.getItemId() == R.id.page_3) {
										Intent budgetIntent = new Intent(TransactionActivity.this, BudgetActivity.class);
										startActivity(budgetIntent);
										finish();
								}
								if (item.getItemId() == R.id.page_4) {
										Intent profileIntent = new Intent(TransactionActivity.this, ProfileActivity.class);
										startActivity(profileIntent);
										finish();
								}
								return false;
						}
				});


				binding.fabBtn.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
								if (isExpanded) {
										ShrinkFab();
								} else {
										ShowFab();
								}
						}
				});

				/* Override the OnBackPressed function */
				OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
						@Override
						public void handleOnBackPressed() {
								if (isExpanded) {
										ShrinkFab();
								} else {
										Intent dashboardIntent = new Intent(TransactionActivity.this, DashboardActivity.class);
										startActivity(dashboardIntent);
										finish();
								}
						}
				};
				/* Activate the onBackPressed callback method */
				getOnBackPressedDispatcher().addCallback(this, backPressedCallback);

				// Retrieve all the transactions and display the data on the recycler view
				GetUserTransactions();

				binding.fabTransactionBtn.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
								//Create bottom-sheet dialog to collect information about the transaction
								BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(TransactionActivity.this);
								//Assign a view with the required fields to collect the data
								View bottomSheetView = LayoutInflater.from(TransactionActivity.this).inflate(R.layout.bottom_sheet_transaction_view, null);
								bottomSheetDialog.setContentView(bottomSheetView);
								bottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
										@Override
										public void onDismiss(DialogInterface dialog) {
												Snackbar.make(binding.getRoot(), "Bottom Sheet dismissed", Snackbar.LENGTH_SHORT).show();
										}
								});

								Button addTransactionBtn = bottomSheetView.findViewById(R.id.add_transaction);
								TextInputLayout transactionInputLayout = bottomSheetView.findViewById(R.id.transaction_name);
								MaterialAutoCompleteTextView transactionTextView = bottomSheetView.findViewById(R.id.transaction_type);
								TextInputLayout categoryInputLayout = bottomSheetView.findViewById(R.id.transaction_category);
								MaterialAutoCompleteTextView categoryTextView = bottomSheetView.findViewById(R.id.category);
								TextInputLayout budgetInputLayout = bottomSheetView.findViewById(R.id.transaction_budget);
								MaterialAutoCompleteTextView budgetTextView = bottomSheetView.findViewById(R.id.budget_name);
								TextInputLayout amountInputLayout = bottomSheetView.findViewById(R.id.transacted_amt);
								TextInputEditText amountTextView = bottomSheetView.findViewById(R.id.amount_transacted);

								// Get a list of transaction types
								ArrayAdapter<TransactionType> transactionAdapter = new ArrayAdapter<>(getApplicationContext(), com.google.android.material.R.layout.mtrl_auto_complete_simple_item, TransactionType.values());
								transactionTextView.setAdapter(transactionAdapter);

								// Get a list of all available categories
								ArrayAdapter<Categories> categoryAdapter = new ArrayAdapter<>(getApplicationContext(), com.google.android.material.R.layout.mtrl_auto_complete_simple_item, Categories.values());
								categoryTextView.setAdapter(categoryAdapter);

								//Get Budget List
								GetBudgetList(new BudgetListCallback() {
										@Override
										public void onBudgetListRetrieved(List<BudgetDto> budgetList) {
												List<String> budgetName = new ArrayList<>();
												for (BudgetDto budget : budgetList) {
														budgetName.add(budget.getBudgetName());
												}
												ArrayAdapter<String> budgetArrayAdapter = new ArrayAdapter<>(getApplicationContext(), com.google.android.material.R.layout.mtrl_auto_complete_simple_item, budgetName);
												budgetTextView.setAdapter(budgetArrayAdapter);
										}
								});

								addTransactionBtn.setOnClickListener(new View.OnClickListener() {
										@Override
										public void onClick(View v) {

												transactionInputLayout.setErrorEnabled(false);
												categoryInputLayout.setErrorEnabled(false);
												budgetInputLayout.setErrorEnabled(false);
												amountInputLayout.setErrorEnabled(false);

												if (transactionTextView.getText().toString().isEmpty()) {
														transactionInputLayout.setErrorEnabled(true);
														transactionInputLayout.setError("Required");
														return;
												}
												if (categoryTextView.getText().toString().isEmpty()) {
														categoryInputLayout.setErrorEnabled(true);
														categoryInputLayout.setError("Required");
														return;
												}
												if (budgetTextView.getText().toString().isEmpty()) {
														budgetInputLayout.setErrorEnabled(true);
														budgetInputLayout.setError("Required");
														return;
												}
												if (!isValidNumber(amountTextView.getText().toString())) {
														amountInputLayout.setErrorEnabled(true);
														amountInputLayout.setError("Amount can only contain numbers");
														return;
												}
												if (Float.parseFloat(amountTextView.getText().toString()) <= 0) {
														amountInputLayout.setErrorEnabled(true);
														amountInputLayout.setError("Amount can't be zero");
														return;
												}

												//Variables to be passed
												String transactionType = transactionTextView.getText().toString();
												String category = categoryTextView.getText().toString();
												String budget = budgetTextView.getText().toString();
												Double amount = Double.valueOf(amountTextView.getText().toString());
												LocalDateTime dateTime = LocalDateTime.now();

												if (firebaseAuth.getCurrentUser() != null) {
														// Create a transaction
														Transaction transaction = new Transaction(budget, category, transactionType, amount);

														addTransactionBtn.setEnabled(false);

														databaseReference = firebaseDatabase.getReference("transactions").child(firebaseAuth.getCurrentUser().getUid());
														databaseReference.keepSynced(true);
														databaseReference.child(String.valueOf(dateTime.getYear()))
																		.child(String.valueOf(dateTime.getMonth()))
																		.child(transaction.getTransactionID())
																		.setValue(transaction).addOnSuccessListener(new OnSuccessListener<Void>() {
																				@Override
																				public void onSuccess(Void unused) {

																						UpdateRemainingBudget(transaction);

																						PostHogAnalytics postHogAnalytics = new PostHogAnalytics();
																						Snackbar.make(bottomSheetView, "Transaction added successfully", Snackbar.LENGTH_SHORT).show();
																						transactionTextView.setText("");
																						categoryTextView.setText("");
																						budgetTextView.setText("");
																						amountTextView.setText("");
																						addTransactionBtn.setEnabled(true);
																						postHogAnalytics.LogTransactions(transaction);
																				}
																		}).addOnFailureListener(new OnFailureListener() {
																				@Override
																				public void onFailure(@NonNull Exception e) {
																						Snackbar.make(bottomSheetView, "An error occurred. Please try again", Snackbar.LENGTH_SHORT)
																										.setBackgroundTint(getResources().getColor(R.color.md_theme_light_error)).
																										show();
																						e.printStackTrace();
																						addTransactionBtn.setEnabled(true);
																				}
																		});

														// Check if the transaction type and register it as an income or expense
														if (transactionType.equals(TransactionType.Expense.name())) {
																Expense expense = new Expense(transaction);

																firestore.collection("Expenses")
																				.document(firebaseAuth.getCurrentUser()
																								.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
																						@Override
																						public void onSuccess(DocumentSnapshot documentSnapshot) {
																								if (documentSnapshot.exists()) {
																										Map<String, Object> expenses = documentSnapshot.getData();
																										if (expenses != null) {
																												expenses.put(expense.getExpenseID(), expense);
																												firestore.collection("Expenses")
																																.document(firebaseAuth.getCurrentUser()
																																				.getUid()).update(expenses).addOnSuccessListener(new OnSuccessListener<Void>() {
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
																										} else {
																												expenses.put(expense.getExpenseID(), expense);
																												firestore.collection("Expenses")
																																.document(firebaseAuth.getCurrentUser()
																																				.getUid()).set(expenses).addOnSuccessListener(new OnSuccessListener<Void>() {
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
																										}
																								}
																						}
																				});
														}
														if (transactionType.equals(TransactionType.Income.name())) {
																Income income = new Income(transaction);


																firestore.collection("Income")
																				.document(firebaseAuth.getCurrentUser()
																								.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
																						@Override
																						public void onSuccess(DocumentSnapshot documentSnapshot) {
																								if (documentSnapshot.exists()) {
																										Map<String, Object> incomes = documentSnapshot.getData();
																										if (!incomes.isEmpty()) {
																												incomes.put(income.getIncomeID(), income);
																												firestore.collection("Income")
																																.document(firebaseAuth.getCurrentUser()
																																				.getUid()).update(incomes).addOnSuccessListener(new OnSuccessListener<Void>() {
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
																										} else {
																												incomes.put(income.getIncomeID(), income);
																												firestore.collection("Income")
																																.document(firebaseAuth.getCurrentUser()
																																				.getUid()).set(incomes).addOnSuccessListener(new OnSuccessListener<Void>() {
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
																										}
																								}
																						}
																				}).addOnFailureListener(new OnFailureListener() {
																						@Override
																						public void onFailure(@NonNull Exception e) {
																								e.printStackTrace();
																						}
																				});
																System.out.println(transaction.getTransactionID());
														}
												}
										}
								});

								bottomSheetDialog.create();
								bottomSheetDialog.show();
						}
				});

				binding.fabBudgetBtn.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
								BottomSheetDialog createBudgetDialog = new BottomSheetDialog(TransactionActivity.this);
								View createBudgetView = LayoutInflater.from(TransactionActivity.this).inflate(R.layout.bottom_sheet_create_budget_view, null);
								createBudgetDialog.setContentView(createBudgetView);

								Button addBudgetBtn = createBudgetView.findViewById(R.id.add_budget);
								TextInputLayout budgetInputLayout = createBudgetView.findViewById(R.id.budget_name);
								TextInputEditText budgetTextView = createBudgetView.findViewById(R.id.budget_name_input);
								TextInputLayout amountInputLayout = createBudgetView.findViewById(R.id.budget_amount);
								TextInputEditText amountTextView = createBudgetView.findViewById(R.id.budget_amount_text);
								ProgressBar spinner = new ProgressBar(createBudgetView.getContext());
								spinner.setIndeterminate(true);


								addBudgetBtn.setOnClickListener(new View.OnClickListener() {
										@Override
										public void onClick(View v) {
												//Validate user data

												//Reset the error state of textInputFields
												amountInputLayout.setErrorEnabled(false);
												budgetInputLayout.setErrorEnabled(false);

												if (budgetTextView.getText().toString().isEmpty()) {
														budgetInputLayout.setErrorEnabled(true);
														budgetInputLayout.setError("Required");
														return;
												}
												if (amountTextView.getText().toString().isEmpty()) {
														amountInputLayout.setErrorEnabled(true);
														amountInputLayout.setError("Required");
														return;
												}
												if (!isValidString(budgetTextView.getText().toString())) {
														budgetInputLayout.setErrorEnabled(true);
														budgetInputLayout.setError("The name can only contain letters and must start with a capital letter");
														return;
												}
												if (!isValidNumber(amountTextView.getText().toString())) {
														amountInputLayout.setErrorEnabled(true);
														amountInputLayout.setError("Amount can only contain numbers");
														return;
												}
												if (Float.parseFloat(amountTextView.getText().toString()) <= 0) {
														amountInputLayout.setErrorEnabled(true);
														amountInputLayout.setError("Amount can't be zero");
														return;
												}

												// Get user information from the database and show the budgets created by the user
												if (firebaseAuth.getCurrentUser() != null) {

														Budget budget = new Budget(
																		budgetTextView.getText().toString().trim(),
																		Double.valueOf(amountTextView.getText().toString().trim()));
														RemainingBudget remainingBudget = new RemainingBudget(budget, budget.getAmount());
														LocalDateTime localDateTime = LocalDateTime.now();

														addBudgetBtn.setEnabled(false);

														GetBudgetList(new BudgetListCallback() {
																@Override
																public void onBudgetListRetrieved(List<BudgetDto> budgetList) {
																		if (budgetList.contains(budgetTextView.getText().toString().trim())) {
																				budgetInputLayout.setErrorEnabled(true);
																				budgetInputLayout.setError("A budget with this name already exists");
																				addBudgetBtn.setEnabled(true);
																		} else {
																				databaseReference = firebaseDatabase.getReference("budgets");
																				databaseReference.child(firebaseAuth.getCurrentUser().getUid())
																								.child(budget.getBudgetName())
																								.setValue(budget).addOnCompleteListener(new OnCompleteListener<Void>() {
																										@Override
																										public void onComplete(@NonNull Task<Void> task) {
																												if (task.isSuccessful()) {
																														PostHogAnalytics postHogAnalytics = new PostHogAnalytics();
																														Snackbar.make(createBudgetView, "Budget created successfully", Snackbar.LENGTH_SHORT).show();
																														budgetTextView.setText("");
																														amountTextView.setText("");
																														addBudgetBtn.setEnabled(true);
																														postHogAnalytics.LogCreatedBudgets(budget);
																												}
																										}
																								}).addOnFailureListener(new OnFailureListener() {
																										@Override
																										public void onFailure(@NonNull Exception e) {
																												Snackbar.make(createBudgetView, "An error occurred. Please try again", Snackbar.LENGTH_SHORT)
																																.setBackgroundTint(getResources().getColor(R.color.md_theme_light_error)).
																																show();
																												addBudgetBtn.setEnabled(true);
																										}
																								});

																				// Assign remaining budget for a specific budget on a specific month
																				databaseReference = firebaseDatabase.getReference("Remaining Budgets");
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
																		}
																}
														});
												}
										}
								});

								createBudgetDialog.create();
								createBudgetDialog.show();
						}
				});
		}

		//Validate string inputs
		private boolean isValidString(String input) {

				String stringRegex = "^(?=.*[A-Z])[A-Za-z]{1,20}$";

				Pattern pattern = Pattern.compile(stringRegex);
				Matcher matcher = pattern.matcher(input);
				return matcher.matches();
		}

		private boolean isValidNumber(String number) {
				String numberRegex = "0-9]+(\\.[0-9]+)?";

				Pattern pattern = Pattern.compile(numberRegex);
				Matcher matcher = pattern.matcher(number);
				return matcher.matches();
		}

		// Shrink the floating Action menu
		private void ShrinkFab() {
				// Button animations
				Animation toBottomAnimation = AnimationUtils.loadAnimation(this, R.anim.to_bottom_anim);
				Animation rotateAnticlockwiseAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_anticlockwise);

				binding.fabBtn.startAnimation(rotateAnticlockwiseAnim);
				binding.fabBudgetBtn.startAnimation(toBottomAnimation);
				binding.fabTransactionBtn.startAnimation(toBottomAnimation);
				isExpanded = !isExpanded;
		}

		// Show the floating action menu
		private void ShowFab() {
				// Button animations
				Animation fromBottomAnimation = AnimationUtils.loadAnimation(this, R.anim.from_bottom_anim);
				Animation rotateClockwiseAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_clockwise);

				binding.fabBtn.startAnimation(rotateClockwiseAnim);
				binding.fabBudgetBtn.startAnimation(fromBottomAnimation);
				binding.fabTransactionBtn.startAnimation(fromBottomAnimation);
				isExpanded = !isExpanded;
		}

		//Interface to get the budget name values in the list
		private interface BudgetListCallback {
				void onBudgetListRetrieved(List<BudgetDto> budgetList);

		}

		private void GetBudgetList(BudgetListCallback callback) {
				//List to store all budgets
				final HashMap<String, String> budgets = new HashMap<String, String>();
				final List<BudgetDto> budgetList = new ArrayList<>();

				//Get budgets from the db
				databaseReference = firebaseDatabase.getReference("budgets").child(firebaseAuth.getCurrentUser().getUid());
				databaseReference.addValueEventListener(new ValueEventListener() {
						@Override
						public void onDataChange(@NonNull DataSnapshot snapshot) {
								snapshot.getChildren().forEach(dataSnapshot -> {
										BudgetDto budget = dataSnapshot.getValue(BudgetDto.class);
										budgetList.add(budget);
								});

								callback.onBudgetListRetrieved(budgetList);
						}

						@Override
						public void onCancelled(@NonNull DatabaseError error) {

						}
				});
		}

		//Get user transactions
		public void GetUserTransactions() {

				ArrayList<TransactionDto> enteredTransactions = new ArrayList<>();
				CategoryRecyclerViewAdapter viewAdapter;
				ArrayList<LocalDate> transactionDate = new ArrayList<>();
				HashMap<LocalDate, ArrayList<TransactionDto>> transactionHashMap = new HashMap<>();


				binding.transactionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
				viewAdapter = new CategoryRecyclerViewAdapter(this, transactionHashMap, sortTransactionDates(transactionDate));
				binding.transactionsRecyclerView.setAdapter(viewAdapter);
				binding.transactionsRecyclerView.addItemDecoration(new MaterialDividerItemDecoration(this, MaterialDividerItemDecoration.VERTICAL));


				if (firebaseAuth.getCurrentUser() != null) {
						databaseReference = firebaseDatabase.getReference("transactions").child(firebaseAuth.getCurrentUser().getUid());
						databaseReference.keepSynced(true);
						databaseReference.addValueEventListener(new ValueEventListener() {
								@Override
								public void onDataChange(@NonNull DataSnapshot snapshot) {
										transactionDate.clear();
										transactionHashMap.clear();
										enteredTransactions.clear();
										binding.transactionFilter.setSingleSelection(true);
										binding.transactionFilter.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
												@Override
												public void onCheckedChanged(@NonNull ChipGroup chipGroup, @NonNull List<Integer> list) {
														transactionDate.clear();
														transactionHashMap.clear();
														enteredTransactions.clear();

														for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
																dataSnapshot.getChildren().forEach(dataSnapshot1 -> {
																		dataSnapshot1.getChildren().forEach(
																						dataSnapshot2 -> {
																								TransactionDto transaction = dataSnapshot2.getValue(TransactionDto.class);
																								DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
																								LocalDateTime localDateTime = LocalDateTime.parse(transaction.getCreatedAt(), formatter);
																								//Filter data according to whether it is an income or expense
																								for (int x : list) {
																										Chip chip1 = findViewById(x);
																										if (chip1.getText().equals(transaction.getTransactionName())) {
																												if (!transactionDate.contains(localDateTime.toLocalDate())) {
																														transactionDate.add(localDateTime.toLocalDate());
																												}
																												if (transactionHashMap.containsKey(localDateTime.toLocalDate())) {
																														if (LocalDate.parse(transaction.getCreatedAt(), formatter).equals(localDateTime.toLocalDate())) {
																																transactionHashMap.get(localDateTime.toLocalDate()).add(transaction);
																														}
																												} else {
																														transactionHashMap.put(localDateTime.toLocalDate(), enteredTransactions);
																												}
																										}
																								}
																								//Return a list of all transactions when there is no filter applied
																								if (list.isEmpty()) {
																										if (!transactionDate.contains(localDateTime.toLocalDate())) {
																												transactionDate.add(localDateTime.toLocalDate());
																										}
																										if (transactionHashMap.containsKey(localDateTime.toLocalDate())) {
																												if (LocalDate.parse(transaction.getCreatedAt(), formatter).equals(localDateTime.toLocalDate())) {
																														transactionHashMap.get(localDateTime.toLocalDate()).add(transaction);
																												}
																										} else {
																												transactionHashMap.put(localDateTime.toLocalDate(), enteredTransactions);
																										}
																								}
																						}
																		);
																});
														}
														viewAdapter.notifyDataSetChanged();

												}
										});

										// On-Load, display all the transactions before filters are applied

										if (!binding.transactionFilter.isSelected()) {
												for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
														dataSnapshot.getChildren().forEach(dataSnapshot1 -> {
																dataSnapshot1.getChildren().forEach(
																				dataSnapshot2 -> {
																						TransactionDto transaction = dataSnapshot2.getValue(TransactionDto.class);

																						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
																						LocalDateTime localDateTime = LocalDateTime.parse(transaction.getCreatedAt(), formatter);


																						if (!transactionDate.contains(localDateTime.toLocalDate())) {
																								transactionDate.add(localDateTime.toLocalDate());
																						}
																						if (!transactionHashMap.containsKey(localDateTime.toLocalDate())) {
																								transactionHashMap.put(localDateTime.toLocalDate(), enteredTransactions);
																						}
																						else {
																								if (LocalDate.parse(transaction.getCreatedAt(), formatter).equals(localDateTime.toLocalDate())) {
																										transactionHashMap.get(localDateTime.toLocalDate()).add(transaction);
																								}
																						}

																				}
																);
														});
												}
												System.out.println(transactionHashMap);
												System.out.println(transactionDate);
												viewAdapter.notifyDataSetChanged();
										}
								}

								@Override
								public void onCancelled(@NonNull DatabaseError error) {
										Log.w("Database Error", error.getDetails());
								}
						});
				}
		}

		// Update remaining budget value
		public void UpdateRemainingBudget(Transaction transaction) {
				LocalDateTime dateTime = LocalDateTime.now();
				Map<String, Object> remainingBudgetMap = new HashMap<>();
				if (transaction.getTransactionName().equals(TransactionType.Expense.name())) {
						databaseReference = firebaseDatabase.getReference("Remaining Budgets").child(firebaseAuth.getCurrentUser().getUid());
						databaseReference.child(String.valueOf(dateTime.getYear()))
										.child(String.valueOf(dateTime.getMonth()))
										.child(transaction.getBudgetID()).addValueEventListener(new ValueEventListener() {
												@Override
												public void onDataChange(@NonNull DataSnapshot snapshot) {
														if (snapshot.exists()) {
																RemainingBudget budget = snapshot.getValue(RemainingBudget.class);
																System.out.println(snapshot.getValue());
																if (budget != null) {
																		Double amount = budget.getRemainingAmt();
																		Double finalAmount = amount - transaction.getAmount();

																		remainingBudgetMap.put("remainingAmt", finalAmount);
																		remainingBudgetMap.put("updatedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

																		databaseReference = firebaseDatabase.getReference("Remaining Budgets").child(firebaseAuth.getCurrentUser().getUid());
																		databaseReference.child(String.valueOf(dateTime.getYear()))
																						.child(String.valueOf(dateTime.getMonth()))
																						.child(transaction.getBudgetID()).updateChildren(remainingBudgetMap);
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

		public static ArrayList<LocalDate> sortTransactionDates(ArrayList<LocalDate> transactionDate) {
				transactionDate.sort(Comparator.reverseOrder());
				return transactionDate;
		}

}