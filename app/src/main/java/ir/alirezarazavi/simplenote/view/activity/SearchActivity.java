package ir.alirezarazavi.simplenote.view.activity;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.view.MenuItemCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;

import java.util.ArrayList;

import javax.inject.Inject;

import ir.alirezarazavi.simplenote.App;
import ir.alirezarazavi.simplenote.R;
import ir.alirezarazavi.simplenote.model.data.NoteDataSource;
import ir.alirezarazavi.simplenote.model.Note;
import ir.alirezarazavi.simplenote.presenter.ListPresenter;
import ir.alirezarazavi.simplenote.view.adapter.SearchAdapter;
import ir.alirezarazavi.simplenote.view.fragment.NoteFragment;
import ir.alirezarazavi.simplenote.view.ListView;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class SearchActivity extends AppCompatActivity implements ListView {
	@Inject SharedPreferences sharedPreferences;
	Toolbar toolbar;
	NoteDataSource dataSource;
	ArrayList<Note> searchResult;
	String search_keyword;
	SearchAdapter searchAdapter;
	RecyclerView recyclerView;
	ListPresenter listPresenter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		App.getAppComponent(this).inject(this);
		CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
				.setDefaultFontPath("fonts/Vazir.ttf")
				.setFontAttrId(R.attr.fontPath)
				.build());
		search_keyword = getIntent().getStringExtra(SearchManager.QUERY);
		toolbar = (Toolbar) findViewById(R.id.app_bar);
		recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
		toolbar.setBackgroundColor(getResources().getColor(R.color.primaryColor2));
		setSupportActionBar(toolbar);
		try {
			getSupportActionBar().setTitle(search_keyword);
		} catch (NullPointerException ignored) {}
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		// make this global variable = true,
		// to prevent return to pin activity (lock)
		PinActivity.truePin = true;
		
		listPresenter.bind(this);
		listPresenter.getSearchResult(search_keyword);
		
	}
	
	@Inject
	public void setListPresenter(ListPresenter listPresenter) {
		this.listPresenter = listPresenter;
	}
	
	
	@Override
	public void showNotes(final ArrayList<Note> notes) {
		dataSource = new NoteDataSource(this);
		dataSource.open();
		searchResult = dataSource.searchQuery(search_keyword);
		
		String showType = sharedPreferences.getString("view_list", "0");
		String appTheme = sharedPreferences.getString("app_theme", "0");
		searchAdapter = new SearchAdapter(this, notes, appTheme, search_keyword);
		
		View view = LayoutInflater.from(this).inflate(R.layout.recyclerview_no_search_result, (ViewGroup) recyclerView.getParent(), false);
		searchAdapter.setEmptyView(view);
		if (showType.equals("0")) {
			recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
		} else {
			recyclerView.setLayoutManager(new LinearLayoutManager(this));
		}
		// set app theme
		if (appTheme.equals("1")) {
			recyclerView.setBackgroundColor(getResources().getColor(R.color.dark_bg_color2));
		}
		recyclerView.setAdapter(searchAdapter);
		recyclerView.addOnItemTouchListener(new OnItemClickListener() {
			@Override
			public void onSimpleItemClick(BaseQuickAdapter adapter, View view, int position) {
				listPresenter.onItemClicked(notes.get(position));
			}
			
			@Override
			public void onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
				listPresenter.onItemLongClicked(notes.get(position), position);
			}
		});
		
	}
	
	@Override
	public void onItemClicked(Note note, boolean editMode) {
		goToFragment(new NoteFragment(), MainActivity.FRAGMENT_NOTE_TAG, note, editMode);
	}
	
	@Override
	public void onItemLongClicked(final Note note, final int position) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(note.getTitle());
		builder.setItems(R.array.long_click_menu, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
					case 0: // Edit
						listPresenter.editNote(note);
						break;
					case 1: // Delete
						AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
						builder.setMessage(note.getTitle() + " حذف شود؟");
						builder.setPositiveButton("بله", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								listPresenter.deleteNote(note, position);
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
						break;
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
		
	}
	
	@Override
	public void goToFragment(Fragment fragment, String tag, Note note, boolean editMode) {
		startActivityForResult(new Intent(SearchActivity.this, MainActivity.class).setAction("search_result").putExtra("note", note), MainActivity.REQUEST_CODE_SEARCH_RESULT);
	}
	
	private void search(Menu menu) {
		final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		searchView.setQuery(search_keyword, false);
		searchView.setIconifiedByDefault(false);
	}
	
	@Override
	public void refreshList(int position) {
	}
	
	@Override
	public void updateUi() {
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_search, menu);
		search(menu);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
			case android.R.id.home:
				finish();
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		PinActivity.truePin = true;
	}
}
