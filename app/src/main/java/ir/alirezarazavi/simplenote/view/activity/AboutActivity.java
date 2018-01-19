package ir.alirezarazavi.simplenote.view.activity;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import ir.alirezarazavi.simplenote.R;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AboutActivity extends ActionBarActivity {

	Toolbar toolbar;
	MainActivity mainActivity;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		
		CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
				.setDefaultFontPath("fonts/Vazir.ttf")
				.setFontAttrId(R.attr.fontPath)
				.build());
		
		toolbar = (Toolbar) findViewById(R.id.app_bar);
		setSupportActionBar(toolbar);
		// Show up to home arrow icon
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(null);
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
			getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_rtl_back);
		}
		
		// Retrieve app version dynamically
		try {
			String appVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			TextView versionText = (TextView) findViewById(R.id.app_version);
			versionText.setText("نسخه " + appVersion);
		} catch (PackageManager.NameNotFoundException e) { e.printStackTrace();	}
		
		// make this global variable = true,
		// to prevent return to pin activity (lock)
		PinActivity.truePin = true;
	}

	public void contactMe(View view) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("ارتباط با من");
		builder.setItems(R.array.contact_me_items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
					case 0: // Go to website
						Intent goToUrl = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.authorFullUrl)));
						startActivity(goToUrl);
						break;
					case 1: // Email me
						Intent sendEmail = new Intent(Intent.ACTION_SEND);
						sendEmail.setType("message/rfc822");
						sendEmail.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.authorEmail)});
						sendEmail.putExtra(Intent.EXTRA_SUBJECT, "Contact from SimpleNote app");
						try{
							startActivity(sendEmail);
						} catch (ActivityNotFoundException e) {
							Toast.makeText(getApplicationContext(), "ببخشید، ولی هیچ اپ ارسال ایمیلی پیدا نشد!", Toast.LENGTH_SHORT).show();
						}
						break;
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//getMenuInflater().inflate(R.menu.menu_about, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
			//case R.id.action_support:
			//	supportApp();
			//	break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	//public void supportApp() {
	//	Intent intent = new Intent(Intent.ACTION_EDIT);
	//	String url = "bazaar://details?id=ir.alirezarazavi.simplenote";
	//	intent.setData(Uri.parse(url));
	//	try {
	//		startActivity(intent);
	//	} catch (Exception e) {
	//		Toast.makeText(getApplicationContext(), getString(R.string.cafebazaar_not_found), Toast.LENGTH_SHORT).show();
	//	}
	//}

}
