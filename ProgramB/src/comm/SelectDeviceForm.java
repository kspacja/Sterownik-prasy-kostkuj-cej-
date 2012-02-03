package comm;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Ta klasa jest oknem GUI, pozwalającym na wyszukanie i wybór urządzenia bluetooth, z którym
 * nawiązane zostanie połączenie bluetooth
 *
 * Klasa używa funkcjonalności z pakietu <code>javax.bluetooth</code> aby przeprowadzić wyszukiwanie
 * urządzeń w otoczeniu.
 */
public class SelectDeviceForm extends JDialog implements ActionListener, DiscoveryListener, ListSelectionListener  {
	private static final long serialVersionUID = -3722504484672472629L;
	
	NxtBluetoothGUI parent;

	//devices
	private javax.swing.JPanel devicesPanel;
	private javax.swing.JLabel devicesLabel;
	private javax.swing.JList devicesList;
	private DefaultListModel devicesListModel;
	private javax.swing.JScrollPane devicesListScrollPane;
	private javax.swing.JButton searchForDevicesButton;
	private javax.swing.JButton connectButton;
	
	private ArrayList<RemoteDevice> devicesDiscovered;

	/**
	 * @param parent obiekt będący głównym oknem GUI programu.
	 */
	public SelectDeviceForm(NxtBluetoothGUI parent) {
		super(parent, true); // Uruchom jako okno modalne
		
		this.parent = parent;
		setLocationRelativeTo(parent);
		try {				// styl wizualny
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException ex) {
			java.util.logging.Logger.getLogger(SelectDeviceForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(SelectDeviceForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(SelectDeviceForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(SelectDeviceForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}

		initComponents();
	}

	private void initComponents() {
		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Wybierz urządzenie");

		devicesDiscovered = new ArrayList<RemoteDevice>();
		devicesListModel = new DefaultListModel();

		// create components
		devicesPanel = new javax.swing.JPanel();
		devicesListScrollPane = new javax.swing.JScrollPane();
		devicesList = new javax.swing.JList(devicesListModel);
		devicesLabel = new javax.swing.JLabel();
		searchForDevicesButton = new javax.swing.JButton();
		connectButton = new javax.swing.JButton();

		// setup components
		devicesListScrollPane.setViewportView(devicesList);
		devicesLabel.setText("Urządzenia:");
		searchForDevicesButton.setText("Szukaj urządzeń");
		searchForDevicesButton.addActionListener(this);
		devicesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		devicesList.setLayoutOrientation(JList.VERTICAL);
		devicesList.setVisibleRowCount(-1);
		devicesList.getSelectionModel().addListSelectionListener(this);
		devicesPanel.setLayout(new BoxLayout(devicesPanel, BoxLayout.PAGE_AXIS));
		devicesListScrollPane.setPreferredSize(new Dimension(250,150));
		connectButton.setText("Wybierz");
		connectButton.addActionListener(this);
		connectButton.setEnabled(false);
		
		// lay out on the pane
		Container contentPane = getContentPane();
		contentPane.add(devicesLabel);
		contentPane.add(devicesListScrollPane);
		contentPane.add(searchForDevicesButton);
		contentPane.add(connectButton);
		SpringLayout layout = new SpringLayout();
		contentPane.setLayout(layout);
		layout.putConstraint(SpringLayout.WEST, devicesLabel, 5, SpringLayout.WEST, contentPane);
		layout.putConstraint(SpringLayout.NORTH, devicesLabel, 5, SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.WEST, devicesListScrollPane, 5, SpringLayout.WEST, contentPane);
		layout.putConstraint(SpringLayout.NORTH, devicesListScrollPane, 5, SpringLayout.SOUTH, devicesLabel);
		layout.putConstraint(SpringLayout.WEST, searchForDevicesButton, 5, SpringLayout.WEST, contentPane);
		layout.putConstraint(SpringLayout.NORTH, searchForDevicesButton, 5, SpringLayout.SOUTH, devicesListScrollPane);
		layout.putConstraint(SpringLayout.WEST, connectButton, 5, SpringLayout.EAST, searchForDevicesButton);
		layout.putConstraint(SpringLayout.EAST, connectButton, 5, SpringLayout.EAST, devicesListScrollPane);
		layout.putConstraint(SpringLayout.NORTH, connectButton, 5, SpringLayout.SOUTH, devicesListScrollPane);
		layout.putConstraint(SpringLayout.EAST, contentPane, 5, SpringLayout.EAST, devicesListScrollPane);
		layout.putConstraint(SpringLayout.SOUTH, contentPane, 5, SpringLayout.SOUTH, connectButton);
		
		pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource()==searchForDevicesButton) {
			String searchString = "Szukaj urządzeń";
			boolean started;
			try {
				if (searchForDevicesButton.getText().equals(searchString)) {
					started = LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, this);
					if (started) {
						System.out.println("Szukam urządzeń...");
						searchForDevicesButton.setText("Anuluj");
						devicesDiscovered.clear();
						devicesListModel.clear();
					}
				} else {
					LocalDevice.getLocalDevice().getDiscoveryAgent().cancelInquiry(this);
					searchForDevicesButton.setText(searchString);
				}
			} catch (BluetoothStateException e1) {
				JOptionPane.showMessageDialog(this,
					    "Błąd korzystania z bluetooth:\n"+e1.getMessage(),
					    "Błąd krytyczny",
					    JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getSource()==connectButton) {
			if ( ! devicesList.isSelectionEmpty()) {
				final DiscoveryListener dl = this;
				new Thread() {
					public void run() {
						try {
							LocalDevice.getLocalDevice().getDiscoveryAgent().cancelInquiry(dl);
						} catch (BluetoothStateException e1) {
							JOptionPane.showMessageDialog(parent,
								    "Błąd korzystania z bluetooth:\n"+e1.getMessage(),
								    "Błąd krytyczny",
								    JOptionPane.ERROR_MESSAGE);
						}
						parent.changeDevice(devicesDiscovered.get(devicesList.getSelectedIndex()));
					}
				}.start();
				this.dispose();
			}
		}
	}

	@Override
	public void deviceDiscovered(RemoteDevice btDevice, DeviceClass arg1) {
		// znaleziono urzadzenie - dodaj je do listy
		devicesDiscovered.add(btDevice);
		try {
			devicesListModel.addElement(btDevice.getBluetoothAddress() + " " + btDevice.getFriendlyName(true));
		} catch (IOException e) {
			devicesListModel.addElement(btDevice.getBluetoothAddress() + " (nie podał nazwy)");
		}

	}

	@Override
	public void inquiryCompleted(int arg0) {
		searchForDevicesButton.setText("Szukaj urządzeń");
		System.out.println("Koniec szukania");
	}

	@Override
	public void serviceSearchCompleted(int arg0, int arg1) {
		System.out.println("serviceSearchCompleted");
	}

	@Override
	public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
		// olewamy uslugi
	}
	

	@Override
	public void valueChanged(ListSelectionEvent e) {
		// Gdy wybrano urzadzenie enabluj przycisk Wybierz
		ListSelectionModel lsm = (ListSelectionModel)e.getSource();
		connectButton.setEnabled( ! lsm.isSelectionEmpty() );
	}
}


