package ir.alirezarazavi.simplenote.model.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NoteDbOpenHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "simplenote.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NOTE = "note";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_TEXT = "text";
    public static final String COLUMN_DATE = "date";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NOTE + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TITLE + " VARCHAR, " +
                    COLUMN_TEXT + " TEXT, " +
                    COLUMN_DATE + " TEXT " +
            ")";

    public NoteDbOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS" + TABLE_NOTE);
        onCreate(db);
    }
}
