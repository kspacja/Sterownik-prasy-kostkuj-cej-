package comm;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.SocketException;
import java.text.NumberFormat;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;

import javax.bluetooth.RemoteDevice;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.border.EtchedBorder;

import comm.BluetoothConnection;
import comm.RobotMaster;

public class NxtBluetoothGUI extends JFrame implements ActionListener, Observer, PropertyChangeListener {

	private static final long serialVersionUID = -3722504484672472629L;
	private final String configFile = "nxt.conf";
	final Properties config = new Properties();
	// Stałe dla pól pliku konfiguracyjnego
	static final String CONF_DEV_ADDR = "Device_address";
	static final String CONF_DEV_NAME = "Device_name";
	static final String CONF_PORT = "Listening_port";
	static final String CONF_TIMEOUT = "Bluetooth_timeout";

	private RobotMaster parent;
	private String deviceAddress;
	private String deviceName;
	private BluetoothConnection bc;

	Integer socketPort, timeout;

	private ImageIcon goodIcon, badIcon, noneIcon, procIcon;

	//device
	private javax.swing.JPanel devicePanel;
	private javax.swing.JPanel deviceLeftPanel;
	private javax.swing.JLabel deviceLabel;
	private javax.swing.JLabel deviceNameLabel;
	private javax.swing.JLabel deviceAddressLabel;
	private javax.swing.JButton selectDeviceButton;

	//connection
	private javax.swing.JPanel connectionPanel;
	private javax.swing.JLabel connectionStatusLabel;
	private javax.swing.JPanel controlPanel;
	private javax.swing.JButton connectButton;

	private javax.swing.JPanel inputsPanel;
	private javax.swing.JLabel portLabel;
	private javax.swing.JFormattedTextField portField;
	private javax.swing.JLabel timeoutLabel;
	private javax.swing.JFormattedTextField timeoutField;

	//tray
	private PopupMenu popup;
	private TrayIcon trayIcon;
	private SystemTray tray;
	private MenuItem aboutItem;
	private MenuItem exitItem;


	public NxtBluetoothGUI() {
		deviceAddress = null;
		bc = null;
		socketPort = new Integer(8765);
		timeout = new Integer(200);

		try { // Ustawiamy look and feel
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


		setIconImage(createImageIcon("/guiIcon.png").getImage());

		//Jesli sie da, to dodajemy ikonke do traya
		if (SystemTray.isSupported()) {
			popup = new PopupMenu();
			trayIcon = new TrayIcon(createImageIcon("/guiIcon.png").getImage());
			tray = SystemTray.getSystemTray();
			aboutItem = new MenuItem("Wyświetl / Ukryj");
			exitItem = new MenuItem("Zamknij");

			popup.add(aboutItem);
			popup.add(exitItem);
			trayIcon.setPopupMenu(popup);
			trayIcon.setToolTip("Klient NXT Bluetooth");
			trayIcon.setImageAutoSize(true);

			try {
				tray.add(trayIcon);
			} catch (AWTException e) {
				System.out.println("TrayIcon could not be added.");
				return;
			}
			trayIcon.addActionListener(this);
			exitItem.addActionListener(this);
			aboutItem.addActionListener(this);

		}

		wczytajConfig();

		initComponents();

		updateDeviceInfo();
		updateConnectionLabel();
	}

	public void setParent(RobotMaster parent) {
		this.parent = parent;
	}

	public void setDeviceAddress(String addr)
	{
		deviceAddress = addr;
		deviceName = null; // Niestety nie mamy tutaj nazwy urządzenia
		updateDeviceInfo();
	}

	private void wczytajConfig() {
		// Plik konfiguracyjny, na razie tylko ostatnio uzywane urzadzenie
		// TODO: zapisywać nazwe, dodac setup portu socket
		File cnfFile = new File(configFile);
		if (cnfFile.exists()) {
			try {
				BufferedReader in = new BufferedReader(new FileReader(cnfFile));
				config.load(in);
				in.close();
				deviceAddress = config.getProperty(CONF_DEV_ADDR);
				deviceName = config.getProperty(CONF_DEV_NAME);
				socketPort = Integer.parseInt(config.getProperty(CONF_PORT, "65500"));
				timeout = Integer.parseInt(config.getProperty(CONF_TIMEOUT, "200"));
			} catch (IOException e) {
			}
		}
	}

	private void zapiszConfig() {
		// Zapisz plik konfiguracyjny
		File cnfFile = new File(configFile);
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(cnfFile));
			config.store(out, null);
			out.close();
		} catch (IOException e) {
		}

	}

	// Pomocnicza metoda tworzaca obiekt ImageIcon ze sciezki wzglednej
	protected ImageIcon createImageIcon(String path) {
		java.net.URL imgURL = getClass().getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL);
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}

	private void initComponents() {
		setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
		setTitle("Program B GUI");

		//		Prerequisites
		goodIcon = createImageIcon("/goodIcon.png");
		badIcon = createImageIcon("/badIcon.png");
		noneIcon = createImageIcon("/noneIcon.png");
		procIcon = createImageIcon("/procIcon.gif");
		//		getResource("/resources/image.png") - przyklad

		// Device panel
		devicePanel = new javax.swing.JPanel();
		deviceLeftPanel = new javax.swing.JPanel();
		deviceLabel = new javax.swing.JLabel();
		deviceNameLabel = new javax.swing.JLabel();
		deviceAddressLabel = new javax.swing.JLabel();
		selectDeviceButton = new javax.swing.JButton();

		deviceLabel.setText("Urządzenie:");
		selectDeviceButton.setText("Wybierz urządzenie");
		selectDeviceButton.setMinimumSize(selectDeviceButton.getSize());
		selectDeviceButton.addActionListener(this);

		devicePanel.setLayout(new BoxLayout(devicePanel, BoxLayout.X_AXIS));
		devicePanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		deviceLeftPanel.setLayout(new BoxLayout(deviceLeftPanel, BoxLayout.Y_AXIS));
		deviceLeftPanel.add(deviceLabel);
		deviceLeftPanel.add(deviceNameLabel);
		deviceLeftPanel.add(deviceAddressLabel);
		deviceLabel.setBorder(BorderFactory.createEmptyBorder(5,5,0,5));
		deviceNameLabel.setBorder(BorderFactory.createEmptyBorder(3,15,0,5));
		deviceAddressLabel.setBorder(BorderFactory.createEmptyBorder(3,15,5,5));
		devicePanel.add(deviceLeftPanel);
		devicePanel.add(Box.createHorizontalGlue());
		devicePanel.add(selectDeviceButton);
		selectDeviceButton.setBorder(BorderFactory.createEmptyBorder(8,10,8,10));


		// Control panel
		controlPanel = new javax.swing.JPanel();
		connectButton = new javax.swing.JButton("Połącz");
		controlPanel.add(connectButton);
		connectButton.setPreferredSize(new Dimension(120, 28));
		connectButton.addActionListener(this);

		// Inputs panel
		inputsPanel = new javax.swing.JPanel();
		inputsPanel.setLayout(new GridLayout(1,4,10,1));
		NumberFormat portFormat = NumberFormat.getNumberInstance();
		portFormat.setParseIntegerOnly(true);
		portField = new JFormattedTextField(portFormat);
		portField.setValue(socketPort);
		portField.setColumns(7);
		portField.addPropertyChangeListener("value",this);
		portLabel = new JLabel("Port socket");
		portLabel.setLabelFor(portField);
		inputsPanel.add(portLabel);
		inputsPanel.add(portField);
		NumberFormat timeoutFormat = NumberFormat.getNumberInstance();
		timeoutFormat.setParseIntegerOnly(true);
		timeoutField = new JFormattedTextField(portFormat);
		timeoutField.setAlignmentX(JFormattedTextField.RIGHT);
		timeoutField.setValue(timeout);
		timeoutField.setColumns(7);
		timeoutField.addPropertyChangeListener("value",this);
		timeoutLabel = new JLabel("Timeout");
		timeoutLabel.setLabelFor(		timeoutField);
		inputsPanel.add(timeoutLabel);
		inputsPanel.add(timeoutField);

		// Connection panel
		connectionPanel = new javax.swing.JPanel();
		connectionStatusLabel = new javax.swing.JLabel();

		//		connectionPanel.setLayout(new BoxLayout(connectionPanel, BoxLayout.Y_AXIS));
		connectionPanel.setLayout(new BorderLayout());
		connectionPanel.setPreferredSize(new Dimension(480,150));
		connectionStatusLabel.setHorizontalAlignment(JLabel.CENTER);
		connectionStatusLabel.setIconTextGap(15);
		connectionStatusLabel.setFont(new Font(null, Font.PLAIN, 25));

		connectionPanel.add(connectionStatusLabel, BorderLayout.CENTER);
		connectionPanel.add(controlPanel, BorderLayout.EAST);
		connectionPanel.add(inputsPanel, BorderLayout.SOUTH);

		// Wrzuc wszystko na contentpane
		setLayout(new BorderLayout());
		add(devicePanel, BorderLayout.NORTH);
		add(connectionPanel, BorderLayout.CENTER);

		pack();
		setMinimumSize(getSize());
	}

	//	wywolywane z selectdevice
	public void changeDevice(RemoteDevice d) {
		try {
			deviceAddress = d.getBluetoothAddress();
			deviceName = d.getFriendlyName(false);
			updateDeviceInfo();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void connectToDevice() {
		if (parent!=null) {
			if (deviceAddress!=null) {
				bc = new BluetoothConnection(deviceAddress, this);
				parent.changeBluetoothConnection(bc);

				if(deviceAddress != null)
					config.setProperty(CONF_DEV_ADDR, deviceAddress);
				if(deviceName != null)
					config.setProperty(CONF_DEV_NAME, deviceName);
				if(socketPort != null)
					config.setProperty(CONF_PORT, socketPort.toString());
				if(timeout != null)
					config.setProperty(CONF_TIMEOUT, timeout.toString());
				zapiszConfig();

			} else System.err.println("Wywolanie connectToDevice() przy device==null"); //nie powinno wystapic
		} else System.err.println("Wywolanie connectToDevice() bez RobotMaster"); //tu trzeba uwazac, nie przewidzailem wszzystkich sytuacji
	}

	private void updateDeviceInfo() {
		if (deviceAddress == null) {
			deviceNameLabel.setText("<brak>");
			deviceAddressLabel.setText(" ");
		} else {
			if (deviceName == null) {
				deviceNameLabel.setText("Nazwa: ");
			} else {
				deviceNameLabel.setText("Nazwa: "+deviceName);
			}
			deviceAddressLabel.setText("Adres: "+deviceAddress);
		}
	}
	private void updateConnectionLabel() {
		if (bc == null) {
			connectionStatusLabel.setText(" ");
			connectionStatusLabel.setIcon(noneIcon);
		} else {
			if (bc.getStatus()==BluetoothConnection.STATUS_OK) {
				connectionStatusLabel.setText("Połączony");
				connectionStatusLabel.setIcon(goodIcon);
			} else if (bc.getStatus()==BluetoothConnection.STATUS_CONNECTING) {
				connectionStatusLabel.setText("Łączenie");
				connectionStatusLabel.setIcon(procIcon);
			} else if (bc.getStatus()==BluetoothConnection.STATUS_DISCONNECTED) {
				connectionStatusLabel.setText("Niepołączony");
				connectionStatusLabel.setIcon(badIcon);
			} else if (bc.getStatus()==BluetoothConnection.STATUS_ERROR) {
				connectionStatusLabel.setText("Wystąpił błąd");
				connectionStatusLabel.setIcon(badIcon);
			} else {
				connectionStatusLabel.setText(" ");
				connectionStatusLabel.setIcon(noneIcon);
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource()==selectDeviceButton) {
			new SelectDeviceForm(this).setVisible(true);
		} else if (e.getSource()==aboutItem || e.getSource()==trayIcon) {
			if (this.isVisible()) {
				setVisible(false);
				aboutItem.setLabel("Wyświetl");
			} else {
				setVisible(true);
				aboutItem.setLabel("Ukryj");
			}
		} else if (e.getSource()==exitItem) {
			tray.remove(trayIcon);
			System.exit(0);
		} else if (e.getSource()==connectButton) {
			if (deviceAddress!=null) {
				try	{
					if(parent == null) 
						parent = new RobotMaster(new SocketConnection(socketPort), timeout);
					connectToDevice();
				} catch(SocketException e1) {
					JOptionPane.showMessageDialog(this,
							"Port jest zajęty. Wybierz inny",
							"Błąd",
							JOptionPane.ERROR_MESSAGE);
				}
			} else {
				JOptionPane.showMessageDialog(this,
						"Nie wybrano urządzenia.",
						"Informacja",
						JOptionPane.INFORMATION_MESSAGE);
			}
		}

	}

	@Override
	public void update(Observable arg0, Object arg1) {
		bc = (BluetoothConnection) arg0;
		updateConnectionLabel();
	}

	public void swapConnection(BluetoothConnection bt)
	{
		bc = bt;
		RemoteDevice d = bt.getDevice();
		deviceAddress = d.getBluetoothAddress();
		try
		{
			deviceName = d.getFriendlyName(false);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		updateDeviceInfo();
		parent.changeBluetoothConnection(bt);
	}

	public void newParent(int port)
	{
		SocketConnection sc;
		try {
			sc = new SocketConnection(port);
			setParent(new RobotMaster(sc, timeout));
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent pce) {
		Object source = pce.getSource();
		if (source == portField) {
			int val = ((Number)portField.getValue()).intValue();
			if (0<val && val<=65535) {
				socketPort = val;
			} else {
				portField.setValue(socketPort);
				JOptionPane.showMessageDialog(this,
						"Wybierz numer portu z zakresu 0 - 65535.",
						"Informacja",
						JOptionPane.WARNING_MESSAGE);
			}
		} else if (source == timeoutField) {
			int val = ((Number)timeoutField.getValue()).intValue();
			if (0<val) {
				timeout = val;
			} else {
				timeoutField.setValue(socketPort);
				JOptionPane.showMessageDialog(this,
						"Wybierz timeout w ms.",
						"Informacja",
						JOptionPane.WARNING_MESSAGE);
			}
		}
	}
}
