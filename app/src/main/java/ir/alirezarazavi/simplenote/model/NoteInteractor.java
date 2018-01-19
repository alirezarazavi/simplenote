package ir.alirezarazavi.simplenote.model;

import java.util.ArrayList;

public interface NoteInteractor {
	ArrayList<Note> getNotes(String orderType);
	
	ArrayList<Note> getSearchResult(String keyword);
}
