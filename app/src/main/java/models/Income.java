package models;

import java.util.Currency;
import java.util.UUID;

public class Income {
		public String incomeID;
		public String transactionID;

		public Income(Transaction transaction){
				this.incomeID = UUID.randomUUID().toString();
				this.transactionID = transaction.getTransactionID();
		}

		public Income(){

		}
		public String getIncomeID() {
				return incomeID;
		}
		public String getTransactionID() {
				return transactionID;
		}

}
