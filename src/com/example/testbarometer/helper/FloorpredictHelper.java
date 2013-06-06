package com.example.testbarometer.helper;

import android.app.ApplicationErrorReport.CrashInfo;
import android.util.Log;

import com.example.testbarometer.model.FloorPressure;
import com.example.testbarometer.model.FloorPressures;

public class FloorPredictHelper {

	private static final String TAG = FloorPredictHelper.class.getSimpleName();

	private FloorPressures floorPressures;

	public void setFloorPressures(FloorPressures floorPressures) {
		this.floorPressures = floorPressures;
	}

	public double getFloorPressurePrediction(int floor) {
		if (this.floorPressures == null || this.floorPressures.isEmpty())
			return 0.0;
		FloorPressure floorLowest = this.floorPressures.get(0);
		double pressureAverage = this.floorPressures.getPressureAverage();
		int floorSpan = getFloorSpan(floorLowest.getFloor(), floor);
		int floorDirection = getFloorDirection(floorLowest.getFloor(), floor);
		return floorLowest.getPressure() - (pressureAverage * floorSpan * floorDirection);
	}

	/**
	 * @param pressure
	 * @return Predicted floor for given pressure
	 */
	public FloorPredictContainer getFloorPrediction(double pressure) {
		FloorPredictContainer floorPredictContainer = new FloorPredictContainer();
		floorPredictContainer.pressure = pressure;
		if (this.floorPressures == null || this.floorPressures.isEmpty())
			return floorPredictContainer;

		floorPredictContainer.floorLowest = this.floorPressures.get(0).getFloor();
		floorPredictContainer.pressureLowestFloor = this.floorPressures.get(0).getPressure();
		floorPredictContainer.pressureAverage = this.floorPressures.getPressureAverage();

		if (floorPredictContainer.pressureAverage == 0.0)
			return floorPredictContainer;

		double floorPredictRaw = floorPredictContainer.getPressureDiff() / floorPredictContainer.pressureAverage;
		floorPredictContainer.floorDiff = (int) Math.floor(floorPredictRaw);
		double floorPredictRest = floorPredictRaw - floorPredictContainer.floorDiff;

		if (floorPredictRest > 0.8)
			floorPredictContainer.floorDiff++;

		int floorPredicted = floorPredictContainer.getFloorPredicted();
		floorPredictContainer.pressureFloor = getFloorPressurePrediction(floorPredicted);
		floorPredictContainer.pressureFloorNext = getFloorPressurePrediction(floorPredicted + 1);
		floorPredictContainer.pressureFloorPrev = getFloorPressurePrediction(floorPredicted - 1);
		floorPredictContainer.isPredicted = true;
		return floorPredictContainer;
	}

	public double getPressurePredictionCurrent(double pressure) {
		if (this.floorPressures == null || this.floorPressures.isEmpty())
			return 0.0;
		double pressureLowestFloor = this.floorPressures.get(0).getPressure();
		double pressureDiff = pressure - pressureLowestFloor;
		double sealevelAverage = this.floorPressures.getPressureAverage();
		return Math.abs(pressureDiff);
	}

	/**
	 * @param pressure
	 * @return Pressure difference between given and lowest floor
	 */
	public double getPressureLowestFloorDifference(double pressure) {
		if (this.floorPressures == null || this.floorPressures.isEmpty())
			return 0.0;
		double pressureLowestFloor = this.floorPressures.get(0).getPressure();
		return pressureLowestFloor - pressure;
	}

	/**
	 * @param floorFirst
	 * @param floorSecond
	 * @return Span between 1st and 2nd floor is 1, 4th and 1st is 3, etc..
	 */
	public static int getFloorSpan(int floorFirst, int floorSecond) {
		return Math.abs(floorFirst - floorSecond);
	}

	/**
	 * @param floorFirst
	 * @param floorSecond
	 * @return 0 same floor, 1 second floor is above first floor, -1 second
	 *         floor is below first flow
	 */
	public static int getFloorDirection(int floorFirst, int floorSecond) {
		return floorFirst == floorSecond ? 0 : (floorFirst > floorSecond ? -1 : 1);
	}

	/**
	 * @param floorPressureFirst
	 * @param floorPressureSecond
	 * @return Average pressure between the two floors
	 */
	public static double getAveragePresure(FloorPressure floorPressureFirst, FloorPressure floorPressureSecond) {
		if (floorPressureFirst == null || floorPressureSecond == null)
			return 0.0;
		int floorSpan = getFloorSpan(floorPressureFirst.getFloor(), floorPressureSecond.getFloor());
		if (floorSpan == 0)
			return 0.0;

		double pressureDiff = Math.abs(floorPressureFirst.getPressure() - floorPressureSecond.getPressure());
		return pressureDiff / floorSpan;
	}

	// CLASS

	public class FloorPredictContainer {
		/**
		 * Lowest floor
		 */
		public int floorLowest;
		/**
		 * Predicted floor to lowest floor difference
		 */
		public int floorDiff;
		/**
		 * Given pressure
		 */
		public double pressure;
		/**
		 * Predicted pressure for predicted floor
		 */
		public double pressureFloor;
		/**
		 * Predicted pressure for next predicted floor
		 */
		public double pressureFloorNext;
		/**
		 * Predicted pressure for previous predicted floor
		 */
		public double pressureFloorPrev;
		/**
		 * Pressure for lowest floor
		 */
		public double pressureLowestFloor;
		/**
		 * Average pressure
		 */
		public double pressureAverage;
		/**
		 * If floor is predicted
		 */
		public boolean isPredicted = false;

		/**
		 * @return Difference between predicted floor pressure and given
		 *         pressure
		 */
		public double getPressureDiff() {
			return this.pressureLowestFloor - this.pressure;
		}

		/**
		 * @return Predicted floor
		 */
		public int getFloorPredicted() {
			return this.floorLowest + this.floorDiff;
		}

		/**
		 * @return Difference between pressure and predicted floor pressure
		 */
		public double getPressureFloorDiff() {
			return this.pressureFloor - this.pressure;
		}

		/**
		 * @return Difference between pressure and next predicted floor pressure
		 */
		public double getPressureFloorNextDiff() {
			return this.pressureFloorNext - this.pressure;
		}

		/**
		 * @return Difference between pressure and previous predicted floor
		 *         pressure
		 */
		public double getPressureFloorPrevDiff() {
			return this.pressureFloorPrev - this.pressure;
		}
	}

	// /CLASS
}
