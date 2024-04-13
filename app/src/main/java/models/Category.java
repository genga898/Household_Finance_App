package models;

import java.util.UUID;

public class Category {
		public String categoryID;
		public String categoryName;

		public Category(String categoryName){
				this.categoryID = UUID.randomUUID().toString();
				this.categoryName = categoryName;
		}
		public String getCategoryID() {
				return categoryID;
		}
		public String getCategoryName() {
				return categoryName;
		}
}
