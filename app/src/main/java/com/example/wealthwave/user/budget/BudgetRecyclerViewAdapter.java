package com.example.wealthwave.user.budget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wealthwave.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.RemainingBudget;
import models.dtos.BudgetDto;

public class BudgetRecyclerViewAdapter extends RecyclerView.Adapter<BudgetRecyclerViewAdapter.MyViewHolder> {

		private final RecyclerViewInterface recyclerViewInterface;
		Context context;
		ArrayList<BudgetDto> list;


		public BudgetRecyclerViewAdapter(RecyclerViewInterface recyclerViewInterface, Context context, ArrayList<BudgetDto> list) {
				this.recyclerViewInterface = recyclerViewInterface;
				this.context = context;
				this.list = list;
		}

		@NonNull
		@Override
		public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
				View v = LayoutInflater.from(context).inflate(R.layout.budget_cardview, parent, false);
				return new MyViewHolder(v, recyclerViewInterface);
		}

		@Override
		public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
				BudgetDto budget = list.get(position);
				DecimalFormat format = new DecimalFormat("#,###,###,###.00");

				holder.BudgetName.setText(budget.getBudgetName());
				holder.amount.setText(String.format("KES %s", format.format(budget.getAmount())));

		}

		@Override
		public int getItemCount() {
				return list.size();
		}


		public static class MyViewHolder extends RecyclerView.ViewHolder {
				TextView BudgetName, amount;
				ImageButton options;

				public MyViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
						super(itemView);

						BudgetName = itemView.findViewById(R.id.budget_name);
						amount = itemView.findViewById(R.id.budget_amount);
						options = itemView.findViewById(R.id.menu_items);
						options.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
										if (recyclerViewInterface != null){
												int pos = getBindingAdapterPosition();

												if (pos != RecyclerView.NO_POSITION){
														recyclerViewInterface.onItemClick(pos);
												}
										}
								}
						});
				}
		}

}
