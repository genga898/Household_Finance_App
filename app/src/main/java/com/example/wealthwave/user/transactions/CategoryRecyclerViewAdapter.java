package com.example.wealthwave.user.transactions;

import android.content.Context;
import android.icu.lang.UCharacter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wealthwave.R;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import models.Transaction;
import models.dtos.TransactionDto;

public class CategoryRecyclerViewAdapter extends RecyclerView.Adapter<CategoryRecyclerViewAdapter.MyViewHolder> {

		Context context;
		HashMap<LocalDate, ArrayList<TransactionDto>> categorizedTransactions;
		ArrayList<LocalDate> dateList;

		public CategoryRecyclerViewAdapter(Context context, HashMap<LocalDate, ArrayList<TransactionDto>> list, ArrayList<LocalDate> date) {
				this.context = context;
				this.categorizedTransactions = list;
				this.dateList = date;
		}

		@NonNull
		@Override
		public CategoryRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
				LayoutInflater layoutInflater = LayoutInflater.from(context);
				View view = layoutInflater.inflate(R.layout.categorized_transaction_view, parent, false);
				return new MyViewHolder(view);
		}

		@Override
		public void onBindViewHolder(@NonNull CategoryRecyclerViewAdapter.MyViewHolder holder, int position) {

				LocalDate date = dateList.get(position);
				System.out.println(dateList);

				TransactionRecyclerViewAdapter adapter = new TransactionRecyclerViewAdapter(holder.itemView.getContext(), sortTransactionsByDate(categorizedTransactions.get(date)));

				holder.transactionDate.setText(String.format("%s, %s %s", UCharacter.toTitleCase(Locale.US, date.getDayOfWeek().toString(), null), UCharacter.toTitleCase(Locale.US, date.getMonth().toString(), null), date.getDayOfMonth()));
				holder.transactionRecyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
				holder.transactionRecyclerView.setAdapter(adapter);
				holder.transactionRecyclerView.setHasFixedSize(false);
				adapter.notifyDataSetChanged();
		}


		@Override
		public int getItemCount() {
				return dateList.size();
		}

		public static class MyViewHolder extends RecyclerView.ViewHolder {
				TextView transactionDate;
				RecyclerView transactionRecyclerView;
				CircularProgressIndicator progressIndicator;

				public MyViewHolder(@NonNull View itemView) {
						super(itemView);

						transactionDate = itemView.findViewById(R.id.transaction_date);
						progressIndicator = itemView.findViewById(R.id.progress_circular);
						transactionRecyclerView = itemView.findViewById(R.id.transaction_category_recycler_view);
				}
		}

		public static ArrayList<TransactionDto> sortTransactionsByDate(ArrayList<TransactionDto> transactions) {
				transactions.sort(new Comparator<TransactionDto>() {
						@Override
						public int compare(TransactionDto t1, TransactionDto t2) {
								return t2.getCreatedAt().compareTo(t1.getCreatedAt());
						}
				});
				return transactions;
		}


}
