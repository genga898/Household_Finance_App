package com.example.wealthwave;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.posthog.java.PostHog;

import java.time.LocalDateTime;
import java.util.HashMap;

import models.Budget;
import models.Transaction;
import models.User;

public class PostHogAnalytics {
		private static final String POSTHOG_API_KEY = "phc_poyx00gH0WmDEfDVv3XI6U1IA2ZkVtRAIFB9SmbhxvE";
		private static final String POSTHOG_HOST = "https://app.posthog.com";

		private final FirebaseAuth auth = FirebaseAuth.getInstance();
		PostHog postHog;

		public PostHogAnalytics() {
				this.postHog = new PostHog.Builder(POSTHOG_API_KEY).host(POSTHOG_HOST).build();
		}

		public void LogRegisteredUser(User user) {
				postHog.capture(user.getEmailAddress(), "user_signed up", new HashMap<String, Object>() {
						{
								put("registration_method", "email");
						}
				});
				postHog.shutdown();
		}

		public void LoggedInUserLogs(){
				String user = auth.getCurrentUser().getUid();
				postHog.capture(user, "User logged in", new HashMap<String, Object>(){
						{
								put("is_email_verified", auth.getCurrentUser().isEmailVerified());
								put("login_time", LocalDateTime.now());
						}
				});
				postHog.shutdown();
		}


		public void LogCreatedBudgets(Budget budget){
				String user = auth.getCurrentUser().getUid();
				postHog.capture(user, "Budget created", new HashMap<String, Object>(){
						{
								put(budget.getBudgetId(), budget.getCreationDate());
						}
				});
				postHog.shutdown();
		}

		public void LogTransactions(@NonNull Transaction transaction){
				String user = auth.getCurrentUser().getUid();
				postHog.capture(user, "Transaction created", new HashMap<String, Object>(){
						{
								put(transaction.getTransactionID(), transaction.createdAt);
						}
				});
				postHog.shutdown();
		}
		public void TestPostHog(){
				postHog.capture("Hello", "Welcome");
		}

}
