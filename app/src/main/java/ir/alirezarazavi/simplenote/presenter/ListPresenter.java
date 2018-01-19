package ir.alirezarazavi.simplenote.presenter;

import android.content.Context;

import java.util.ArrayList;

import javax.inject.Inject;

import ir.alirezarazavi.simplenote.model.NoteInteractor;
import ir.alirezarazavi.simplenote.view.activity.MainActivity;
import ir.alirezarazavi.simplenote.model.data.NoteDataSource;
import ir.alirezarazavi.simplenote.model.Note;
import ir.alirezarazavi.simplenote.view.ListView;

public class ListPresenter implements BasePresenter<ListView> {
	private static final String TAG = "SimpleNote";
	private ListView view;
	private NoteInteractor interactor;
	private Context context;
	private ArrayList<Note> notes;
	
	@Inject
	public ListPresenter(NoteInteractor interactor, Context context) {
		this.interactor = interactor;
		this.context = context;
	}
	
	@Override
	public void bind(ListView view) {
		this.view = view;
	}
	
	@Override
	public void unbind() {
		this.view = null;
	}
	
	public Context getContext() {
		return (MainActivity) view;
	}
	
	public void loadNotes(String orderType) {
		notes = interactor.getNotes(orderType);
		view.showNotes(notes);
	}
	
	public void onItemClicked(Note note) {
		if (view != null) {
			view.onItemClicked(note, false);
		}
	}
	
	public void onItemLongClicked(Note note, int position) {
		if (view != null) {
			view.onItemLongClicked(note, position);
		}
	}
	
	public void editNote(Note note) {
		view.onItemClicked(note, true);
	}
	
	public void deleteNote(Note note, int which) {
		NoteDataSource dataSource = new NoteDataSource(context);
		dataSource.open();
		dataSource.delete(note);
		dataSource.close();
		
		// update Ui
		refreshUi(which);
	}
	
	public void refreshUi(int position) {
		view.refreshList(position);
	}
	
	@Override
	public void updateUi() {
		view.updateUi();
	}
	
	
	public void getSearchResult(String keyword) {
		notes = interactor.getSearchResult(keyword);
		view.showNotes(notes);
	}
	
}
