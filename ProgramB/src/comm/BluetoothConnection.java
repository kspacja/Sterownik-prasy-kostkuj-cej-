package comm;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

import javax.bluetooth.RemoteDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

/**
 * Klasa odpowiedzialna za nawiązanie i utrzymanie połączenia Bluetooth z robotem
 * Pozwala na wysyłanie i otrzymywanie wiadomości od robota. Ta klasa obsługuje
 * protokół używany przez kostkę Lego NXT
 * 
 * Obiekty tej klasy generalnie nie generują wyjątków, w przypadku wystąpienia błędu
 * połączenia ustawiane są odpowiednie zmienne wewnętrzne, których wartość można odczytać
 * za pomocą <code>getStatus</code> i <code>getError</code>
 */
public class BluetoothConnection extends Observable implements Closeable
{
	private Thread closer; // Wątek zamykający połączenie, dla ShutdownHook
	
	public static final int STATUS_CONNECTING = 2;
	public static final int STATUS_OK = 1;
	public static final int STATUS_DISCONNECTED = 0;
	public static final int STATUS_ERROR = 3;
	
	public static final int ERROR_NONE = 0;
	public static final int ERROR_AUTHENTICATION = 1;
	public static final int ERROR_SENDING = 2;
	public static final int ERROR_RECEIVING = 3;
	
	private StreamConnection conn;
	private InputStream istream;
	private OutputStream ostream;
	
	// Ustawiamy odpowiedni status, jesli wystąpią jakieś problemy z połączeniem
	private int status;
	private int error;
	
	public BluetoothConnection(RemoteDevice device, Observer observer) {
		setStatus(BluetoothConnection.STATUS_DISCONNECTED);
		setError(ERROR_NONE);
		addObserver(observer);
		connectTo(device);
		
		registerClose(this);
	}
	
	public BluetoothConnection(RemoteDevice device) {
		setStatus(BluetoothConnection.STATUS_DISCONNECTED);
		setError(ERROR_NONE);
		connectTo(device);
		
		registerClose(this);
	}
	
	public BluetoothConnection(String addr, Observer observer)
	{
		setStatus(BluetoothConnection.STATUS_DISCONNECTED);
		setError(ERROR_NONE);
		addObserver(observer);
		connectTo(addr);
		
		registerClose(this);
	}
	
	/**
	 * @param addr Adres urządzenia Bluetooth, z którym ma być nawiązane połączenie
	 */
	public BluetoothConnection(String addr)
	{
		setStatus(BluetoothConnection.STATUS_DISCONNECTED);
		setError(ERROR_NONE);
		connectTo(addr);
		
		registerClose(this);
	}
	
	private static void registerClose(final BluetoothConnection bt)
	{
		bt.closer = new Thread(new Runnable(){
			public void run()
			{
				bt.close();
			}
		});
		
		Runtime.getRuntime().addShutdownHook(bt.closer);
	}
	
	public void connectTo(RemoteDevice device) {
		try {
			setStatus(BluetoothConnection.STATUS_CONNECTING);
			String deviceURL = "btspp://"+device.getBluetoothAddress()+":1;authenticate=false;encrypt=false;master=false";
			System.out.println("Łączenie z ");
			conn = (StreamConnection) Connector.open(deviceURL);
			istream = conn.openInputStream();
			ostream = conn.openOutputStream();
			setStatus(BluetoothConnection.STATUS_OK);
			
			// system powinien przeprowadzic autentykacje tylko jesli nie pamieta pinu
			if (!device.authenticate()) {
				setStatus(BluetoothConnection.STATUS_ERROR);
				setError(BluetoothConnection.ERROR_AUTHENTICATION);
			} else {
				setStatus(BluetoothConnection.STATUS_OK);
			}
			
			System.out.println("Połączono");
			
		} catch (IOException e) {
			setStatus(BluetoothConnection.STATUS_DISCONNECTED);
			e.printStackTrace();
		}
	}
	
	/**
	 * Nawiązuje połączenie z wybranym urządzeniem Bluetooth
	 * @param addr Adres urządzenia, do z którym ma być nawiązane połączenie 
	 */
	public void connectTo(String addr)
	{
		try
		{
			setStatus(BluetoothConnection.STATUS_CONNECTING);
			String deviceURL = "btspp://"+addr+":1;authenticate=false;encrypt=false;master=false";
			System.out.println("Łączenie z ");
			conn = (StreamConnection) Connector.open(deviceURL);
			istream = conn.openInputStream();
			ostream = conn.openOutputStream();
			setStatus(BluetoothConnection.STATUS_OK);
			
			RemoteDevice device = RemoteDevice.getRemoteDevice(conn);
			// system powinien przeprowadzic autentykacje tylko jesli nie pamieta pinu
			if (!device.authenticate())
			{
				setStatus(BluetoothConnection.STATUS_ERROR);
				setError(BluetoothConnection.ERROR_AUTHENTICATION);
			}
			else
				setStatus(BluetoothConnection.STATUS_OK);
			
			System.out.println("Połączono");
		}
		catch (IOException e)
		{
			setStatus(BluetoothConnection.STATUS_DISCONNECTED);
			e.printStackTrace();
		}
	}
	
	/**
	 * Sprawdza status połączenia
	 * @return Stan połączenia (jedna ze stałych klasowych)
	 */
	public int getStatus() {
		return status;
	}

	private void setStatus(int status) {
		if (this.status!=status) setChanged();
		this.status = status;
		notifyObservers();
	}
	
	/**
	 * Sprawdza, na jakim etapie łączenia wystąpił błąd. Wywołanie tej metody ma sens jedynie gdy
	 * <code>getStatus</code> zwraca wartość <code>STATUS_ERROR</code>
	 * @return Rodzaj błędu (jedna ze stałych klasowych)
	 */
	public int getError() {
		return error;
	}

	private void setError(int error) {
		this.error = error;
	}
	
	@Override
	public void close()
	{
		try
		{
			if (conn!=null) conn.close();
		}
		// I tak już kończymy, nie ma znaczenia co stanie się z tym połączeniem
		catch(IOException e)
		{}
		
		deleteObservers();
	}
	
	/**
	 * Wysyła wiadomość do urządzenia z którym nawiązano połączenie
	 * @param msg Wiadomość w postaci ciągu bajtów
	 */
	public void send(byte[] msg) {
		try {
			System.out.println("Wysłano do robota: " + Arrays.toString(msg));
			ostream.write(msg.length%256);
			ostream.write(msg.length/256);
			ostream.write(msg);
			ostream.flush();
			setError(ERROR_NONE);
		} catch (IOException e) {
			setError(ERROR_SENDING);
			e.printStackTrace();
		}
	}
	
	/**
	 * Metoda odczytuje i zwraca dokładnie jedną wiadomość od robota, nawet jeśli
	 * oczekujących w strumieniu jest więcej.
	 * @return wiadomość od robota w postaci ciągu bajtów, <code>null</code> jeśli nie ma żadnej oczekującej
	 */
	public byte[] receive() {
		byte[] res = null;
		
		// Ponieważ znamy protokół, możemy założyć, że wszystkie wiadomości będą się do niego
		// stosować i nie musimy sprawdzać, ile dokładnie otrzymaliśmy bajtów
		try {
			if(istream.available() > 0)
			{
				byte[] lenbuf = new byte[2];
				istream.read(lenbuf);
				
				res = new byte[lenbuf[1]*256+lenbuf[0]];
				istream.read(res);
			}
		} catch (IOException e) {
			setError(ERROR_RECEIVING);
			e.printStackTrace();
		}
		
		return res;
	}
	
	/**
	 * Pozbywa się nieodczytanych wiadomości zalegających w buforze wejściowym
	 */
	public void clearInput()
	{
		try
		{
			while(istream.available() > 0)
				istream.read();
		}
		catch(IOException e)
		{
			setError(ERROR_RECEIVING);
			e.printStackTrace();
		}
	}
	
	/**
	 * @return Obiekt klasy <code>javax.bluetooth.RemoteDevice</code> odnoszący się do urządzenia z którym jest nawiązane połączenie
	 */
	public RemoteDevice getDevice()
	{
		try
		{
			return RemoteDevice.getRemoteDevice(conn);
		}
		catch(IOException e)
		{
			setError(ERROR_RECEIVING);
			e.printStackTrace();
			return null;
		}
	}
}
