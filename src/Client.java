import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class Client implements Runnable {

	Thread receiveDataThread = null;
	DatagramSocket socket = null; 
	
	public int port;
	public String location;
	public String temperature;
	public String humidity;
	public long currentTime;
	public long lastUpdateTime; // last time the temperature and humidity were set
	public ArrayList<ArrayList<String>> peerData = new ArrayList<ArrayList<String>>(); // initial capacity of 10
	
	public Client(String location) throws IOException  {
		// set values
		this.location = location;
		setTempAndHumidity();
		this.lastUpdateTime = new Date().getTime(); 
		System.out.println("location: " + location);
		System.out.println("temperature: " + this.temperature + " °C");
		System.out.println("humidity: " + this.humidity + " %");
		// initialize peer data arraylist
		
		// set port, initialize socket
	    socket = getFreePort();
	    if(socket == null) {
	    	System.out.println("Socket could not be initialized. Perhaps no free port.\n");
	    	return;
	    } 
		this.port = socket.getLocalPort();
 		System.out.println("uses port: " + this.port);
		socket.setBroadcast(true);
		
		// open receiveDataThread thread
		receiveDataThread = new Thread(this, "my runnable thread");
	    System.out.println("my thread created" + receiveDataThread);
	    receiveDataThread.start();
}

public void setTempAndHumidity() {
		// between -50 and +50 degrees
			double temp = (double) Math.round((Math.random() * 100 - 50) * 100d) / 100d; // two decimal numbers max
			String tempS = Double.toString(temp);
		    this.temperature = tempS;
			// between 0 and 100%
			double humidity = (double) Math.round(Math.random() * 100 * 100d) / 100d; // two decimal numbers max
			String humidityS = Double.toString(humidity);
		    this.humidity = humidityS;
}

public DatagramSocket getFreePort() {
	for(int i=50001; i<50011; i++) {
		try {
		socket = new DatagramSocket(i);
		break;
		} catch(Exception e) { 
			continue;
		}
	}
	return socket;
}


public void run() {
	// send requests to other clients
	// receive and store data
	receive(this.socket);
}


public void receive(DatagramSocket socket)  {
	System.out.println("enter receive() \n");
	// Strings: The = = operator compares references not values.
	// The String equals() method compares the original content of the string
	// s1.equals(s2)
	try {
	while(true) {
		// length of data packet: 20 letters of location -> 40 bytes, 3 longs -> 24 bytes, 3 semicolons -> 6 bytes ==> 70bytes
		byte[] received = new byte[70];
		DatagramPacket packet = new DatagramPacket(received, received.length);
		socket.receive(packet);
		currentTime = new Date().getTime(); 
		String receivedMessage = new String(packet.getData());
		// System.out.println("received message: " + receivedMessage);
		String[] receivedStrings = receivedMessage.split(";");
		String location = receivedStrings[0];
		if(location.equals(this.location)) {
			// don't save any received status information that refers to our own location/client
			continue;
		}
		long time = Long.parseLong(receivedStrings[1]);
		if(!validTime(time, currentTime)) {
			System.out.println("packet came from future - return");
			continue;
		}
		double temperature = Double.parseDouble(receivedStrings[2]);
		if(!validTemperature(temperature)) {
			System.out.println("it cannot possibly be so hot/cold according to our rules - return");
			continue;
		}
		double humidity = Double.parseDouble(receivedStrings[3]);
		if(!validHumidity(humidity)) {
			System.out.println("received a percentage under 0 or above 100 - return");
			continue;
		}
		storePeerData(location, time, temperature, humidity);
	}
	} catch (IOException e) {
		System.out.println("socket could not receive datagram packet: ");
		e.printStackTrace();
	}
}


public void storePeerData(String location, long time, double temperature, double humidity) {
	// store data of a client
	// check if we already have an entry for this client
	for(int i=0; i<peerData.size(); i++) {
		ArrayList<String> oneArray = this.peerData.get(i);
		String existingLocation = oneArray.get(0);
		if(location.equals(existingLocation)) {
			// System.out.println("equal strings: " + location +" and " + existingLocation + "\n");
			String existingTime = oneArray.get(1);
			if(Long.parseLong(existingTime) < time) {
				// the entry we already have is older than the new information
				oneArray.set(1, Long.toString(time)); // new time
				oneArray.set(2, Double.toString(temperature)); // new temp
				oneArray.set(3, Double.toString(humidity)); // new humidity
				peerData.set(i, oneArray);
				System.out.println("peer list: " + peerData + "\n");
				return;
			}
			else if((Long.parseLong(existingTime) == time) || (Long.parseLong(existingTime) > time)) {
				return; // new info is not newer
			}
		}
	}
	// Peer is not yet in the ArrayList peerData --> add entry for Peer
	ArrayList<String> newPeer = new ArrayList<String>();
	// ArrayList.set() only to replace; ArrayList.add() to save new elements
	newPeer.add(0, location);
	newPeer.add(1, Long.toString(time));
	newPeer.add(2, Double.toString(temperature));
	newPeer.add(3, Double.toString(humidity));
	peerData.add(newPeer);
	System.out.println("peer list: " + peerData + "\n");
}


public static void main(String[] args) throws IOException {
	try {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter the location: ");
		// location can only have 20 letters
		String location = scanner.nextLine();
		while(location.length() > 20) {
			System.out.println("Location name is too long (>20 characters). Please enter a location: ");
			location = scanner.nextLine();
		}
		scanner.close();
		Client client = new Client(location);
		while(client.receiveDataThread.isAlive()) {
			// get requests from other clients
			// send all the data out
			client.send();
		}  		
	} catch(IOException e) {
		System.out.println("Datagram socket in Client constructor throws exception:\n");
		e.printStackTrace();
	}
}

public void send() throws IOException, UnknownHostException {
		while(true) {
			long timeLeft = -1;
			do {
				currentTime = new Date().getTime();
				timeLeft = 30000 - (currentTime - this.lastUpdateTime); // have more than 30.000 milliseconds passed? yes if timeLeft <= 0
				sendCustomPackage(currentTime);
				sendAllForeignPackages(currentTime);
			} while(timeLeft > 0);
			renew();
		}
}

public void sendCustomPackage(long currentTime) {
	try {
		String currentTimeString = Long.toString(currentTime);
		String message = this.location + ";" + currentTimeString + ";" + this.temperature + ";" + this.humidity;
		// System.out.println("send custom package: " + message);
		byte[] data = message.getBytes();
		int packetLength = data.length;
		InetAddress address = null;
		address = InetAddress.getByName("255.255.255.255");
		for(int i=50001; i<50011; i++) {
			if(this.port == i) {
			continue;
			}
		DatagramPacket packet = new DatagramPacket(data, packetLength, address, i);
		// Constructs a datagram packet for sending packets of length length to the specified port number on the specified host.
		this.socket.send(packet);
		}	
	} catch(UnknownHostException e) {
		System.out.println("Cannot create InetAddress object from broadcast IP address:\n");
		e.printStackTrace();
	} catch(IOException e) {
		e.printStackTrace();
	}
}

public void sendAllForeignPackages(long currentTime) {
	try {
	// System.out.println("enter send foreign package\n");
	// 	public ArrayList<ArrayList<String>> peerData = new ArrayList<>(9);
		// ArrayList.size() gives number of elements, not number of space
	for(int i=0; i<peerData.size(); i++) {
		ArrayList<String> oneArray = peerData.get(i);
		String message = "";
		String location = oneArray.get(0);
		if(location != null && !location.isEmpty()) {
			message = location + ";"; 
		}
		else {
			// System.out.println("empty location\n");
			continue;
		}
		String time = oneArray.get(1);
		if(time != null && !time.isEmpty()) {
			// it has already been checked in receive() method above if time is a validTime() 
			message = message + time + ";"; 
		}
		else {
			System.out.println("empty time\n");
			continue;
		}
		String temperature = oneArray.get(2);
		if(temperature != null && !temperature.isEmpty()) {
			message = message + temperature + ";"; 
		}
		else {
			System.out.println("empty temperature.\n");
			continue;
		}
		String humidity = oneArray.get(3);
		if(humidity != null && !humidity.isEmpty()) {
			message = message + humidity + ";"; 
		}
		else {
			System.out.println("empty humidity\n");
			continue;
		}
		// System.out.println("message is valid: " + message);
		byte[] data = message.getBytes();
		int packetLength = data.length;
		InetAddress address = null;
		address = InetAddress.getByName("255.255.255.255");
		for(int j=50001; j<50011; j++) {
			if(j == this.port) {
			continue;
			}
			DatagramPacket packet = new DatagramPacket(data, packetLength, address, j);
		// Constructs a datagram packet for sending packets of length length to the specified port number on the specified host.
			this.socket.send(packet);
		}	
	}
	} catch(UnknownHostException e) {
		System.out.println("Cannot create InetAddress object from broadcast IP address:\n");
		e.printStackTrace();
	} catch(IOException e) {
		e.printStackTrace();
	}
}

public void renew() throws UnknownHostException, IOException {
	setTempAndHumidity();
	System.out.println("new temperature: " + this.temperature + " °C");
	System.out.println("new humidity: " + this.humidity + " %");
	this.lastUpdateTime = new Date().getTime(); 
}

public boolean validTemperature(double temp) {
	// between -50 and +50 degrees
	if(temp >= -50 && temp <= 50) {
		return true;
	}
	else {
		return false;
	}
}

public boolean validHumidity(double hum) {
	// between 0 and 100%
	if(hum >= 0 && hum <= 100) {
		return true;
	}
	else {
		return false;
	}
}

public boolean validTime(long time, long currentTime) {
	if(time > currentTime) {
		return false;
	}
	else {
		return true;
	}
}
}
