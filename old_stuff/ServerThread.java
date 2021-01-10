import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ServerThread extends Thread {
	private ServerSocket serverSocket;
	private Socket clientSocket;
	private PrintWriter dataout;
	int portNumber;
	String location;
	double temperature;
	double humidity;
	// delete them from Client.java
	
	public ServerThread(int portNumber, String location, double temperature, double humidity) throws IOException {
		serverSocket = new ServerSocket(portNumber);
		this.humidity = humidity;
		this.temperature = temperature;
		this.location = location;
	}
	// called by serverThread.start() in Client
	public void run() {
		try {
		// create message as object list
		List<Object> list = new ArrayList<Object>();
		list.add(location);
		list.add(temperature);
		list.add(humidity);
		long timestamp = new Date().getTime(); // // number of milliseconds since 01.01.1970
		list.add(timestamp);
		
		clientSocket = serverSocket.accept(); //  serverSocket hat Anfrage erhalten und kennt nun einen clientSocket
		// schicke diesem clientSocket alle deine Daten
		dataout = new PrintWriter(clientSocket.getOutputStream(), true);
		printWriter.println(list.get(0).toString() + ";" + String.valueOf(list.get(1)) + ";" + String.valueOf(list.get(2)));
		} catch(IOException e) {
			System.out.println("server socket threw IOException: \n");
			e.printStackTrace();
		}
	}
}
