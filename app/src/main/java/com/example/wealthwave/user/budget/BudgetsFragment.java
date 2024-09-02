package com.example.wealthwave.user.budget;

import static com.google.android.material.color.MaterialColors.getColor;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.wealthwave.R;
import com.example.wealthwave.databinding.FragmentBudgetsBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.RemainingBudget;
import models.dtos.BudgetDto;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BudgetsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BudgetsFragment extends Fragment implements RecyclerViewInterface{

		FragmentBudgetsBinding budgetsBinding;
		ArrayList<BudgetDto> list;
		FirebaseDatabase databaseReference = FirebaseDatabase.getInstance("https://wealthwave-c1cca-default-rtdb.europe-west1.firebasedatabase.app") ;
		BudgetRecyclerViewAdapter adapter;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
		                         Bundle savedInstanceState) {

				budgetsBinding = FragmentBudgetsBinding.inflate(inflater, container, false);
				View view = budgetsBinding.getRoot();

				list = new ArrayList<>();

				budgetsBinding.budgetRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
				adapter = new BudgetRecyclerViewAdapter(this, this.getContext(), list);
				budgetsBinding.budgetRecyclerView.setAdapter(adapter);

				databaseReference.getReference("budgets")
								.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
								.addValueEventListener(new ValueEventListener() {
						@Override
						public void onDataChange(@NonNull DataSnapshot snapshot) {
								list.clear();
								for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
										BudgetDto budgetDto = dataSnapshot.getValue(BudgetDto.class);
										list.add(budgetDto);
								}
								adapter.notifyDataSetChanged();
						}

						@Override
						public void onCancelled(@NonNull DatabaseError error) {

						}
				});

				// Inflate the layout for this fragment
				return view;
		}

		@Override
		public void onItemClick(int position) {
				showPopup(this.getView(), R.menu.edit_delete_options_menu, position);
		}

		private void EditBudget(int position){
				BudgetDto budget = list.get(position);
				View view = LayoutInflater.from(this.getContext()).inflate(R.layout.update_budget_amount_view, null);
				View view1 = LayoutInflater.from(this.getContext()).inflate(R.layout.budget_details_view, null);

				TextInputLayout amountInputLayout = view.findViewById(R.id.budget_amount);
				TextInputEditText amountTextView = view.findViewById(R.id.budget_amount_text);
				TextView textView = view1.findViewById(R.id.amount_text);
				textView.setText(String.format("KES %s", budget.getAmount().toString()));
				amountTextView.setText(String.format(budget.getAmount().toString()));
				Map<String, Object> map = new HashMap<>();

				MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this.getContext());
				dialogBuilder.setTitle(String.format("Budget Details: %s", budget.getBudgetName()))
								.setView(view1)
								.setCancelable(false)
								.setNegativeButton("Close", new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
												dialog.dismiss();
										}
								}).setPositiveButton("Edit", new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
												MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(BudgetsFragment.this.getContext())
																.setTitle("Update Budget")
																.setView(view)
																.setCancelable(false)
																.setNegativeButton("Close", new DialogInterface.OnClickListener() {

																		@Override
																		public void onClick(DialogInterface dialog, int which) {
																				dialog.dismiss();
																		}
																}).setPositiveButton("Update", new DialogInterface.OnClickListener() {

																		@Override
																		public void onClick(DialogInterface dialog, int which) {

																		}
																});
												alertDialogBuilder.show().getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
														@Override
														public void onClick(View v) {
																amountInputLayout.setErrorEnabled(false);

																if (amountTextView.getText().toString().isEmpty()) {
																		amountInputLayout.setErrorEnabled(true);
																		amountInputLayout.setError("Required");
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


																if (Double.valueOf(amountTextView.getText().toString()).equals(budget.getAmount())) {
																		amountInputLayout.setErrorEnabled(true);
																		amountInputLayout.setError("Amount can't be the same");
																		return;
																} else {

																		Double amount = Double.valueOf(amountTextView.getText().toString());
																		String user = FirebaseAuth.getInstance().getCurrentUser().getUid();
																		LocalDateTime dateTime = LocalDateTime.now();

																		map.put("amount", amount);

																		databaseReference.getReference("budgets").child(user)
																						.child(budget.getBudgetName())
																						.updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
																								@Override
																								public void onComplete(@NonNull Task<Void> task) {
																										if (task.isSuccessful()) {
																												Snackbar.make(BudgetsFragment.this.getView(), "Budget updated successfully", Snackbar.LENGTH_LONG).show();
																												UpdateRemainingBudget(budget, amount, dateTime, user);
																												adapter.notifyDataSetChanged();
																										}
																								}
																						});


																}
														}
												});
												dialog.dismiss();
										}
								});

				dialogBuilder.create().show();
		}

		private void DeleteBudget(int position){
				BudgetDto budget = list.get(position);
				LocalDateTime dateTime = LocalDateTime.now();
				String user = FirebaseAuth.getInstance().getCurrentUser().getUid();
				if (!budget.getBudgetName().equals("Main")) {
						MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(BudgetsFragment.this.getContext())
										.setTitle("Are you sure you want to delete this budget").setCancelable(false)
										.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface dialog, int which) {
														Query query = databaseReference.getReference("budgets")
																		.child(user)
																		.child(budget.getBudgetName());
														query.addListenerForSingleValueEvent(new ValueEventListener() {
																@Override
																public void onDataChange(@NonNull DataSnapshot snapshot) {
																		snapshot.getRef().removeValue();
																		Snackbar.make(BudgetsFragment.this.getView(), "Successfully deleted", Snackbar.LENGTH_SHORT).show();
																		Query query1 = databaseReference.getReference("Remaining Budgets")
																						.child(user)
																						.child(budget.getBudgetName());
																		query1.addListenerForSingleValueEvent(new ValueEventListener() {
																				@Override
																				public void onDataChange(@NonNull DataSnapshot snapshot) {
																						snapshot.getRef().removeValue();
																						Log.d("Successful", "Value removed");
																				}

																				@Override
																				public void onCancelled(@NonNull DatabaseError error) {
																						Log.d("Database Error", error.getDetails());
																				}
																		});
																}

																@Override
																public void onCancelled(@NonNull DatabaseError error) {
																		Snackbar.make(BudgetsFragment.this.getView(), "An error occurred while deleting.\n Please try again ", Snackbar.LENGTH_LONG).setBackgroundTint(getResources().getColor(R.color.md_theme_light_error)).show();
																}
														});
												}
										}).setNegativeButton("No", new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface dialog, int which) {
														Snackbar.make(BudgetsFragment.this.getView(), "Clicked", Snackbar.LENGTH_LONG).show();
												}
										});
						alertDialogBuilder.create().show();
				}
				else {
						Snackbar.make(BudgetsFragment.this.getView(), "Main budget can't be deleted", Snackbar.LENGTH_LONG).setBackgroundTint(getResources().getColor(R.color.md_theme_light_error)).show();
				}
		}

		public void UpdateRemainingBudget(BudgetDto budget, Double amount, LocalDateTime dateTime, String user) {
				Map<String, RemainingBudget> remainingBudgetMap = new HashMap<>();
				Map<String, Object> newRemainingBudgetMap = new HashMap<>();

				//Update remaining budget
				databaseReference.getReference("Remaining Budgets").child(user).child(String.valueOf(dateTime.getYear()))
								.child(String.valueOf(dateTime.getMonth())).get()
								.addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
										@Override
										public void onComplete(@NonNull Task<DataSnapshot> task) {

												task.getResult().getChildren().forEach(dataSnapshot -> {
														RemainingBudget remainingBudget = dataSnapshot.getValue(RemainingBudget.class);
														remainingBudgetMap.put(dataSnapshot.getKey(), remainingBudget);
												});
												Double remainder = amount - budget.getAmount();
												Double remainingAmt = remainingBudgetMap.get(budget.getBudgetName()).getRemainingAmt();
												Double newRemainingAmt = remainingAmt + remainder;
												newRemainingBudgetMap.put("remainingAmt", newRemainingAmt);
												newRemainingBudgetMap.put("updatedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

												databaseReference.getReference("Remaining Budgets").child(user).child(String.valueOf(dateTime.getYear()))
																.child(String.valueOf(dateTime.getMonth())).child(budget.getBudgetName()).updateChildren(newRemainingBudgetMap)
																.addOnCompleteListener(new OnCompleteListener<Void>() {
																		@Override
																		public void onComplete(@NonNull Task<Void> task) {
																				if (task.isSuccessful()){
																						Log.w("Successful", "Success");
																				}
																		}
																});

										}
								});
		}

		private void showPopup(View view, @MenuRes Integer menuRes, Integer position){
				PopupMenu popupMenu = new PopupMenu(this.getContext(), view);
				popupMenu.getMenuInflater().inflate(menuRes, popupMenu.getMenu());

				popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
						@Override
						public boolean onMenuItemClick(MenuItem item) {
								switch (item.toString()){
										case "Edit":
												EditBudget(position);
												break;
										case "Delete":
												DeleteBudget(position);
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
				String numberRegex = "[0-9]+(\\.[0-9]+)?";

				Pattern pattern = Pattern.compile(numberRegex);
				Matcher matcher = pattern.matcher(number);
				return matcher.matches();
		}
}