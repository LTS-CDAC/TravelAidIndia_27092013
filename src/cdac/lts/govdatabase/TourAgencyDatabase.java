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
public class TourAgencyDatabase extends DBAdapter {

	private static final String ROW_ID = "rowid";
	private static final String NAME_OF_AGENCY = "agencyname";
	private static final String ADDRESS = "address";
	private static final String PHONE = "phone";
	private static final String FAX = "fax";
	private static final String EMAIL = "email";
	private static final String REGION = "region";
	private static final String CITY = "city";
	private static final String STATE = "state";
	private static final String CONTACT_PERSON = "contactperson";
	private static final String TYPE = "type";

	public TourAgencyDatabase(Context ctx) {
		super(ctx, "Travel_Tour_Agency_DB", "TravelTours", "create table " + "TravelTours" + " (" + ROW_ID
				+ " text primary key, " + NAME_OF_AGENCY + " text not null, "
				+ ADDRESS + " text not null, " + PHONE + " text not null, " + FAX
				+ " text not null, " + EMAIL + " text not null, " + REGION
				+ " text not null, " + CITY + " text not null, " + STATE
				+ " text not null, " + CONTACT_PERSON + " text not null, "
				+ TYPE + " text not null" + ");");
		//initializeTableCreationPara();
	}

	private void initializeTableCreationPara() {
		DATABASE_NAME = "TravelDB";
		DATABASE_VERSION = 1;
		DATABASE_TABLE = "TravelTours";
		DATABASE_CREATE = "create table " + DATABASE_TABLE + " (" + ROW_ID
				+ " text primary key, " + NAME_OF_AGENCY + " text not null, "
				+ ADDRESS + " text not null, " + PHONE + " text not null, " + FAX
				+ " text not null, " + EMAIL + " text not null, " + REGION
				+ " text not null, " + CITY + " text not null, " + STATE
				+ " text not null, " + CONTACT_PERSON + " text not null, "
				+ TYPE + " text not null" + ");";

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
	public long insertTouristAgencyDetails(String rowid, String nameOfAgency,
			String address, String phone, String fax, String email,
			String region, String city, String state, String contactPerson,
			String type) {

		ContentValues initialValues = new ContentValues();
		initialValues.put(ROW_ID, rowid);
		initialValues.put(NAME_OF_AGENCY, nameOfAgency);
		initialValues.put(ADDRESS, address);
		initialValues.put(PHONE, phone);
		initialValues.put(FAX, fax);
		initialValues.put(EMAIL, email);
		initialValues.put(REGION, region);
		initialValues.put(CITY, city);
		initialValues.put(STATE, state);
		initialValues.put(CONTACT_PERSON, contactPerson);
		initialValues.put(TYPE, type);
		return db.insert(DATABASE_TABLE, null, initialValues);
	}

	// TODO ---deletes a particular contact---
	public boolean deleteContact(long rowId) {

		return db.delete(DATABASE_TABLE, ROW_ID + "=" + rowId, null) > 0;
	}

	/**
	 * Retrieves all Approved hotels
	 * 
	 * @return
	 */
	public Cursor getAllTourismHotelDetails() {

		return db.query(DATABASE_TABLE, new String[] { ROW_ID, NAME_OF_AGENCY,
				ADDRESS, PHONE, FAX, EMAIL, REGION, CITY, STATE,
				CONTACT_PERSON, TYPE }, null, null, null, null, null);
	}

	/**
	 * Retrieves all distinct STATES in TABLE TravelTours for giving suggestions while querying 
	 * @return
	 */
	public Cursor getTouristAgencyState() {

		return db.query(true, DATABASE_TABLE, new String[] { STATE }, null,
				null, null, null, null, null);
	}

	/**
	 * Retrieves all distinct CITY in TABLE TravelTours for giving suggestions while querying 
	 * @return
	 */
	public Cursor getTouristAgencyCity() {

		return db.query(true, DATABASE_TABLE, new String[] { CITY }, null,
				null, null, null, null, null);
	}

	/**
	 * Retrieves all distinct REGION in TABLE TravelTours for giving suggestions while querying 
	 * @return
	 */
	public Cursor getTouristAgencyRegion() {

		return db.query(true, DATABASE_TABLE, new String[] { REGION }, null,
				null, null, null, null, null);
	}

	/**
	 * Retrieves approved Tourist Agency based upon key STATE in TABLE HotelTours
	 * 
	 * @param rowId
	 * @return
	 * @throws SQLException
	 */
	public Cursor getTouristAgencyDetailByState(String rowId) throws SQLException {
		/*
		 * (String rowid, String hotelname, String address, String state, String
		 * phone, String fax, String email_id, String website, String type,
		 * String rooms)
		 */
		Cursor mCursor = db.query(true, DATABASE_TABLE, new String[] { ROW_ID,
				NAME_OF_AGENCY, ADDRESS, PHONE, FAX, EMAIL, REGION, CITY,
				STATE, CONTACT_PERSON, TYPE }, STATE + "=?",
				new String[] { rowId }, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	
	/**
	 * Retrieves approved hotel based upon key CITY in TABLE TravelTours
	 * 
	 * @param rowId
	 * @return
	 * @throws SQLException
	 */
	public Cursor getTouristAgencyDetailByCity(String rowId) throws SQLException {
		/*
		 * (String rowid, String hotelname, String address, String state, String
		 * phone, String fax, String email_id, String website, String type,
		 * String rooms)
		 */
		Cursor mCursor = db.query(true, DATABASE_TABLE, new String[] { ROW_ID,
				NAME_OF_AGENCY, ADDRESS, PHONE, FAX, EMAIL, REGION, CITY,
				STATE, CONTACT_PERSON, TYPE }, CITY + "=?",
				new String[] { rowId }, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	
	/**
	 * Retrieves approved hotel based upon key REGION in TABLE HotelTours
	 * 
	 * @param rowId
	 * @return
	 * @throws SQLException
	 */
	public Cursor getTouristAgencyDetailByRegion(String rowId) throws SQLException {
		/*
		 * (String rowid, String hotelname, String address, String state, String
		 * phone, String fax, String email_id, String website, String type,
		 * String rooms)
		 */
		Cursor mCursor = db.query(true, DATABASE_TABLE, new String[] { ROW_ID,
				NAME_OF_AGENCY, ADDRESS, PHONE, FAX, EMAIL, REGION, CITY,
				STATE, CONTACT_PERSON, TYPE }, REGION + "=?",
				new String[] { rowId }, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	
	public Cursor getTouristAgencyDetailByStateCityRegion(String state, String city, String region){
		/*
		 * (String rowid, String hotelname, String address, String state, String
		 * phone, String fax, String email_id, String website, String type,
		 * String rooms)
		 */
		Cursor mCursor = db.query(true, DATABASE_TABLE, new String[] { ROW_ID,
				NAME_OF_AGENCY, ADDRESS, PHONE, FAX, EMAIL, REGION, CITY,
				STATE, CONTACT_PERSON, TYPE }, STATE + "=?"+ " AND " + CITY + "=?" + " AND " + REGION + "=?",
				new String[] { state, city, region }, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	
	
	
	public Cursor getTouristAgencyDetailByStateCity(String state, String city){
		/*
		 * (String rowid, String hotelname, String address, String state, String
		 * phone, String fax, String email_id, String website, String type,
		 * String rooms)
		 */
		Cursor mCursor = db.query(true, DATABASE_TABLE, new String[] { ROW_ID,
				NAME_OF_AGENCY, ADDRESS, PHONE, FAX, EMAIL, REGION, CITY,
				STATE, CONTACT_PERSON, TYPE }, STATE + "=?"+ " AND " + CITY + "=?",
				new String[] { state, city}, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	
	public Cursor getTouristAgencyDetailByStateRegion(String state, String region){
		/*
		 * (String rowid, String hotelname, String address, String state, String
		 * phone, String fax, String email_id, String website, String type,
		 * String rooms)
		 */
		Cursor mCursor = db.query(true, DATABASE_TABLE, new String[] { ROW_ID,
				NAME_OF_AGENCY, ADDRESS, PHONE, FAX, EMAIL, REGION, CITY,
				STATE, CONTACT_PERSON, TYPE }, STATE + "=?"+ " AND " + REGION + "=?",
				new String[] { state, region }, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	
	
	
	
	
	
}