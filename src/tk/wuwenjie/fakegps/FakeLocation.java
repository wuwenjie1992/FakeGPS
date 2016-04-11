package tk.wuwenjie.fakegps;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

public class FakeLocation extends Activity {

	TextView fake_info;
	AutoCompleteTextView dbfile;

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.fake_location);

		fake_info = (TextView) findViewById(R.id.fake_info);
		fake_info.setText("开始模拟,在下面的地址填写GPS记录文件！");

		// Get a reference to the AutoCompleteTextView in the layout
		dbfile = (AutoCompleteTextView) findViewById(R.id.dbfile);
		// Get the string array
		String[] dbs = getResources().getStringArray(R.array.db_array);
		// Create the adapter and set it to the AutoCompleteTextView
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, dbs);
		dbfile.setAdapter(adapter);

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
	}

	protected void onResume() {
		super.onResume();
	}

	protected void onStop() {
		super.onStop();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.fake, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		switch (id) {
		case R.id.START_simulate:

			String path = dbfile.getText().toString();

			if (!new File(path).exists()) {

				Toast.makeText(this, "NO MATCH File!", Toast.LENGTH_SHORT)
						.show();

				return false;
			}

			Toast.makeText(this, path, Toast.LENGTH_SHORT).show();

			Intent mIntent01 = new Intent(FakeLocation.this,
					FakeLocationService.class);
			Bundle mBundle = new Bundle();
			mBundle.putString("Action", "Start");
			mBundle.putString("DB", path);
			mIntent01.putExtras(mBundle); // 添加附加信息
			startService(mIntent01);

			break;

		case R.id.STOP_simulate:

			Intent mIntent02 = new Intent(FakeLocation.this,
					FakeLocationService.class);
			stopService(mIntent02);

			break;

		}

		return super.onOptionsItemSelected(item);
	}

}
