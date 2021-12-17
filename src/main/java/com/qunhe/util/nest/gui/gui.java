package com.qunhe.util.nest.gui;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.batik.anim.dom.SVG12OMDocument;
import org.apache.batik.ext.awt.geom.Polygon2D;
import org.apache.batik.swing.JSVGCanvas;
import org.dom4j.DocumentException;
import org.w3c.dom.svg.SVGDocument;

import com.qunhe.util.nest.Nest;
import com.qunhe.util.nest.Nest.ListPlacementObserver;
import com.qunhe.util.nest.config.Config;
import com.qunhe.util.nest.data.NestPath;
import com.qunhe.util.nest.data.Placement;
import com.qunhe.util.nest.data.Result;
import com.qunhe.util.nest.util.Placementworker;
import com.qunhe.util.nest.util.SvgUtil;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.Dimension;
import javax.swing.JPanel;

class gui {

	private JFrame frmGUI;
	private JButton btnLoadSVG;
	private JSVGCanvas svgcanvasInput;
	private JLabel lblNewLabel;
	private JSVGCanvas svgcanvasFirst;
	private JSVGCanvas svgcanvasFinal;
	private JLabel lblFirstSolution;
	private JLabel lblFinalSolution;
	private JLabel lblMessage;

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
		frmGUI.setMinimumSize(new Dimension(850, 600));
		frmGUI.setTitle("GUI TEST");
		frmGUI.getContentPane().setBackground(new Color(245, 245, 245));
		frmGUI.setBounds(100, 100, 857, 629);
		frmGUI.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		JButton btnStart = new JButton("Start");
		btnStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				List<NestPath> polygons;
				guiUtil.setMessageLabel("Starting Loading Input File", lblMessage);
				guiUtil.refresh(frmGUI);

				NestPath bin = new NestPath();
				double binWidth = 500;
				double binHeight = 339.235;

				// double binWidth = 1511;
				// double binHeight = 339;

				bin.add(0, 0);
				bin.add(binWidth, 0);
				bin.add(binWidth, binHeight);
				bin.add(0, binHeight);

				List<String> strings = null;
				SVGDocument docFirst = null;
				SVGDocument docFinal = null;
				SVGDocument docInput = null;

				Config config = null;

				Nest nest = null;
				List<List<Placement>> appliedPlacement = null;

				try {
					polygons = guiUtil.transferSvgIntoPolygons();
					// TODO mostrare poligoni non nestati
				} catch (Exception e1) {
					guiUtil.showError("error during import", frmGUI, lblMessage);
					return;
				} finally {
					svgcanvasInput.setSVGDocument(docInput);

					guiUtil.setMessageLabel("Input File loaded", lblMessage);
					guiUtil.refresh(frmGUI);

					// JOptionPane.showMessageDialog(frmGUI, "Input File loaded");
				}

				// bin.bid = -1;
				config = new Config();
				// config.IS_DEBUG=false;
				config.SPACING = 0;
				config.POPULATION_SIZE = 6;
				// si può togliere
				nest = new Nest(bin, polygons, config, 1);
				
				guiUtil.setMessageLabel("Starting Nesting", lblMessage);
				guiUtil.refresh(frmGUI);

				// JOptionPane.showMessageDialog(frmGUI, "Starting Nesting");

				appliedPlacement = nest.startNest();

				try {

					strings = SvgUtil.svgGenerator(polygons, appliedPlacement, binWidth, binHeight);
					docFirst = guiUtil.CreateSvgFile(strings, binWidth, binHeight);

					guiUtil.saveSvgFile(strings, Config.OUTPUT_DIR + "problem.html", binWidth, binHeight);
				} catch (Exception exc) {
					guiUtil.showError("error during first saving", frmGUI, lblMessage);
					return;

				} finally {
					// File f = new File(Config.OUTPUT_DIR+"problem.html");
					// svgcanvasFirst.setURI(f.toURI().toString());
					svgcanvasFirst.setSVGDocument(docFirst);

				}

				// if(null==null) return;
				// find solution
				guiUtil.setMessageLabel("Starting Deeper Nesting", lblMessage);
				// JOptionPane.showMessageDialog(frmGUI, "Starting Deeper Nesting");
				guiUtil.refresh(frmGUI);

				nest = new Nest(bin, polygons, config, 10);
				// aggiungi un observer per osservare il cambiamento ad ogni passo
				nest.observers.add(new ListPlacementObserver() {					
					@Override
					public void populationUpdate(List<List<Placement>> appliedPlacement) {
						System.out.println("best solution cambiata");
						// TODO visualizzala
						try {
							List<String> stringss = SvgUtil.svgGenerator(polygons, appliedPlacement, binWidth, binHeight);
							SVGDocument docFinals = guiUtil.CreateSvgFile(stringss, binWidth, binHeight);
							svgcanvasFinal.setSVGDocument(docFinals);
							guiUtil.refresh(frmGUI);							
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});

				appliedPlacement = nest.startNest();
				try {
					strings = SvgUtil.svgGenerator(polygons, appliedPlacement, binWidth, binHeight);
					docFinal = guiUtil.CreateSvgFile(strings, binWidth, binHeight);
					guiUtil.saveSvgFile(strings, Config.OUTPUT_DIR + "solution.html", binWidth, binHeight);

				} catch (Exception exc2) {
					guiUtil.showError("error during  saving", frmGUI, lblMessage);
					return;
				} finally {
					// File f = new File(Config.OUTPUT_DIR+"solution.html");
					svgcanvasFinal.setSVGDocument(docFinal);
					guiUtil.setMessageLabel("Finished Deeper Nesting", lblMessage);

				}
				guiUtil.refresh(frmGUI);
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
					svgcanvasInput.setURI(selectedFile.toURI().toString());
				}

			}
		});

		svgcanvasInput = new JSVGCanvas();

		lblNewLabel = new JLabel("Input polygons");

		svgcanvasFirst = new JSVGCanvas();

		svgcanvasFinal = new JSVGCanvas();

		lblFirstSolution = new JLabel("First solution");

		lblFinalSolution = new JLabel("Final solution");

		lblMessage = new JLabel("Nesting Tool");
		GroupLayout groupLayout = new GroupLayout(frmGUI.getContentPane());
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout
				.createSequentialGroup()
				.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup().addGap(169).addComponent(lblNewLabel))
						.addGroup(groupLayout.createSequentialGroup().addGap(35)
								.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
										.addComponent(btnLoadSVG, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)
										.addComponent(btnStart, GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE))
								.addGap(28)
								.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
										.addGroup(groupLayout.createSequentialGroup()
												.addComponent(svgcanvasInput, GroupLayout.DEFAULT_SIZE, 631,
														Short.MAX_VALUE)
												.addGap(7))
										.addGroup(groupLayout.createSequentialGroup().addGroup(groupLayout
												.createParallelGroup(Alignment.LEADING)
												.addGroup(groupLayout.createSequentialGroup()
														.addComponent(svgcanvasFirst, GroupLayout.DEFAULT_SIZE, 315,
																Short.MAX_VALUE)
														.addGap(3))
												.addGroup(groupLayout.createSequentialGroup()
														.addComponent(lblFirstSolution, GroupLayout.DEFAULT_SIZE, 78,
																Short.MAX_VALUE)
														.addGap(240)))
												.addGap(0)
												.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
														.addGroup(groupLayout.createSequentialGroup()
																.addComponent(lblFinalSolution,
																		GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
																.addGap(240))
														.addGroup(groupLayout.createSequentialGroup().addGap(1)
																.addComponent(svgcanvasFinal, GroupLayout.DEFAULT_SIZE,
																		314, Short.MAX_VALUE)
																.addGap(5))))))
						.addGroup(groupLayout.createSequentialGroup().addContainerGap().addComponent(lblMessage)))
				.addGap(38)));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout
				.createSequentialGroup().addGap(6).addComponent(lblNewLabel).addGap(6)
				.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
								.addComponent(svgcanvasInput, GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE).addGap(23)
								.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
										.addComponent(lblFirstSolution).addComponent(lblFinalSolution))
								.addPreferredGap(ComponentPlacement.UNRELATED)
								.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
										.addComponent(svgcanvasFirst, GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE)
										.addComponent(svgcanvasFinal, GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE))
								.addGap(35))
						.addGroup(groupLayout.createSequentialGroup().addGap(9).addComponent(btnLoadSVG).addGap(18)
								.addComponent(btnStart)))
				.addComponent(lblMessage).addContainerGap()));
		frmGUI.getContentPane().setLayout(groupLayout);
	}
}
