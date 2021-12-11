package com.qunhe.util.nest.gui;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.batik.swing.JSVGCanvas;
import org.dom4j.DocumentException;

import com.qunhe.util.nest.Nest;
import com.qunhe.util.nest.config.Config;
import com.qunhe.util.nest.data.NestPath;
import com.qunhe.util.nest.data.Placement;
import com.qunhe.util.nest.util.SvgUtil;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.Dimension;


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
		        SwingUtilities.updateComponentTreeUI(frmGUI);
		        frmGUI.invalidate();
		        frmGUI.validate();
		        frmGUI.repaint();
				try {
					polygons = guiUtil.transferSvgIntoPolygons();
				} catch (DocumentException e1) {
					guiUtil.showError("error during import", frmGUI, lblMessage);
					return;
				}finally {
			        guiUtil.setMessageLabel("Input File loaded", lblMessage);
			        JOptionPane.showMessageDialog(frmGUI, "Input File loaded");

				}
		        NestPath bin = new NestPath();
		        double binWidth = 500;
		        double binHeight = 339.235;
		        
		        //double binWidth = 1511;
		        //double binHeight = 339;
		        
		        
		        bin.add(0, 0);
		        bin.add(binWidth, 0);
		        bin.add(binWidth, binHeight);
		        bin.add(0, binHeight);
		        bin.bid = -1;
		        Config config = new Config();
		        config.SPACING = 0;
		        config.POPULATION_SIZE = 6;
		        Nest nest = new Nest(bin, polygons, config, 1);
		        guiUtil.setMessageLabel("Starting Nesting", lblMessage);
		        JOptionPane.showMessageDialog(frmGUI, "Starting Nesting");

		        List<List<Placement>> appliedPlacement = nest.startNest();
		        List<String> strings;
		        try {
			        strings = SvgUtil.svgGenerator(polygons, appliedPlacement, binWidth, binHeight);
			        guiUtil.saveSvgFile(strings,Config.OUTPUT_DIR+"problem.html",binWidth, binHeight);
		        }catch (Exception exc) {
					guiUtil.showError("error during first saving", frmGUI, lblMessage);
					return;
					
					
				}
		        
		        File f = new File(Config.OUTPUT_DIR+"problem.html");
				svgcanvasFirst.setURI(f.toURI().toString());
		        
		        //if(null==null) return;
		        // find solution
		        guiUtil.setMessageLabel("Starting Deeper Nesting", lblMessage);
		        JOptionPane.showMessageDialog(frmGUI, "Starting Deeper Nesting");


		        nest = new Nest(bin, polygons, config, 10);
		        appliedPlacement = nest.startNest();
		        try {
			        strings = SvgUtil.svgGenerator(polygons, appliedPlacement, binWidth, binHeight);
			        guiUtil.saveSvgFile(strings,Config.OUTPUT_DIR+"solution.html", binWidth, binHeight);

		        }catch (Exception exc2) {
		        	guiUtil.showError("error during  saving", frmGUI, lblMessage);
					return;				
				}
		        finally
		        {
		        	f = new File(Config.OUTPUT_DIR+"solution.html");
					svgcanvasFinal.setURI(f.toURI().toString());
			        guiUtil.setMessageLabel("Finished Deeper Nesting", lblMessage);

		        }
		        
				
				
				

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
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(169)
							.addComponent(lblNewLabel))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(35)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(btnLoadSVG)
								.addComponent(btnStart, GroupLayout.PREFERRED_SIZE, 85, GroupLayout.PREFERRED_SIZE))
							.addGap(45)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup()
									.addComponent(svgcanvasInput, GroupLayout.DEFAULT_SIZE, 631, Short.MAX_VALUE)
									.addGap(7))
								.addGroup(groupLayout.createSequentialGroup()
									.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
										.addGroup(groupLayout.createSequentialGroup()
											.addComponent(svgcanvasFirst, GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE)
											.addGap(3))
										.addGroup(groupLayout.createSequentialGroup()
											.addComponent(lblFirstSolution, GroupLayout.DEFAULT_SIZE, 78, Short.MAX_VALUE)
											.addGap(240)))
									.addGap(0)
									.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
										.addGroup(groupLayout.createSequentialGroup()
											.addComponent(lblFinalSolution, GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
											.addGap(240))
										.addGroup(groupLayout.createSequentialGroup()
											.addGap(1)
											.addComponent(svgcanvasFinal, GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)
											.addGap(5))))))
						.addGroup(groupLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblMessage)))
					.addGap(38))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(6)
					.addComponent(lblNewLabel)
					.addGap(6)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(9)
							.addComponent(btnLoadSVG)
							.addGap(18)
							.addComponent(btnStart))
						.addComponent(svgcanvasInput, GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE))
					.addGap(23)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblFirstSolution)
						.addComponent(lblFinalSolution))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(svgcanvasFirst, GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE)
						.addComponent(svgcanvasFinal, GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE))
					.addGap(35)
					.addComponent(lblMessage)
					.addContainerGap())
		);
		frmGUI.getContentPane().setLayout(groupLayout);
	}
}
