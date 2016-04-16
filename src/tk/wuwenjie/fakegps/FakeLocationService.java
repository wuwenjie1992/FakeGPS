package tk.wuwenjie.fakegps;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class FakeLocationService extends Service {

	private static final String TAG = "FakeLocationService";
	SQLiteDatabase db;
	Cursor cursor;
	long First_Time;
	Handler mHandler;
	String serviceName = Context.LOCATION_SERVICE;

	List<Double> Latitude = new ArrayList<>();
	List<Double> Longitude = new ArrayList<>();
	List<Double> Altitude = new ArrayList<>();
	List<Float> Bearing = new ArrayList<>();
	List<Float> Speed = new ArrayList<>();
	List<Long> Time = new ArrayList<>();

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean onUnbind(Intent intent) {
		Log.i(TAG, "onUnbind");
		return super.onUnbind(intent);
	}

	// 第一次启动Service时，先后调用onCreate()--->onStart()
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "onCreate");

	}

	public void onStart(Intent intent, int startId) {

		Bundle bundle = intent.getExtras();// 得到传过来的bundle
		String Action = bundle.getString("Action"); // 用于接收字符串
		Log.i(TAG, "Action:" + Action);

		if (Action.equals("Start")) {

			// 判断是否允许模拟位置
			boolean isCloseMOCK = Settings.Secure.getInt(getContentResolver(),
					Settings.Secure.ALLOW_MOCK_LOCATION, 0) == 0;

			if (isCloseMOCK) {

				Toast.makeText(this, "请开启模拟位置...", Toast.LENGTH_SHORT).show();

				Intent intent2 = new Intent(
						Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
				intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent2);

				return;

			}

			String DbPath = bundle.getString("DB"); // 用于接收字符串

			// 打开或创建test.db数据库
			db = SQLiteDatabase.openDatabase(DbPath, null,
					SQLiteDatabase.OPEN_READONLY);

			cursor = db.rawQuery("select * from gps_info order by id ", null);

			while (cursor.moveToNext()) {

				Latitude.add(cursor.getDouble(cursor.getColumnIndex("Latitude")));
				Longitude.add(cursor.getDouble(cursor
						.getColumnIndex("Longitude")));
				Altitude.add(cursor.getDouble(cursor.getColumnIndex("Altitude")));
				Bearing.add(cursor.getFloat(cursor.getColumnIndex("Bearing")));
				Speed.add(cursor.getFloat(cursor.getColumnIndex("Speed")));
				Time.add(cursor.getLong(cursor.getColumnIndex("Time")));

			}

			Log.i(TAG, cursor.getPosition() + "" + cursor.getCount());

			cursor.close();
			db.close();

			setLocationForward();

		}
	}

	public void onDestroy() {

		super.onDestroy();

		Log.i(TAG, "onDestroy");

		if (cursor != null)
			cursor.close();

		if (db != null)
			db.close();
		//

	}

	// 按顺序模拟位置
	public void setLocationForward() {

		final LocationManager mLocationManager = (LocationManager) getSystemService(serviceName);

		// stackoverflow.com/android-mock-location-on-device

		if (mLocationManager.getProvider(LocationManager.GPS_PROVIDER) != null) {

			// Log.i(TAG, LocationManager.GPS_PROVIDER);
			// mLocationManager.removeTestProvider(LocationManager.GPS_PROVIDER);

			mLocationManager.addTestProvider(LocationManager.GPS_PROVIDER,
					"requiresNetwork" == "", "requiresSatellite" == "",
					"requiresCell" == "", "hasMonetaryCost" == "",
					"supportsAltitude" == "", "supportsSpeed" == "",
					"supportsBearing" == "",
					android.location.Criteria.POWER_LOW,
					android.location.Criteria.ACCURACY_FINE);

			mLocationManager.setTestProviderEnabled(
					LocationManager.GPS_PROVIDER, true);

			mHandler = new Handler();

			int i;

			for (i = 0; i < Latitude.size(); i++) {

				final int j = i;

				mHandler.postDelayed(new Runnable() {
					public void run() {

						/*
						 * Log.i(TAG, "gps_info:" + j + ":" + Latitude.get(j) +
						 * ":" + Longitude.get(j) + ":" + Altitude.get(j) + ":"
						 * + Bearing.get(j) + ":" + Speed.get(j) + ":" +
						 * Time.get(j));
						 */

						Location loc = new Location(
								LocationManager.GPS_PROVIDER);
						loc.setLatitude(Latitude.get(j));
						loc.setLongitude(Longitude.get(j));
						loc.setAltitude(Altitude.get(j));
						loc.setBearing(Bearing.get(j));
						loc.setProvider(LocationManager.GPS_PROVIDER);
						loc.setSpeed(Speed.get(j));
						loc.setTime(System.currentTimeMillis());

						// stackoverflow.com/
						// settestproviderlocation-error-location-not-being-set-despite-all-parameters

						if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
							loc.setElapsedRealtimeNanos(SystemClock
									.elapsedRealtimeNanos());
							loc.setAccuracy(100);
						}

						try {

							mLocationManager.setTestProviderLocation(
									LocationManager.GPS_PROVIDER, loc);
						} catch (java.lang.SecurityException e) {
							e.printStackTrace();
							return;
						}

						if (j == Latitude.size() - 1) {
							Log.i(TAG, "Forward End! Valar Morghulis");
							Log.i(TAG, "Backward Begin! Valar Dohaeris");
							setLocationBackward();
						}

					} // run

				}, (long) Math.floor(Math.abs(Time.get(j) - Time.get(2)) * 0.2));

			} // for

		}
	} // setLocationForward

	// 倒序模拟位置
	public void setLocationBackward() {

		Log.i(TAG, "Backward In! Valar Morghulis");

		final LocationManager mLocationManager = (LocationManager) getSystemService(serviceName);

		// stackoverflow.com/android-mock-location-on-device

		// if (mLocationManager.getProvider(LocationManager.GPS_PROVIDER) !=
		// null) {

		// Log.i(TAG, LocationManager.GPS_PROVIDER);
		// mLocationManager.removeTestProvider(LocationManager.GPS_PROVIDER);

		mLocationManager.addTestProvider(LocationManager.GPS_PROVIDER,
				"requiresNetwork" == "", "requiresSatellite" == "",
				"requiresCell" == "", "hasMonetaryCost" == "",
				"supportsAltitude" == "", "supportsSpeed" == "",
				"supportsBearing" == "", android.location.Criteria.POWER_LOW,
				android.location.Criteria.ACCURACY_FINE);

		mLocationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER,
				true);

		Handler mHandler = new Handler();

		int i;

		for (i = Latitude.size() - 1; i >= 0; i--) {

			final int j = i;

			mHandler.postDelayed(new Runnable() {
				public void run() {

					Location loc = new Location(LocationManager.GPS_PROVIDER);
					loc.setLatitude(Latitude.get(j));
					loc.setLongitude(Longitude.get(j));
					loc.setAltitude(Altitude.get(j));
					loc.setBearing(Bearing.get(j));
					loc.setProvider(LocationManager.GPS_PROVIDER);
					loc.setSpeed(Speed.get(j));
					loc.setTime(System.currentTimeMillis());

					if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
						loc.setElapsedRealtimeNanos(SystemClock
								.elapsedRealtimeNanos());
						loc.setAccuracy(100);
					}

					try {

						mLocationManager.setTestProviderLocation(
								LocationManager.GPS_PROVIDER, loc);
					} catch (java.lang.SecurityException e) {
						e.printStackTrace();
						return;
					}

					if (j == 0) {
						Log.i(TAG, "Backward End ! Valar Morghulis");
						Log.i(TAG, "Forward Begin ! Valar Dohaeris");
						setLocationForward();
					}

				} // run

			}, (long) Math.floor(Math.abs(Time.get(j)
					- Time.get(Latitude.size() - 1)) * 0.2));

		} // for

	} // setLocationForward

}
