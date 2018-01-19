package ir.alirezarazavi.simplenote.view.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;

import java.util.ArrayList;

import javax.inject.Inject;

import ir.alirezarazavi.simplenote.App;
import ir.alirezarazavi.simplenote.R;
import ir.alirezarazavi.simplenote.model.Note;
import ir.alirezarazavi.simplenote.presenter.ListPresenter;
import ir.alirezarazavi.simplenote.view.ListView;
import ir.alirezarazavi.simplenote.view.activity.AboutActivity;
import ir.alirezarazavi.simplenote.view.activity.MainActivity;
import ir.alirezarazavi.simplenote.view.activity.SettingsActivity;
import ir.alirezarazavi.simplenote.view.adapter.QuickAdapter;

public class ListFragment extends Fragment implements ListView {
	@Inject SharedPreferences sharedPreferences;
	MainActivity mainActivity;
	ArrayList<Note> allNotes;
	RecyclerView recyclerView;
	QuickAdapter quickAdapter;
	TextView addNoteBtn;
	ListPresenter listPresenter;
	private Menu menu;
	
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		App.getAppComponent(getActivity()).inject(this);
	}
	
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		listPresenter.bind(this);
		String orderType = sharedPreferences.getString("list_order", "0");
		listPresenter.loadNotes(orderType);
	}
	
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_list, container, false);
		setHasOptionsMenu(true);
		
		mainActivity = (MainActivity) getActivity();
		
		try {
			mainActivity.getSupportActionBar().setTitle(getString(R.string.app_name));
			mainActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		} catch (NullPointerException ignored) {}
		
		recyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
		addNoteBtn = (TextView) v.findViewById(R.id.add_note);
		
		addNoteBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				goToFragment(new NoteFragment(), MainActivity.FRAGMENT_NOTE_TAG, null, false);
			}
		});
		
		return v;
	}
	
	@Inject
	public void setNotePresenter(ListPresenter listPresenter) {
		this.listPresenter = listPresenter;
	}
	
	@Override
	public void showNotes(ArrayList<Note> notes) {
		allNotes = notes;
		loadAdapter(allNotes);
	}
	
	private void loadAdapter(final ArrayList<Note> notes) {
		String showType = sharedPreferences.getString("view_list", "0");
		String appTheme = sharedPreferences.getString("app_theme", "0");
		quickAdapter = new QuickAdapter(getActivity(), notes, appTheme);
		// set notes list view type
		if (showType.equals("0")) {
			recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
		} else {
			recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		}
		// set app theme
		if (appTheme.equals("1")) {
			recyclerView.setBackgroundColor(getResources().getColor(R.color.dark_bg_color2));
		}
		recyclerView.setAdapter(quickAdapter);
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
		quickAdapter.setEmptyView(R.layout.recyclerview_emptyview, (ViewGroup) recyclerView.getParent());
	}
	
	@Override
	public void onItemClicked(Note note, boolean editMode) {
		goToFragment(new NoteFragment(), MainActivity.FRAGMENT_NOTE_TAG, note, editMode);
	}
	
	@Override
	public void onItemLongClicked(final Note note, final int position) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(note.getTitle());
		builder.setItems(R.array.long_click_menu, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
					case 0: // Edit
						listPresenter.editNote(note);
						break;
					case 1: // Delete
						AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
	
	private void search() {
		SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
		SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		this.menu = menu;
		inflater.inflate(R.menu.menu_main, menu);
		search();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
			case R.id.action_settings:
				startActivity(new Intent(getActivity(), SettingsActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
				break;
			case R.id.action_about:
				startActivity(new Intent(getActivity(), AboutActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
				break;
			//case R.id.action_support:
			//	mainActivity.supportApp();
			//	break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void goToFragment(Fragment fragment, String tag, Note note, boolean sharedContent) {
		mainActivity.goToFragment(fragment, tag, note, sharedContent);
	}
	
	@Override
	public void refreshList(int position) {
		allNotes.remove(position);
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				quickAdapter.notifyDataSetChanged();
			}
		});
	}
	
	@Override
	public void updateUi() {
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		listPresenter.unbind();
	}
}
