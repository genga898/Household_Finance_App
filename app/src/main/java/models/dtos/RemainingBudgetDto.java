package models.dtos;

public class RemainingBudgetDto {
		public String budget;
		public Double remainingAmt;
		public String createdAt;
		public String updatedAt;

		public String getBudget() {
				return budget;
		}

		public Double getRemainingAmt() {
				return remainingAmt;
		}

		public String getCreatedAt() {
				return createdAt;
		}

		public String getUpdatedAt() {
				return updatedAt;
		}
}
