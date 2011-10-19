import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

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


public class Main {
	
	private static int byteToInt(byte b) {
		if (b<0)
			return b+255;
		else
			return b;
	}
	
	public static final Vector<RemoteDevice> devicesDiscovered = new Vector<RemoteDevice>();
	public static final Vector<ServiceRecord> serviceFound = new Vector<ServiceRecord>();
    static final UUID RFCOMM_PROTOCOL_UUID = new UUID(0x0003);
	
	
	public static void main(String[] args) throws IOException, InterruptedException {

        final Object inquiryCompletedEvent = new Object();
        final Object serviceSearchCompletedEvent = new Object();

        devicesDiscovered.clear();
        serviceFound.clear();
        
        
        UUID serviceUUID = RFCOMM_PROTOCOL_UUID;
		
		DiscoveryListener listener = new DiscoveryListener() {

            public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
                try {
					System.out.println("Znalaz�em " + btDevice.getBluetoothAddress() + " o nazwie " + btDevice.getFriendlyName(false));
				} catch (IOException e) {
					System.out.println("Znalaz�em " + btDevice.getBluetoothAddress() + ", ale nie wiem jak si� nazywa.");
				}
                devicesDiscovered.addElement(btDevice);
            }
            public void inquiryCompleted(int discType) {
                System.out.println("Razem "+devicesDiscovered.size()+" urz�dze�.");
                synchronized(inquiryCompletedEvent){
                    inquiryCompletedEvent.notifyAll();
                }
            }

            public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
                for (int i = 0; i < servRecord.length; i++) {
                    String url = servRecord[i].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
                    if (url == null) {
                        continue;
                    }
                    serviceFound.add(servRecord[i]);
                    DataElement serviceName = servRecord[i].getAttributeValue(0x0100);
                    if (serviceName != null) {
                        System.out.println("Us�uga " + serviceName.getValue() + " z adresem " + url);
                    } else {
                        System.out.println("Us�uga z adresem " + url);
                    }
                }
            }
            public void serviceSearchCompleted(int transID, int respCode) {
                System.out.println("service search completed!");
                synchronized(serviceSearchCompletedEvent){
                    serviceSearchCompletedEvent.notifyAll();
                }
            }
        };
        
        synchronized(inquiryCompletedEvent) {
            boolean started = LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, listener);
            if (started) {
                System.out.println("Szukam urz�dze�...");
                inquiryCompletedEvent.wait();
            }
        }
		
        
        
        UUID[] searchUuidSet = new UUID[] { serviceUUID };
        int[] attrIDs =  new int[] {
                0x0100 // Service name
        };
        
        RemoteDevice device;
        int devNum=-1;
        do {
        	device = devicesDiscovered.get(++devNum);
        } while (!device.getFriendlyName(true).equals("NXTJarek") && devNum<devicesDiscovered.size());
        
        if (!(devNum<devicesDiscovered.size())) {
            System.out.println("Nie znalaz�em naszej Ewy.");
            return;
        }

        synchronized(serviceSearchCompletedEvent) {
        	System.out.println("Szukam us�ug na " + device.getBluetoothAddress() + " " + device.getFriendlyName(false));
        	LocalDevice.getLocalDevice().getDiscoveryAgent().searchServices(attrIDs, searchUuidSet, device, listener);
        	serviceSearchCompletedEvent.wait();
        }
        if (!serviceFound.isEmpty()) {
        ServiceRecord service = serviceFound.lastElement();

        String serviceURL =
        		service.getConnectionURL(ServiceRecord.AUTHENTICATE_ENCRYPT, false);
        try {
            StreamConnection connection = (StreamConnection) Connector.open(serviceURL);
            byte buffer[] = {0x02,0x00,0x01,(byte) 0x88};
            
            InputStream is = connection.openInputStream();
            OutputStream os = connection.openOutputStream();
            // send data to the client
            System.out.println(" - - Pytam GetFirmwareVersion");
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
            	System.out.print(Integer.toHexString(byteToInt(buffer[i]))+" ");
            System.out.println();
            connection.close();
        } catch(IOException e) {
          e.printStackTrace();
        }
        
        } else {System.out.println("Brak us�ug.      <<<<<<<< ");}
	}

}