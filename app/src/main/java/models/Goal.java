package models;

import java.util.Currency;
import java.util.Date;
import java.util.UUID;

public class Goal {
		public UUID goalID;
		public User userID;
		public String goalName;
		public Currency amount;
		public Date targetDate;

		public User getUserID() {
				return userID;
		}
}
