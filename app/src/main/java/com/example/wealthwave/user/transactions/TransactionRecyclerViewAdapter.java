package com.example.wealthwave.user.transactions;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wealthwave.R;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.text.DecimalFormat;
import java.util.ArrayList;

import models.dtos.BudgetDto;
import models.dtos.TransactionDto;

public class TransactionRecyclerViewAdapter extends RecyclerView.Adapter<TransactionRecyclerViewAdapter.MyViewHolder> {

		Context context;
		ArrayList<TransactionDto> list;

		public TransactionRecyclerViewAdapter(Context context, ArrayList<TransactionDto> list) {
				this.context = context;
				this.list = list;
		}

		@NonNull
		@Override
		public TransactionRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
				View v = LayoutInflater.from(context).inflate(R.layout.budget_cardview, parent, false);
				return new MyViewHolder(v);
		}

		@Override
		public void onBindViewHolder(@NonNull TransactionRecyclerViewAdapter.MyViewHolder holder, int position) {
				TransactionDto transaction = list.get(position);
				DecimalFormat format = new DecimalFormat("#,###,###,###.00");

				holder.BudgetName.setText(transaction.getCategory());
				holder.amount.setText(String.format("KES %s", format.format(transaction.getAmount())));
		}

		@Override
		public int getItemCount() {
				return list.size();
		}

		public static class MyViewHolder extends RecyclerView.ViewHolder {
				TextView BudgetName, amount;
				CircularProgressIndicator progressIndicator;

				public MyViewHolder(@NonNull View itemView) {
						super(itemView);

						BudgetName = itemView.findViewById(R.id.budget_name);
						amount = itemView.findViewById(R.id.budget_amount);
						progressIndicator = itemView.findViewById(R.id.progress_circular);
				}
		}
}
