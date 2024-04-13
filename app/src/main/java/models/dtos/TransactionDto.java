package models.dtos;

public class TransactionDto {
		public String transactionID;
		public String budgetID;
		public String category;
		public String transactionName;
		public Double amount;
		public String createdAt;

		public String getTransactionID() {
				return transactionID;
		}

		public String getBudgetID() {
				return budgetID;
		}

		public String getCategory() {
				return category;
		}

		public String getTransactionName() {
				return transactionName;
		}

		public Double getAmount() {
				return amount;
		}

		public String getCreatedAt() {
				return createdAt;
		}
}
