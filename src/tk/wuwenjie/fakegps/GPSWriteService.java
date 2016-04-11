package tk.wuwenjie.fakegps;

import java.io.File;
import java.util.Iterator;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class GPSWriteService extends Service {

	private static final String TAG = "GPSWriteService";

	LocationManager locationManager;
	GpsStatus gs = null;
	String lastLocation;
	String Status = "\n未搜索到卫星";

	Intent intent = new Intent("tk.wuwenjie.UP_Main");

	SQLiteDatabase db;

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

		String serviceName = Context.LOCATION_SERVICE;
		locationManager = (LocationManager) getSystemService(serviceName);
		// String provider = LocationManager.GPS_PROVIDER;

		// 判断GPS是否正常启动
		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			Toast.makeText(this, "请开启GPS导航...", Toast.LENGTH_SHORT).show();

			// 返回开启GPS导航设置界面
			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);

			return;
		}

		String path = Environment.getExternalStorageDirectory() + "/FakeGPS";
		String dbname = path + "/GPS.db";
		File dir = new File(path);
		if (!dir.exists())
			dir.mkdirs();

		// 打开或创建test.db数据库
		db = SQLiteDatabase.openOrCreateDatabase(dbname, null);
		db.execSQL("create table if not exists gps_info(id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "Latitude double,Longitude double,Altitude double,"
				+ "Bearing float,Speed float,Time long,Provider varchar(10));");

	}

	public void onStart(Intent intent, int startId) {

		Bundle bundle = intent.getExtras();// 得到传过来的bundle
		String Action = bundle.getString("Action"); // 用于接收字符串
		Log.i(TAG, "Action:" + Action);

		if (Action.equals("Start")) {

			Criteria criteria = new Criteria();
			criteria.setAccuracy(Criteria.ACCURACY_FINE);
			criteria.setAltitudeRequired(false);
			criteria.setBearingRequired(false);
			criteria.setSpeedRequired(false);
			criteria.setCostAllowed(true);
			criteria.setPowerRequirement(Criteria.POWER_HIGH);

			gs = locationManager.getGpsStatus(gs);

			String provider = locationManager.getBestProvider(criteria, true);
			Location location = locationManager.getLastKnownLocation(provider);
			updateWithNewLocation(location);

			locationManager.addGpsStatusListener(listener);

			locationManager.requestLocationUpdates(provider, 1000, 1,
					locationListener);
		}

		else {

			this.onDestroy();

		}

	}

	public void onDestroy() {
		super.onDestroy();
		locationManager.removeUpdates(locationListener);
		db.close();

	}

	private final LocationListener locationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			updateWithNewLocation(location);
		}

		public void onProviderDisabled(String provider) {
			updateWithNewLocation(null);
		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			switch (status) {
			// GPS状态为可见时
			case LocationProvider.AVAILABLE:
				Log.i(TAG, "simpleGPS 当前GPS状态为可见状态");
				break;
			// GPS状态为服务区外时
			case LocationProvider.OUT_OF_SERVICE:
				Log.i(TAG, "当前GPS状态为服务区外状态");
				break;
			// GPS状态为暂停服务时
			case LocationProvider.TEMPORARILY_UNAVAILABLE:
				Log.i(TAG, "当前GPS状态为暂停服务状态");
				break;
			}

		}

	};

	// 状态监听
	GpsStatus.Listener listener = new GpsStatus.Listener() {
		public void onGpsStatusChanged(int event) {

			switch (event) {
			case GpsStatus.GPS_EVENT_FIRST_FIX:
				Log.i(TAG, "第一次定位");

				intent.putExtra("GPS_info", "第一次定位");
				sendBroadcast(intent);

				break;

			case GpsStatus.GPS_EVENT_SATELLITE_STATUS:

				// 获取当前状态
				gs = locationManager.getGpsStatus(null);
				int maxSatellites = gs.getMaxSatellites();// 获取卫星颗数的默认最大值
				Iterator<GpsSatellite> iters = gs.getSatellites().iterator();
				// 创建一个迭代器保存所有卫星
				int count = 0;
				String info = "";

				while (iters.hasNext() && count <= maxSatellites) {
					GpsSatellite s = iters.next();
					count++;
					info += count + "\t:" + s.getPrn() + "\t信噪比:" + s.getSnr()
							+ "\t方位角:" + s.getAzimuth() + "\t海拔:"
							+ s.getElevation() + "\n";
				}

				if (count != 0)
					Status = "\n搜索到：" + count + "颗卫星\n" + info;
				else
					Status = "\n未搜索到卫星\n";

				// Log.i(TAG, "您当前的位置是:\n" + lastLocation + Status);

				intent.putExtra("GPS_info", "您当前的位置是:\n" + lastLocation
						+ Status);
				sendBroadcast(intent);

				break;

			case GpsStatus.GPS_EVENT_STARTED:// 定位启动

				Log.i(TAG, "定位启动");

				intent.putExtra("GPS_info", "定位启动");
				sendBroadcast(intent);

				break;
			case GpsStatus.GPS_EVENT_STOPPED:// 定位结束
				Log.i(TAG, "定位结束");

				intent.putExtra("GPS_info", "定位结束");
				sendBroadcast(intent);
				break;
			}
		}
	};

	private void updateWithNewLocation(Location location) {

		if (location != null) {
			double lat = location.getLatitude();
			double lng = location.getLongitude();
			double alt = location.getAltitude();
			float bea = location.getBearing();
			String pro = location.getProvider();
			float spe = location.getSpeed();
			long tim = location.getTime();

			lastLocation = "纬度:" + lat + "\n经度:" + lng + "\n海拔:" + alt
					+ "  方位:" + bea + "  速度:" + spe + "  供应商:" + pro + "\n时间:"
					+ tim;

			//
			db.execSQL(
					"insert into gps_info(Latitude,Longitude,Altitude,Bearing,Speed,Time,Provider)values(?,?,?,?,?,?,?);",
					new Object[] { lat, lng, alt, bea, spe, tim, pro });

		} else {
			lastLocation = "无法获取地理信息";
		}
		// Log.i(TAG, "您当前的位置是:\n" + lastLocation + Status);

		intent.putExtra("GPS_info", "您当前的位置是:\n" + lastLocation + Status);
		sendBroadcast(intent);
	}

}
