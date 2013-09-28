/* 
 * Copyright (C) 2013 by the Centre for Development of Advanced Computing Trivandrum
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package cdac.lts.govdatabase;



import cdac.lts.travelaid.BuildConfig;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author root
 *
 */
public class DBAdapter {

	private static String LOGTAG;
		
	
	public String DATABASE_NAME;
	//private static final String DATABASE_TABLE = "TravelTours";
	public String DATABASE_TABLE;
	
	public int DATABASE_VERSION = 1;
	public  String DATABASE_CREATE;

	
	
	
	
	
    private final Context context;
	
    public DatabaseHelper DBHelper;
    public SQLiteDatabase db;

    
    public DBAdapter(Context ctx, String DATABASE_NAME, String DATABASE_TABLE, String DATABASE_CREATE ){
    	this.context = ctx;
    	this.DATABASE_NAME = DATABASE_NAME;
    	this.DATABASE_TABLE = DATABASE_TABLE;
    	this.DATABASE_CREATE = DATABASE_CREATE;
    	DBHelper = new DatabaseHelper(context);
    }
    
    
    
   private class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			try{
			 db.execSQL(DATABASE_CREATE);
			}catch (SQLException e){
				
				e.printStackTrace();
				
				if(BuildConfig.DEBUG){
					Log.e(LOGTAG, "SQLException during Database create");
				}
				
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if(BuildConfig.DEBUG){
				Log.w(LOGTAG, "Upgrading database from version " + oldVersion + " to " +
			      newVersion + ", which will destroy all old data");
			}
			
			db.execSQL("DROP TABLE IF EXISTS "+ DATABASE_TABLE);
			
			onCreate(db);
		}
    	
    }
	
    
    
    //---opens the database---
    public DBAdapter open() throws SQLException
    {
        db = DBHelper.getWritableDatabase();
        return this;
    }
    
    
    //---closes the database---
    public void close()
    {
        DBHelper.close();
    }
    
    
    //---insert a contact into the database---
    /*
    public long insertContact(int row_id, String name_of_agency, String address, int phone, int fax, String email,
    		String region, String city, String state, String contact_person, String type )*/
    
    
    
  
            
    
}
