package models;

import java.util.UUID;

public class Expense {
		public String expenseID;
		public String transactionID;

		public Expense(Transaction transaction){
				this.transactionID = transaction.getTransactionID();
				this.expenseID = UUID.randomUUID().toString();
		}

		public Expense(){

		}
		public String getExpenseID() {
				return expenseID;
		}

		public String getTransactionID() {
				return transactionID;
		}

}
