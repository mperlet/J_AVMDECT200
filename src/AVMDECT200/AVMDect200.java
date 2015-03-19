
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

public class AVMDect200 {
	private String fritzbox_url = "fritz.box";
	private String fritzbox_pw = "";
	private String SID = "0000000000000";
	private String homeswitch = "/webservices/homeautoswitch.lua";

	public AVMDect200(String fritzbox_pw) {
		this.fritzbox_pw = fritzbox_pw;
	}

	public AVMDect200(String fritzbox_pw, String host) {
		this.fritzbox_pw = fritzbox_pw;
		this.fritzbox_url = host;
	}

	public void init() throws IOException, InvalidPasswordException {
		if (!fritzbox_url.startsWith("http://"))
			this.fritzbox_url = "http://" + this.fritzbox_url;
		String base_url = fritzbox_url + "/login_sid.lua";
		String challenge = getTagValue(readURL(base_url), "Challenge");
		String response = challenge + "-" + MD5(challenge + "-" + fritzbox_pw);
		SID = getTagValue(readURL(base_url + "?response=" + response), "SID");
		if (SID.equals("0000000000000000"))
			throw new InvalidPasswordException("invalid password");
	}

	private String homeauto_url_with_sid() {
		return this.fritzbox_url + this.homeswitch + "?sid=" + this.SID;
	}

	private String callCommand(String command, String devId) throws IOException {
		String url = homeauto_url_with_sid() + "&switchcmd=" + command
				+ "&ain=" + devId;
		return readURL(url);
	}

	public String getInfo() throws IOException {
		String url = homeauto_url_with_sid() + "&switchcmd=getswitchlist";
		return readURL(url);
	}

	public String getSocketName(String devId) throws IOException {
		return callCommand("getswitchname", devId);
	}
	
	public Boolean getSocketAvailability(String devId) throws IOException {
		return !callCommand("getswitchpower", devId).equals("inval");
	}
	
	public int getSocketPower(String devId) throws IOException {
		
		try {
			return Integer.parseInt(callCommand("getswitchpower", devId));
		} catch (NumberFormatException | IOException e) {
			return Integer.MIN_VALUE;
		}
	}
	
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

	public String setSocketOn(String devId) throws IOException {
		return callCommand("setswitchon", devId);
	}

	public String setSocketOff(String devId) throws IOException {
		return callCommand("setswitchoff", devId);
	}

	public boolean getSocketState(String devId) throws IOException {
		String ret = callCommand("getswitchstate", devId);
		if (ret.equals("1"))
			return true;
		return false;
	}

	public ArrayList<Socket> getSocketList() throws IOException {
		ArrayList<Socket> Sockets = new ArrayList<Socket>();
		String[] devs = getInfo().split(",");
		for (String dev : devs) {
			Sockets.add(new Socket(dev, this));
		}
		return Sockets;
	}

	public String getSID() {
		return this.SID;
	}

	public String readURL(String url) throws IOException {
		URL temp_url = new URL(url);
		URLConnection yc = temp_url.openConnection();
		// set 10 seconds timeout
		yc.setConnectTimeout(10 * 1000);
		BufferedReader in = new BufferedReader(new InputStreamReader(
				yc.getInputStream()));
		return in.readLine();
	}

	public String getTagValue(String xml, String tagName) {
		try {
			return xml.split("<" + tagName + ">")[1]
					.split("</" + tagName + ">")[0];
		} catch (ArrayIndexOutOfBoundsException e) {
			return "";
		}
	}

	public String MD5(String md5) {
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

		public int getSocketPower() throws IOException {
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
		
		public int getSocketTemperature() throws IOException, JSONException {
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