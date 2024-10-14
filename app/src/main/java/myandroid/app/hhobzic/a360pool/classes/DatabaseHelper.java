package myandroid.app.hhobzic.a360pool.classes;


import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "pool360.db";
    private static final String TABLE_NAME = "user";
    private static final String COL1 = "ID";
    private static final String COL2 = "displayName";
    private static final String COL3 = "email";
    private static final String COL4 = "expiresIn";
    private static final String COL5 = "idToken";
    private static final String COL6 = "localId";
    private static final String COL7 = "refreshToken";
    private static final String COL8 = "registered";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, displayName TEXT, email TEXT, expiresIn TEXT, idToken TEXT, localId TEXT, refreshToken TEXT, registered INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(LoginResponse response) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL2, response.getDisplayName());
        contentValues.put(COL3, response.getEmail());
        contentValues.put(COL4, response.getExpiresIn());
        contentValues.put(COL5, response.getIdToken());
        contentValues.put(COL6, response.getLocalId());
        contentValues.put(COL7, response.getRefreshToken());
        contentValues.put(COL8, response.getRegistered() ? 1 : 0);

        long result = db.insert(TABLE_NAME, null, contentValues);
        return result != -1;
    }

    public boolean isUserLoggedIn() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        boolean isLoggedIn = cursor.getCount() > 0;
        cursor.close();
        return isLoggedIn;
    }
    @SuppressLint("Range")
    public String getLocalId() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL6 + " FROM " + TABLE_NAME + " LIMIT 1", null);
        String localId = null;
        if (cursor.moveToFirst()) {
            localId = cursor.getString(cursor.getColumnIndex(COL6));
        }
        cursor.close();
        return localId;
    }
}
