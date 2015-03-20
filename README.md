# Java AVM Dect200 Api

# Example

```
AVMDect200 fritzbox = new AVMDect200("my_fritz_password", "fritz.box");
fritzbox.init();
ArrayList<AVMDect200.Socket> sockets = fritzbox.getSocketList();
for (AVMDect200.Socket socket : sockets) {
	System.out.println("Name: \t" + socket.getSocketName());
	System.out.println("Id:   \t" + socket.getSocketId());
	System.out.println("OK?:  \t" + socket.getSocketAvailability());
	System.out.println("Watt: \t" + socket.getSocketPower()/1000.0 + "W");
	if (socket.getSocketState())
		System.out.println("State: \tON");
	if (!socket.getSocketState())
		System.out.println("State: \tOFF");
	System.out.println("Temp: \t" + socket.getSocketTemperature()/10.0 + "°C");
	System.out.println();
		}
```

```
Name: 	Wohnzimmer Steckdose
Id:   	087610097318
OK?:  	true
Watt: 	4.0W
State: 	ON
Temp: 	22.8°C
```

# Documentation

## `public class AVMDect200`

AVM Fritzbox Dect-200 Socket Api-Wrapper

 * **Author:** Mathias Perlet
 * **Version:** 0.0.1

## `public AVMDect200(String fritzbox_pw)`

 * **Parameters:** `fritzbox_pw` — the frizbox password for login

## `public AVMDect200(String fritzbox_pw, String host)`

 * **Parameters:**
   * `fritzbox_pw` — the frizbox password for login
   * `host` — the fritzbox url, standard is http://fritz.box

## `public void init() throws IOException, InvalidPasswordException`

initial communication with the fritzbox generates a session id for further actions

 * **Exceptions:**
   * `IOException` — connections errors, like host not found
   * `InvalidPasswordException` — throws if password not valid

## `private String homeauto_url_with_sid()`

 * **Returns:** the absolute url to fritzbox-api with valid session key

## `private String callCommand(String command, String devId) throws IOException`

 * **Parameters:**
   * `command` — a valid command from dect200 documentation
   * `devId` — the dect200 socket AIN (socket-id)
 * **Returns:** api-response from fritzbox
 * **Exceptions:** `IOException` — communication errors

## `public String getInfo() throws IOException`

 * **Returns:** returns a comma separated list of connected socket IDs
 * **Exceptions:** `IOException` — 

## `public String getSocketName(String devId) throws IOException`

 * **Parameters:** `devId` — the dect200 socket AIN (socket-id)
 * **Returns:** the configured socket-name like "Living Room"
 * **Exceptions:** `IOException` — 

## `public Boolean getSocketAvailability(String devId) throws IOException`

 * **Parameters:** `devId` — the dect200 socket AIN (socket-id)
 * **Returns:** true if the socket is available
 * **Exceptions:** `IOException` — 

## `public int getSocketPower(String devId)`

 * **Parameters:** `devId` — the dect200 socket AIN (socket-id)
 * **Returns:** the current power in milli watt as a integer, on error it returns the minimal integer value
 * **Exceptions:** `IOException` — 

## `public int getSocketTemperature(String devId) throws IOException`

 * **Parameters:** `devId` — the dect200 socket AIN (socket-id)
 * **Returns:**
   * the current temperature with offset in centi grad celsius (245 is 24.5°C).
   * on error, it returns integer min value
 * **Exceptions:** `IOException` — 

## `public String setSocketOn(String devId) throws IOException`

switch a socket on

 * **Parameters:** `devId` — the dect200 socket AIN (socket-id)
 * **Returns:** the success value for switch the socket on
 * **Exceptions:** `IOException` — 

## `public String setSocketOff(String devId) throws IOException`

switch a socket off

 * **Parameters:** `devId` — the dect200 socket AIN (socket-id)
 * **Returns:** the success value for switch the socket off
 * **Exceptions:** `IOException` — 

## `public boolean getSocketState(String devId) throws IOException`

 * **Parameters:** `devId` — the dect200 socket AIN (socket-id)
 * **Returns:** true if socket is on, false if socket if off
 * **Exceptions:** `IOException` — 

## `public ArrayList<Socket> getSocketList() throws IOException`

 * **Returns:** a ArryList of sockets
 * **Exceptions:** `IOException` — 

## `public String getSID()`

 * **Returns:** current session id for fritzbox communication

## `private String readURL(String url) throws IOException`

 * **Parameters:** `url` — http communication with the fritzbox-api, only http-GET
 * **Returns:** returns the http response
 * **Exceptions:** `IOException` — 

## `private String getTagValue(String xml, String tagName)`

 * **Parameters:**
   * `xml` — every tag must be unique, otherwise it will return an empty string
   * `tagName` — name of the xml-tag
 * **Returns:** returns the value from the tag as a string

## `private String md5(String md5)`

 * **Parameters:** `md5` — string to hash
 * **Returns:** returns the md5-hash in utf-16le encoding

## `public static JSONObject xmlToJson(String callbackResponse)`

 * **Parameters:** `callbackResponse` — xml string
 * **Returns:** a json-object
