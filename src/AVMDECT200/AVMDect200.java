package org.ws4d.pipes.packages.fritz.server.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

/**
 * AVM Fritzbox Dect-200 Socket Api-Wrapper
 * 
 * @author Mathias Perlet
 * @version 0.0.1
 *
 */
public class AVMDect200 {
	// standard fritzbox url
	private String fritzbox_url = "fritz.box";
	// initial fritzbox password
	private String fritzbox_pw = "";
	// initial session id
	private String SID = "0000000000000";
	//path to fritz api
	private String homeswitch = "/webservices/homeautoswitch.lua";

	/**
	 * @param fritzbox_pw the frizbox password for login
	 */
	public AVMDect200(String fritzbox_pw) {
		this.fritzbox_pw = fritzbox_pw;
	}
	
	
	/**
	 * @param fritzbox_pw the frizbox password for login
	 * @param host the fritzbox url, standard is http://fritz.box
	 */
	public AVMDect200(String fritzbox_pw, String host) {
		this.fritzbox_pw = fritzbox_pw;
		this.fritzbox_url = host;
	}

	/**
	 * initial communication with the fritzbox
	 * generates a session id for further actions
	 * 
	 * @throws IOException connections errors, like host not found
	 * @throws InvalidPasswordException throws if password not valid
	 */
	public void init() throws IOException, InvalidPasswordException {
		if (!fritzbox_url.startsWith("http://"))
			this.fritzbox_url = "http://" + this.fritzbox_url;
		String base_url = fritzbox_url + "/login_sid.lua";
		String challenge = getTagValue(readURL(base_url), "Challenge");
		String response = challenge + "-" + md5(challenge + "-" + fritzbox_pw);
		SID = getTagValue(readURL(base_url + "?response=" + response), "SID");
		if (SID.equals("0000000000000000"))
			throw new InvalidPasswordException("invalid password");
	}

	/**
	 * @return the absolute url to fritzbox-api with valid session key
	 */
	private String homeauto_url_with_sid() {
		return this.fritzbox_url + this.homeswitch + "?sid=" + this.SID;
	}

	/**
	 * @param command a valid command from dect200 documentation
	 * @param devId the dect200 socket AIN (socket-id)
	 * @return api-response from fritzbox
	 * @throws IOException communication errors
	 */
	private String callCommand(String command, String devId) throws IOException {
		String url = homeauto_url_with_sid() + "&switchcmd=" + command
				+ "&ain=" + devId;
		return readURL(url);
	}

	/**
	 * @return returns a comma separated list of connected socket IDs
	 * @throws IOException
	 */
	public String getInfo() throws IOException {
		String url = homeauto_url_with_sid() + "&switchcmd=getswitchlist";
		return readURL(url);
	}

	/**
	 * @param devId the dect200 socket AIN (socket-id)
	 * @return the configured socket-name like "Living Room" 
	 * @throws IOException
	 */
	public String getSocketName(String devId) throws IOException {
		return callCommand("getswitchname", devId);
	}

	/**
	 * @param devId the dect200 socket AIN (socket-id)
	 * @return true if the socket is available
	 * @throws IOException
	 */
	public Boolean getSocketAvailability(String devId) throws IOException {
		return !callCommand("getswitchpower", devId).equals("inval");
	}

	/**
	 * @param devId the dect200 socket AIN (socket-id)
	 * @return the current power in milli watt as a integer, on error it returns the minimal integer value
	 * @throws IOException
	 */
	public int getSocketPower(String devId) {

		try {
			return Integer.parseInt(callCommand("getswitchpower", devId));
		} catch (NumberFormatException | IOException e) {
			return Integer.MIN_VALUE;
		}
	}

	/**
	 * @param devId the dect200 socket AIN (socket-id)
	 * @return the current temperature with offset in centi grad celsius (245 is 24.5°C).
	 * @return on error, it returns integer min value
	 * @throws IOException
	 */
	public int getSocketTemperature(String devId) throws IOException {
		try {
			JSONArray sockets = xmlToJson(
					callCommand("getdevicelistinfos", devId))
					.getJSONObject("devicelist").
					getJSONArray("device");

			for (int i = 0; i < sockets.length(); i++) {
				JSONObject s = sockets.getJSONObject(i);
				if(s.getString("identifier").replace(" " , "").equals(devId))
					return s.getJSONObject("temperature").getInt("celsius") + 
							s.getJSONObject("temperature").getInt("offset");
			}
		} catch (JSONException e) {
		}
		return Integer.MIN_VALUE;
	}

	/**
	 * switch a socket on
	 * 
	 * @param devId the dect200 socket AIN (socket-id)
	 * @return the success value for switch the socket on
	 * @throws IOException
	 */
	public String setSocketOn(String devId) throws IOException {
		return callCommand("setswitchon", devId);
	}

	/**
	 * switch a socket off
	 * 
	 * @param devId the dect200 socket AIN (socket-id)
	 * @return the success value for switch the socket off
	 * @throws IOException
	 */
	public String setSocketOff(String devId) throws IOException {
		return callCommand("setswitchoff", devId);
	}

	/**
	 * @param devId the dect200 socket AIN (socket-id)
	 * @return true if socket is on, false if socket if off
	 * @throws IOException
	 */
	public boolean getSocketState(String devId) throws IOException {
		String ret = callCommand("getswitchstate", devId);
		if (ret.equals("1"))
			return true;
		return false;
	}

	/**
	 * @return a ArryList of sockets
	 * @throws IOException
	 */
	public ArrayList<Socket> getSocketList() throws IOException {
		ArrayList<Socket> Sockets = new ArrayList<Socket>();
		String[] devs = getInfo().split(",");
		for (String dev : devs) {
			Sockets.add(new Socket(dev, this));
		}
		return Sockets;
	}

	/**
	 * @return current session id for fritzbox communication
	 */
	public String getSID() {
		return this.SID;
	}

	/**
	 * @param url http communication with the fritzbox-api, only http-GET
	 * @return returns the http response
	 * @throws IOException
	 */
	private String readURL(String url) throws IOException {
		URL temp_url = new URL(url);
		URLConnection yc = temp_url.openConnection();
		// set 10 seconds timeout
		yc.setConnectTimeout(10 * 1000);
		BufferedReader in = new BufferedReader(new InputStreamReader(
				yc.getInputStream()));
		return in.readLine();
	}

	/**
	 * @param xml every tag must be unique, otherwise it will return an empty string
	 * @param tagName name of the xml-tag
	 * @return returns the value from the tag as a string
	 */
	private String getTagValue(String xml, String tagName) {
		try {
			return xml.split("<" + tagName + ">")[1]
					.split("</" + tagName + ">")[0];
		} catch (ArrayIndexOutOfBoundsException e) {
			return "";
		}
	}

	/**
	 * @param md5 string to hash
	 * @return returns the md5-hash in utf-16le encoding
	 */
	private String md5(String md5) {
		try {
			java.security.MessageDigest md = java.security.MessageDigest
					.getInstance("MD5");
			byte[] tmp = null;
			try {
				tmp = md5.getBytes("UTF-16LE");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			byte[] array = md.digest(tmp);
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < array.length; ++i) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100)
						.substring(1, 3));
			}
			return sb.toString();
		} catch (java.security.NoSuchAlgorithmException e) {
		}
		return null;
	}

	/**
	 * @param callbackResponse xml string
	 * @return a json-object
	 */
	public static JSONObject xmlToJson(String callbackResponse) {
		JSONObject xmlJSONObj = new JSONObject();
		try {
			xmlJSONObj = XML.toJSONObject(callbackResponse);
		} catch (JSONException je) {
			JSONObject err = new JSONObject();
			try {
				err.put("error", je.toString());
			} catch (JSONException e) {
			}
			return err;
		}
		return xmlJSONObj;
	}

	public class Socket {
		public String name;
		public String id;
		private AVMDect200 jF;

		public Socket(String devId, AVMDect200 jF) throws IOException {
			this.id = devId;
			this.name = jF.getSocketName(this.id);
			this.jF = jF;
		}

		public String getSocketId() {
			return this.id;
		}

		public String getSocketName() {
			return this.name;
		}

		public int getSocketPower() {
			return jF.getSocketPower(this.id);
		}

		public String setSocketOn() throws IOException {
			return jF.setSocketOn(this.id);
		}

		public String setSocketOff() throws IOException {
			return jF.setSocketOff(this.id);
		}

		public String setSocketToggle() throws IOException {
			if (getSocketState()) {
				return setSocketOff();
			} else {
				return setSocketOff();
			}
		}

		public boolean getSocketState() throws IOException {
			return jF.getSocketState(this.id);
		}

		public Boolean getSocketAvailability() throws IOException {
			return jF.getSocketAvailability(this.id);
		}

		public int getSocketTemperature() throws IOException {
			return jF.getSocketTemperature(this.id);
		}

		public String toString() {
			return this.id + ":" + this.name;
		}

	}

	@SuppressWarnings("serial")
	public class InvalidPasswordException extends Exception {
		public InvalidPasswordException(String message) {
			super(message);
		}
	}
}