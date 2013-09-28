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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class Utility {

	final String LOGTAG = "Utility";

	public static boolean saveUrlAsFile(String url, String filename) {

		try {
			// TODO (aup): Improve the exception handling. This is cruel.
			Log.v("Flite.Utility", "Trying to save " + url + " as " + filename);
			URL u = new URL(url);
			URLConnection uc = u.openConnection();
			int contentLength = uc.getContentLength();

			InputStream raw = uc.getInputStream();
			InputStream in = new BufferedInputStream(raw, 8000);
			byte[] data = new byte[contentLength];
			int bytesRead = 0;
			int offset = 0;
			while (offset < contentLength) {
				bytesRead = in.read(data, offset, data.length - offset);
				if (bytesRead == -1)
					break;
				offset += bytesRead;
			}
			in.close();

			if (offset != contentLength) {
				throw new IOException("Only read " + offset
						+ " bytes; Expected " + contentLength + " bytes");
			}

			FileOutputStream out = new FileOutputStream(filename);
			out.write(data);
			out.flush();
			out.close();
			return true;
		} catch (Exception e) {
			Log.e("Flite Utility",
					"Could not save url as file.: " + e.getMessage());
			return false;
		}
	}

	public static boolean pathExists(String pathname) {
		File tempFile = new File(pathname);
		if ((!tempFile.exists())) {
			return false;
		}
		return true;
	}

	/**
	 * TODO Url when fetched gives negative content length
	 * 
	 * @param link
	 * @param filename
	 */
	static void stub(Context context, String link, String fileName) {

		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(X509Certificate[] certs,
					String authType) {
			}

			public void checkServerTrusted(X509Certificate[] certs,
					String authType) {
			}
		} };
		// Install the all-trusting trust manager
		SSLContext sc;
		try {
			sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection
					.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Create all-trusting host name verifier
		HostnameVerifier allHostsValid = new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};

		// Install the all-trusting host verifier
		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

		URL url;
		try {
			url = new URL(link);
			URLConnection urlConnection = url.openConnection();

			//int l = urlConnection.getContentLength();
			
						
			final Reader reader = new InputStreamReader(
					urlConnection.getInputStream());
			final BufferedReader br = new BufferedReader(reader);

			FileOutputStream out = context.openFileOutput(fileName,
					Context.MODE_PRIVATE);

			//FileInputStream fin = context.openFileInput(fileName);

			//BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));

			String line = "";

			while ((line = br.readLine()) != null) {
				out.write(line.getBytes());
			}
			
			br.close();
			out.close();

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// copyInputStreamToOutputStream(in, System.out);
	}

	/**
	 * TODO Url when fetched gives negative content length
	 * 
	 * @param appContext
	 * @param link
	 * @param filename
	 */
	/*
	 * static void stub2(Context appContext, String link, String filename){
	 * 
	 * 
	 * // Load CAs from an InputStream // (could be from a resource or
	 * ByteArrayInputStream or ...) CertificateFactory cf = null; try { cf =
	 * CertificateFactory.getInstance("X.509"); } catch (CertificateException e)
	 * { // TODO Auto-generated catch block e.printStackTrace(); } // From
	 * https://www.washington.edu/itconnect/security/ca/load-der.crt InputStream
	 * caInput = new
	 * BufferedInputStream(appContext.getResources().openRawResource
	 * (R.raw.hotel)); java.security.cert.Certificate ca = null; try { ca =
	 * cf.generateCertificate(caInput); System.out.println("ca=" +
	 * ((X509Certificate) ca).getSubjectDN()); } catch (CertificateException e)
	 * { // TODO Auto-generated catch block e.printStackTrace(); } finally { try
	 * { caInput.close(); } catch (IOException e) { // TODO Auto-generated catch
	 * block e.printStackTrace(); } }
	 * 
	 * // Create a KeyStore containing our trusted CAs String keyStoreType =
	 * KeyStore.getDefaultType(); KeyStore keyStore = null; try { keyStore =
	 * KeyStore.getInstance(keyStoreType); keyStore.load(null, null);
	 * keyStore.setCertificateEntry("ca", ca); } catch (KeyStoreException e) {
	 * // TODO Auto-generated catch block e.printStackTrace(); } catch
	 * (NoSuchAlgorithmException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } catch (CertificateException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); } catch (IOException e) {
	 * // TODO Auto-generated catch block e.printStackTrace(); }
	 * 
	 * 
	 * // Create a TrustManager that trusts the CAs in our KeyStore String
	 * tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
	 * TrustManagerFactory tmf = null; try { tmf =
	 * TrustManagerFactory.getInstance(tmfAlgorithm); tmf.init(keyStore); }
	 * catch (NoSuchAlgorithmException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } catch (KeyStoreException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); }
	 * 
	 * 
	 * // Create an SSLContext that uses our TrustManager SSLContext context =
	 * null; try { context = SSLContext.getInstance("TLS"); context.init(null,
	 * tmf.getTrustManagers(), null); context.init(null, tmf.getTrustManagers(),
	 * null); } catch (NoSuchAlgorithmException e) { // TODO Auto-generated
	 * catch block e.printStackTrace(); } catch (KeyManagementException e) { //
	 * TODO Auto-generated catch block e.printStackTrace(); }
	 * 
	 * 
	 * // Tell the URLConnection to use a SocketFactory from our SSLContext URL
	 * url; try { /* URL u = new URL(link); URLConnection urlConnection =
	 * u.openConnection(); int contentLength = urlConnection.getContentLength();
	 * InputStream in = urlConnection.getInputStream();
	 */
	/*
	 * url = new URL(link); HttpsURLConnection urlConnection =
	 * (HttpsURLConnection)url.openConnection();
	 * 
	 * urlConnection.setSSLSocketFactory(context.getSocketFactory());
	 * 
	 * InputStream in = urlConnection.getInputStream(); int contentLength =
	 * urlConnection.getContentLength();
	 * 
	 * if(contentLength == -1){
	 * 
	 * Utility utility = new Utility(); throw utility.new
	 * NegativeContentLengthException(); }
	 * 
	 * byte[] data = new byte[contentLength]; int bytesRead = 0; int offset = 0;
	 * while (offset < contentLength) { bytesRead = in.read(data, offset,
	 * data.length - offset); if (bytesRead == -1) break; offset += bytesRead; }
	 * in.close();
	 * 
	 * if (offset != contentLength) { throw new IOException("Only read " +
	 * offset + " bytes; Expected " + contentLength + " bytes"); }
	 * 
	 * FileOutputStream out = new FileOutputStream(filename); out.write(data);
	 * out.flush(); out.close();
	 * 
	 * } catch (MalformedURLException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } catch (IOException e) { // TODO Auto-generated
	 * catch block e.printStackTrace(); } catch (NegativeContentLengthException
	 * e) { // TODO Auto-generated catch block e.printStackTrace(); }
	 * 
	 * //copyInputStreamToOutputStream(in, System.out); }
	 */

	class NegativeContentLengthException extends Exception {

		NegativeContentLengthException() {
			super();
			Log.i(LOGTAG, "url content stream length negative");
		}

	}

	public static String md5CheckSum(Context context, String fileName) {
		int nread = 0;
		MessageDigest md;
		StringBuffer sb = null;
		try {
			md = MessageDigest.getInstance("MD5");
			FileInputStream fiss = context.openFileInput(fileName);
			byte[] dataBytes = new byte[1024];
			nread = 0;

			while ((nread = fiss.read(dataBytes)) != -1) {
				md.update(dataBytes, 0, nread);
			}
			;
			byte[] mdbytes = md.digest();

			// convert the byte to hex format method 1
			sb = new StringBuffer();
			for (int i = 0; i < mdbytes.length; i++) {
				sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16)
						.substring(1));
			}

			System.out.println("Digest(in hex format):: " + sb.toString());

			// convert the byte to hex format method 2
			/*
			 * StringBuffer hexString = new StringBuffer(); for (int i = 0; i <
			 * mdbytes.length; i++) { String hex = Integer.toHexString(0xff &
			 * mdbytes[i]); if (hex.length() == 1) hexString.append('0');
			 * hexString.append(hex); }
			 * System.out.println("Digest(in hex format):: " +
			 * hexString.toString());
			 */

		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sb.toString();

	}

	private void writeFileToInternalStorage(Context context, String fileName) {
		String eol = System.getProperty("line.separator");
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					context.openFileOutput(fileName, Context.MODE_PRIVATE)));
			writer.write("This is a test1." + eol);
			writer.write("This is a test2." + eol);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void readFileFromInternalStorage(Context context, String fileName) {
		String eol = System.getProperty("line.separator");
		BufferedReader input = null;
		try {
			input = new BufferedReader(new InputStreamReader(
					context.openFileInput(fileName)));
			String line;
			StringBuffer buffer = new StringBuffer();
			while ((line = input.readLine()) != null) {
				buffer.append(line + eol);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Description : Copies the map files into the app directory
	 * 
	 * @param file
	 *            File object to be copied
	 * @param resId
	 *            ID of the resource
	 * 
	 */
	public static void resourceRestore(Context context, String fileName,
			int resId) {

		int cBuffer;

		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;

		try {
			/*
			bis = new BufferedInputStream(context.getResources()
					.openRawResource(resId));
			bos = new BufferedOutputStream(context.openFileOutput(fileName,
					Context.MODE_WORLD_READABLE));

			while ((cBuffer = bis.read()) != -1) {
				bos.write(cBuffer);*/
			
			
			
			final Reader reader = new InputStreamReader(
					context.getResources()
					.openRawResource(resId));
			final BufferedReader br = new BufferedReader(reader);

			FileOutputStream out = context.openFileOutput(fileName,
					Context.MODE_PRIVATE);
			
			String line = "";

			while ((line = br.readLine()) != null) {
				out.write(line.getBytes());
			}
			
			br.close();
			out.close();
						
		} catch (FileNotFoundException e) {
			Log.e("Utility.resourceRestore",
					"FileNotFoundException when restoring *.xml files");
			e.printStackTrace();
		} catch (IOException e) {
			Log.e("Utility.resourceRestore",
					"IOException when restoring *.xml files");
			e.printStackTrace();
		} finally {
			/*try {
				//bis.close();
				//bos.close();
			} catch (IOException e) {
				// e.printStackTrace();
			}*/
		}
	}
	
	/**
	 * Checks whether network is available, returns true if network is available 
	 * @return
	 */
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
}
