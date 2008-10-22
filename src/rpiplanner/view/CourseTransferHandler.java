package rpiplanner.view;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

import rpiplanner.POSController;
import rpiplanner.model.Course;

public class CourseTransferHandler extends TransferHandler {
	private POSController controller;

	public CourseTransferHandler(POSController controller) {
		this.controller = controller;
	}

	@Override
	protected Transferable createTransferable(JComponent c) {
		if(c instanceof JList){ // dragging from course list
			Course toExport = (Course)((JList)c).getSelectedValue();
			return new CourseTransfer(toExport); 
		}
		else if(c instanceof CourseDisplay){ // dragging from plan
			CourseDisplay cd = (CourseDisplay)c;
			Course toExport = cd.getCourse();
			return new CourseTransfer(toExport, cd); 
		}
		return super.createTransferable(c);
	}
	
	@Override
	public int getSourceActions(JComponent c) {
		return MOVE;
	}

	@Override
	public boolean importData(JComponent comp, Transferable t) {
		try {
			if(comp instanceof JList){ // dropping on catalog, so just remove
				CourseDisplay home = (CourseDisplay) t.getTransferData(new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType+";class=rpiplanner.view.CourseDisplay"));
				if(home != null)
					controller.removeCourse(home.getParent(), home);
				return true;
			}
			
			Course toAdd = (Course) t.getTransferData(new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType+";class=rpiplanner.model.Course"));
			if(comp instanceof CourseDisplay)
				comp = (JComponent) comp.getParent();
			Component[] panels = comp.getParent().getComponents();
			int index = 0;
			while(comp != panels[index])
				index++;
			CourseDisplay home = (CourseDisplay) t.getTransferData(new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType+";class=rpiplanner.view.CourseDisplay"));
			if(home != null)
				controller.removeCourse(home.getParent(), home);
			controller.addCourse(index, toAdd);
			return true;
		} catch (UnsupportedFlavorException e) {
			return false;
		} catch (IOException e) {
			return false;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
		for(DataFlavor f : transferFlavors){
			if(f.getRepresentationClass() == Course.class)
				return true;
		}
		return false;
	}
	
	public class CourseTransfer implements Transferable {
		private Course payload;
		private CourseDisplay payloadSource;
		
		public CourseTransfer(Course toExport) {
			payload = toExport;
		}

		public CourseTransfer(Course toExport, CourseDisplay cd) {
			payload = toExport;
			payloadSource = cd;
		}

		public Object getTransferData(DataFlavor flavor)
				throws UnsupportedFlavorException, IOException {
			if(flavor.getRepresentationClass() == Course.class){
				return payload;
			} else if(flavor.getRepresentationClass() == CourseDisplay.class){
				return payloadSource;
			} else{
				throw new UnsupportedFlavorException(flavor);
			}
		}

		public DataFlavor[] getTransferDataFlavors() {
			try{
				if(payloadSource != null){
					DataFlavor[] flavors = {new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType+";class=rpiplanner.model.Course"),
							new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType+";class=rpiplanner.view.CourseDisplay")};
					return flavors;
				}
				else{
					DataFlavor[] flavors = {new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType+";class=rpiplanner.model.Course")};
					return flavors;
				}
			} catch (ClassNotFoundException e){
				// TODO Auto-generated exception handler
				e.printStackTrace();
			}
			return null;
		}
		
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return flavor.getRepresentationClass() == Course.class;
		}
	}
}