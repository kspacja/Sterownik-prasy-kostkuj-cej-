package comm;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import javax.bluetooth.RemoteDevice;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.border.EtchedBorder;

import comm.BluetoothConnection;
import comm.RobotMaster;

public class NxtBluetoothGUI extends JFrame implements ActionListener, Observer {

	private static final long serialVersionUID = -3722504484672472629L;
	
	RobotMaster parent;
	RemoteDevice device;
	BluetoothConnection bc;
	
	ImageIcon goodIcon, badIcon, noneIcon, procIcon;

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
	private javax.swing.JPanel actionPanel;
	private javax.swing.JLabel actionLabel;

	public NxtBluetoothGUI(RobotMaster parent) {
		this.parent = parent;
		device = null;
		bc = null;

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

		initComponents();
	}
	
	// Pomocnicza metoda tworzaca obiekt ImageIcon ze sciezki
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
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		
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
		
	// Connection panel
		connectionPanel = new javax.swing.JPanel();
		connectionStatusLabel = new javax.swing.JLabel();
		actionPanel = new javax.swing.JPanel();
		actionLabel = new javax.swing.JLabel();
		
//		connectionPanel.setLayout(new BoxLayout(connectionPanel, BoxLayout.Y_AXIS));
		connectionPanel.setLayout(new BorderLayout());
		connectionPanel.setPreferredSize(new Dimension(400,100));
		connectionStatusLabel.setHorizontalAlignment(JLabel.CENTER);
		connectionStatusLabel.setIconTextGap(15);
		connectionStatusLabel.setFont(new Font(null, Font.PLAIN, 25));
		actionPanel.add(actionLabel);
		
		connectionPanel.add(connectionStatusLabel, BorderLayout.CENTER);
		connectionPanel.add(actionLabel, BorderLayout.SOUTH);
		
	// Wrzuc wszystko na contentpane
		setLayout(new BorderLayout());
		add(devicePanel, BorderLayout.NORTH);
		add(connectionPanel, BorderLayout.CENTER);
		
		updateDeviceInfo();
		updateConnectionLabel();
		
		pack();
		setMinimumSize(getSize());
	}
	
//	wywolywane z selectdevice
	public void changeDevice(RemoteDevice d) {
		if (device == null || device.getBluetoothAddress() != d.getBluetoothAddress()) {
			device = d;
			updateDeviceInfo();
			connectToDevice();
		}
	}
	
	public void connectToDevice() {
		if (device!=null) {
			bc = new BluetoothConnection(device, this);
			parent.changeBluetoothConnection(bc);
		} else System.err.println("Wywołanie connectToDevice() przy device==null");
	}
	
	private void updateDeviceInfo() {
		if (device == null) {
			deviceNameLabel.setText("<brak>");
			deviceAddressLabel.setText(" ");
		} else {
			try {
				deviceNameLabel.setText("Nazwa: "+device.getFriendlyName(false));
			} catch (IOException e) {
				deviceNameLabel.setText("Nazwa niedostępna");
			}
			deviceAddressLabel.setText("Adres: "+device.getBluetoothAddress());
			
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
		}
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		bc = (BluetoothConnection) arg0;
		updateConnectionLabel();
	}

}


