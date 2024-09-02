package com.example.wealthwave.user.notifications;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class Notifications extends Worker {

		public Notifications(@NonNull Context context, @NonNull WorkerParameters workerParams) {
				super(context, workerParams);
		}

		@NonNull
		@Override
		public Result doWork() {
				return null;
		}
}
