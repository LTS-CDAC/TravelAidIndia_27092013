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

package cdac.lts.travelaid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import cdac.lts.govdatabase.HotelDatabase;


import cdac.lts.travelaid.HotelDatabaseMainActivity.HotelToursEntity;
import cdac.lts.travelaid.HotelDatabaseMainActivity.database1;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

public class SynchronizeHotelDbActivity extends Activity {

	final String LOGTAG = "SynchronizeHotelDbActivity";
    //static Boolean database1flag = false;
	//static Boolean database2flag = false;
  
    ProgressDialog loadingFileProgress;
    final int ALERT_DIALOG_OPTIONS_ID_NONETWORK = 1;
    
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_synchronize_hotel_db);
        
        if(Utility.isNetworkAvailable(getApplicationContext())){
         new SynchronizeDatabase().execute();	
        }
        else{
        	Toast.makeText(getApplicationContext(), "Network not available..", Toast.LENGTH_LONG).show();
        	openNetworkStatusDialog();
        	
        }
        
        
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_synchronize_hotel_db, menu);
        return true;
    }

    
    /**
	 * Synchronize app database with website XML
	 */
	private class SynchronizeDatabase extends AsyncTask<Void, Void, Void> {
	
		
		  
		
		@Override
		protected void onPostExecute(Void result) {
			loadingFileProgress.cancel();
			super.onPostExecute(result);
			try {
				SynchronizeHotelDbActivity.this.finish();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		protected void onPreExecute() {
			 loadingFileProgress = ProgressDialog.show(SynchronizeHotelDbActivity.this,
					"", "Please wait..\nSynchronizing depends upon network..");
			 loadingFileProgress.setCancelable(true);
			 loadingFileProgress.setOnCancelListener(new OnCancelListener(){

				@Override
				public void onCancel(DialogInterface dialog) {
					finish();
					
				}
				 
			 });
			 super.onPreExecute();
		}

		
		
        void downloadXmlFromWeb(){
			String url = "https://datacms.nic.in/datatool/?url=http://data.gov.in//sites/default/files/HotelTourismData.xls&format=xml";
			Utility.stub(getApplicationContext(), url, "hotel_tourism_data_new.xml");
		}

        /**
         * Checking whether downloaded XML newer than local one, if true local copy is replaced by latest
         * note : uses MD5
         */
		boolean checkXmlAnUpdate() {
			if (BuildConfig.DEBUG) {
				Log.i(LOGTAG,
						"hotel_tourism_data "
								+ Utility.md5CheckSum(getApplicationContext(),
										"hotel_tourism_data.xml"));
				Log.i(LOGTAG,
						"hotel_tourism_data_new "
								+ Utility.md5CheckSum(getApplicationContext(),
										"hotel_tourism_data_new.xml"));
			}

			if (!Utility.md5CheckSum(getApplicationContext(),
					"hotel_tourism_data_new.xml").equals(
					Utility.md5CheckSum(getApplicationContext(),
							"hotel_tourism_data.xml"))) {
				{
					File fileHotelTourData = getApplicationContext()
							.getFileStreamPath("hotel_tourism_data.xml");
					fileHotelTourData.delete();
				}

				File fileHotelTourData = getApplicationContext()
						.getFileStreamPath("hotel_tourism_data_new.xml");
				fileHotelTourData.renameTo(new File(getApplicationContext()
						.getFilesDir() + "hotel_tourism_data.xml"));

				if (BuildConfig.DEBUG) {
					Log.i(LOGTAG, "hotel_tourism_data MD5CHECKSUM mismatch, latest copy found!");
				}
				
				return true;
			}
			
			return false;
		}
		
		
		
	
		@Override
		protected Void doInBackground(Void... params) {
						
			downloadXmlFromWeb();
			if (checkXmlAnUpdate()) {

				final HotelDatabase db = new HotelDatabase(
						getApplicationContext());
				db.open();
				//database1flag = true;

				try {

					SAXParserFactory factory = SAXParserFactory.newInstance();
					SAXParser saxParser = factory.newSAXParser();

					DefaultHandler handler = new DefaultHandler() {

						public void startElement(String uri, String localName,
								String qName, Attributes attributes)
								throws SAXException {

							System.out.println("Start Element :" + qName);

							if (qName.contains("ROW")) {

								HotelToursEntity.rowid = qName;

							}

							//if (database1flag) {
								if (qName.equalsIgnoreCase("HOTEL_NAME")) {
									database1.bhotelname = true;
								}

								if (qName.equalsIgnoreCase("ADDRESS")) {
									database1.baddress = true;
								}

								if (qName.equalsIgnoreCase("STATE")) {
									database1.bstate = true;
								}

								if (qName.equalsIgnoreCase("PHONE")) {
									database1.bphone = true;
								}

								if (qName.equalsIgnoreCase("FAX")) {
									database1.bfax = true;
								}

								if (qName.equalsIgnoreCase("EMAIL_ID")) {
									database1.bemail_id = true;
								}

								if (qName.equalsIgnoreCase("WEBSITE")) {
									database1.bwebsite = true;
								}

								if (qName.equalsIgnoreCase("TYPE")) {
									database1.btype = true;
								}

								if (qName.equalsIgnoreCase("ROOMS")) {
									database1.brooms = true;
								}
							//}
							/*
							 * if (database2flag) {
							 * 
							 * if (qName.equalsIgnoreCase("NAME_OF_AGENCY")) {
							 * database2.bnameofagency = true; }
							 * 
							 * if (qName.equalsIgnoreCase("ADDRESS")) {
							 * database2.baddress = true; }
							 * 
							 * if (qName.equalsIgnoreCase("PHONE")) {
							 * database2.bphone = true; }
							 * 
							 * if (qName.equalsIgnoreCase("FAX")) {
							 * database2.bfax = true; }
							 * 
							 * if (qName.equalsIgnoreCase("EMAIL")) {
							 * database2.bemail = true; }
							 * 
							 * if (qName.equalsIgnoreCase("REGION")) {
							 * database2.bregion = true; }
							 * 
							 * if (qName.equalsIgnoreCase("CITY")) {
							 * database2.bcity = true; }
							 * 
							 * if (qName.equalsIgnoreCase("STATE")) {
							 * database2.bstate = true; }
							 * 
							 * if (qName.equalsIgnoreCase("CONTACT_PERSON")) {
							 * database2.bcontactperson = true; }
							 * 
							 * if (qName.equalsIgnoreCase("TYPE")) {
							 * database2.btype = true; }
							 * 
							 * }
							 */

						}

						public void endElement(String uri, String localName,
								String qName) throws SAXException {

							System.out.println("End Element :" + qName);

							if (HotelToursEntity.rowid.equals(qName)) {

								if (BuildConfig.DEBUG) {
									Log.v(LOGTAG, "Its time to update...");
								}

								long id = db.insertTourismHotelDetails(
										HotelToursEntity.rowid,
										HotelToursEntity.hotelname,
										HotelToursEntity.address,
										HotelToursEntity.state.trim(),
										HotelToursEntity.phone,
										HotelToursEntity.fax,
										HotelToursEntity.email_id,
										HotelToursEntity.website,
										HotelToursEntity.type,
										HotelToursEntity.rooms);

								if (BuildConfig.DEBUG) {
									Log.v(LOGTAG, new Long(id).toString());
								}
							}

						}

						public void characters(char ch[], int start, int length)
								throws SAXException {

							//if (database1flag) {
								if (database1.bhotelname) {

									HotelToursEntity.hotelname = new String(ch,
											start, length);
									System.out.println("HOTEL_NAME : "
											+ HotelToursEntity.hotelname);
									database1.bhotelname = false;
								}

								if (database1.baddress) {

									HotelToursEntity.address = new String(ch,
											start, length);
									System.out.println("ADDRESS : "
											+ HotelToursEntity.address);
									database1.baddress = false;
								}

								if (database1.bstate) {
									HotelToursEntity.state = new String(ch,
											start, length);
									System.out.println("STATE : "
											+ HotelToursEntity.state);
									database1.bstate = false;
								}

								if (database1.bphone) {
									HotelToursEntity.phone = new String(ch,
											start, length);
									System.out.println("PHONE : "
											+ HotelToursEntity.phone);
									database1.bphone = false;
								}

								if (database1.bfax) {

									HotelToursEntity.fax = new String(ch,
											start, length);
									System.out.println("FAX : "
											+ HotelToursEntity.fax);
									database1.bfax = false;
								}

								if (database1.bwebsite) {

									HotelToursEntity.website = new String(ch,
											start, length);
									System.out.println("WEBSITE : "
											+ HotelToursEntity.website);
									database1.bwebsite = false;
								}

								if (database1.bemail_id) {

									HotelToursEntity.email_id = new String(ch,
											start, length);
									System.out.println("EMAIL_ID : "
											+ HotelToursEntity.email_id);
									database1.bemail_id = false;
								}

								if (database1.btype) {
									HotelToursEntity.type = new String(ch,
											start, length);
									System.out.println("TYPE : "
											+ HotelToursEntity.type);
									database1.btype = false;
								}

								if (database1.brooms) {
									HotelToursEntity.rooms = new String(ch,
											start, length);
									System.out.println("ROOMS : "
											+ HotelToursEntity.rooms);
									database1.brooms = false;
								}

							}
							/*
							 * if (database2flag) { if (database2.bnameofagency)
							 * { System.out.println("NAME_OF_AGENCY : " + new
							 * String(ch, start, length));
							 * database2.bnameofagency = false; }
							 * 
							 * if (database2.baddress) {
							 * System.out.println("ADDRESS : " + new String(ch,
							 * start, length)); database2.baddress = false; }
							 * 
							 * if (database2.bphone) {
							 * System.out.println("PHONE : " + new String(ch,
							 * start, length)); database2.bphone = false; }
							 * 
							 * if (database2.bfax) { System.out.println("FAX : "
							 * + new String(ch, start, length)); database2.bfax
							 * = false; }
							 * 
							 * if (database2.bemail) {
							 * System.out.println("EMAIL : " + new String(ch,
							 * start, length)); database2.bemail = false; }
							 * 
							 * if (database2.bregion) {
							 * System.out.println("REGION : " + new String(ch,
							 * start, length)); database2.bregion = false; }
							 * 
							 * if (database2.bcity) {
							 * System.out.println("CITY : " + new String(ch,
							 * start, length)); database2.bcity = false; }
							 * 
							 * if (database2.bstate) {
							 * System.out.println("STATE : " + new String(ch,
							 * start, length)); database2.bstate = false; }
							 * 
							 * if (database2.bcontactperson) {
							 * System.out.println("CONTACT_PERSON : " + new
							 * String(ch, start, length));
							 * database2.bcontactperson = false; }
							 * 
							 * if (database2.btype) {
							 * System.out.println("TYPE : " + new String(ch,
							 * start, length)); database2.btype = false; }
							 * 
							 * }
							 */

						//}

					};

					// final String SDCARDLOC =
					// Environment.getExternalStorageDirectory()
					// .getAbsolutePath() + "/";

					File fileHotelTourData = getApplicationContext()
							.getFileStreamPath("hotel_tourism_data.xml");

					// if hotel_tourism_data.xml doesn't exist then
					// copy raw/hotel_tourism_data.xml to files directory
					if (!fileHotelTourData.exists()) {
						Utility.resourceRestore(getApplicationContext(),
								"hotel_tourism_data.xml",
								R.raw.hotel_tourism_data);
					}

					//if (database1flag) {
						BufferedReader bufReader = new BufferedReader(
								new FileReader(fileHotelTourData));
						InputSource inSource = new InputSource(bufReader);
						saxParser.parse((inSource), handler);
						bufReader.close();
					//}

					/*
					 * if (database1flag) saxParser.parse(new InputSource(new
					 * BufferedReader( new FileReader(SDCARDLOC +
					 * "HotelTourismData.xml"))), handler);
					 */

					db.close();
					Toast.makeText(getApplicationContext(),
							"Database Synchronization complete",
							Toast.LENGTH_SHORT).show();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return null;
		}

	}
	

	public void onBackPressed() {

		finish();

	}
	
	
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
	      case ALERT_DIALOG_OPTIONS_ID_NONETWORK:
			return NetworkStatusDialog();

		default:
			return null;
		}
	}
	
	
	
	private void openAlertOptionsDialog(int id) {
		showDialog(id);
	}
	
	
	/**
	 * Description : Opens Create New File Dialog
	 */
	private void openNetworkStatusDialog() {
		openAlertOptionsDialog(ALERT_DIALOG_OPTIONS_ID_NONETWORK);
	}
		
	
	/**
	 * Description : Displays Create new file dialog
	 * 
	 * @return AlertDialog
	 */

	private AlertDialog NetworkStatusDialog() {

		return new AlertDialog.Builder(this).setTitle("Network Status").setMessage("Network not available.Please turn on your network.")
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int arg1) {
				// TODO Auto-generated method stub
				dialog.cancel();
				finish(); // finishing the activity
			}

		}).setCancelable(false).create();
	}
}
