package org.rti.rcd.ict.lgug.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *  Code extracted from Dale Harvey's MobileFuton: https://github.com/daleharvey/Android-MobileFuton
 *
 */

public class HTTPRequest {

	public String[][] headers;
	public JSONObject json;
	public String result;
	public int status;

	public HTTPRequest(String[][] headers, JSONObject json, String result,
			int status) {
		this.headers = headers;
		this.json = json;
		this.result = result;
		this.status = status;
	}

	public static HTTPRequest post(String url, String data) throws JSONException, ConnectException {
		return post(url, data, new String[][]{});
	}

	public static HTTPRequest post(String url, String data, String[][] headers)
			throws JSONException, ConnectException {
		return HTTPRequest.httpRequest("POST", url, data, headers);
	}

	public static HTTPRequest put(String url, String data) throws JSONException, ConnectException {
		return put(url, data, new String[][]{});
	}

	public static HTTPRequest put(String url, String data, String[][] headers)
			throws JSONException, ConnectException {
		return HTTPRequest.httpRequest("PUT", url, data, headers);
	}

	public static HTTPRequest get(String url) throws JSONException, ConnectException {
		return get(url, new String[][] {});
	}

	public static HTTPRequest get(String url, String[][] headers)
			throws JSONException, ConnectException {
		return HTTPRequest.httpRequest("GET", url, null, headers);
	}

	public static HTTPRequest httpRequest(String method, String url,
			String data, String[][] headers) throws JSONException, ConnectException {

		StringBuffer sb = new StringBuffer();
		int statusCode = 0;

		try {

			HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
			String charEncoding = "iso-8859-1";

			c.setDoOutput(true);
			c.setUseCaches(false);

			c.setRequestMethod(method);
			c.setRequestProperty("Content-type", "application/json; charset="
					+ "UTF-8");

			for (String[] tmp : headers) {
				c.setRequestProperty(tmp[0], tmp[1]);
			}

			if (method != "GET" && data != null) {
				c.setDoInput(true);
				c.setRequestProperty("Content-Length",
						Integer.toString(data.length()));
				c.getOutputStream().write(data.getBytes(charEncoding));
			}

			c.connect();

			statusCode = c.getResponseCode();

			// TODO: Nasty
			try {
				BufferedReader rd = new BufferedReader(new InputStreamReader(
						c.getInputStream()));
				String line;
				while ((line = rd.readLine()) != null) {
					sb.append(line);
				}
				rd.close();
			} catch (FileNotFoundException e) {

			} catch (NullPointerException e) {

			}

			finally {
				c.disconnect();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		JSONObject json= sb.toString().length() == 0
			? new JSONObject()
			: new JSONObject(sb.toString());

		return new HTTPRequest(headers, json, sb.toString(), statusCode);
	};

	@Override
	public String toString() {
		return "HTTPResult -> status: " + Integer.toString(status);
	}
}
