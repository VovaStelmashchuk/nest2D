package com.qunhe.util.nest.gui;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.batik.swing.JSVGCanvas;

class gui {

	private JFrame frmGUI;
	private JButton btnLoadSVG;
	private JSVGCanvas svgcanvas;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {

					gui window = new gui();
					window.frmGUI.setVisible(true);
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
		frmGUI = new JFrame();
		frmGUI.setTitle("GUI TEST");
		frmGUI.getContentPane().setBackground(new Color(245, 245, 245));
		frmGUI.setBounds(100, 100, 708, 549);
		frmGUI.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		JButton btnStart = new JButton("Start");
		btnStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

			}
		});

		btnLoadSVG = new JButton("LOAD SVG");
		btnLoadSVG.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				// svgcanvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);

				JFileChooser fileChooser = new JFileChooser();

				fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("SVG files", "svg"));
				// fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
				fileChooser.setCurrentDirectory(new File("./samples"));
				int result = fileChooser.showOpenDialog(fileChooser);
				if (result == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					System.out.println("Selected file: " + selectedFile.getAbsolutePath());
					svgcanvas.setURI(selectedFile.toURI().toString());
				}

			}
		});

		svgcanvas = new JSVGCanvas();
		GroupLayout groupLayout = new GroupLayout(frmGUI.getContentPane());
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup().addGap(35)
						.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING).addComponent(btnLoadSVG)
								.addComponent(btnStart, GroupLayout.PREFERRED_SIZE, 85, GroupLayout.PREFERRED_SIZE))
						.addGap(101).addComponent(svgcanvas, GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE)
						.addGap(21)));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup().addGap(28)
						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup().addComponent(btnLoadSVG).addGap(182)
										.addComponent(btnStart))
								.addComponent(svgcanvas, GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE))
						.addGap(30)));
		frmGUI.getContentPane().setLayout(groupLayout);
	}
}
