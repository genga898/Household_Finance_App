package models;

import com.google.type.DateTime;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Currency;
import java.util.Date;
import java.util.UUID;

public class Budget {
		public String budgetId;
		public String budgetName;
		public Double amount;
		public String createdAt;

		public Budget(String budgetName, Double amount){
				this.budgetId = UUID.randomUUID().toString();
				this.budgetName = budgetName;
				this.amount = amount;
				this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
		}

		public Budget(){

		}

		public String getBudgetId() {
				return budgetId;
		}

		public String getBudgetName() {
				return budgetName;
		}

		public Double getAmount() {
				return amount;
		}

		public void setAmount(Double amount) {
				this.amount = amount;
		}

		public String getCreationDate() {
				return createdAt;
		}
}