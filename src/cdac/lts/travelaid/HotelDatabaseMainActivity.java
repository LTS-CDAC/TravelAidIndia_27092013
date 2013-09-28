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
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import cdac.lts.govdatabase.HotelDatabase;

import cdac.lts.shakedetection.ShakeDetection;

import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Toast;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.util.Linkify;

public class HotelDatabaseMainActivity extends Activity implements
		OnInitListener {

	final String LOGTAG = "HotelDatabaseMainActivity";
	ImageButton buttonSpeak;

	// Default TTS engine
	public TextToSpeech textSpeech;
	boolean ttsFlag = false;

	ProgressDialog loadingFileProgress;
	
	final int ALERT_DIALOG_OPTIONS_ID_NOQUERYRESULT = 1;
	final int TTS_DATA_CHECK_CODE = 2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hotel_database_main);

		buttonSpeak = (ImageButton) findViewById(R.id.button_speak);
		buttonSpeak.setOnClickListener(speakContentListener);

		checkVoiceRecognition();

		SharedPreferences SP = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		if (SP.getBoolean("key_preference_read_search_results", false)) {
			// TODO should be activated based on shared preferences
			ttsFlag = true;
			initializeTTS();
		}

		if (SP.getBoolean("Key_preference_stop_tts_on_shake", false)) {
			ShakeDetection sDetection = new ShakeDetection(textSpeech);
			sDetection.registerShakeSensor(getApplicationContext());
		}

		if (!getSharedPreferences(HOTEL_DATABASE_PREF, 0).getBoolean(
				"first_start_flag", false)) {
			new SynchronizeDatabase().execute();
			activateFirstStart();
		} else {
			initialize();
		}

	}

	/**
	 * Synchronize app database with website XML
	 */
	private class SynchronizeDatabase extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPostExecute(Void result) {
			loadingFileProgress.cancel();
			initialize();
			super.onPostExecute(result);
			try {
				this.finalize();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		protected void onPreExecute() {
			loadingFileProgress = ProgressDialog
					.show(HotelDatabaseMainActivity.this, "",
							"Please wait..\nDatabase update on first start under progress..");
			super.onPreExecute();
		}

/*		void downloadXmlFromWeb() {
			String url = "https://datacms.nic.in/datatool/?url=http://data.gov.in//sites/default/files/HotelTourismData.xls&format=xml";
			Utility.stub(getApplicationContext(), url, "HotelTourismData.xml");
		}*/

		@Override
		protected Void doInBackground(Void... params) {
			
			final HotelDatabase db = new HotelDatabase(getApplicationContext());
			db.open();

			database1flag = true;

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

						if (database1flag) {
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
						}
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
						 * if (qName.equalsIgnoreCase("FAX")) { database2.bfax =
						 * true; }
						 * 
						 * if (qName.equalsIgnoreCase("EMAIL")) {
						 * database2.bemail = true; }
						 * 
						 * if (qName.equalsIgnoreCase("REGION")) {
						 * database2.bregion = true; }
						 * 
						 * if (qName.equalsIgnoreCase("CITY")) { database2.bcity
						 * = true; }
						 * 
						 * if (qName.equalsIgnoreCase("STATE")) {
						 * database2.bstate = true; }
						 * 
						 * if (qName.equalsIgnoreCase("CONTACT_PERSON")) {
						 * database2.bcontactperson = true; }
						 * 
						 * if (qName.equalsIgnoreCase("TYPE")) { database2.btype
						 * = true; }
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

						if (database1flag) {
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
								HotelToursEntity.state = new String(ch, start,
										length);
								System.out.println("STATE : "
										+ HotelToursEntity.state);
								database1.bstate = false;
							}

							if (database1.bphone) {
								HotelToursEntity.phone = new String(ch, start,
										length);
								System.out.println("PHONE : "
										+ HotelToursEntity.phone);
								database1.bphone = false;
							}

							if (database1.bfax) {

								HotelToursEntity.fax = new String(ch, start,
										length);
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
								HotelToursEntity.type = new String(ch, start,
										length);
								System.out.println("TYPE : "
										+ HotelToursEntity.type);
								database1.btype = false;
							}

							if (database1.brooms) {
								HotelToursEntity.rooms = new String(ch, start,
										length);
								System.out.println("ROOMS : "
										+ HotelToursEntity.rooms);
								database1.brooms = false;
							}

						}
						/*
						 * if (database2flag) { if (database2.bnameofagency) {
						 * System.out.println("NAME_OF_AGENCY : " + new
						 * String(ch, start, length)); database2.bnameofagency =
						 * false; }
						 * 
						 * if (database2.baddress) {
						 * System.out.println("ADDRESS : " + new String(ch,
						 * start, length)); database2.baddress = false; }
						 * 
						 * if (database2.bphone) { System.out.println("PHONE : "
						 * + new String(ch, start, length)); database2.bphone =
						 * false; }
						 * 
						 * if (database2.bfax) { System.out.println("FAX : " +
						 * new String(ch, start, length)); database2.bfax =
						 * false; }
						 * 
						 * if (database2.bemail) { System.out.println("EMAIL : "
						 * + new String(ch, start, length)); database2.bemail =
						 * false; }
						 * 
						 * if (database2.bregion) {
						 * System.out.println("REGION : " + new String(ch,
						 * start, length)); database2.bregion = false; }
						 * 
						 * if (database2.bcity) { System.out.println("CITY : " +
						 * new String(ch, start, length)); database2.bcity =
						 * false; }
						 * 
						 * if (database2.bstate) { System.out.println("STATE : "
						 * + new String(ch, start, length)); database2.bstate =
						 * false; }
						 * 
						 * if (database2.bcontactperson) {
						 * System.out.println("CONTACT_PERSON : " + new
						 * String(ch, start, length)); database2.bcontactperson
						 * = false; }
						 * 
						 * if (database2.btype) { System.out.println("TYPE : " +
						 * new String(ch, start, length)); database2.btype =
						 * false; }
						 * 
						 * }
						 */

					}

				};

				// final String SDCARDLOC = Environment
				// .getExternalStorageDirectory().getAbsolutePath() + "/";

				File fileHotelTourData = getApplicationContext()
						.getFileStreamPath("hotel_tourism_data.xml");

				// if hotel_tourism_data.xml doesn't exist then
				// copy raw/hotel_tourism_data.xml to files directory
				if (!fileHotelTourData.exists()) {
					Utility.resourceRestore(getApplicationContext(),
							"hotel_tourism_data.xml", R.raw.hotel_tourism_data);
				}

				if (database1flag){
					BufferedReader bufReader = new BufferedReader(new FileReader(fileHotelTourData));
					InputSource inSource = new InputSource(bufReader);
					saxParser.parse((inSource), handler);
					bufReader.close();
				}
				/*
				 * if (database2flag) saxParser.parse(new InputSource(new
				 * BufferedReader( new FileReader(SDCARDLOC +
				 * "TravelTourData.xml"))), handler);
				 */

				db.close();
				Toast.makeText(getApplicationContext(),
						"Database Synchronization complete", Toast.LENGTH_SHORT)
						.show();

			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}

	}

	final String HOTEL_DATABASE_PREF = "hotel_database_pref";

	private void activateFirstStart() {
		SharedPreferences settings = getSharedPreferences(HOTEL_DATABASE_PREF,
				0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("first_start_flag", true);
		editor.commit();
	}

	private void initializeTTS() {
		textSpeech = new TextToSpeech(getApplicationContext(), this);
		Intent findIntent = new Intent();
		findIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(findIntent, TTS_DATA_CHECK_CODE);
	}

	private OnClickListener speakContentListener = new OnClickListener() {

		public void onClick(View v) {

			speak();
		}

	};

	public void checkVoiceRecognition() {
		// Check if voice recognition is present
		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
				RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() == 0) {
			buttonSpeak.setEnabled(false);
			// buttonSpeak.setText("Voice recognizer not present");
			Toast.makeText(this, "Voice recognizer not present",
					Toast.LENGTH_SHORT).show();
		}
	}

	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1001;

	public void speak() {

		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

		// Specify the calling package to identify your application
		intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass()
				.getPackage().getName());

		// Display an hint to the user about what he should say.
		// intent.putExtra(RecognizerIntent.EXTRA_PROMPT, metTextHint.getText()
		// .toString());

		// Given an hint to the recognizer about what the user is going to say
		// There are two form of language model available
		// 1.LANGUAGE_MODEL_WEB_SEARCH : For short phrases
		// 2.LANGUAGE_MODEL_FREE_FORM : If not sure about the words or phrases
		// and its domain.
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);

		// If number of Matches is not selected then return show toast message
		/*
		 * if (msTextMatches.getSelectedItemPosition() ==
		 * AdapterView.INVALID_POSITION) { Toast.makeText(this,
		 * "Please select No. of Matches from spinner",
		 * Toast.LENGTH_SHORT).show(); return; }
		 */

		/*
		 * int noOfMatches = Integer.parseInt(msTextMatches.getSelectedItem()
		 * .toString());
		 */
		int noOfMatches = 1;

		// Specify how many results you want to receive. The results will be
		// sorted where the first result is the one with higher confidence.
		intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, noOfMatches);
		// Start the Voice recognizer activity for the result.
		startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
	}

	/**
	 * Description :
	 * 
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {

		case VOICE_RECOGNITION_REQUEST_CODE: {

			// If Voice recognition is successful then it returns RESULT_OK
			if (resultCode == RESULT_OK && data != null) {

				ArrayList<String> textMatchList = data
						.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

				if (!textMatchList.isEmpty()) {
					// If first Match contains the 'search' word
					// Then start web search.
					if (textMatchList.get(0).contains("search")) {

						String searchQuery = textMatchList.get(0);
						searchQuery = searchQuery.replace("search", "");
						Intent search = new Intent(Intent.ACTION_WEB_SEARCH);
						search.putExtra(SearchManager.QUERY, searchQuery);
						startActivity(search);
					} else {
						// populate the Matches
						/*
						 * mlvTextMatches.setAdapter(new ArrayAdapter<String>(
						 * this, android.R.layout.simple_list_item_1,
						 * textMatchList));
						 */
						AutoCompleteTextView editText = (AutoCompleteTextView) findViewById(R.id.autocompletetextview);
						editText.setText(textMatchList.get(0));
					}

				}
				// Result code for various error.
			} else if (resultCode == RecognizerIntent.RESULT_AUDIO_ERROR) {
				showToastMessage("Audio Error");
			} else if (resultCode == RecognizerIntent.RESULT_CLIENT_ERROR) {
				showToastMessage("Client Error");
			} else if (resultCode == RecognizerIntent.RESULT_NETWORK_ERROR) {
				showToastMessage("Network Error");
			} else if (resultCode == RecognizerIntent.RESULT_NO_MATCH) {
				showToastMessage("No Match");
			} else if (resultCode == RecognizerIntent.RESULT_SERVER_ERROR) {
				showToastMessage("Server Error");
			}

			break;
		}
		
        case TTS_DATA_CHECK_CODE:{
			
			if (requestCode == TTS_DATA_CHECK_CODE) {
				if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {

					textSpeech = new TextToSpeech(getApplicationContext(), this);

				} else {

					Intent installIntent = new Intent();
					installIntent
							.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
					startActivity(installIntent);
				}
			}
			
			
		}
		
		}
	}

	/**
	 * Helper method to show the toast message
	 **/
	void showToastMessage(String message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_hotel_database_main, menu);
		return true;
	}
	*/

	static Boolean database1flag = false;
	static Boolean database2flag = false;

	static class database1 {
		static boolean bhotelname = false;
		static boolean baddress = false;
		static boolean bstate = false;
		static boolean bphone = false;
		static boolean bfax = false;
		static boolean bemail_id = false;
		static boolean bwebsite = false;
		static boolean btype = false;
		static boolean brooms = false;
	}

	static class database2 {
		static boolean bnameofagency = false;
		static boolean baddress = false;
		static boolean bphone = false;
		static boolean bfax = false;
		static boolean bemail = false;
		static boolean bregion = false;
		static boolean bcity = false;
		static boolean bstate = false;
		static boolean bcontactperson = false;
		static boolean btype = false;

	}

	static class HotelToursEntity {
		public static String rowid;
		public static String hotelname;
		public static String address;
		public static String state;
		public static String phone;
		public static String fax;
		public static String email_id;
		public static String website;
		public static String type;
		public static String rooms;
	}

	static class TravelToursEntity {
		public static String nameofagency;
		public static String address;
		public static String phone;
		public static String fax;
		public static String email;
		public static String region;
		public static String city;
		public static String state;
		public static String contactperson;
		public static String type;
	}

	AutoCompleteTextView textView;

	/**
	 * Initializes autosuggestion list
	 */
	private void initialize() {

		HotelDatabase db = new HotelDatabase(getApplicationContext());
		db.open();

		Cursor c = db.getTourismHotelState();
		String[] STATES = new String[c.getCount()];

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, STATES);

		if (c.moveToFirst()) {
			int i = 0;
			do {

				STATES[i++] = c.getString(0);

			} while (c.moveToNext());
		} else
			Toast.makeText(this, "No info found", Toast.LENGTH_LONG).show();
		db.close();

		textView = (AutoCompleteTextView) findViewById(R.id.autocompletetextview);
		textView.setAdapter(adapter);

		textView.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub

				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					switch (keyCode) {
					case KeyEvent.KEYCODE_DPAD_CENTER:
					case KeyEvent.KEYCODE_ENTER:
						if (textSpeech != null) {
							if (-1 == textSpeech.stop()) {
								Log.i(LOGTAG, "stopping failed");
							}

						}
						new GetFromDatabase().execute();

						return true;
					}
				}
				return false;
			}
		});
	}

	private class GetFromDatabase extends AsyncTask<Void, Void, Void> {

		Cursor c;

		@Override
		protected void onPostExecute(Void result) {
			formatDataInWebview(c);
			loadingFileProgress.cancel();
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			loadingFileProgress = ProgressDialog.show(
					HotelDatabaseMainActivity.this, "", "Please wait..");
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			HotelDatabase db = new HotelDatabase(getApplicationContext());
			db.open();
			c = db.getTourismHotelDetail(textView.getText().toString().toUpperCase().trim());
			db.close();
			return null;
		}

	}

	private void formatDataInWebview(Cursor c) {

		WebView webview = (WebView) findViewById(R.id.webView1);

		String HEADER_OPEN = "<html><body>";
		String HEADER_CLOSE = "</body></html>";
		String BR = "<br>";
		String BOLD_OPEN = "<b>";
		String BOLD_CLOSE = "</b>";
		/*
		 * String A_TAG = "<a"; String TAG_CLOSE = ">"; String A_CLOSE_TAG =
		 * "</a>"; String HREF_OPEN = "href=\""; String HTTP = "http://"; String
		 * MAIL_TO = "mailto:"; String TEL_OPEN = "tel://"; String FAX_OPEN =
		 * "fax://"; String DOUBLE_QUOTES = "\"";
		 */

		// <a href="mailto:webmaster@example.com">Jon Doe</a>.<br>
		// <a href="tel://555-5555"><img src="phone.png" alt="Call Now!" /></a>
		// <a href="http://www.w3schools.com">Visit W3Schools</a>

		StringBuilder BODY = new StringBuilder();

		// ... although note that there are restrictions on what this HTML can
		// do.
		// See the JavaDocs for loadData() and loadDataWithBaseURL() for more
		// info.

		if (c.moveToFirst()) {

			do {

				BODY.append(BOLD_OPEN + "Hotel Name " + BOLD_CLOSE
						+ c.getString(1) + BR + BOLD_OPEN + "Address "
						+ BOLD_CLOSE + c.getString(2) + BR + BOLD_OPEN
						+ "STATE " + BOLD_CLOSE + c.getString(3) + BR
						+ BOLD_OPEN + "Phone " + BOLD_CLOSE + c.getString(4)
						+ BR + BOLD_OPEN + "Fax " + BOLD_CLOSE + c.getString(5)
						+ BR + BOLD_OPEN + "Email-id " + BOLD_CLOSE
						+ c.getString(6) + BR + BOLD_OPEN + "Website "
						+ BOLD_CLOSE + c.getString(7) + BR + BOLD_OPEN
						+ "Type " + BOLD_CLOSE + c.getString(8) + BR
						+ BOLD_OPEN + "Rooms " + BOLD_CLOSE + c.getString(9));
				BODY.append(BR);
				BODY.append(BR);

				if (ttsFlag) {
					talk("Hotel Name " + c.getString(1));
					talk("Address " + c.getString(2));
					talk("STATE " + c.getString(3));
					talk("Phone " + c.getString(4));
					talk("Fax " + c.getString(5));
					talk("Email-id " + c.getString(6));
					talk("Website " + c.getString(7));
					talk("Type " + c.getString(8));
					talk("Rooms " + c.getString(9));
				}

				/*
				 * BODY.append(BOLD_OPEN + "Hotel Name " +BOLD_CLOSE +
				 * c.getString(1) + BR + BOLD_OPEN + "Address " + BOLD_CLOSE +
				 * c.getString(2) + BR + BOLD_OPEN + "STATE " + BOLD_CLOSE +
				 * c.getString(3) + BR + BOLD_OPEN + A_TAG + HREF_OPEN +
				 * TEL_OPEN + c.getString(4) + DOUBLE_QUOTES + TAG_CLOSE +
				 * "Phone " + c.getString(4) + A_CLOSE_TAG + BOLD_CLOSE + BR +
				 * BOLD_OPEN + A_TAG + HREF_OPEN + FAX_OPEN + c.getString(5) +
				 * DOUBLE_QUOTES + TAG_CLOSE + "Fax " + c.getString(5) +
				 * A_CLOSE_TAG + BOLD_CLOSE + BR + BOLD_OPEN + A_TAG + HREF_OPEN
				 * + MAIL_TO + c.getString(6) + DOUBLE_QUOTES + TAG_CLOSE +
				 * "Email-id " + c.getString(6) + A_CLOSE_TAG + BOLD_CLOSE + BR
				 * + BOLD_OPEN + A_TAG + HREF_OPEN + HTTP + c.getString(7) +
				 * DOUBLE_QUOTES + TAG_CLOSE + "Website " + c.getString(7) +
				 * A_CLOSE_TAG + BOLD_CLOSE + BOLD_OPEN + "Type " + BOLD_CLOSE +
				 * c.getString(8) + BR + BOLD_OPEN + "Rooms " + BOLD_CLOSE +
				 * c.getString(9)); BODY.append(BR); BODY.append(BR);
				 * BODY.append(BOLD_OPEN+"<a href=\"tel://555-5555\">58956</a>"+
				 * BOLD_CLOSE);
				 */

			} while (c.moveToNext());

		} else{
			Toast.makeText(this, "No info found", Toast.LENGTH_LONG).show();
			openQueryStatusDialog();
		}
		String summary = HEADER_OPEN + BODY + HEADER_CLOSE;
		Spannable sp = new SpannableString(Html.fromHtml(summary));
		Linkify.addLinks(sp, Linkify.ALL);
		webview.loadData(Html.toHtml(sp), "text/html", null);

		// webview.loadData(summary, "text/html", null);
	}

	private void talk(String text) {
		textSpeech.speak(text, TextToSpeech.QUEUE_ADD, null);
	}

	public void displayContact(Cursor c) {

		if (c.moveToFirst()) {

			do {
				Toast.makeText(
						this,
						"id: " + c.getString(0) + "\n" + "HotelName:"
								+ c.getString(1) + "\n" + "Address:"
								+ c.getString(2) + "\n" + "State:"
								+ c.getString(3) + "\n" + "Phone:"
								+ c.getString(4) + "\n" + "Fax:"
								+ c.getString(5) + "\n" + "Email ID:"
								+ c.getString(6) + "\n" + "Website:"
								+ c.getString(7) + "\n" + "Type:"
								+ c.getString(8) + "\n" + "Rooms:"
								+ c.getString(9), Toast.LENGTH_LONG).show();
			} while (c.moveToNext());
		} else
			Toast.makeText(this, "No info found", Toast.LENGTH_LONG).show();

	}

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			if (BuildConfig.DEBUG)
				Log.i(LOGTAG,
						"Sucessfull intialization of Text-To-Speech engine");

		} else if (status == TextToSpeech.ERROR) {
			if (BuildConfig.DEBUG)
				Log.i(LOGTAG, "Unable to initialize Text-To-Speech engine");
		}

	}

	@Override
	public void onDestroy() {

		super.onDestroy();

		if (textSpeech != null) {
			if (-1 == textSpeech.stop()) {
				Log.i(LOGTAG, "stopping failed");
			}
			textSpeech.shutdown();
		}
	}

	
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
	      case ALERT_DIALOG_OPTIONS_ID_NOQUERYRESULT:
			return QueryStatusDialog();

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
	private void openQueryStatusDialog() {
		openAlertOptionsDialog(ALERT_DIALOG_OPTIONS_ID_NOQUERYRESULT);
	}
		
	
	/**
	 * Description : Displays Create new file dialog
	 * 
	 * @return AlertDialog
	 */

	private AlertDialog QueryStatusDialog() {

		return new AlertDialog.Builder(this).setTitle("Query Status").setMessage("No results found for this query..")
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int arg1) {
				// TODO Auto-generated method stub
				dialog.cancel();
				//finish(); // finishing the activity
			}

		}).setCancelable(false).create();
	}
		
	
}
