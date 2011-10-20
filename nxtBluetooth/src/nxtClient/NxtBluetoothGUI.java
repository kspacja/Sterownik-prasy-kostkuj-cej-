package nxtClient;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DataElement;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

public class NxtBluetoothGUI extends JFrame implements ActionListener, DiscoveryListener {

	private static final long serialVersionUID = -3722504484672472629L;
	
	//devices
	private javax.swing.JLabel devicesLabel;
    private javax.swing.JList<String> devicesList;
    private DefaultListModel<String> devicesListModel;
    private javax.swing.JScrollPane devicesListScrollPane;
    private javax.swing.JPanel devicesPanel;
    private javax.swing.JButton searchForDevicesButton;
	//services
	private javax.swing.JLabel servicesLabel;
    private javax.swing.JList<String> servicesList;
    private DefaultListModel<String> servicesListModel;
    private javax.swing.JScrollPane servicesListScrollPane;
    private javax.swing.JPanel servicesPanel;
    private javax.swing.JButton searchForServicesButton;
    
    private ArrayList<RemoteDevice> devicesDiscovered;
    private ArrayList<ServiceRecord> servicesDiscovered;
    
    
    	
	public NxtBluetoothGUI() {
		
		try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(NxtBluetoothGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(NxtBluetoothGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(NxtBluetoothGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(NxtBluetoothGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
		
        initComponents();
	}

	private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		setLayout(new FlowLayout());
		
		devicesDiscovered = new ArrayList<RemoteDevice>();
	    servicesDiscovered = new ArrayList<ServiceRecord>();

		devicesListModel = new DefaultListModel<String>();
		servicesListModel = new DefaultListModel<String>();
        
		devicesPanel = new javax.swing.JPanel();
        devicesListScrollPane = new javax.swing.JScrollPane();
        devicesList = new javax.swing.JList<String>(devicesListModel);
        devicesLabel = new javax.swing.JLabel();
        searchForDevicesButton = new javax.swing.JButton();
        
        servicesPanel = new javax.swing.JPanel();
        servicesListScrollPane = new javax.swing.JScrollPane();
        servicesList = new JList<String>(servicesListModel);
		servicesListModel = new DefaultListModel<String>();
        servicesLabel = new javax.swing.JLabel();
        searchForServicesButton = new javax.swing.JButton();
        
        
        devicesListScrollPane.setViewportView(devicesList);
        devicesLabel.setText("Urz¹dzenia:");
        searchForDevicesButton.setText("Szukaj urz¹dzeñ");
        searchForDevicesButton.addActionListener(this);
        devicesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        devicesList.setLayoutOrientation(JList.VERTICAL);
        devicesList.setVisibleRowCount(-1);
        devicesPanel.setLayout(new BoxLayout(devicesPanel, BoxLayout.PAGE_AXIS));
        devicesPanel.add(searchForDevicesButton);
        devicesPanel.add(devicesLabel);
        devicesListScrollPane.setPreferredSize(new Dimension(200,150));
        devicesPanel.add(devicesListScrollPane);
        
        servicesListScrollPane.setViewportView(servicesList);
        servicesLabel.setText("Us³ugi:");
        searchForServicesButton.setText("Szukaj us³ug");
        searchForServicesButton.addActionListener(this);
        servicesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        servicesList.setLayoutOrientation(JList.VERTICAL);
        servicesList.setVisibleRowCount(-1);
        servicesPanel.setLayout(new BoxLayout(servicesPanel, BoxLayout.PAGE_AXIS));
        servicesPanel.add(searchForServicesButton);
        servicesPanel.add(servicesLabel);
        servicesListScrollPane.setPreferredSize(new Dimension(200,150));
        servicesPanel.add(servicesListScrollPane);
        
        
        getContentPane().add(devicesPanel);
        getContentPane().add(servicesPanel);
        pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource()==searchForDevicesButton) {
			boolean started;
			try {
				started = LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, this);
		        if (started) {
		            System.out.println("Szukam urz¹dzeñ...");
		            devicesDiscovered.clear();
		            devicesListModel.clear();
		        }
			} catch (BluetoothStateException e1) {
				e1.printStackTrace();
			}
		} else if (e.getSource()==searchForServicesButton) {
			final UUID RFCOMM_PROTOCOL_UUID = new UUID(0x0003);
			UUID serviceUUID = RFCOMM_PROTOCOL_UUID;
			UUID[] searchUuidSet = new UUID[] { serviceUUID };
	        int[] attrIDs =  new int[] {
	                0x0100 // Service name
	        };
			if (devicesList.getSelectedIndex()>-1) {
				try {
					System.out.println("Szukam uslug na "+devicesDiscovered.get(devicesList.getSelectedIndex()).getFriendlyName(true));
					LocalDevice.getLocalDevice().getDiscoveryAgent()
						.searchServices(attrIDs, searchUuidSet, devicesDiscovered.get(devicesList.getSelectedIndex()), this);
				} catch (IOException e1) {
					e1.printStackTrace();
					System.out.println("mesyd¿: "+e1.getMessage());
				}
			} else {
				System.out.println("Nie zosta³o wybrane ¿adne urz¹dzenie.");
			}
		}
	}

	@Override
	public void deviceDiscovered(RemoteDevice btDevice, DeviceClass arg1) {
		devicesDiscovered.add(btDevice);
		try {
			devicesListModel.addElement(btDevice.getBluetoothAddress() + " " + btDevice.getFriendlyName(true));
		} catch (IOException e) {
			devicesListModel.addElement(btDevice.getBluetoothAddress() + " (nie podal nazwy)");
		}
		
	}

	@Override
	public void inquiryCompleted(int arg0) {
		System.out.println("Koniec szukania");
	}

	@Override
	public void serviceSearchCompleted(int arg0, int arg1) {
		System.out.println("serviceSearchCompleted");
	}

	@Override
	public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
        for (int i = 0; i < servRecord.length; i++) {
            String url = servRecord[i].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
            if (url == null) {
                continue;
            }
            servicesDiscovered.add(servRecord[i]);
            DataElement serviceName = servRecord[i].getAttributeValue(0x0100);
            if (serviceName != null) {
            	servicesListModel.addElement((String) serviceName.getValue());
            	
            } else {
            	servicesListModel.addElement("Us³uga bez nazwy?");
            }
        }
	}
	
}
