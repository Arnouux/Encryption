package main.ui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JTextField;

import main.network.Client;

import javax.swing.JButton;
import javax.swing.JList;
import java.awt.event.ActionListener;
import java.util.List;
import java.awt.event.ActionEvent;

public class ClientUI {

	private JFrame frame;
	private JTextField textField;
	
	private Client client;
	private JTextField textFieldContact;

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
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				client.send("user2", textField.getText());
			}
		});
		btnSend.setBounds(321, 9, 85, 21);
		panelText.add(btnSend);
		
		JPanel panelContacts = new JPanel();
		panelContacts.setBounds(10, 10, 86, 193);
		frame.getContentPane().add(panelContacts);
		panelContacts.setLayout(null);
		
		textFieldContact = new JTextField();
		textFieldContact.setBounds(0, 0, 86, 19);
		panelContacts.add(textFieldContact);
		textFieldContact.setColumns(10);
		
		JList<String> listContacts = new JList<String>();
		listContacts.setBounds(80, 59, -73, 124);
		panelContacts.add(listContacts);
		
		JButton btnAddContact = new JButton("Add");
		btnAddContact.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				client.addContact(textFieldContact.getText());
				contacts = client.getContacts();
				listContacts.setListData(contacts.toArray(new String[1]));
				frame.invalidate();
			}
		});
		btnAddContact.setBounds(0, 22, 85, 21);
		panelContacts.add(btnAddContact);

		
		JPanel panelMessages = new JPanel();
		panelMessages.setBounds(106, 10, 320, 193);
		frame.getContentPane().add(panelMessages);
	}
}
