package models.dtos;

public class BudgetDto {
		public String budgetId;
		public String budgetName;
		public Double amount;
		public String createdAt;

		public String getBudgetId() {
				return budgetId;
		}

		public String getBudgetName() {
				return budgetName;
		}

		public Double getAmount() {
				return amount;
		}

		public String getCreatedAt() {
				return createdAt;
		}
}
