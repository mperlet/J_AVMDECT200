# Java AVM Dect200 Api

# Example

```
AVMDect200 fritzbox = new AVMDect200("my_fritzbox_password");
fritzbox.init();
ArrayList<AVMDect200.Socket> sockets = fritzbox.getSocketList();
	
for (AVMDect200.Socket socket : sockets) {
	System.out.println(socket);
	System.out.println(socket.getSocketPower() + "W");
	if (socket.getSocketState())
		System.out.println("Socket is ON");
	if (!socket.getSocketState())
		System.out.println("Socket is OFF");
}

AVMDect200.Socket socket = sockets.get(0);
socket.setSocketOn();
```

```
087610094277:Beleuchtung
66.08W
Socket is ON
087610097318:Fernseher
114.22W
Socket is OFF
```
