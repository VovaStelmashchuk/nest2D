package com.qunhe.util.nest.gui;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
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

import org.apache.batik.ext.awt.geom.Polygon2D;
import org.apache.batik.swing.JSVGCanvas;
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
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.Dimension;
import de.lighti.clipper.Path;
import de.lighti.clipper.Point.LongPoint;
import de.lighti.clipper.gui.PolygonCanvas;
import de.lighti.clipper.gui.StatusBar;


/**
 * @author  Alberto Gambarara
 */
class gui {

	private JFrame frmGUI;
	private JButton btnLoadSVG;
	private JSVGCanvas svgcanvasInput;
	private JLabel lblNewLabel;
	private JSVGCanvas svgcanvasFinal;
	private JLabel lblFinalSolution;
	private JLabel lblMessage;
	private PolygonCanvas inputpolygoncanvas;

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
		frmGUI.setBounds(100, 100, 984, 674);
		frmGUI.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		JButton btnStart = new JButton("Start");
		btnStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				List<NestPath> polygons;
				guiUtil.setMessageLabel("Starting Loading Input File", lblMessage);
				guiUtil.refresh(frmGUI);

				NestPath bin = new NestPath();
			
				double binWidth = 420;
				double binHeight = 420;

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
					inputpolygoncanvas.getSubjects().clear();
					inputpolygoncanvas.getClips().clear();
					
			        Map<String, List<NestPath>> nfpCache = new HashMap<>();
			        Placementworker placementworker = new Placementworker(bin,config,nfpCache);
			        Result result = placementworker.placePaths(polygons);

					
					for (NestPath p: polygons)
					{
						Polygon2D newp = p.toPolygon2D();												
						final Path clip = new Path( newp.npoints );						
						for (int i = 0; i < newp.npoints; ++i) {
				            clip.add( new LongPoint( (long)newp.xpoints[i], (long)newp.ypoints[i] ) );
				        }
						
						inputpolygoncanvas.getClips().add( clip );
					}
					
					
					inputpolygoncanvas.updateSolution();
				} catch (Exception e1) {
					guiUtil.showError("error during import", frmGUI, lblMessage);
					return;
				} finally {			
					
					svgcanvasInput.setSVGDocument(docInput);
					guiUtil.setMessageLabel("Input File loaded", lblMessage);
					guiUtil.refresh(frmGUI);
					// JOptionPane.showMessageDialog(frmGUI, "Input File loaded");
				}
								
				// find solution
				guiUtil.setMessageLabel("Starting Nesting", lblMessage);
				JOptionPane.showMessageDialog(frmGUI, "Starting Nesting");
				guiUtil.refresh(frmGUI);
				
				config = new Config();
				// config.IS_DEBUG=false;
				config.SPACING = 0;
				config.POPULATION_SIZE = 60;
				config.BIN_HEIGHT=binHeight;
				config.BIN_WIDTH=binWidth;
				
				nest = new Nest(bin, polygons, config, 100);
				// aggiungi un observer per osservare il cambiamento ad ogni passo
				nest.observers.add(new ListPlacementObserver() {					
					@Override
					public void populationUpdate(List<List<Placement>> appliedPlacement) {
						System.out.println("best solution cambiata");
						try {
							List<String> strings = SvgUtil.svgGenerator(polygons, appliedPlacement, binWidth, binHeight);
							SVGDocument docFinals = guiUtil.CreateSvgFile(strings, binWidth, binHeight);
							//if(svgcanvasFirst.getSVGDocument()==null) svgcanvasFirst.setSVGDocument(docFinals);
							svgcanvasFinal.setSVGDocument(docFinals);
							guiUtil.refresh(frmGUI);
							JOptionPane.showMessageDialog(frmGUI, "best solution cambiata");
						} catch (Exception e) {
							guiUtil.showError("error showing solution: " + e.getMessage(), frmGUI, lblMessage);
							return;							
						}
					}
				});

				appliedPlacement = nest.startNest();
				guiUtil.setMessageLabel("Nesting finished", lblMessage);
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

		svgcanvasFinal = new JSVGCanvas();

		lblFinalSolution = new JLabel("Best solution");

		lblMessage = new JLabel("Nesting Tool");
		
		inputpolygoncanvas = new PolygonCanvas(new StatusBar());
		GroupLayout groupLayout = new GroupLayout(frmGUI.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(35)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
								.addComponent(btnLoadSVG, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(btnStart, GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE))
							.addGap(28)
							.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
								.addGroup(groupLayout.createSequentialGroup()
									.addComponent(inputpolygoncanvas, GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE)
									.addGap(28)
									.addComponent(svgcanvasInput, GroupLayout.PREFERRED_SIZE, 287, GroupLayout.PREFERRED_SIZE)
									.addGap(7))
								.addGroup(groupLayout.createSequentialGroup()
									.addComponent(svgcanvasFinal, GroupLayout.DEFAULT_SIZE, 777, Short.MAX_VALUE)
									.addGap(5))
								.addComponent(lblFinalSolution, GroupLayout.DEFAULT_SIZE, 782, Short.MAX_VALUE)
								.addComponent(lblNewLabel, Alignment.LEADING)))
						.addGroup(groupLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblMessage)))
					.addGap(38))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(10)
					.addComponent(lblNewLabel)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(svgcanvasInput, GroupLayout.DEFAULT_SIZE, 255, Short.MAX_VALUE)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(9)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(inputpolygoncanvas, GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE)
								.addComponent(btnLoadSVG))))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(18)
							.addComponent(btnStart))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(lblFinalSolution)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(svgcanvasFinal, GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE)
							.addGap(35)))
					.addComponent(lblMessage)
					.addContainerGap())
		);
		frmGUI.getContentPane().setLayout(groupLayout);
	}
}
