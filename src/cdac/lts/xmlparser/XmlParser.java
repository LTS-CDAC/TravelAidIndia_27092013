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

package cdac.lts.xmlparser;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XmlParser {

	static Boolean database1flag = false;
	static Boolean database2flag = false;

	
	 static class database1{
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
	
	static class database2{
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
	
	
	static class HotelToursEntity{
		public static String rowid;
		public static String hotelname;
		public static String address;
		public static String state ;
		public static String phone ;
		public static String fax ;
		public static String email_id ;
		public static String website ;
		public static String type;
		public static String rooms;
	}
	
	static class TravelToursEntity{
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
	
	
	
	
	
	public static void main(String argv[]) {

		database1flag = true;
		
		try {

			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();

			
			DefaultHandler handler = new DefaultHandler() {

			

				public void startElement(String uri, String localName,
						String qName, Attributes attributes)
						throws SAXException {

					System.out.println("Start Element :" + qName);

					if(qName.contains("ROW")){
						
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
					
					
				
					
					
					if (database2flag) {
						
						
						
						if (qName.equalsIgnoreCase("NAME_OF_AGENCY")) {
							database2.bnameofagency = true;
						}

						if (qName.equalsIgnoreCase("ADDRESS")) {
							database2.baddress = true;
						}

						if (qName.equalsIgnoreCase("PHONE")) {
							database2.bphone = true;
						}

						if (qName.equalsIgnoreCase("FAX")) {
							database2.bfax = true;
						}

						if (qName.equalsIgnoreCase("EMAIL")) {
							database2.bemail = true;
						}

						if (qName.equalsIgnoreCase("REGION")) {
							database2.bregion = true;
						}

						if (qName.equalsIgnoreCase("CITY")) {
							database2.bcity = true;
						}

						if (qName.equalsIgnoreCase("STATE")) {
							database2.bstate = true;
						}
						
						if (qName.equalsIgnoreCase("CONTACT_PERSON")) {
							database2.bcontactperson = true;
						}
						
						if (qName.equalsIgnoreCase("TYPE")) {
							database2.btype = true;
						}
						
					}
					
					
					
					
					

				}

				public void endElement(String uri, String localName,
						String qName) throws SAXException {

					System.out.println("End Element :" + qName);
					
					if(HotelToursEntity.rowid.equals(qName)){
						
						System.out.println("Its time to update... " + qName);
						// TODO carry out insertions in Database
						
					}
				
				}

				public void characters(char ch[], int start, int length)
						throws SAXException {

					if (database1flag) {
						if (database1.bhotelname) {
							
							HotelToursEntity.hotelname = new String(ch, start, length);
							System.out.println("HOTEL_NAME : "
									+ HotelToursEntity.hotelname);
							database1.bhotelname = false;
						}

						if (database1.baddress) {
							
							HotelToursEntity.address = new String(ch, start, length);
							System.out.println("ADDRESS : "
									+ HotelToursEntity.address);
							database1.baddress = false;
						}

						if (database1.bstate) {
							HotelToursEntity.state = new String(ch, start, length);
							System.out.println("STATE : "
									+ HotelToursEntity.state);
							database1.bstate = false;
						}

						if (database1.bphone) {
							HotelToursEntity.phone = new String(ch, start, length);
							System.out.println("PHONE : "
									+ HotelToursEntity.phone);
							database1.bphone = false;
						}

						if (database1.bfax) {
							
							HotelToursEntity.fax =  new String(ch, start, length);
							System.out.println("FAX : "
									+ HotelToursEntity.fax );
							database1.bfax = false;
						}

						if (database1.bwebsite) {
							
							HotelToursEntity.website =  new String(ch, start, length);
							System.out.println("WEBSITE : "
									+ HotelToursEntity.website);
							database1.bwebsite = false;
						}
						
                        if (database1.bemail_id) {
							
                        	HotelToursEntity.email_id =  new String(ch, start, length);
							System.out.println("EMAIL_ID : "
									+ HotelToursEntity.email_id);
							database1.bemail_id = false;
						}

						if (database1.btype) {
							HotelToursEntity.type =  new String(ch, start, length);
							System.out.println("TYPE : "
									+ HotelToursEntity.type);
							database1.btype = false;
						}

						if (database1.brooms) {
							HotelToursEntity.rooms =  new String(ch, start, length);
							System.out.println("ROOMS : "
									+ HotelToursEntity.rooms);
							database1.brooms = false;
						}

					}
					
									
					
					if (database2flag) {
						if (database2.bnameofagency) {
							System.out.println("NAME_OF_AGENCY : "
									+ new String(ch, start, length));
							database2.bnameofagency = false;
						}

						if (database2.baddress) {
							System.out.println("ADDRESS : "
									+ new String(ch, start, length));
							database2.baddress = false;
						}

						if (database2.bphone) {
							System.out.println("PHONE : "
									+ new String(ch, start, length));
							database2.bphone = false;
						}

						if (database2.bfax) {
							System.out.println("FAX : "
									+ new String(ch, start, length));
							database2.bfax = false;
						}

						if (database2.bemail) {
							System.out.println("EMAIL : "
									+ new String(ch, start, length));
							database2.bemail = false;
						}

						if (database2.bregion) {
							System.out.println("REGION : "
									+ new String(ch, start, length));
							database2.bregion = false;
						}

						if (database2.bcity) {
							System.out.println("CITY : "
									+ new String(ch, start, length));
							database2.bcity = false;
						}

						if (database2.bstate) {
							System.out.println("STATE : "
									+ new String(ch, start, length));
							database2.bstate = false;
						}
						
						if (database2.bcontactperson) {
							System.out.println("CONTACT_PERSON : "
									+ new String(ch, start, length));
							database2.bcontactperson = false;
						}
						
						if (database2.btype) {
							System.out.println("TYPE : "
									+ new String(ch, start, length));
							database2.btype = false;
						}

					}

				}

			};

			
			if(database1flag)
			 saxParser.parse("HotelTourismData.xml", handler);
			
			if(database2flag)
				 saxParser.parse("TravelTourData.xml", handler);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}