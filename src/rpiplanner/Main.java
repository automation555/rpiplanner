/* RPI Planner - Customized plans of study for RPI students.
 *
 * Copyright (C) 2008 Eric Allen allene2@rpi.edu
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package rpiplanner;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.OrientationRequested;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;

import rpiplanner.model.Course;
import rpiplanner.model.DefaultCourseDatabase;
import rpiplanner.model.PlanOfStudy;
import rpiplanner.model.ShadowCourseDatabase;
import rpiplanner.model.Term;
import rpiplanner.model.YearPart;
import xml.RequisiteSetConverter;

import com.apple.eawt.ApplicationEvent;
import com.thoughtworks.xstream.XStream;

/**
 *
 * @author Eric Allen
 */
public class Main extends Application {
    private MainFrame mainFrame;
    private static POSController planControl;
    private static ShadowCourseDatabase courseDatabase;
	private static XStream xs;
	private boolean newPlan;

    @Override
    protected void startup() {
        planControl = new POSController();

        loadFromXML();
        PlanValidator.getInstance(); // boot up ruby environment
        planControl.setCourseDatabase(courseDatabase);
        mainFrame = new MainFrame();
        planControl.setView(mainFrame.getPlanCard());
        mainFrame.setController(planControl);
        if(!newPlan)
        	mainFrame.goToPlanEditor(false);
        
        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                mainFrame.setVisible(false);
                exit();
            }
        });
        
        initializeMenuBar();

        try {
        	initializeOSXExtensions();
        } catch (ClassNotFoundException e){
        	// silently ignore because we're not on a mac
        } catch (NullPointerException e){
        	// silently ignore because we're not on a mac
        }
        mainFrame.setVisible(true);
    }
    
    private javax.swing.Action getAction(String actionName) {
        ApplicationContext ac = this.getContext();
        return ac.getActionMap(getClass(), this).get(actionName);
    }
    
	private void initializeMenuBar() {
		final JMenuBar menu = new JMenuBar();
		final JMenu fileMenu = new JMenu("Plan");
		
		fileMenu.add(getAction("savePlan"));
		fileMenu.add(getAction("loadPlan"));
		fileMenu.add(getAction("print"));
		fileMenu.add(getAction("details"));
		
		final JMenu helpMenu = new JMenu("Help");
		helpMenu.add(getAction("feedback"));

		final JMenuItem fileUpdate = new JMenuItem("Update from course database");
		fileUpdate.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				planControl.updatePlanFromDatabase();
			}
		});
		fileMenu.add(fileUpdate);

		menu.add(fileMenu);
		menu.add(helpMenu);
		mainFrame.setJMenuBar(menu);
	}

	protected void loadFromXML(){
		File localStorageDir = getContext().getLocalStorage().getDirectory();
		DefaultCourseDatabase mainDB = (DefaultCourseDatabase) xs.fromXML(getClass().getResourceAsStream("/course_database.xml"));
		ShadowCourseDatabase shadowDB = null;
		try {
			shadowDB = (ShadowCourseDatabase) (xs.fromXML(new FileInputStream(new File(localStorageDir, "course_database.xml"))));
		} catch (IOException e1) {
			shadowDB = new ShadowCourseDatabase();
		}
		if(shadowDB == null)
			shadowDB = new ShadowCourseDatabase();
		
		shadowDB.shadow(mainDB);
		courseDatabase = shadowDB;

		try {
			planControl.setPlan((PlanOfStudy) xs.fromXML(new FileInputStream(new File(localStorageDir, "default_pos.xml"))));
			if(planControl.getPlan().numTerms() == SchoolInformation.getDefaultSemesterCount()){
				// legacy updating
				Term apTerm = new Term();
				apTerm.setYear(0);
				planControl.getPlan().getTerms().add(0, apTerm);
			}
			newPlan = false;
		} catch (FileNotFoundException e) { // not there, use a blank one
			newPlan = true;
			planControl.setPlan(new PlanOfStudy());
		}
    }
	
	private void initializeOSXExtensions() throws ClassNotFoundException {
		com.apple.eawt.Application fApplication = com.apple.eawt.Application.getApplication();
		fApplication.setEnabledPreferencesMenu(false);
		fApplication.setEnabledAboutMenu(false);
		fApplication.addApplicationListener(new com.apple.eawt.ApplicationAdapter() {
			public void handleAbout(ApplicationEvent e) {
			}
			public void handleOpenApplication(ApplicationEvent e) {
			}
			public void handleOpenFile(ApplicationEvent e) {
			}
			public void handlePreferences(ApplicationEvent e) {
			}
			public void handlePrintFile(ApplicationEvent e) {
			}
			public void handleQuit(ApplicationEvent e) {
				quit(null);
			}
		});
	}
	
    @Override
    protected void shutdown() {
    	try {
    		File localStorageDir = getContext().getLocalStorage().getDirectory();
    		localStorageDir.mkdir();

    		File cdbFile = new File(localStorageDir, "course_database.xml");
       		File planFile = new File(localStorageDir, "default_pos.xml");
       		
			xs.toXML(courseDatabase, new FileOutputStream(cdbFile));
			xs.toXML(planControl.getPlan(), new FileOutputStream(planFile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    @Action
    public void savePlan(){
	    JFileChooser chooser = new JFileChooser();
        int returnVal = chooser.showSaveDialog(mainFrame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if(!file.getName().endsWith(".plan")){
            	file = new File(file.getPath()+".plan");
            }
    		try {
				xs.toXML(planControl.getPlan(), new FileOutputStream(file));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }
    
    @Action
    public void loadPlan(){
	    JFileChooser chooser = new JFileChooser();
	    chooser.setFileFilter(new FileFilter(){
			@Override
			public boolean accept(File f) {
				return f.isFile() && f.getName().endsWith(".plan");
			}
			@Override
			public String getDescription() {
				return "Plan of Study file";
			}
	    });
	    int option = chooser.showOpenDialog(mainFrame);
	    if (option == JFileChooser.APPROVE_OPTION) {
	    	File file = chooser.getSelectedFile();
			try {
				planControl.setPlan((PlanOfStudy) xs.fromXML(new FileInputStream(file)));
				planControl.validatePlan();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
    }

	public static PlanOfStudy loadPlanFromFile(String filePath) {
		InputStream in = Main.class.getResourceAsStream("/"+filePath);
		if(in == null)
			return null;
		PlanOfStudy plan = (PlanOfStudy) xs.fromXML(in);
		try {
			in.close();
		} catch (IOException e) {}
		return plan;
	}
    
    @Action
    public void print() {
		PrinterJob job = PrinterJob.getPrinterJob();
		PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
		aset.add(OrientationRequested.LANDSCAPE);
		job.setJobName("RPI Planner");
		job.setPrintable(new Printable() {
			public int print(Graphics graphics, PageFormat pageFormat,
					int pageIndex) throws PrinterException {
				if (pageIndex > 0) {
					return NO_SUCH_PAGE;
				}

				JPanel planPanel = mainFrame.getPlanCard().getPlanPanel();
				Dimension oldSize = planPanel.getSize();

				planPanel.setSize((int)pageFormat.getImageableWidth(), (int)pageFormat.getImageableHeight()-72);
				planPanel.validate();
				Graphics2D g2d = (Graphics2D) graphics;
				g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY()+72);

				planPanel.printAll(g2d);
				
				g2d.setFont(new Font("Arial", Font.BOLD, 24));
				g2d.drawString("Plan of study for "+planControl.getPlan().getFullname(), 72*3, -10);
				
				planPanel.setSize(oldSize);
				planPanel.validate();

				return PAGE_EXISTS;
			}

		});
		
		if(job.printDialog()){
			try {
				job.print(aset);
			} catch (PrinterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
    
    @Action
    public void details(){
    	mainFrame.goToDetailsPanel();
    }
    
    @Action
    public void feedback(){
		BrowserLauncher.openURL("http://rpiplanner.uservoice.com/");
    }
    
    @Action
    public void about(){
    	AboutDialog.showDialog();
    }

	public static void main(String[] args) {
		xs = new XStream();
		xs.processAnnotations(PlanOfStudy.class);
		xs.processAnnotations(Course.class);
		xs.processAnnotations(DefaultCourseDatabase.class);
		xs.processAnnotations(ShadowCourseDatabase.class);
		xs.processAnnotations(YearPart.class);
		xs.registerConverter(new RequisiteSetConverter(xs.getMapper()));
        Application.launch(Main.class, args);
    }
}
