package com.qunhe.util.nest.gui;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import org.apache.batik.ext.awt.geom.Polygon2D;
import org.apache.batik.swing.JSVGCanvas;
import org.w3c.dom.svg.SVGDocument;

import com.qunhe.util.nest.Nest;
import com.qunhe.util.nest.Nest.ListPlacementObserver;
import com.qunhe.util.nest.config.Config;
import com.qunhe.util.nest.data.NestPath;
import com.qunhe.util.nest.data.Placement;
import com.qunhe.util.nest.util.SvgUtil;

import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.Dimension;
import de.lighti.clipper.Path;
import de.lighti.clipper.Point.LongPoint;
import de.lighti.clipper.gui.PolygonCanvas;
import de.lighti.clipper.gui.StatusBar;
import java.awt.SystemColor;


/**
 * @author  Alberto Gambarara
 */
class gui {

	private JFrame frmGUI;
	private JButton btnLoad;
	private JLabel lblNewLabel;
	private JSVGCanvas svgcanvasFinal;
	private JLabel lblFinalSolution;
	private JLabel lblMessage;
	private PolygonCanvas inputPolygonCanvas;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		System.setProperty("sun.java2d.uiScale", "1"); // disable high-dpi

		EventQueue.invokeLater(() -> {
			try {
				gui window = new gui();
				window.frmGUI.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
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
		frmGUI.getContentPane().setBackground(SystemColor.menu);
		frmGUI.setBounds(100, 100, 984, 900);
		frmGUI.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frmGUI.setLocationRelativeTo(null); // center in screen
		
		JButton btnStart = new JButton("START");
		btnStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				List<NestPath> polygons;
				guiUtil.setMessageLabel("Starting Loading Input File", lblMessage);
				guiUtil.refresh(frmGUI);

				NestPath bin = new NestPath();
			
				double binWidth = 200;
				double binHeight = 200;

				bin.add(0, 0);
				bin.add(binWidth, 0);
				bin.add(binWidth, binHeight);
				bin.add(0, binHeight);

				List<List<Placement>> appliedPlacement = null;
								
				try {
					polygons = guiUtil.transferSvgIntoPolygons();
//					inputpolygoncanvas.getSubjects().clear();
//					inputpolygoncanvas.getClips().clear();
					
			       /// Map<String, List<NestPath>> nfpCache = new HashMap<>();
//			        Placementworker placementworker = new Placementworker(bin,config,nfpCache);
//			        Result result = placementworker.placePaths(polygons);

					
//					for (NestPath p: polygons)
//					{
//						Polygon2D newp = p.toPolygon2D();												
//						final Path clip = new Path( newp.npoints );						
//						for (int i = 0; i < newp.npoints; ++i) {
//				            clip.add( new LongPoint( (long)newp.xpoints[i], (long)newp.ypoints[i] ) );
//				        }
//						
//						inputpolygoncanvas.getClips().add( clip );
//					}
					
					
					//inputPolygonCanvas.updateSolution();
				} catch (Exception e1) {
					guiUtil.showError("error during import", frmGUI, lblMessage);
					return;
				} finally {			
					
					//svgcanvasInput.setSVGDocument(docInput);
					guiUtil.setMessageLabel("Input File loaded", lblMessage);
					guiUtil.refresh(frmGUI);
					// JOptionPane.showMessageDialog(frmGUI, "Input File loaded");
				}
								
				// find solution
//				guiUtil.setMessageLabel("Starting Nesting", lblMessage);
//				JOptionPane.showMessageDialog(frmGUI, "Starting Nesting");
				guiUtil.refresh(frmGUI);
				
				Config config = new Config();
				// config.IS_DEBUG=false;
				config.SPACING = 0;
				config.POPULATION_SIZE = 10;
				Config.BIN_HEIGHT=binHeight;
				Config.BIN_WIDTH=binWidth;
				
				for (NestPath np : polygons) {
					np.setPossibleNumberRotations(4);
				}
				
				Nest nest = new Nest(bin, polygons, config, 25);
				// aggiungi un observer per osservare il cambiamento ad ogni passo
				nest.observers.add(new ListPlacementObserver() {					
					@Override
					public void populationUpdate(List<List<Placement>> appliedPlacement) {
						//System.out.println("best solution changed");
						try {
							List<String> strings = SvgUtil.svgGenerator(polygons, appliedPlacement, binWidth, binHeight);
							SVGDocument docFinals = guiUtil.CreateSvgFile(strings, binWidth, binHeight);
							//if(svgcanvasFirst.getSVGDocument()==null) svgcanvasFirst.setSVGDocument(docFinals);
							svgcanvasFinal.setSVGDocument(docFinals);
							guiUtil.refresh(frmGUI);
							//JOptionPane.showMessageDialog(frmGUI, "best solution changed");
						} catch (Exception e) {
							guiUtil.showError("error showing solution: " + e.getMessage(), frmGUI, lblMessage);
							return;							
						}
					}
				});

				long startTime = System.nanoTime();
				appliedPlacement = nest.startNest();
				long elapsedTime = System.nanoTime() - startTime;
				
				try {
					List<String> strings = SvgUtil.svgGenerator(polygons, appliedPlacement, binWidth, binHeight);
					guiUtil.saveSvgFile(strings,Config.OUTPUT_DIR+"solution.html");
				}catch (Exception ex) {
					guiUtil.showError("error saving solution: " + ex.getMessage(), frmGUI, lblMessage);
					return;
				}
				guiUtil.setMessageLabel("Nesting finished in " + elapsedTime / 1000000 + "ms", lblMessage);
				guiUtil.refresh(frmGUI);

				System.out.println("Total execution in millis: " + elapsedTime / 1000000);
			}
		});

		btnLoad = new JButton("LOAD XML");
		btnLoad.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

//				// svgcanvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);
//				JFileChooser fileChooser = new JFileChooser();
//
//				fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("SVG files", "svg"));
//				// fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
//				fileChooser.setCurrentDirectory(new File("./samples"));
//				int result = fileChooser.showOpenDialog(fileChooser);
//				if (result == JFileChooser.APPROVE_OPTION) {
//					File selectedFile = fileChooser.getSelectedFile();
//					System.out.println("Selected file: " + selectedFile.getAbsolutePath());
//					svgcanvasInput.setURI(selectedFile.toURI().toString());
//				}
				//List<NestPath> polygons;
				
				try {
					List<NestPath> polygons = guiUtil.transferSvgIntoPolygons();
					inputPolygonCanvas.getSubjects().clear();
					inputPolygonCanvas.getClips().clear();
					
			        //Map<String, List<NestPath>> nfpCache = new HashMap<>();
			        //Placementworker placementworker = new Placementworker(bin,config,nfpCache);
			        //Result result = placementworker.placePaths(polygons);

					
					for (NestPath p: polygons)
					{
						Polygon2D newp = p.toPolygon2D();												
						final Path clip = new Path( newp.npoints );						
						for (int i = 0; i < newp.npoints; ++i) {
				            clip.add( new LongPoint( (long)newp.xpoints[i], (long)newp.ypoints[i] ) );
				        }
						
						inputPolygonCanvas.getClips().add( clip );
					}
					
					
					inputPolygonCanvas.updateSolution();
				}
				catch (Exception exc) {
					guiUtil.showError("error during import", frmGUI, lblMessage);
					return;
				}
				
			}
		});

		lblNewLabel = new JLabel("Input polygons");

		svgcanvasFinal = new JSVGCanvas();

		lblFinalSolution = new JLabel("Best solution");

		lblMessage = new JLabel("Nesting tool output");
		
		inputPolygonCanvas = new PolygonCanvas(new StatusBar());
		GroupLayout groupLayout = new GroupLayout(frmGUI.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(35)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(btnLoad, GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE)
								.addComponent(btnStart, GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE))
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
								.addGroup(groupLayout.createSequentialGroup()
									.addComponent(svgcanvasFinal, GroupLayout.DEFAULT_SIZE, 777, Short.MAX_VALUE)
									.addGap(5))
								.addComponent(lblFinalSolution, GroupLayout.DEFAULT_SIZE, 782, Short.MAX_VALUE)
								.addComponent(lblNewLabel, Alignment.LEADING)
								.addComponent(inputPolygonCanvas, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 787, Short.MAX_VALUE)))
						.addGroup(groupLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblMessage)))
					.addGap(38))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(19)
					.addComponent(lblNewLabel)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
							.addComponent(inputPolygonCanvas, GroupLayout.DEFAULT_SIZE, 359, Short.MAX_VALUE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(lblFinalSolution))
						.addComponent(btnLoad, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(svgcanvasFinal, GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE)
							.addGap(35))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(btnStart, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)))
					.addComponent(lblMessage)
					.addContainerGap())
		);
		frmGUI.getContentPane().setLayout(groupLayout);
	}
}
