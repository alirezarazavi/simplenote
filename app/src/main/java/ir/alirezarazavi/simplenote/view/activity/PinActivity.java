package ir.alirezarazavi.simplenote.view.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import javax.inject.Inject;

import ir.alirezarazavi.simplenote.App;
import ir.alirezarazavi.simplenote.R;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class PinActivity extends ActionBarActivity {
	@Inject SharedPreferences sharedPreferences;
	// Global Variable to Access and Check from other Activity,
	// to check if pin code enter correctly and prevent to show PinActivity again
	public static boolean truePin = false;
	EditText etPin;
	TextView pinAlert;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		App.getAppComponent(this).inject(this);
		setContentView(R.layout.activity_pin);
		CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
				.setDefaultFontPath("fonts/Vazir.ttf")
				.setFontAttrId(R.attr.fontPath)
				.build());
		// Get Settings to Check Pin
		final String pinCode = sharedPreferences.getString("protect_pin", "");

		pinAlert = (TextView) findViewById(R.id.pinAlert);
		etPin = (EditText) findViewById(R.id.etPin);
		// Popup Keyboard when activity launch (main code is in manifests)
		etPin.requestFocus();
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.showSoftInput(etPin, InputMethodManager.SHOW_FORCED);
		// EditText Change Listener
		etPin.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				pinAlert.setText("");
				if (s.length() == 4) {
					if (pinCode.equals(etPin.getText().toString())) {
						truePin = true;
						startActivity(new Intent(PinActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
						finish();
					} else {
						pinAlert.setText("رمز اشتباه است");
					}
				}
			}
			@Override
			public void afterTextChanged(Editable s) {

			}
		});

	}
	
	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
	}

}
