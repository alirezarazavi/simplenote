package ir.alirezarazavi.simplenote.view.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import javax.inject.Inject;

import ir.alirezarazavi.simplenote.App;
import ir.alirezarazavi.simplenote.R;
import ir.alirezarazavi.simplenote.model.Note;
import ir.alirezarazavi.simplenote.view.fragment.ListFragment;
import ir.alirezarazavi.simplenote.view.fragment.NoteFragment;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class MainActivity extends ActionBarActivity {
	public static final int REQUEST_CODE_SEARCH_RESULT = 5;
	private static final String FRAGMENT_LIST_TAG = "fragment_list";
	public static final String FRAGMENT_NOTE_TAG = "fragment_note";
	public static final String APP_SHORTCUT_NAME = "ir.alirezarazavi.simplenote.view.activity.MainActivity.COMPOSE";
	@Inject	SharedPreferences sharedPreferences;
	Toolbar toolbar;
	FragmentManager fragmentManager;
	Bundle instanceState;
	boolean pin;
	String pinCode;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		App.getAppComponent(this).inject(this);
        setContentView(R.layout.activity_main);
		CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
				.setDefaultFontPath("fonts/Vazir.ttf")
				.setFontAttrId(R.attr.fontPath)
				.build());
		instanceState = savedInstanceState;
		
		toolbar = (Toolbar) findViewById(R.id.app_bar);
		setSupportActionBar(toolbar);
		
		fragmentManager = getFragmentManager();
		if (fragmentManager.findFragmentById(R.id.frame_layout) == null) {
			if (instanceState != null) {
				return;
			}
			ListFragment listFragment = new ListFragment();
			fragmentManager.beginTransaction()
					.add(R.id.frame_layout, listFragment, FRAGMENT_LIST_TAG)
					.commit();
		}
		
		// Show PIN Activity if pin_protect activated in settings
		pin = sharedPreferences.getBoolean("protect_checkbox", false);
		pinCode = sharedPreferences.getString("protect_pin", "");
		
		if (pin && pinCode.length() == 4 && !PinActivity.truePin) {
			startActivity(new Intent(getApplicationContext(), PinActivity.class));
			finish();
		}
		
		handleIntents();
		
    }
	
	private void handleIntents() {
		Intent receivedIntent = getIntent();
		// App shortcut
		if (APP_SHORTCUT_NAME.equals(receivedIntent.getAction())) {
			goToFragment(new NoteFragment(), FRAGMENT_NOTE_TAG, null, false);
		}
		
		if (receivedIntent.getExtras() != null) {
			// Handle Share to SimpleNote
			String receivedType = receivedIntent.getType();
			if (receivedType != null && receivedType.startsWith("text/")) {
				String receivedTitle = receivedIntent.getStringExtra(Intent.EXTRA_SUBJECT);
				String receivedText = receivedIntent.getStringExtra(Intent.EXTRA_TEXT);
				Note note = new Note();
				note.setText(receivedText);
				note.setTitle(receivedTitle);
				goToFragment(new NoteFragment(), FRAGMENT_NOTE_TAG, note, true);
			}
			
			//	open note from search result activity
			if (receivedIntent.getAction().equals("search_result")) {
				PinActivity.truePin = true;
				Note note = receivedIntent.getParcelableExtra("note");
				//goToFragment(new NoteFragment(), FRAGMENT_NOTE_TAG, note, false);
				NoteFragment fragment = new NoteFragment();
				if (note != null) {
					Bundle bundle = new Bundle();
					bundle.putParcelable("note", note);
					bundle.putBoolean("editMode", false);
					fragment.setArguments(bundle);
				}
				fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
				fragmentManager.beginTransaction().replace(R.id.frame_layout, fragment).commit();
			}
			
		}
	}
	
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0) {
			if(resultCode == RESULT_CANCELED){
				//hide_search_bar();
			}
		}
	}
	
	
	private Fragment checkFragmentInstance(int id, Object instanceClass) {
		Fragment result = null;
		if (fragmentManager != null) {
			Fragment fragment = fragmentManager.findFragmentById(id);
			if (instanceClass.equals(fragment.getClass())) {
				result = fragment;
			}
		}
		return result;
	}
	
	public void goToFragment(Fragment fragment, String tag, Note note, boolean sharedContent) {
		
		if (fragmentManager.findFragmentByTag(tag) == null) {
			PinActivity.truePin = true;
			if (instanceState != null) {
				return;
			}
			if (note != null) {
				Bundle bundle = new Bundle();
				bundle.putParcelable("note", note);
				//bundle.putBoolean("editMode", editMode);
				bundle.putBoolean("sharedContent", sharedContent);
				fragment.setArguments(bundle);
			}
			fragmentManager.beginTransaction().replace(R.id.frame_layout, fragment).addToBackStack(tag).commit();
			
		}
	}
	
	@Override
	public void onBackPressed()	{
		// save note (NoteFragment)
		Fragment fragment = checkFragmentInstance(R.id.frame_layout, NoteFragment.class);
		if (fragment != null) {
			NoteFragment noteFragment = (NoteFragment) getFragmentManager().findFragmentById(R.id.frame_layout);
			noteFragment.saveNote();
		}
		// go back to previous fragment
		if (fragmentManager.getBackStackEntryCount() > 0) {
			fragmentManager.popBackStack();
		}
		else {
			super.onBackPressed();
		}
	}


	@Override
	protected void onPause() {
		super.onPause();
		// Make this global variable false, when app close
		// to show pin_activity (lock) when app start again
		if (pin && pinCode.length() == 4) {
			PinActivity.truePin = false;
		}
	}
	
	
	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
			case android.R.id.home:
				onBackPressed();
				break;
		}
		return super.onOptionsItemSelected(item);
	}
}


