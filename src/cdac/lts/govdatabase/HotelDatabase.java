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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

/**
 * @author root
 *
 */
public class HotelDatabase extends DBAdapter{

	
	private static final String _ROW_ID = "rowid";
	private static final String HOTEL_NAME = "hotelname";
	private static final String ADDRESS = "address";
	private static final String STATE = "state";
	private static final String PHONE = "phone";
	private static final String FAX = "fax";
	private static final String EMAIL_ID = "emailid";
	private static final String WEBSITE = "website";
	private static final String TYPE = "type";
	private static final String ROOMS = "rooms";
	
	
			
	
	
	
	public HotelDatabase(Context ctx) {
		
		super(ctx, "Travel_Hotel_DB1", "HotelTours", "create table " + "HotelTours" + " (" + _ROW_ID + " text primary key, "+ 
				HOTEL_NAME + " text not null, " + ADDRESS + " text not null, " + STATE + " text not null, " + PHONE + " text not null, "
				+ FAX + " text not null, " + EMAIL_ID + " text not null, " + WEBSITE + " text not null, "
				+ TYPE + " text not null, " + ROOMS + " text not null" + ");");
	}

	
	
	private void initializeTableCreationPara(){
		DATABASE_NAME = "TravelDB";
		DATABASE_VERSION = 1;
		DATABASE_TABLE = "HotelTours";
		DATABASE_CREATE = "create table " + DATABASE_TABLE + " (" + _ROW_ID + " text primary key, "+ 
				HOTEL_NAME + " text not null, " + ADDRESS + " text not null, " + STATE + " text not null, " + PHONE + " text not null, "
				+ FAX + " text not null, " + EMAIL_ID + " text not null, " + WEBSITE + " text not null, "
				+ TYPE + " text not null, " + ROOMS + " text not null" + ");";
	}
	
	
	
	
	
	  /**
     * Inserts Hotel details into Table HotelTours
     * 
     * @param rowid
     * @param hotelname
     * @param address
     * @param state
     * @param phone
     * @param fax
     * @param email_id
     * @param website
     * @param type
     * @param rooms
     * @return
     */
    public long insertTourismHotelDetails  (String rowid, String hotelname, String address, String state, String phone, String fax,
			  String email_id, String website, String type, String rooms){
    
    	   	
        ContentValues initialValues = new ContentValues();
        
        initialValues.put(_ROW_ID, rowid);
        initialValues.put(HOTEL_NAME, hotelname);
        initialValues.put(ADDRESS, address);
        initialValues.put(STATE, state);
        initialValues.put(PHONE, phone);
        initialValues.put(FAX, fax);
        initialValues.put(EMAIL_ID, email_id);
        initialValues.put(WEBSITE, website);
        initialValues.put(TYPE, type);
        initialValues.put(ROOMS, rooms);
                
        /*
        initialValues.put(ROW_ID, row_id);
        initialValues.put(NAME_OF_AGENCY, name_of_agency);
        initialValues.put(ADDRESS, address);
        initialValues.put(PHONE, phone);
        initialValues.put(FAX, fax);
        initialValues.put(EMAIL, email);
        initialValues.put(REGION, region);
        initialValues.put(CITY, city);
        initialValues.put(STATE, state);
        initialValues.put(CONTACT_PERSON, contact_person);
        initialValues.put(TYPE, type);
        */
               
        return db.insert(DATABASE_TABLE, null, initialValues);
    }
    
    
    //TODO ---deletes a particular contact---  
    public boolean deleteContact(long rowId){
    
        return db.delete(DATABASE_TABLE, _ROW_ID + "=" + rowId, null) > 0;
    }
    
    
   /**
    * Retrieves all Approved hotels 
    * @return
    */
    public Cursor getAllTourismHotelDetails(){
    
		return db.query(DATABASE_TABLE, new String[] { _ROW_ID,
				HOTEL_NAME, ADDRESS, STATE, PHONE, FAX, EMAIL_ID, WEBSITE,
				TYPE, ROOMS }, null, null, null, null, null);
    }
    
    
    /**
     * Retrieves all distinct STATES in TABLE HotelTours for giving suggestions while querying 
     * @return
     */
    public Cursor getTourismHotelState(){
    	
     return db.query(true, DATABASE_TABLE, new String[] { STATE }, null, null, null, null, null,null);
    }
    
    
	/**
	 * Retrieves approved hotel based upon key STATE in  TABLE HotelTours
	 * @param rowId
	 * @return
	 * @throws SQLException
	 */
	public Cursor getTourismHotelDetail(String rowId) throws SQLException {
		/*
		 * (String rowid, String hotelname, String address, String state, String
		 * phone, String fax, String email_id, String website, String type,
		 * String rooms)
		 */
		Cursor mCursor = db.query(true, DATABASE_TABLE, new String[] { _ROW_ID,
				HOTEL_NAME, ADDRESS, STATE, PHONE, FAX, EMAIL_ID, WEBSITE,
				TYPE, ROOMS }, STATE + "=?", new String[]{rowId}, null, null, null,
				null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
}
