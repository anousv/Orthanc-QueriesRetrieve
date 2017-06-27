/**
Copyright (C) 2017 VONGSALAT Anousone

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public v.3 License as published by
the Free Software Foundation;

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import ij.plugin.PlugIn;

public class ConnectionSetup extends JFrame implements PlugIn{
	
	private static final long serialVersionUID = 1L;
	private Preferences jpreferPerso = Preferences.userRoot().node("<unnamed>/queryplugin");
	
	public ConnectionSetup(){
		
		this.setTitle("Setup");
		this.setResizable(true);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		JPanel mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		
		JTextField ipTxt = new JTextField();
		ipTxt.setPreferredSize(new Dimension(100,18));
		JTextField portTxt = new JTextField();
		portTxt.setPreferredSize(new Dimension(100,18));
		JTextField usernameTxt = new JTextField();
		usernameTxt.setPreferredSize(new Dimension(100,18));
		JPasswordField passwordTxt = new JPasswordField();
		passwordTxt.setPreferredSize(new Dimension(100,18));
		
		ipTxt.setText(jpreferPerso.get("ip", "http://"));
		portTxt.setText(jpreferPerso.get("port", ""));
		usernameTxt.setText(jpreferPerso.get("username", ""));
		passwordTxt.setText(jpreferPerso.get("password", ""));
		
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.gridx = 0;
		gbc.gridy = 0;
		mainPanel.add(new JLabel("IP"), gbc);
		gbc.gridx = 1;
		mainPanel.add(ipTxt, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		mainPanel.add(new JLabel("Port"), gbc);
		gbc.gridx = 1;
		mainPanel.add(portTxt, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 2;
		mainPanel.add(new JLabel("Username"), gbc);
		gbc.gridx = 1;
		mainPanel.add(usernameTxt, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 3;
		mainPanel.add(new JLabel("Password"), gbc);
		gbc.gridx = 1;
		mainPanel.add(passwordTxt, gbc);
		
		JButton submit = new JButton("Submit");
		submit.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				jpreferPerso.put("ip", ipTxt.getText());
				jpreferPerso.put("port", portTxt.getText());
				jpreferPerso.put("password", new String(passwordTxt.getPassword()));
				jpreferPerso.put("username", usernameTxt.getText());
				dispose();
			}
		});
		
		gbc.gridy = 4;
		mainPanel.add(submit, gbc);
		
		Image image = new ImageIcon(ClassLoader.getSystemResource("OrthancIcon.png")).getImage();
		this.setIconImage(image);
		this.getContentPane().add(mainPanel);
	}
	
	public static void main(String... arg0){
		ConnectionSetup vue = new ConnectionSetup();
		vue.setSize(1200, 400);
		vue.pack();
		vue.setVisible(true);
	}
	
	@Override
	public void run(String arg0) {
		ConnectionSetup vue = new ConnectionSetup();
		vue.setSize(1200, 400);
		vue.pack();
		vue.setVisible(true);
	}
}
