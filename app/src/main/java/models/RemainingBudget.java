package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RemainingBudget {
		public String budget;
		public Double remainingAmt;
		public String createdAt;
		public String updatedAt;

		public RemainingBudget(Budget budget, Double remainingAmt){
				this.budget = budget.getBudgetName();
				this.remainingAmt = remainingAmt;
				this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
				this.updatedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
		}

		public RemainingBudget(Budget budget){
				this.budget = budget.getBudgetName();
				this.remainingAmt = budget.getAmount();
				this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
				this.updatedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
		}

		public RemainingBudget(){

		}

		public String getBudget() {
				return budget;
		}

		public Double getRemainingAmt() {
				return remainingAmt;
		}

		public String getCreatedAt() {
				return createdAt;
		}

		public String getUpdateAt() {
				return updatedAt;
		}
}
