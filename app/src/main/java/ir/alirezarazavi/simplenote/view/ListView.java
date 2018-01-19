package ir.alirezarazavi.simplenote.view;

import android.app.Fragment;

import java.util.ArrayList;

import ir.alirezarazavi.simplenote.model.Note;

public interface ListView extends BaseView {
	
	void showNotes(ArrayList<Note> notes);
	
	void goToFragment(Fragment fragment, String tag, Note note, boolean sharedContent);
	
	void onItemClicked(Note note, boolean editMode);
	
	void onItemLongClicked(Note note, int position);
	
	void refreshList(int position);
	
}
