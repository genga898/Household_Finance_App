package com.example.wealthwave.networkChecks;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;

import androidx.annotation.NonNull;

public class NetworkConnectivity {

		enum NetworkStates{
				Available,
				Lost,
				Losing,
				Unavailable
		}

		Context context;

		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		public void Observe(){

				ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {

				};
		}
}

