package ir.alirezarazavi.simplenote.view.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.inject.Inject;

import ir.alirezarazavi.simplenote.App;
import ir.alirezarazavi.simplenote.R;
import ir.alirezarazavi.simplenote.model.Note;
import ir.alirezarazavi.simplenote.model.data.NoteDataSource;
import ir.alirezarazavi.simplenote.model.data.NoteDbOpenHelper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SettingsActivity extends PreferenceActivity {
	@Inject SharedPreferences preferences;
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		App.getAppComponent(this).inject(this);
        addPreferencesFromResource(R.xml.pref_general);
		
		CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
				.setDefaultFontPath("fonts/Vazir.ttf")
				.setFontAttrId(R.attr.fontPath)
				.build());
		
		// make this global variable true,
		// to prevent return to pin activity (lock)
		PinActivity.truePin = true;
		// Create custom toolbar(actionbar), because preference_activity has no toolbar
		LinearLayout root = (LinearLayout)findViewById(android.R.id.list).getParent().getParent().getParent();
		Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.app_bar, root, false);
		root.addView(bar, 0); // insert at top
		// insert up/back arrow icon
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
			bar.setNavigationIcon(R.drawable.ic_rtl_back);
		}
		bar.setTitle(getString(R.string.action_settings));
		
		// if up/back button pressed
		bar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				goToMainActivity();
			}
		});

		// Listen to settings changes
		SharedPreferences.OnSharedPreferenceChangeListener changeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				switch (key) {
					// protect_pin toast if pin length is less than 4 character
					case "protect_pin":
						if (sharedPreferences.getString("protect_pin", "").length() != 4) {
							Toast.makeText(SettingsActivity.this, "قفل برنامه غیرفعال شد \nرمز وارد شده فقط باید چهاررقمی باشد", Toast.LENGTH_SHORT).show();
						}
					break;
				}
			}
		};
		// ListView OnClick Listener
		preferences.registerOnSharedPreferenceChangeListener(changeListener);
		
		backupDb();
		
		restoreDb();

    }
    
    void restoreDb() {
		Preference restoreBackup = findPreference("restore_notes");
		restoreBackup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Dexter.withActivity(SettingsActivity.this)
						.withPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
						.withListener(new PermissionListener() {
							@Override
							public void onPermissionGranted(PermissionGrantedResponse response) {
								AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
								builder.setCancelable(false);
								builder.setMessage("فایل پشتیبان را در حافظه دستگاه، داخل پوشه simplenote قرار دهید تا بازگردانده شود")
										.setNegativeButton(R.string.action_discard, new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												dialog.cancel();
											}
										})
										.setPositiveButton(R.string.do_it, new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												File f = new File(Environment.getExternalStorageDirectory() + "/simplenote/simplenote.db");
												if (f.isFile()) {
													try {
														RestoreDatabase restoreDatabase = new RestoreDatabase();
														restoreDatabase.execute();
													} catch (Exception e) {
														e.printStackTrace();
													}
												} else {
													Toast.makeText(getApplicationContext(), "خطا! فایل پشتیبان پیدا نشد", Toast.LENGTH_SHORT).show();
												}
											}
										});
								AlertDialog alert = builder.create();
								alert.show();
							}
							
							@Override
							public void onPermissionDenied(PermissionDeniedResponse response) {
								Toast.makeText(SettingsActivity.this, R.string.permission_denied_msg, Toast.LENGTH_SHORT).show();
							}
							
							@Override
							public void onPermissionRationaleShouldBeShown(PermissionRequest permission, final PermissionToken token) {
								new AlertDialog.Builder(SettingsActivity.this)
										.setTitle(R.string.permssion_rationale_title)
										.setMessage(R.string.permssion_rationale_msg)
										.setCancelable(false)
										.setNegativeButton(R.string.action_discard, new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												dialog.dismiss();
												token.cancelPermissionRequest();
											}
										})
										.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												dialog.dismiss();
												token.continuePermissionRequest();
											}
										})
										.show();
							}
						})
						.check();
				
				return true;
			}
		});
	}
	
	void backupDb() {
		Preference backup = findPreference("backup_notes");
		backup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Dexter.withActivity(SettingsActivity.this)
						.withPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
						.withListener(new PermissionListener() {
							@Override
							public void onPermissionGranted(PermissionGrantedResponse response) {
								AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
								builder.setCancelable(false)
										.setMessage(R.string.backup_location_msg)
										.setNegativeButton(R.string.action_discard, new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												dialog.cancel();
											}
										})
										.setPositiveButton(R.string.do_it, new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												try {
													BackupDatabase backupDatabase = new BackupDatabase();
													backupDatabase.execute();
												} catch (Exception e) {
													e.printStackTrace();
												}
											}
										});
								AlertDialog alert = builder.create();
								alert.show();
							}

							@Override
							public void onPermissionDenied(PermissionDeniedResponse response) {
								Toast.makeText(SettingsActivity.this, R.string.permission_denied_msg, Toast.LENGTH_SHORT).show();
							}

							@Override
							public void onPermissionRationaleShouldBeShown(PermissionRequest permission, final PermissionToken token) {
								new AlertDialog.Builder(SettingsActivity.this)
										.setTitle(R.string.permssion_rationale_title)
										.setMessage(R.string.permssion_rationale_msg)
										.setCancelable(false)
										.setNegativeButton(R.string.action_discard, new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												dialog.dismiss();
												token.cancelPermissionRequest();
											}
										})
										.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												dialog.dismiss();
												token.continuePermissionRequest();
											}
										})
										.show();
							}
						})
						.check();
				return false;
			}
		});
		
		
	}

	//private boolean checkPermission() {
	//	Dexter.withActivity(SettingsActivity.this)
	//			.withPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
	//			.withListener(new PermissionListener() {
	//				@Override
	//				public void onPermissionGranted(PermissionGrantedResponse response) {
	//					permissionResult = true;
	//				}
	//
	//				@Override
	//				public void onPermissionDenied(PermissionDeniedResponse response) {
	//					Toast.makeText(SettingsActivity.this, R.string.permission_denied_msg, Toast.LENGTH_SHORT).show();
	//					permissionResult = false;
	//				}
	//
	//				@Override
	//				public void onPermissionRationaleShouldBeShown(PermissionRequest permission, final PermissionToken token) {
	//					new AlertDialog.Builder(SettingsActivity.this)
	//							.setTitle(R.string.permssion_rationale_title)
	//							.setMessage(R.string.permssion_rationale_msg)
	//							.setNegativeButton(R.string.action_discard, new DialogInterface.OnClickListener() {
	//								@Override
	//								public void onClick(DialogInterface dialog, int which) {
	//									dialog.dismiss();
	//									token.cancelPermissionRequest();
	//								}
	//							})
	//							.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	//								@Override
	//								public void onClick(DialogInterface dialog, int which) {
	//									dialog.dismiss();
	//									token.continuePermissionRequest();
	//								}
	//							})
	//							.show();
	//				}
	//			})
	//			.check();
	//
	//	return permissionResult;
	//}

	private class BackupDatabase extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			// Make directory
			try {
				if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
					Toast.makeText(getApplicationContext(), "خطا! حافظه دستگاه خود را بررسی کنید", Toast.LENGTH_SHORT).show();
				} else {
					File directory = new File(Environment.getExternalStorageDirectory() + File.separator + "simplenote");
					directory.mkdirs();
				}
				//Open your local db as the input stream
				String inFileName = getDatabasePath(NoteDbOpenHelper.DATABASE_NAME).toString();
				File dbFile = new File(inFileName);
				FileInputStream fis = new FileInputStream(dbFile);

				String outFileName = Environment.getExternalStorageDirectory() + "/simplenote/simplenote.db";
				//Open the empty db as the output stream
				OutputStream output = new FileOutputStream(outFileName);
				//transfer bytes from the inputfile to the outputfile
				byte[] buffer = new byte[1024];
				int length;
				while ((length = fis.read(buffer)) > 0) {
					output.write(buffer, 0, length);
				}
				//Close the streams
				output.flush();
				output.close();
				fis.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void s) {
			Toast.makeText(getApplicationContext(), "عملیات ایجاد پشتیبان انجام شد", Toast.LENGTH_SHORT).show();
		}
	}

	private class RestoreDatabase extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			NoteDataSource dataSource;
			try {
				SQLiteDatabase database = SQLiteDatabase.openDatabase(Environment.getExternalStorageDirectory() + "/simplenote/simplenote.db", null, 0);
				Cursor cursor = database.rawQuery("SELECT * FROM note;", null);
				while (cursor.moveToNext()) {
					dataSource = new NoteDataSource(getApplicationContext());
					dataSource.open();
					Note note = new Note();
					note.setTitle(cursor.getString(1));
					note.setText(cursor.getString(2));
					note.setDate(cursor.getString(3));
					dataSource.create(note);
					dataSource.close();
					database.close();
				}
				//relaunchApp();
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), "خطا! مشکلی در بازگرداندن پشتیبان پیش آمد", Toast.LENGTH_SHORT).show();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void s) {
			Toast.makeText(getApplicationContext(), "عملیات بازگرداندن پشتیبان انجام شد", Toast.LENGTH_SHORT).show();
		}

	}

	public void goToMainActivity() {
		// finish settings activity and start main activity
		startActivity(new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
		finish();
	}

	@Override
	public void onBackPressed() {
		goToMainActivity();
		super.onBackPressed();
	}
	
	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
	}
	
}

