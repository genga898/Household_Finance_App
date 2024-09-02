package com.example.wealthwave.user.transactions;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;

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
import com.google.android.material.checkbox.MaterialCheckBox;
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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
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

		enum IncomeCategories {
				Salary("Salary"),
				Bonuses("Bonuses"),
				Rental_income("Rental income"),
				Commissions("Commissions"),
				Investment_income("Investment income"),
				Pension("Pension"),
				Inheritance("Inheritance"),
				Government_benefits("Government Benefits");

				private final String label;

				IncomeCategories(String s) {
						this.label = s;
				}

				@NonNull
				@Override
				public String toString() {
						return label;
				}

				public static ArrayList<String> getEnumValues() {
						IncomeCategories[] categories = IncomeCategories.values();
						List<String> labelsList = Arrays.asList(Arrays.stream(categories)
										.map(IncomeCategories::toString)
										.toArray(String[]::new));
						return new ArrayList<>(labelsList);
				}
		}

		enum ExpenseCategories {
				Rent("Rent/Mortgage"),
				Groceries("Groceries"),
				Transport("Transport"),
				Health_Insurance("Health Insurance"),
				Entertainment("Entertainment"),
				Dining_out("Dining out"),
				Personal_Grooming("Personal Grooming"),
				Repairs("Repairs"),
				Water_bills("Water Bills"),
				Electricity_bills("Electricity Bills"),
				Internet_bills("Internet Bills");

				private final String label;

				ExpenseCategories(String s) {
						this.label = s;
				}

				@NonNull
				@Override
				public String toString() {
						return label;
				}

				public static ArrayList<String> getEnumValues() {
						ExpenseCategories[] categories = ExpenseCategories.values();
						List<String> labelsList = Arrays.asList(Arrays.stream(categories)
										.map(ExpenseCategories::toString)
										.toArray(String[]::new));
						return new ArrayList<>(labelsList);
				}

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
								TextInputLayout groceriesInputLayout = bottomSheetView.findViewById(R.id.transaction_budget);
								MaterialAutoCompleteTextView groceriesTextView = bottomSheetView.findViewById(R.id.budget_name);
								TextInputLayout amountInputLayout = bottomSheetView.findViewById(R.id.transacted_amt);
								TextInputEditText amountTextView = bottomSheetView.findViewById(R.id.amount_transacted);

								// Get a list of transaction types
								ArrayAdapter<TransactionType> transactionAdapter = new ArrayAdapter<>(getApplicationContext(), com.google.android.material.R.layout.mtrl_auto_complete_simple_item, TransactionType.values());
								transactionTextView.setAdapter(transactionAdapter);
								transactionTextView.addTextChangedListener(new TextWatcher() {
										@Override
										public void beforeTextChanged(CharSequence s, int start, int count, int after) {

										}

										@Override
										public void onTextChanged(CharSequence s, int start, int before, int count) {

										}

										@Override
										public void afterTextChanged(Editable s) {
												String transactionType = s.toString();
												if (!transactionType.isEmpty()) {
														ArrayAdapter<String> categoryAdapter = null;
														if (transactionType.equals(TransactionType.Expense.name())) {
																categoryAdapter = new ArrayAdapter<>(getApplicationContext(), com.google.android.material.R.layout.mtrl_auto_complete_simple_item, ExpenseCategories.getEnumValues());
														} else if (transactionType.equals(TransactionType.Income.name())) {
																categoryAdapter = new ArrayAdapter<>(getApplicationContext(), com.google.android.material.R.layout.mtrl_auto_complete_simple_item, IncomeCategories.getEnumValues());
														}

														categoryTextView.setAdapter(categoryAdapter);
												}

										}
								});


								//Get Budget List
								GetBudgetList(budgetList -> {
										List<String> budgetName = new ArrayList<>();
										for (BudgetDto budget : budgetList) {
												budgetName.add(budget.getBudgetName());
										}
										ArrayAdapter<String> budgetArrayAdapter = new ArrayAdapter<>(getApplicationContext(), com.google.android.material.R.layout.mtrl_auto_complete_simple_item, budgetName);
										groceriesTextView.setAdapter(budgetArrayAdapter);
								});

								amountTextView.addTextChangedListener(new TextWatcher() {
										@Override
										public void beforeTextChanged(CharSequence s, int start, int count, int after) {

										}

										@Override
										public void onTextChanged(CharSequence s, int start, int before, int count) {
												amountInputLayout.setErrorEnabled(false);
												if (!isValidNumber(amountTextView.getText().toString().trim())) {
														amountInputLayout.setErrorEnabled(true);
														amountInputLayout.setError("Amount can only contain numbers");
														return;
												}
												getRemainingBudget(groceriesTextView.getText().toString().trim(), remainingBudget -> {
														if (remainingBudget != null && !TextUtils.isEmpty(amountTextView.getText().toString())) {
																if (Double.parseDouble(amountTextView.getText().toString().trim()) > remainingBudget.getRemainingAmt()) {
																		amountInputLayout.setErrorEnabled(true);
																		amountInputLayout.setError("Amount cannot be greater than budget balance");
																}
														}
												});
										}

										@Override
										public void afterTextChanged(Editable s) {
												amountInputLayout.setErrorEnabled(false);
												getRemainingBudget(groceriesTextView.getText().toString().trim(), remainingBudget -> {
														if (remainingBudget != null && !TextUtils.isEmpty(amountTextView.getText().toString())) {
																if (Double.parseDouble(amountTextView.getText().toString().trim()) > remainingBudget.getRemainingAmt()) {
																		amountInputLayout.setErrorEnabled(true);
																		amountInputLayout.setError("Amount cannot be greater than budget balance");
																}
														}
												});
										}
								});

								addTransactionBtn.setOnClickListener(new View.OnClickListener() {
										@Override
										public void onClick(View v) {

												transactionInputLayout.setErrorEnabled(false);
												categoryInputLayout.setErrorEnabled(false);
												groceriesInputLayout.setErrorEnabled(false);
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
												if (groceriesTextView.getText().toString().isEmpty()) {
														groceriesInputLayout.setErrorEnabled(true);
														groceriesInputLayout.setError("Required");
														return;
												}
												if (!isValidNumber(amountTextView.getText().toString().trim())) {
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
												String budget = groceriesTextView.getText().toString();
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
																						saveTransactionToFirestore(transaction);

																						PostHogAnalytics postHogAnalytics = new PostHogAnalytics();
																						Snackbar.make(bottomSheetView, "Transaction added successfully", Snackbar.LENGTH_SHORT).show();
																						transactionTextView.setText("");
																						categoryTextView.setText("");
																						groceriesTextView.setText("");
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
								View createBudgetView = LayoutInflater.from(TransactionActivity.this).inflate(R.layout.bottomsheet_create_budget_view, null);
								createBudgetDialog.setContentView(createBudgetView);
								HashMap<String, Budget> selectedBudgetValues = new HashMap<>();

								Button addBudgetBtn = createBudgetView.findViewById(R.id.add_budget);

								MaterialCheckBox groceriesCheckBox = createBudgetView.findViewById(R.id.checkbox_groceries);
								MaterialCheckBox feesCheckBox = createBudgetView.findViewById(R.id.checkbox_sch_fees);
								MaterialCheckBox insuranceCheckBox = createBudgetView.findViewById(R.id.checkbox_insurance);
								MaterialCheckBox entertainmentCheckBox = createBudgetView.findViewById(R.id.checkbox_entertainment);
								MaterialCheckBox utilitiesCheckBox = createBudgetView.findViewById(R.id.checkbox_utilities);
								MaterialCheckBox debtCheckBox = createBudgetView.findViewById(R.id.checkbox_loans);

								TextInputEditText groceriesAmt = createBudgetView.findViewById(R.id.amt_groceries);
								TextInputEditText feesAmt = createBudgetView.findViewById(R.id.amt_sch_fees);
								TextInputEditText insuranceAmt = createBudgetView.findViewById(R.id.amt_insurance);
								TextInputEditText entertainmentAmt = createBudgetView.findViewById(R.id.amt_entertainment);
								TextInputEditText utilitiesAmt = createBudgetView.findViewById(R.id.amt_utilities);
								TextInputEditText debtsAmt = createBudgetView.findViewById(R.id.amt_loans);


								CompoundButton.OnCheckedChangeListener checkedChangeListener = (buttonView, isChecked) -> {
										MaterialCheckBox checkBox = (MaterialCheckBox) buttonView;
										String name = checkBox.getText().toString();

										TextInputLayout inputLayout = getTextInputLayout(name, createBudgetView);
										TextInputEditText amount = getBudgetAmount(name, createBudgetView);

										TextWatcher watcher = new TextWatcher() {
												@Override
												public void beforeTextChanged(CharSequence s, int start, int count, int after) {

												}

												@Override
												public void onTextChanged(CharSequence s, int start, int before, int count) {
														if (!isValidNumber(amount.getText().toString())) {
																inputLayout.setErrorEnabled(true);
																inputLayout.setError("Please enter a valid number");
														}
												}

												@Override
												public void afterTextChanged(Editable s) {
														if (!TextUtils.isEmpty(s.toString())) {
																inputLayout.setErrorEnabled(false);
																Budget budget = new Budget(name, Double.valueOf(s.toString()));
																selectedBudgetValues.put(name, budget);
														}
												}
										};
										if (isChecked) {
												amount.addTextChangedListener(watcher);
										}
								};

								groceriesCheckBox.setOnCheckedChangeListener(checkedChangeListener);
								feesCheckBox.setOnCheckedChangeListener(checkedChangeListener);
								insuranceCheckBox.setOnCheckedChangeListener(checkedChangeListener);
								entertainmentCheckBox.setOnCheckedChangeListener(checkedChangeListener);
								utilitiesCheckBox.setOnCheckedChangeListener(checkedChangeListener);
								debtCheckBox.setOnCheckedChangeListener(checkedChangeListener);

								addBudgetBtn.setOnClickListener(v1 -> {
										addBudgetBtn.setEnabled(false);
										LocalDateTime localDateTime = LocalDateTime.now();
										PostHogAnalytics analytics = new PostHogAnalytics();
										databaseReference = firebaseDatabase.getReference("budgets");

										selectedBudgetValues.forEach((string, budget) -> {
												databaseReference
																.child(firebaseAuth.getCurrentUser().getUid())
																.child(budget.getBudgetName())
																.setValue(budget).addOnCompleteListener(task -> {
																		if (task.isSuccessful()) {
																				analytics.LogCreatedBudgets(budget);
																				addBudgetBtn.setEnabled(true);
																				Snackbar.make(createBudgetView, "Budget created successfully", Snackbar.LENGTH_SHORT).show();


																				//Assign remaining budget values
																				RemainingBudget remainingBudget = new RemainingBudget(budget, budget.getAmount());
																				databaseReference = firebaseDatabase.getReference("Remaining Budgets");
																				databaseReference.child(firebaseAuth.getCurrentUser().getUid())
																								.child(String.valueOf(localDateTime.getYear()))
																								.child(String.valueOf(localDateTime.getMonth()))
																								.child(budget.getBudgetName())
																								.setValue(remainingBudget).addOnSuccessListener(unused -> {
																										Log.d("Success", "Successful");

																										//Clear user inputs
																										groceriesCheckBox.setChecked(false);
																										feesCheckBox.setChecked(false);
																										insuranceCheckBox.setChecked(false);
																										entertainmentCheckBox.setChecked(false);
																										utilitiesCheckBox.setChecked(false);
																										debtCheckBox.setChecked(false);

																										groceriesAmt.setText("");
																										feesAmt.setText("");
																										insuranceAmt.setText("");
																										entertainmentAmt.setText("");
																										utilitiesAmt.setText("");
																										debtsAmt.setText("");


																								}).addOnFailureListener(e -> e.printStackTrace());

																		}
																});
										});
								});
								createBudgetDialog.create();
								createBudgetDialog.show();
						}
				});
		}

		private TextInputEditText getBudgetAmount(String category, View view) {
				switch (category) {
						case "Groceries":
								return view.findViewById(R.id.amt_groceries);
						case "School Fees":
								return view.findViewById(R.id.amt_sch_fees);
						case "Insurance":
								return view.findViewById(R.id.amt_insurance);
						case "Entertainment":
								return view.findViewById(R.id.amt_entertainment);
						case "Utilities":
								return view.findViewById(R.id.amt_utilities);
						case "Debts":
								return view.findViewById(R.id.amt_loans);
						default:
								return null;
				}
		}

		private TextInputLayout getTextInputLayout(String category, View view) {
				switch (category) {
						case "Groceries":
								return view.findViewById(R.id.groceries_amt);
						case "School Fees":
								return view.findViewById(R.id.sch_fees_amt);
						case "Insurance":
								return view.findViewById(R.id.insurance_amt);
						case "Entertainment":
								return view.findViewById(R.id.entertainment_amt);
						case "Utilities":
								return view.findViewById(R.id.utilities_amt);
						case "Debts":
								return view.findViewById(R.id.loans_amt);
						default:
								return null;
				}
		}


		private interface RemainingBudgetCallback {
				void onCallBack(RemainingBudget remainingBudget);
		}

		private void getRemainingBudget(String budget, RemainingBudgetCallback callback) {
				LocalDateTime dateTime = LocalDateTime.now();
				databaseReference = firebaseDatabase.getReference("Remaining Budgets").child(firebaseAuth.getCurrentUser().getUid());
				databaseReference.child(String.valueOf(dateTime.getYear()))
								.child(String.valueOf(dateTime.getMonth()))
								.child(budget).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
										@Override
										public void onComplete(@NonNull Task<DataSnapshot> task) {

												DataSnapshot snapshot = task.getResult();
												if (snapshot.exists()) {
														RemainingBudget remainingBudget = snapshot.getValue(RemainingBudget.class);
														callback.onCallBack(remainingBudget);
												} else {
														callback.onCallBack(null);
												}
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
				String numberRegex = "^(?:-(?:[1-9](?:\\d{0,2}(?:,\\d{3})+|\\d*))|(?:0|(?:[1-9](?:\\d{0,2}(?:,\\d{3})+|\\d*))))(?:.\\d+|)$";

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

		// Save expense or income to db
		private void saveTransactionToFirestore(Transaction transaction) {
				String transactionType = transaction.getTransactionName();
				String userId = firebaseAuth.getCurrentUser().getUid();

				if (transactionType.equals(TransactionType.Expense.name())) {
						Expense expense = new Expense(transaction);
						saveToFirestoreCollection("Expenses", userId, expense.getExpenseID(), expense);
				} else if (transactionType.equals(TransactionType.Income.name())) {
						Income income = new Income(transaction);
						saveToFirestoreCollection("Income", userId, income.getIncomeID(), income);
				}
		}

		private void saveToFirestoreCollection(String collectionName, String userId, String documentId, Object data) {
				FirebaseFirestore firestore = FirebaseFirestore.getInstance();

				firestore.collection(collectionName)
								.document(userId)
								.get()
								.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
										@Override
										public void onSuccess(DocumentSnapshot documentSnapshot) {
												if (documentSnapshot.exists()) {
														Map<String, Object> dataMap = documentSnapshot.getData();
														if (dataMap != null) {
																dataMap.put(documentId, data);
																firestore.collection(collectionName)
																				.document(userId)
																				.update(dataMap)
																				.addOnSuccessListener(new OnSuccessListener<Void>() {
																						@Override
																						public void onSuccess(Void unused) {
																								Log.d("Success", "Successful");
																						}
																				})
																				.addOnFailureListener(new OnFailureListener() {
																						@Override
																						public void onFailure(@NonNull Exception e) {
																								e.printStackTrace();
																						}
																				});
														} else {
																dataMap.put(documentId, data);
																firestore.collection(collectionName)
																				.document(userId)
																				.set(dataMap)
																				.addOnSuccessListener(new OnSuccessListener<Void>() {
																						@Override
																						public void onSuccess(Void unused) {
																								Log.d("Success", "Successful");
																						}
																				})
																				.addOnFailureListener(new OnFailureListener() {
																						@Override
																						public void onFailure(@NonNull Exception e) {
																								e.printStackTrace();
																						}
																				});
														}
												}
										}
								})
								.addOnFailureListener(new OnFailureListener() {
										@Override
										public void onFailure(@NonNull Exception e) {
												e.printStackTrace();
										}
								});
		}


		//Get user transactions
		public void GetUserTransactions() {
				CategoryRecyclerViewAdapter viewAdapter;
				ArrayList<LocalDate> transactionDate = new ArrayList<>();
				HashMap<LocalDate, ArrayList<TransactionDto>> transactionHashMap = new HashMap<>();


				binding.transactionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
				viewAdapter = new CategoryRecyclerViewAdapter(this, transactionHashMap, transactionDate);
				binding.transactionsRecyclerView.setAdapter(viewAdapter);
				binding.transactionsRecyclerView.setHasFixedSize(false);
				binding.transactionsRecyclerView.addItemDecoration(new MaterialDividerItemDecoration(this, MaterialDividerItemDecoration.VERTICAL));


				if (firebaseAuth.getCurrentUser() != null) {
						databaseReference = firebaseDatabase.getReference("transactions").child(firebaseAuth.getCurrentUser().getUid());
						databaseReference.keepSynced(true);
						databaseReference.addValueEventListener(new ValueEventListener() {
								@Override
								public void onDataChange(@NonNull DataSnapshot snapshot) {
										transactionDate.clear();
										transactionHashMap.clear();
										binding.transactionFilter.setSingleSelection(true);
										binding.transactionFilter.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
												@Override
												public void onCheckedChanged(@NonNull ChipGroup chipGroup, @NonNull List<Integer> list) {
														transactionDate.clear();
														transactionHashMap.clear();

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
																														transactionHashMap.put(localDateTime.toLocalDate(), new ArrayList<>());
																												}

																												sortTransactionDates(transactionDate);
																												transactionHashMap.get(localDateTime.toLocalDate()).add(transaction);
																										}
																								}
																								//Return a list of all transactions when there is no filter applied
																								if (list.isEmpty()) {
																										if (!transactionDate.contains(localDateTime.toLocalDate())) {
																												transactionDate.add(localDateTime.toLocalDate());
																												transactionHashMap.put(localDateTime.toLocalDate(), new ArrayList<>());
																										}

																										sortTransactionDates(transactionDate);
																										transactionHashMap.get(localDateTime.toLocalDate()).add(transaction);
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
																								transactionHashMap.put(localDateTime.toLocalDate(), new ArrayList<>());
																						}

																						sortTransactionDates(transactionDate);
																						transactionHashMap.get(localDateTime.toLocalDate()).add(transaction);

																				}
																);
														});
												}
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
										.child(transaction.getBudgetID()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
												@Override
												public void onComplete(@NonNull Task<DataSnapshot> task) {

														DataSnapshot snapshot = task.getResult();
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
										});
				}
		}

		public static ArrayList<LocalDate> sortTransactionDates(ArrayList<LocalDate> transactionDate) {
				transactionDate.sort(Comparator.reverseOrder());
				return transactionDate;
		}

}