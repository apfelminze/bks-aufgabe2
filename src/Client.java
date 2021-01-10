import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Scanner;

public class Client {

	public int port;
	public String location;
	public String temperature;
	public String humidity;
	public long currentTime;
	public long lastUpdateTime; // last time the temperature and humidity were set
	
	public Client(String location) throws IOException  {
		// set values
		this.location = location;
		setTempAndHumidity();
		this.lastUpdateTime = new Date().getTime(); 
		System.out.println("location: " + location);
		System.out.println("temperature: " + this.temperature + " °C");
		System.out.println("humidity: " + this.humidity + " %");
		// set port, initialize socket
	    DatagramSocket socket = getFreePort();
		this.port = socket.getLocalPort();
 		System.out.println("uses port: " + this.port);
		socket.setBroadcast(true);
		send(socket);
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
	DatagramSocket socket = null;
	for(int i=50001; i<50011; i++) {
		// InetSocketAddress address = new InetSocketAddress("localhost", i);
		// Creates a socket address from a hostname and a port number.
		try {
		//socket.bind(address);
		socket = new DatagramSocket(i);
		break;
		} catch(Exception e) { 
			continue;
		}
	}
	return socket;
}

public void send(DatagramSocket socket) throws IOException, UnknownHostException {
	try {
	long timeLeft = -1;
	do {
		currentTime = new Date().getTime();
		timeLeft = 30000 - (currentTime - this.lastUpdateTime); // have more than 30.000 milliseconds passed?
		String currentTimeString = Long.toString(currentTime);
		  String message = this.location + ";" + currentTimeString + ";" + this.temperature + ";" + this.humidity;
		  // System.out.println("message is: " + message);
		  byte[] data = message.getBytes();
		  int packetLength = data.length;
		  InetAddress address = null;
		  address = InetAddress.getByName("255.255.255.255");
		 for(int i=50001; i<50011; i++) {
			DatagramPacket packet = new DatagramPacket(data, packetLength, address, i);
			// Constructs a datagram packet for sending packets of length length to the specified port number on the specified host.
			socket.send(packet);
		}
	} while(timeLeft > 0);
	renew(socket);
	} catch (UnknownHostException e1) {
		System.out.println("Cannot create InetAddress object from broadcast IP address:\n");
		e1.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
}

public void renew(DatagramSocket socket) throws UnknownHostException, IOException {
	setTempAndHumidity();
	System.out.println("temperature: " + this.temperature + " °C");
	System.out.println("humidity: " + this.humidity + " %");
	this.lastUpdateTime = new Date().getTime(); 
	try {
	send(socket);
	} catch(UnknownHostException e) {
		System.out.println("calling send() method from renew() method threw exception: \n");
		e.printStackTrace();
	} catch(IOException e) {
		System.out.println("calling send() method from renew() method threw exception: \n");
		e.printStackTrace();
	}
}

public static void main(String[] args) throws IOException {
	try {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter the location: ");
		String location = scanner.nextLine();
		Client client = new Client(location);
		scanner.close();
	} catch(IOException e) {
		System.out.println("Datagram socket in Client constructor throws exception:\n");
		e.printStackTrace();
	}
}
}