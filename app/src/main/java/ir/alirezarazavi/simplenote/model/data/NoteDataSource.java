package ir.alirezarazavi.simplenote.model.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import ir.alirezarazavi.simplenote.model.Note;

public class NoteDataSource {

    private SQLiteOpenHelper dbHelper;
    private SQLiteDatabase database;

    private static final String[] allColumn = {
        NoteDbOpenHelper.COLUMN_ID,
        NoteDbOpenHelper.COLUMN_TITLE,
        NoteDbOpenHelper.COLUMN_TEXT,
        NoteDbOpenHelper.COLUMN_DATE,
    };

    public NoteDataSource (Context context) {
        dbHelper = new NoteDbOpenHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
        //Log.i("log", "DB Opened");
    }

    public void close() {
        dbHelper.close();
        //Log.i("log", "DB Closed");
    }

    public Note create(Note note) {
        ContentValues values = new ContentValues();
        values.put(NoteDbOpenHelper.COLUMN_TITLE, note.getTitle());
        values.put(NoteDbOpenHelper.COLUMN_TEXT, note.getText());
        values.put(NoteDbOpenHelper.COLUMN_DATE, note.getDate());
        long insertId = database.insert(NoteDbOpenHelper.TABLE_NOTE, null, values);
        note.setId(insertId);
        return note;
    }

    public boolean delete(Note note) {
        String where = NoteDbOpenHelper.COLUMN_ID + "=" + note.getId();
        int result = database.delete(NoteDbOpenHelper.TABLE_NOTE, where, null);
        return (result == 1);
    }

	public Note update(Note note) {
		ContentValues values = new ContentValues();
		values.put(NoteDbOpenHelper.COLUMN_TITLE, note.getTitle());
		values.put(NoteDbOpenHelper.COLUMN_TEXT, note.getText());
		String where = NoteDbOpenHelper.COLUMN_ID + "=" + note.getId();
		long insertId = database.update(NoteDbOpenHelper.TABLE_NOTE, values, where, null);

		note.setId(insertId);
		return note;
	}

	public ArrayList<Note> findAll(String orderType) {
        ArrayList<Note> notes = new ArrayList<>();
		String orderBy;
		switch (orderType) {
			case "3":
				orderBy = NoteDbOpenHelper.COLUMN_TEXT;
				break;
			case "2":
				orderBy = NoteDbOpenHelper.COLUMN_TITLE;
				break;
			case "1":
				orderBy = NoteDbOpenHelper.COLUMN_ID + " ASC";
				break;
			default:
				orderBy = NoteDbOpenHelper.COLUMN_ID + " DESC";
				break;
		}
        Cursor cursor = database.query(NoteDbOpenHelper.TABLE_NOTE, allColumn, null, null, null, null, orderBy);

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                Note note = new Note();
                note.setId(cursor.getLong(cursor.getColumnIndex(NoteDbOpenHelper.COLUMN_ID)));
                note.setTitle(cursor.getString(cursor.getColumnIndex(NoteDbOpenHelper.COLUMN_TITLE)));
                note.setText(cursor.getString(cursor.getColumnIndex(NoteDbOpenHelper.COLUMN_TEXT)));
                note.setDate(cursor.getString(cursor.getColumnIndex(NoteDbOpenHelper.COLUMN_DATE)));
                notes.add(note);
            }
        }

		cursor.close();

        return notes;
    }

    public ArrayList<Note> searchQuery(String keyword) {
        ArrayList<Note> notes = new ArrayList<>();
        Cursor cursor = database.query(true, NoteDbOpenHelper.TABLE_NOTE, allColumn, NoteDbOpenHelper.COLUMN_TITLE + " LIKE '%" + keyword + "%'" + " OR " + NoteDbOpenHelper.COLUMN_TEXT + " LIKE '%" + keyword + "%'", null, null, null, null, null);
        while (cursor.moveToNext()) {
            Note note = new Note();
            note.setId(cursor.getLong(cursor.getColumnIndex(NoteDbOpenHelper.COLUMN_ID)));
            note.setTitle(cursor.getString(cursor.getColumnIndex(NoteDbOpenHelper.COLUMN_TITLE)));
            note.setText(cursor.getString(cursor.getColumnIndex(NoteDbOpenHelper.COLUMN_TEXT)));
            note.setDate(cursor.getString(cursor.getColumnIndex(NoteDbOpenHelper.COLUMN_DATE)));
            notes.add(note);
        }

        cursor.close();

        return notes;
    }

}
