import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DataElement;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import javax.print.attribute.standard.Severity;


public class Test {
	
	private static int byteToInt(byte b) {
		if (b<0)
			return b+255;
		else
			return b;
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {

     // assuming the service UID has been retrieved
        String serviceURL =
        		"btspp://0016530D3A52:1;authenticate=false;encrypt=false;master=false";
        try {
            StreamConnection connection = (StreamConnection) Connector.open(serviceURL);

            

//            byte buffer[] = {0x00,0x04,0x01,-100,0x01,0x01,0x50,0x20,0x01,0x00,0x00,0x00};
//            write message
//            byte buffer[] = {0x00,0x09,0x00,0x05,(byte) 0xD0,0x03,0x00,0x00,'\0'};
//            silnik infinity
//            byte buffer[] = {0x00,0x04,0x02,70,0x01,0x00,0,0x00,0,0,0x00,0, 0};
            
            /// Sensory:

            // set mode
//            byte buffer[] = {0, 5, 1, 6, 0};
            // read
//            byte buffer[] = {0, 7, 1};
            
            /// Strefa groźnych eksperymentów z sensorem ultradźwiękowym:
            
            // set mode
            byte buffer[] = {0, 5, 1, 0x0B, 0};
            
            // lswrite
//            byte buffer[] = {0, 0x0F, 1, 2, 1, 0x02, 0x42};
            
            InputStream is = connection.openInputStream();
            OutputStream os = connection.openOutputStream();
            // send data to the client
            System.out.println(" - - Write");
            os.write(buffer.length);
            os.write(0x00);
            os.write(buffer);
            os.flush();
            // read data from client
            buffer=new byte[2];
            System.out.println(" - - Read length");
            is.read(buffer);
            System.out.println(" - - Read message");
            buffer=new byte[buffer[0]];
            is.read(buffer);
            for (int i=0;i<buffer.length;i++)
            	System.out.print((Integer.toHexString(byteToInt(buffer[i]))+" ").toUpperCase() );
            System.out.println();
            connection.close();
        } catch(IOException e) {
          e.printStackTrace();
        }
        
        
	}

}