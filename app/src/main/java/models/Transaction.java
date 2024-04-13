package models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.time.LocalDateTime;

import java.time.format.DateTimeFormatter;
import java.util.Currency;
import java.util.UUID;

public class Transaction {
		public String transactionID;
		public String budgetID;
		public String category;
		public String transactionName;
		public Double amount;
		public String createdAt;

		public Transaction(String budgetID, String category, String transactionName, Double amount){
				this.transactionID = UUID.randomUUID().toString();
				this.budgetID = budgetID;
				this.category = category;
				this.transactionName = transactionName;
				this.amount = amount;
				this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
		}

		public Transaction(){

		}
		public String getTransactionID() {
				return transactionID;
		}
		public String getBudgetID() {
				return budgetID;
		}
		public String getCategory() {
				return category;
		}
		public Double getAmount() {
				return amount;
		}
		public String getTransactionName() {
				return transactionName;
		}
		public String getLocalDateTime() {
				return createdAt;
		}

}
