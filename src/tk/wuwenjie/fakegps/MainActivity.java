package tk.wuwenjie.fakegps;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	TextView location_info;

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		location_info = (TextView) findViewById(R.id.location_info);

		// 注册广播，接收service中启动的线程发送过来的信息，同时更新UI
		IntentFilter filter = new IntentFilter("tk.wuwenjie.UP_Main");
		this.registerReceiver(new MyReceiver(), filter);

	}

	// 生命周期
	protected void onDestroy() {
		super.onDestroy();

	}

	protected void onPause() {
		super.onPause();
	}

	protected void onRestart() {
		super.onRestart();
		// initCriteria(criteria);
		// locationManager.requestLocationUpdates(provider, 2000, 10,
		// locationListener);
	}

	protected void onResume() {
		super.onResume();
		// initCriteria(criteria);
		// locationManager.requestLocationUpdates(provider, 2000, 10,
		// locationListener);
	}

	protected void onStop() {
		super.onStop();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		switch (id) {
		case R.id.START_GPS:

			// Toast.makeText(this, "action_settings",
			// Toast.LENGTH_SHORT).show();

			Intent mIntent01 = new Intent(MainActivity.this,
					GPSWriteService.class);
			Bundle mBundle = new Bundle();
			mBundle.putString("Action", "Start");
			mIntent01.putExtras(mBundle); // 添加附加信息
			startService(mIntent01);

			break;

		case R.id.STOP_GPS:

			Intent mIntent02 = new Intent(MainActivity.this,
					GPSWriteService.class);
			Bundle mBundle02 = new Bundle();
			mBundle02.putString("Action", "Stop");
			mIntent02.putExtras(mBundle02); // 添加附加信息
			startService(mIntent02);

			break;

		case R.id.simulate:

			Intent mIntent03 = new Intent(MainActivity.this, FakeLocation.class);
			startActivity(mIntent03);

			break;

		case R.id.about:

			Toast.makeText(MainActivity.this, "Author：wuwenjie For：joyrun",
					Toast.LENGTH_LONG).show();

		}

		return super.onOptionsItemSelected(item);
	}

	// 自定义一个广播接收器
	public class MyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			System.out.println("OnReceiver");
			Bundle bundle = intent.getExtras();
			String a = bundle.getString("GPS_info");
			// pb.setProgress(a);
			location_info.setText(a);
			// 处理接收到的内容

		}

		public MyReceiver() {
			System.out.println("MyReceiver");
			// 构造函数，做一些初始化工作，本例中无任何作用
		}

	}

}
