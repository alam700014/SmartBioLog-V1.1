package com.android.fortunaattendancesystem.fm220;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.acpl.access_computech_fm220_sdk.acpl_FM220_SDK;
import com.android.fortunaattendancesystem.fm220.Fm200TableData.Fm200TableInfo;

/**
 * Created by suman-dhara on 16/10/17.
 */

public class Fm200DbOperations extends SQLiteOpenHelper {
    /**
     * Create a helper object to create, open, and/or manage a database.
     * This method always returns very quickly.  The database is not actually
     * created or opened until one of {@link #getWritableDatabase} or
     * {@link #getReadableDatabase} is called.
     *
     * @param context to use to open or create the database
     * @param name    of the database file, or null for an in-memory database
     * @param factory to use for creating cursor objects, or null for the default
     * @param version number of the database (starting at 1); if the database is older,
     *                {@link #onUpgrade} will be used to upgrade the database; if the database is
     *                newer, {@link #onDowngrade} will be used to downgrade the database
     */
    public static int database_version = 1;
    private static final String DATABASE_NAME = "fm200_db";
    private String create_quary;

    private SQLiteDatabase sqLiteDatabase;
    private acpl_FM220_SDK fm220_sdk;

    public Fm200DbOperations(Context context, acpl_FM220_SDK fm220_sdk) {
        super(context, DATABASE_NAME, null, database_version);
        this.fm220_sdk = fm220_sdk;
        create_quary = "CREATE TABLE IF NOT EXISTS " + Fm200TableInfo.TABLE_NAME + " ("
                        + Fm200TableInfo._ID + " INTEGER PRIMARY KEY, "
                        + Fm200TableInfo.EMPLOYEE_ID + " INTEGER NOT NULL, "
                        + Fm200TableInfo.TEMPLATE_B64 + " TEXT NOT NULL UNIQUE, "
                        + Fm200TableInfo.FINGER_INDEX + " INTEGER NOT NULL"
                        + Fm200TableInfo.TEMPLATE_TYPE + " TEXT NOT NULL );";
    }

    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(create_quary);

    }

    /**
     * Called when the database needs to be upgraded. The implementation
     * should use this method to drop tables, add tables, or do anything else it
     * needs to upgrade to the new schema version.
     * <p>
     * <p>
     * The SQLite ALTER TABLE documentation can be found
     * <a href="http://sqlite.org/lang_altertable.html">here</a>. If you add new columns
     * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
     * you can use ALTER TABLE to rename the old table, then create the new table and then
     * populate the new table with the contents of the old table.
     * </p><p>
     * This method executes within a transaction.  If an exception is thrown, all changes
     * will automatically be rolled back.
     * </p>
     *
     * @param db         The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * This function use for insert tempate data and security level at inbuild data base,
     *
     * @param employee_id it is employee id get from employee registration
     * @param template_b64 it is base 64 tempalte data captured from scanner
     * @param finger_index it is a user finger index value[1 to 10] for ten fingers
     * @param template_type security level for template matching at compare time.
     *
     * @return long integer of inserted row id. putData method call failed if retur -1;
     */
    public long putData(long employee_id, String template_b64,int finger_index,char template_type){
        this.sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Fm200TableInfo.EMPLOYEE_ID,employee_id);
        contentValues.put(Fm200TableInfo.TEMPLATE_B64,template_b64);
        contentValues.put(Fm200TableInfo.FINGER_INDEX,finger_index);
        contentValues.put(Fm200TableInfo.TEMPLATE_TYPE,String.valueOf(template_type));
        long k = sqLiteDatabase.insert(Fm200TableInfo.TABLE_NAME,null,contentValues);
        return k;
    }


    /**
     * @param template_b64
     *
     * @return integer number of rows affected if a whereClause is passed in, 0 otherwise.
     *
     */
    public int deleteTemplate(String template_b64){
        this.sqLiteDatabase = this.getWritableDatabase();
        return this.sqLiteDatabase.delete(Fm200TableInfo.TABLE_NAME,Fm200TableInfo._ID +" ?",new String[] { String.valueOf(this.getMatched(template_b64).getTempalteId()) });
    }


    /**
     * @param id
     * @return integer number of rows affected if a whereClause is passed in, 0 otherwise.
     *
     */
    public int deleteTemplate(long id){
        this.sqLiteDatabase = this.getWritableDatabase();
        return this.sqLiteDatabase.delete(Fm200TableInfo.TABLE_NAME,Fm200TableInfo._ID +"?",new String[]{String.valueOf(id)});
    }

    /**
     * @return deleted row count
     */
    public int deleteAll(){
        this.sqLiteDatabase = this.getReadableDatabase();
        return this.sqLiteDatabase.delete(Fm200TableInfo.TABLE_NAME,"1",null);
    }

    /**
     * @param template_b64
     * @return Fm200TableData object of database it contain matched row.
     */
    public Fm200TableData oneToNCompare(String template_b64){
        this.sqLiteDatabase = this.getReadableDatabase();
        Fm200TableData fm200RowData = getMatched(template_b64);
        if(fm200RowData != null){
            return fm200RowData;
        }
        return null;
    }

    /**
     * @param template_b64
     * @return on succes match return Fm200TableData with matched row data
     * else return null Fm200TableData
     */
    private Fm200TableData getMatched(String template_b64){
        Fm200TableData fm200TableData;
        Cursor cursor =  this.sqLiteDatabase.rawQuery("SELECT * FROM "+Fm200TableInfo.TABLE_NAME,null);
        if(cursor.moveToFirst() && cursor.getCount()>0){
            do{
                if(this.fm220_sdk.MatchFM220String(template_b64,cursor.getString(cursor.getColumnIndex(Fm200TableInfo.TEMPLATE_B64)))){
                    fm200TableData =  new Fm200TableData(cursor);
                    cursor.close();
                    return fm200TableData;
                }

            }while (cursor.moveToNext());
        }
       return null;
    }

}
