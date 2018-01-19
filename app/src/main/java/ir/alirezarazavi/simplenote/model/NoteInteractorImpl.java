package ir.alirezarazavi.simplenote.model;

import android.content.Context;

import java.util.ArrayList;

import ir.alirezarazavi.simplenote.model.data.NoteDataSource;

public class NoteInteractorImpl implements NoteInteractor {
	
	private NoteDataSource dataSource;
	
	//public NoteInteractorImpl() {
	//}
	
	public NoteInteractorImpl(Context context) {
		this.dataSource = new NoteDataSource(context);
	}
	
	@Override
	public ArrayList<Note> getNotes(String orderType) {
		dataSource.open();
		ArrayList<Note> noteArrayList = dataSource.findAll(orderType);
		dataSource.close();
		return noteArrayList;
	}
	
	@Override
	public ArrayList<Note> getSearchResult(String keyword) {
		dataSource.open();
		ArrayList<Note> result = dataSource.searchQuery(keyword);
		dataSource.close();
		return result;
	}
}
