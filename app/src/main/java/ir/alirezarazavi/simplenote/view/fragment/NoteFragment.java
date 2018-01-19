package ir.alirezarazavi.simplenote.view.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import java.util.TimeZone;

import javax.inject.Inject;

import ir.alirezarazavi.simplenote.App;
import ir.alirezarazavi.simplenote.R;
import ir.alirezarazavi.simplenote.model.Note;
import ir.alirezarazavi.simplenote.model.data.NoteDataSource;
import ir.alirezarazavi.simplenote.presenter.NotePresenter;
import ir.alirezarazavi.simplenote.utils.persiancalendar.PersianCalendar;
import ir.alirezarazavi.simplenote.view.NoteView;
import ir.alirezarazavi.simplenote.view.activity.MainActivity;

public class NoteFragment extends Fragment implements NoteView {
	@Inject SharedPreferences sharedPreferences;
	MainActivity mainActivity;
	Note note;
	EditText noteTitle;
	EditText noteText;
	TextView noteDate;
	SeekBar changeFontSeekBar;
	boolean nightMode = false;
	FrameLayout seekBar;
	ScrollView scrollView;
	LinearLayout linearLayout;
	
	NoteDataSource dataSource;
	FragmentManager fragmentManager;
	NotePresenter notePresenter;
	
	private MenuItem item;
	boolean updateNote;
	boolean sharedContent;
	
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		App.getAppComponent(getActivity()).inject(this);
	}
	
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		notePresenter.bind(this);
	}
	
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_note, container, false);
		
		mainActivity = (MainActivity) getActivity();
		fragmentManager = getFragmentManager();
		dataSource = new NoteDataSource(getActivity());
		Bundle bundle = getArguments();
		if (bundle != null) {
			note = bundle.getParcelable("note");
			// if current content is received from shared
			sharedContent = bundle.getBoolean("sharedContent");
		}
		setHasOptionsMenu(true);
		mainActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
			mainActivity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_rtl_back);
		}
		try { mainActivity.getSupportActionBar().setTitle(null); } catch (NullPointerException ignored) {}
		
		// Initialize view elements
		noteTitle = (EditText) v.findViewById(R.id.note_title);
		noteText = (EditText) v.findViewById(R.id.note_text);
		noteDate = (TextView) v.findViewById(R.id.note_date);
		seekBar = (FrameLayout) v.findViewById(R.id.seek_bar);
		changeFontSeekBar = (SeekBar) v.findViewById(R.id.change_font_seek_bar);
		scrollView = (ScrollView) v.findViewById(R.id.scroll_view);
		linearLayout = (LinearLayout) v.findViewById(R.id.linear_view);
		
		// set app theme from settings
		String appTheme = sharedPreferences.getString("app_theme", "0");
		if (appTheme.equals("1")) {
			nightMode = true;
		}
		toggleNightMode();
		
		if (note != null) {
			// show mode
			noteTitle.setText(note.getTitle());
			noteText.setText(note.getText());
			noteDate.setText(note.getDate());
			onContentChangeListener();
			//getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			
		} else {
			// create mode
			popUpKeyboard(noteTitle);
		}
		
		return v;
	}
	
	private void popUpKeyboard(EditText edittext) {
		edittext.requestFocus();
		((InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
	}
	
	
	private void onContentChangeListener() {
		// Hide cursor and clear focus on edit text when keyboard is not visible (on back press)
		KeyboardVisibilityEvent.setEventListener(getActivity(), new KeyboardVisibilityEventListener() {
			@Override
			public void onVisibilityChanged(boolean isOpen) {
				if (!isOpen) {
					linearLayout.requestFocus();
					noteText.clearFocus();
					noteTitle.clearFocus();
				}
			}
		});
		// when edit text content change, change updateNote value to trigger update
		noteTitle.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				updateNote = true;
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		noteText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				updateNote = true;
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				
			}
		});
	}
	
	public void deleteNote() {
		if (note != null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage(note.getTitle() + " حذف شود؟");
			builder.setPositiveButton("بله", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					notePresenter.deleteNote(note);
				}
			});
			builder.setNegativeButton("خیر", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
		} else {
			discardNote();
		}
	}
	
	public void shareNote() {
		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
		sharingIntent.setType("text/plain");
		sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, note.getTitle());
		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, note.getText());
		startActivity(Intent.createChooser(sharingIntent, "اشتراک گذاری از طریق "));
	}
	
	public void toggleNightMode() {
		if (!nightMode) {
			// deactivate night mode
			scrollView.setBackgroundColor(getResources().getColor(R.color.main_bg_color));
			linearLayout.setBackgroundColor(getResources().getColor(R.color.main_bg_color));
			noteTitle.setTextColor(getResources().getColor(R.color.title_light_primary));
			noteText.setTextColor(getResources().getColor(R.color.text_light_primary));
			//noteDate.setTextColor(getResources().getColor(R.color.half_black));
			if (item != null) {
				item.setIcon(getResources().getDrawable(R.drawable.ic_night_mode));
			}
			nightMode = true;
		} else {
			// active night mode
			scrollView.setBackgroundColor(getResources().getColor(R.color.dark_bg_color));
			linearLayout.setBackgroundColor(getResources().getColor(R.color.dark_bg_color));
			noteTitle.setTextColor(getResources().getColor(R.color.title_dark));
			noteText.setTextColor(getResources().getColor(R.color.text_dark));
			//noteDate.setTextColor(getResources().getColor(R.color.date_dark_secondary));
			if (item != null) {
				item.setIcon(getResources().getDrawable(R.drawable.ic_night_mode_active));
			}
			nightMode = false;
		}
	}
	
	public void fontSizeSeekBar(MenuItem item) {
		changeFontSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				// seekBar step
				progress = progress / 5;
				progress = progress * 5;
				// seekBar min
				if (progress >= 10) {
					noteText.setTextSize(progress);
				}
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});
		if (seekBar.getVisibility() == View.VISIBLE) {
			seekBar.setVisibility(View.GONE);
			item.setIcon(getResources().getDrawable(R.drawable.ic_font_size));
		} else {
			seekBar.setVisibility(View.VISIBLE);
			item.setIcon(getResources().getDrawable(R.drawable.ic_font_size_active));
		}
	}
	
	private void discardNote() {
		hideKeyboard();
		fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
	}
	
	private void hideKeyboard() {
		// Hide keyboard
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
	}
	
	public void saveNote() {
		if (note == null || sharedContent) {
			if (!noteTitle.getText().toString().isEmpty() || !noteText.getText().toString().isEmpty()) {
				dataSource.open();
				PersianCalendar persianCalendar = new PersianCalendar();
				persianCalendar.setTimeZone(TimeZone.getTimeZone("Iran"));
				String date = persianCalendar.getPersianDate() + "\n" + persianCalendar.getPersianTime();
				Note note = new Note();
				note.setTitle(noteTitle.getText().toString());
				note.setText(noteText.getText().toString());
				note.setDate(date);
				dataSource.create(note);
				dataSource.close();
				Toast.makeText(getActivity(), note.getTitle() + " ذخیره شد", Toast.LENGTH_SHORT).show();
				// update ui
				notePresenter.updateUi();
			}
		} else if (updateNote) {
			dataSource.open();
			note.setTitle(noteTitle.getText().toString());
			note.setText(noteText.getText().toString());
			dataSource.update(note);
			dataSource.close();
			Toast.makeText(getActivity(), note.getTitle() + " ذخیره شد", Toast.LENGTH_SHORT).show();
		}
		hideKeyboard();
	}
	
	@Inject
	public void setNotePresenter(NotePresenter notePresenter) {
		this.notePresenter = notePresenter;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if (note != null) {
			inflater.inflate(R.menu.menu_single_note, menu);
			if (nightMode) {
				menu.findItem(R.id.action_night_mode).setIcon(getResources().getDrawable(R.drawable.ic_night_mode));
			} else {
				menu.findItem(R.id.action_night_mode).setIcon(getResources().getDrawable(R.drawable.ic_night_mode_active));
			}
		} else {
			inflater.inflate(R.menu.menu_create_note, menu);
		}
		
	}
	
	public void updateUi() {
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		this.item = item;
		int id = item.getItemId();
		switch (id) {
			case R.id.action_delete:
				deleteNote();
				break;
			case R.id.action_share:
				shareNote();
				break;
			case R.id.action_night_mode:
				toggleNightMode();
				break;
			case R.id.action_font_size:
				fontSizeSeekBar(item);
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void close() {
		fragmentManager.popBackStack();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		notePresenter.unbind();
	}
	
}
