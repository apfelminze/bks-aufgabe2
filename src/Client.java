import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

public class Client {

	private int port;
	public String location;
	private double temperature;
	private double humidity;
	private int time;
	public long lastUpdateTime; // last time the temperature and humidity were set
	
	public Client(String location) throws UnknownHostException, IOException {
		this.location = location;
		System.out.println("location: " + location);
		this.temperature = createTemperature();
		System.out.println("temperature: " + this.temperature + " °C");
		this.humidity = createHumidity();
		System.out.println("humidity: " + this.humidity + " %");
		this.lastUpdateTime = new Date().getTime(); 
		while(true) {
			long currentTime = new Date().getTime();
			if(currentTime - this.lastUpdateTime >= 30000) { // number of milliseconds since last update of temp & humidity
			    // updates temperature & humidity
				double temp = createTemperature();
			    this.temperature = temp;
			    double humidity = createHumidity();
			    this.humidity = humidity;
				lastUpdateTime = currentTime;
			    }
			
			
		}
	}


public static void main(String[] args) {
	try {
	Scanner scanner = new Scanner(System.in);
	System.out.println("Enter the location: ");
	String location = scanner.nextLine();
	Client client = new Client(location);

	}
	catch (UnknownHostException e) {
		
	}
	catch (IOException e) {
		
	}
}

public int getFreePort() {
	Random random = new Random();
	int port = random.nextInt(10) + 50001; // creates number between 50001 and 50010
	// check if free
	return port;
}


public long getLastUpdateTime() {
	return this.lastUpdateTime;
}

public double createTemperature() { 
	// between -50 and +50 degrees
	double temp = (double) Math.round((Math.random() * 100 - 50) * 100d) / 100d; // two decimal numbers max
	return temp;
}

public double createHumidity() {
	// between 0 and 100%
	double humiditiy = (double) Math.round(Math.random() * 100 * 100d) / 100d; // two decimal numbers max
	return humiditiy;
}



}