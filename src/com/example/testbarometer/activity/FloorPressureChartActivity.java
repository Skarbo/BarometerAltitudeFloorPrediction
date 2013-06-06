package com.example.testbarometer.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.webkit.WebView;

import com.example.testbarometer.R;

public class FloorPressureChartActivity extends Activity {

	public static final String ARG_FLOORPRESSURES = "floorpressures";

	protected static final String TAG = FloorPressureChartActivity.class.getSimpleName();

	private WebView chartWebView;

	public class JavaScriptInterface {
		Context mContext;
		private String floorPressures = "";
		private int height;
		private int width;

		JavaScriptInterface(Context c) {
			mContext = c;
		}

		public void setFloorPressures(String floorPressures) {
			this.floorPressures = floorPressures;
		}

		public String getFloorPressures() {
			return this.floorPressures;
		}

		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return height;
		}

		public void setWidth(int width) {
			this.width = width;
		}

		public void setHeight(int height) {
			this.height = height;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_floor_pressure_chart);

		Intent intent = getIntent();
		String floorPressures = intent.getExtras().getString(ARG_FLOORPRESSURES);

		JavaScriptInterface javaScriptInterface = new JavaScriptInterface(this);
		javaScriptInterface.setFloorPressures(floorPressures);
		
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		javaScriptInterface.setHeight(size.y);
		javaScriptInterface.setWidth(size.x);

		chartWebView = (WebView) findViewById(R.id.chartWebView);

		chartWebView.getSettings().setJavaScriptEnabled(true);
		chartWebView.addJavascriptInterface(javaScriptInterface, "Android");

		chartWebView.loadUrl("file:///android_asset/www/floor_pressure_chart.html");

	}

}
