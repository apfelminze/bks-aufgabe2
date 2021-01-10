import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class PeerThread extends Thread {
	private BufferedReader bufferedReader;
	public PeerThread(Socket socket) throws IOException {
		bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}
	
	public void run() {
		// receives messages and saves content
		String messageLine;
		try {
			messageLine = bufferedReader.readLine();
			System.out.println(messageLine);
		} catch (IOException e) {
			System.out.println("did not get proper message: \n");
			e.printStackTrace();
		}
	}
}
