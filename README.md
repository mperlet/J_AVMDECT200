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
