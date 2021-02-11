package main.ui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import main.network.Client;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import java.awt.event.ActionListener;
import java.util.List;
import java.awt.event.ActionEvent;
import javax.swing.AbstractListModel;

public class ClientUI {

	private JFrame frame;
	private JTextField textField;
	
	private Client client;
	private JTextField textFieldContact;
	JPanel panelContacts = new JPanel();
	JList<String> listContacts = new JList();

	private List<String> contacts;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientUI window = new ClientUI(args[0]);
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ClientUI(String title) {
		client = new Client(title);
		new Thread(client).start();
		initialize(title);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize(String title) {
		frame = new JFrame();
		frame.setTitle(title);
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JPanel panelText = new JPanel();
		panelText.setBounds(10, 213, 416, 40);
		frame.getContentPane().add(panelText);
		panelText.setLayout(null);
		
		textField = new JTextField();
		textField.setBounds(10, 10, 301, 20);
		panelText.add(textField);
		textField.setColumns(10);
		
		JButton btnSend = new JButton("Send");
		btnSend.setEnabled(false);
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				client.send(listContacts.getSelectedValue(), textField.getText());
			}
		});
		btnSend.setBounds(321, 9, 85, 21);
		panelText.add(btnSend);
		
		panelContacts.setBounds(10, 10, 86, 193);
		frame.getContentPane().add(panelContacts);
		panelContacts.setLayout(null);
		
		textFieldContact = new JTextField();
		textFieldContact.setBounds(0, 0, 86, 19);
		panelContacts.add(textFieldContact);
		textFieldContact.setColumns(10);

		
		listContacts.setBounds(10, 46, 66, 147);
		panelContacts.add(listContacts);
		listContacts.setModel(new AbstractListModel<String>() {
			String[] values = new String[] {"hello"};
			public int getSize() {
				return values.length;
			}
			public String getElementAt(int index) {
				return values[index];
			}
		});
		listContacts.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent arg0) {
                if (!arg0.getValueIsAdjusting()) {
                  btnSend.setEnabled(true);
                }
            }
        });
		
		JButton btnAddContact = new JButton("Add");
		btnAddContact.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				client.addContact(textFieldContact.getText());
				getAndShowContacts();
			}
		});
		btnAddContact.setBounds(0, 22, 85, 21);
		panelContacts.add(btnAddContact);
		
	}
	
	public void getAndShowContacts() {
		contacts = client.getContacts();
		DefaultListModel<String> model = new DefaultListModel<String>();
		for(String c : contacts) {
			model.addElement(c);
		}
		listContacts.setModel(model);
	}
}

