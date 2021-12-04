package com.qunhue.util.nest.gui;

import java.awt.EventQueue;

import java.awt.Label;

import javax.swing.JFrame;
import javax.swing.BoxLayout;
import javax.swing.SpringLayout;
import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Color;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.Document;
import javax.swing.JTextField;
import javax.swing.JLabel;
import java.awt.Component;
import javax.swing.Box;
import javax.swing.JToggleButton;
import javax.swing.JSpinner;
import javax.swing.JSlider;
import java.awt.Button;
import java.awt.TextField;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.awt.Dimension;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;



import org.apache.batik.swing.*;
import org.apache.batik.swing.svg.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.awt.Event.*;


public class gui {

	private JFrame frmTest;
	private JButton btnNewButton_1;
	private JSVGCanvas svgcanvas;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					
					
					
					
					
					gui window = new gui();
					window.frmTest.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public gui() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmTest = new JFrame();
		frmTest.setTitle("GUI TEST");
		frmTest.getContentPane().setBackground(new Color(245, 245, 245));
		frmTest.setBounds(100, 100, 708, 549);
		frmTest.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JButton btnNewButton = new JButton("Start");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			
				
				
			
			}
		});
		
		btnNewButton_1 = new JButton("LOAD SVG");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				
				//svgcanvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);
				
				JFileChooser fileChooser = new JFileChooser();
				
				fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("SVG files", "svg"));
				fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
				int result = fileChooser.showOpenDialog(fileChooser);
				if (result == JFileChooser.APPROVE_OPTION) {
				    File selectedFile = fileChooser.getSelectedFile();
				    System.out.println("Selected file: " + selectedFile.getAbsolutePath());
				    svgcanvas.setURI(selectedFile.toURI().toString());
				}
				
				
				
				
			}
		});
		
		svgcanvas = new JSVGCanvas();
		GroupLayout groupLayout = new GroupLayout(frmTest.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(35)
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addComponent(btnNewButton_1)
						.addComponent(btnNewButton, GroupLayout.PREFERRED_SIZE, 85, GroupLayout.PREFERRED_SIZE))
					.addGap(101)
					.addComponent(svgcanvas, GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE)
					.addGap(21))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(28)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(btnNewButton_1)
							.addGap(182)
							.addComponent(btnNewButton))
						.addComponent(svgcanvas, GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE))
					.addGap(30))
		);
		frmTest.getContentPane().setLayout(groupLayout);
	}
}
