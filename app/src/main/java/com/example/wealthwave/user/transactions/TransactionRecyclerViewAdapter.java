package com.example.wealthwave.user.transactions;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wealthwave.PostHogAnalytics;
import com.example.wealthwave.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.Budget;
import models.RemainingBudget;
import models.Transaction;
import models.dtos.TransactionDto;

public class TransactionRecyclerViewAdapter extends RecyclerView.Adapter<TransactionRecyclerViewAdapter.MyViewHolder> {

		Context context;
		ArrayList<TransactionDto> list;
		FirebaseDatabase database = FirebaseDatabase.getInstance("https://wealthwave-c1cca-default-rtdb.europe-west1.firebasedatabase.app");

		public TransactionRecyclerViewAdapter(Context context, ArrayList<TransactionDto> list) {
				this.context = context;
				this.list = list;
		}

		@NonNull
		@Override
		public TransactionRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
				View v = LayoutInflater.from(context).inflate(R.layout.transaction_details_view, parent, false);
				return new MyViewHolder(v);
		}

		@Override
		public void onBindViewHolder(@NonNull TransactionRecyclerViewAdapter.MyViewHolder holder, int position) {
				TransactionDto transaction = list.get(position);
				DecimalFormat format = new DecimalFormat("#,###,###,###.00");

				holder.BudgetName.setText(transaction.getCategory());
				holder.amount.setText(String.format("KES %s", format.format(transaction.getAmount())));
				holder.options.setOnClickListener(v -> {
						showPopup(holder.itemView, R.menu.edit_delete_options_menu, transaction);
				});
		}

		@Override
		public int getItemCount() {
				return list.size();
		}

		public static class MyViewHolder extends RecyclerView.ViewHolder {
				TextView BudgetName, amount;
				ImageButton options;

				public MyViewHolder(@NonNull View itemView) {
						super(itemView);

						BudgetName = itemView.findViewById(R.id.budget_name);
						amount = itemView.findViewById(R.id.budget_amount);
						options = itemView.findViewById(R.id.menu_items);
				}
		}


		private void EditTransaction(TransactionDto transaction, View topView) {

				DatabaseReference dbRef = database.getReference("transactions");
				View view = LayoutInflater.from(this.context).inflate(R.layout.edit_transaction_view, null);


				TextInputLayout transactionInputLayout = view.findViewById(R.id.transaction_name);
				MaterialAutoCompleteTextView transactionTextView = view.findViewById(R.id.transaction_type);
				TextInputLayout categoryInputLayout = view.findViewById(R.id.transaction_category);
				MaterialAutoCompleteTextView categoryTextView = view.findViewById(R.id.category);
				TextInputLayout budgetInputLayout = view.findViewById(R.id.transaction_budget);
				MaterialAutoCompleteTextView budgetTextView = view.findViewById(R.id.budget_name);
				TextInputLayout amountInputLayout = view.findViewById(R.id.transacted_amt);
				TextInputEditText amountTextView = view.findViewById(R.id.amount_transacted);

				transactionTextView.setText(transaction.getTransactionName());
				categoryTextView.setText(transaction.getCategory());
				budgetTextView.setText(transaction.getBudgetID());
				amountTextView.setText(transaction.getAmount().toString());

				ArrayAdapter<String> budgetArrayAdapter = new ArrayAdapter<>(this.context, com.google.android.material.R.layout.mtrl_auto_complete_simple_item, GetBudget());
				budgetTextView.setAdapter(budgetArrayAdapter);

				MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this.context)
								.setView(view)
								.setNegativeButton("No", ((dialog, which) -> {
										dialog.dismiss();
								})).setPositiveButton("Edit", ((dialog, which) -> {

								}));

				builder.show().getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
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
						String budget = budgetTextView.getText().toString();
						Double amount = Double.valueOf(amountTextView.getText().toString());
						LocalDateTime dateTime = LocalDateTime.now();
						Map<String, Object> transactionEdit = new HashMap<>();

						if (FirebaseAuth.getInstance().getCurrentUser() != null) {
								// Create a transaction

								transactionEdit.put("budgetID", transaction.getBudgetID());
								transactionEdit.put("amount", transaction.getAmount());

								dbRef.child(String.valueOf(dateTime.getYear()))
												.child(String.valueOf(dateTime.getMonth()))
												.child(transaction.getTransactionID())
												.updateChildren(transactionEdit)
												.addOnSuccessListener(unused ->
																Snackbar.make(topView, "Updated successfully",  Snackbar.LENGTH_SHORT).show()
												).addOnFailureListener(e -> e.printStackTrace());
						}



				});

		}

		private void DeleteTransaction(TransactionDto transaction) {
				DatabaseReference dbRef = database.getReference("transactions");
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
				LocalDateTime dateTime = LocalDateTime.parse(transaction.getCreatedAt(), formatter);

				MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this.context);
				dialogBuilder.setTitle("Are you sure you want to delete this transaction?")
								.setPositiveButton("Yes", (dialog, which) -> {
										Query query = dbRef
														.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
														.child(String.valueOf(dateTime.getYear()))
														.child(String.valueOf(dateTime.getMonth()))
														.child(transaction.getTransactionID());

										query.addListenerForSingleValueEvent(new ValueEventListener() {
												@Override
												public void onDataChange(@NonNull DataSnapshot snapshot) {
														snapshot.getRef().removeValue();
														Log.d("Successfull", "Value removed");

														UpdateRemainingBudget(transaction, transaction.getAmount(), dateTime, FirebaseAuth.getInstance().getCurrentUser().getUid());
												}

												@Override
												public void onCancelled(@NonNull DatabaseError error) {
														Log.d("Database error", error.getDetails());
												}
										});
								}).setNegativeButton("No", (dialog, which) -> {
										dialog.cancel();
								});

				dialogBuilder.create().show();
		}

		private void showPopup(View view, @MenuRes Integer menuRes, TransactionDto transaction) {
				PopupMenu popupMenu = new PopupMenu(this.context, view);
				popupMenu.getMenuInflater().inflate(menuRes, popupMenu.getMenu());

				popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
						@Override
						public boolean onMenuItemClick(MenuItem item) {
								switch (item.toString()) {
										case "Edit":
												EditTransaction(transaction, view);
												break;
										case "Delete":
												DeleteTransaction(transaction);
												break;
								}
								return false;
						}
				});
				popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
						@Override
						public void onDismiss(PopupMenu menu) {
								menu.dismiss();
						}
				});

				popupMenu.show();
		}

		private boolean isValidNumber(String number) {
				String numberRegex = "^(?:-(?:[1-9](?:\\d{0,2}(?:,\\d{3})+|\\d*))|(?:0|(?:[1-9](?:\\d{0,2}(?:,\\d{3})+|\\d*))))(?:.\\d+|)$";

				Pattern pattern = Pattern.compile(numberRegex);
				Matcher matcher = pattern.matcher(number);
				return matcher.matches();
		}

		private List<String> GetBudget(){

				List<String> budgets = new ArrayList<>();
				DatabaseReference dbRef = database.getReference("budgets");

				dbRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
								.get()
								.addOnCompleteListener(task -> {

										if(task.isSuccessful()){
												task.getResult().getChildren().forEach(dataSnapshot -> {
														Budget budget = dataSnapshot.getValue(Budget.class);
														budgets.add(budget.getBudgetName());
												});
										}
								});

				return budgets;
		}

		public void UpdateRemainingBudget(TransactionDto transaction, Double amount, LocalDateTime dateTime, String user) {
				Map<String, RemainingBudget> remainingBudgetMap = new HashMap<>();
				Map<String, Object> newRemainingBudgetMap = new HashMap<>();

				//Update remaining transaction
				database.getReference("Remaining Budgets").child(user).child(String.valueOf(dateTime.getYear()))
								.child(String.valueOf(dateTime.getMonth())).get()
								.addOnCompleteListener(task -> {

										task.getResult().getChildren().forEach(dataSnapshot -> {
												RemainingBudget remainingBudget = dataSnapshot.getValue(RemainingBudget.class);
												remainingBudgetMap.put(dataSnapshot.getKey(), remainingBudget);
										});
										System.out.println(remainingBudgetMap);
										Double remainingAmt = remainingBudgetMap.get(transaction.getBudgetID()).getRemainingAmt();
										Double newRemainingAmt = remainingAmt + amount;
										newRemainingBudgetMap.put("remainingAmt", newRemainingAmt);
										newRemainingBudgetMap.put("updatedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

										database.getReference("Remaining Budgets").child(user).child(String.valueOf(dateTime.getYear()))
														.child(String.valueOf(dateTime.getMonth())).child(transaction.getBudgetID()).updateChildren(newRemainingBudgetMap)
														.addOnCompleteListener(new OnCompleteListener<Void>() {
																@Override
																public void onComplete(@NonNull Task<Void> task) {
																		if (task.isSuccessful()) {
																				Log.w("Successful", "Success");
																		}
																}
														});

								});
		}
}
