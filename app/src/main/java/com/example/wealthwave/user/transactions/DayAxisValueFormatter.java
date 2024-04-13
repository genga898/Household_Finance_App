package com.example.wealthwave.user.transactions;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

public class DayAxisValueFormatter extends IndexAxisValueFormatter {
		private final String[] mMonths = new String[]{
						"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
		};

		private final BarLineChartBase<?> chartBase;

		public DayAxisValueFormatter(BarLineChartBase<?> chart){
				this.chartBase = chart;
		}

		@Override
		public String getFormattedValue(float value, AxisBase axis) {

				int days = (int) value;
				return "x";
		}
}
