package ir.alirezarazavi.simplenote.presenter;

import android.app.Activity;
import android.content.Context;

import javax.inject.Inject;

import ir.alirezarazavi.simplenote.model.data.NoteDataSource;
import ir.alirezarazavi.simplenote.model.Note;
import ir.alirezarazavi.simplenote.view.NoteView;

public class NotePresenter implements BasePresenter<NoteView> {
	private static final String TAG = "SimpleNote";
	private NoteView view;
	private Context context;
	private ListPresenter listPresenter;
	
	@Inject
	public NotePresenter(Context context) {
		this.context = context;
	}
	
	@Override
	public void bind(NoteView view) {
		this.view = view;
	}
	
	@Override
	public void unbind() {
		view = null;
	}
	
	public Context getContext() {
		return (Activity) view;
	}
	
	@Inject
	public void setNotePresenter(ListPresenter listPresenter) {
		this.listPresenter = listPresenter;
	}
	
	public void deleteNote(Note note) {
		NoteDataSource dataSource = new NoteDataSource(context);
		dataSource.open();
		dataSource.delete(note);
		dataSource.close();
		view.close();
	}
	
	@Override
	public void updateUi() {
		view.updateUi();
	}
	
	
	
	
	
}
