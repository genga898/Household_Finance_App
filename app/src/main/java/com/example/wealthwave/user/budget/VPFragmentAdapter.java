package com.example.wealthwave.user.budget;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.wealthwave.user.budget.GoalsFragment;
import com.example.wealthwave.user.budget.BudgetsFragment;

public class VPFragmentAdapter extends FragmentStateAdapter {

		public VPFragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
				super(fragmentActivity);
		}

		@NonNull
		@Override
		public Fragment createFragment(int position) {
				if (position == 0) {
						return new BudgetsFragment();
				}
				return new GoalsFragment();
		}

		@Override
		public int getItemCount() {
				return 2;
		}
}
