import java.io.IOException;


public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			new ServerThread("blue_deamon").start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
