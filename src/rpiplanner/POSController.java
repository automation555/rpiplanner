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

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import rpiplanner.model.Course;
import rpiplanner.model.CourseDatabase;
import rpiplanner.model.Degree;

import rpiplanner.model.Pair;
import rpiplanner.model.Term;

import rpiplanner.model.PlanOfStudy;
import rpiplanner.model.Term;
import rpiplanner.model.ValidationError;
import rpiplanner.model.YearPart;
import rpiplanner.model.ValidationError.Type;
import rpiplanner.view.CourseDatabaseFilter;
import rpiplanner.view.CourseDisplay;
import rpiplanner.view.CourseTransferHandler;
import rpiplanner.view.DegreeListModel;
import rpiplanner.view.DegreeProgressPanel;
import rpiplanner.view.PlanOfStudyEditor;

public class POSController {
	protected PlanOfStudy plan;
	protected rpiplanner.view.PlanOfStudyEditor view;
	private ArrayList<JPanel> semesterPanels;
	private CourseDatabase courseDatabase;
	private CourseDatabaseFilter courseDatabaseModel;
	private JPanel detailsPanel;
	private DegreeListModel degreeListModel;
	private JList degreeList;
	protected final PropertyChangeSupport support = new PropertyChangeSupport(this);
	private DegreeProgressPanel progressPanel;
	
	public POSController(){
		plan = new PlanOfStudy();
	}

	public void setSemesterPanels(ArrayList<JPanel> semesterPanels) {
		this.semesterPanels = semesterPanels;
		for(int i = 0; i < semesterPanels.size(); i++){
			JPanel p = semesterPanels.get(i);
			p.setTransferHandler(new CourseTransferHandler(this));
		}
        updateSemesterPanels();
	}

	public void setCourseDatabase(CourseDatabase courseDatabase) {
		this.courseDatabase = courseDatabase;
		this.courseDatabaseModel = new CourseDatabaseFilter(courseDatabase);
		courseDatabase.addPropertyChangeListener(courseDatabaseModel);
	}
	
	public CourseDatabase getCourseDatabase(){
		return courseDatabase;
	}

	public PlanOfStudy getPlan() {
		return plan;
	}
	
	public void setPlan(PlanOfStudy plan){
        PlanOfStudy oldPlan = this.plan;
		this.plan = plan;
        if(semesterPanels != null){
            setSemesterPanels(semesterPanels);
            validatePlan();
        }
        plan.addPropertyChangeListener("courses", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                PlanOfStudy plan = (PlanOfStudy) evt.getSource();
                if(semesterPanels != null){
                    updateSemesterPanels();
                    validatePlan();
                }
                totalCredits();
            }
        });
        support.firePropertyChange("plan", oldPlan, plan);
	}

	public ListModel getCourseListModel() {
		return courseDatabaseModel;
	}

	public void setView(PlanOfStudyEditor planCard) {
		this.view = planCard;
		view.setController(this);
		totalCredits();
		validatePlan();
	}

	public void addCourse(int term, Course toAdd) {
		Term toModify = plan.getTerm(term);
		toModify.add(toAdd);
	}

    private void updateSemesterPanels() {
        for (int i = 0; i < plan.numTerms(); i++)
            updateSemesterPanel(i, plan.getTerm(i));
        ((JComponent)semesterPanels.get(0).getParent()).revalidate();
    }

	private void updateSemesterPanel(int term, Term model) {
		ArrayList<Course> courses = model.getCourses();

		JPanel semesterPanel = semesterPanels.get(term);
		semesterPanel.removeAll();
		int creditTotal = 0;
		
		for(Course c : courses){
			semesterPanel.add(new CourseDisplay(this, c));
			creditTotal += c.getCredits();
		}
		semesterPanel.add(new CourseDisplay(this));
		for(int i = 1; i < (SchoolInformation.getDefaultCoursesPerSemester()-courses.size()); i++){
			semesterPanel.add(new CourseDisplay(this));
		}
		if(model.getYear() == 0){ // AP/transfer credit
			semesterPanel.setBorder(new TitledBorder("AP/transfer credit ("
					+ String.valueOf(creditTotal) + ")"));
		}
		else{
			semesterPanel.setBorder(new TitledBorder(model.getTerm().toString()
					+ " " + String.valueOf(model.getYear()) + " ("
					+ String.valueOf(creditTotal) + ")"));
		}
	}

	public void initializeTerms(int startingYear) {
		plan.setStartingYear(startingYear);
		plan.rebuildTerms();
        updateSemesterPanels();
	}

	public void removeCourse(Container semesterPanel, CourseDisplay course) {
		int semester = 0;
		int courseIndex = 0;
		// find the semester
		while(semesterPanels.get(semester) != semesterPanel)
			semester++;
		
		// find course
		while(semesterPanel.getComponent(courseIndex) != course)
			courseIndex++;

		plan.getTerm(semester).remove(courseIndex);
	}

	private void totalCredits() {
		int credits = 0;
		for(Term t : plan.getTerms()){
			for(Course c : t.getCourses())
				credits += c.getCredits();
		}
		support.firePropertyChange("creditTotal", -1, credits);
	}

	/**
	 * Show the details for the specified course in the course details panel
	 * @param course
	 */
	public void setDetailDisplay(Course course) {
		if (course == null) {
			;
		} else {
			for (Component c : detailsPanel.getComponents()) {
				if (c.getName() == "basicInfo") {
					((JLabel) c).setText(course.toString() + " ("
							+ course.getCredits() + " credits)");
				}
				if (c.getName() == "descriptionViewport") {
					JTextArea desc = (JTextArea)((JScrollPane) c).getViewport().getComponent(0);
					desc.setText(course.getDescription());
					final JScrollPane pane = (JScrollPane)c;
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							pane.getViewport().setViewPosition(
									new Point(0, 0));
						}
					});
				}
				if (c.getName() == "description") {
					((JTextArea) c).setText(course.getDescription());
				}
			}
		}
	}
	
	public void setDetailsPanel(JPanel detailsPanel) {
		this.detailsPanel = detailsPanel;
	}

	public void searchTextChanged(String text) {
		courseDatabaseModel.setSearchText(text);
	}

	public void updatePlanFromDatabase() {
		for(Term t : plan.getTerms()){
			ArrayList<Course> courses = t.getCourses();
			for(int j = 0; j < courses.size(); j++){
				Course replacement = Course.get(courses.get(j).getCatalogNumber());
				if(replacement != null){
					t.replace(j, replacement);
				}
			}
		}
	}

	public ComboBoxModel getDegreeListModel() {
		if(degreeListModel == null){
			degreeListModel = new DegreeListModel(courseDatabase);
		}
		return degreeListModel;
	}

	public void addDegree(Degree toAdd) {
		plan.getDegrees().add(toAdd);
		progressPanel.rebuildSections();
		validatePlan();
	}
	
	public void validatePlan(){
		// validate prerequisites, corequisites, and term restructions
		for(int i = 1; i < plan.numTerms(); i++){
			Term currentTerm = plan.getTerm(i);
			int courseIdx = 0;
			for(Course course : currentTerm.getCourses()){
				StringBuilder tooltip = new StringBuilder();

				boolean availableInTerm = false;
				for(YearPart y : course.getAvailableTerms()){
					if(y == currentTerm.getTerm())
						availableInTerm = true;
				}
				CourseDisplay cd = (CourseDisplay) semesterPanels.get(i).getComponent(courseIdx);
				List<ValidationError> errors = new ArrayList<ValidationError>();

				if(!availableInTerm)
					errors.add(new ValidationError(
							ValidationError.Type.WARNING, "Not offered during "
									+ currentTerm.getTerm().toString()
									+ " term"));
				
				List<ValidationError> thisErrors;
				
				thisErrors = course.getCorequisites().check(plan, i, false);
				errors.addAll(thisErrors);

				thisErrors = course.getPrerequisites().check(plan, i-1, true);
				errors.addAll(thisErrors);
				
				// now set its properties
				ValidationError worstError = new ValidationError(Type.NONE, "");
				for(ValidationError e : errors){
					if(e.compareTo(worstError) > 0)
						worstError = e;
					tooltip.append(e.getMessage());
					tooltip.append("\n");
				}
				if(worstError.getType() == Type.ERROR)
					cd.setBackground(Color.red);
				else if(worstError.getType() == Type.WARNING)
					cd.setBackground(Color.yellow);
				else
					cd.setBackground(null);
				cd.setToolTipText(tooltip.toString());
				courseIdx++;
			}
		}
	}

	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener propertyChangeListener) {
		support.addPropertyChangeListener(propertyName, propertyChangeListener);
	}

	public void loadTemplate() {
		ArrayList<Degree> degrees = plan.getDegrees();
		if(degrees.size() == 1){
			PlanOfStudy template = Main.loadPlanFromFile("degrees/"+degrees.get(0).getID()+".plan");
			if(template != null){
				plan.setTerms(template.getTerms());
                plan.rebuildTerms();
			}
		}
	}
	
	public int getTerm(Course toFind) {
		List<Term> terms = getPlan().getTerms();
		
		for (int i = 0; i < terms.size(); i++) {
			ArrayList<Course> termCourses = terms.get(i).getCourses();
			
			for (int k = 0; k < termCourses.size(); k++) {
				if (toFind.equals(termCourses.get(k))) {
					return i;
				}
			}
		}
		
		return -1;
	}
	
	public Term getTerm(int termNum) {
		if (termNum >= 0 && getPlan().getTerms().size() > termNum) {
			return getPlan().getTerms().get(termNum);
		}
		
		else {
			return null;
		}
	}
	
	public boolean wouldCourseBeValid(Course course, int term, ArrayList<Pair<Course, Integer>> dummyPOS) {
		ArrayList<Course> coursesUpToTerm = new ArrayList<Course>();
		for (int i = 0; i < term; i++) {
			for (int k = 0; k < getPlan().getTerms().get(i).getCourses().size(); k++) {
				coursesUpToTerm.add(getPlan().getTerms().get(i).getCourses().get(k));
			}
		}
		
		for (int i = 0; i < dummyPOS.size(); i++) {
			if (dummyPOS.get(i).getSecond() < term) {
				coursesUpToTerm.add(dummyPOS.get(i).getFirst());
			}
		}
		
		boolean courseFound = false;
		for (int i = 0; i < course.getPrerequisites().size(); i++) {
			courseFound = false;
			for (int k = 0; k < coursesUpToTerm.size(); k++) {
				if (course.getPrerequisites().get(i).equals(coursesUpToTerm.get(k)) || course.getPrerequisites().get(i).getCatalogNumber().equals("MATH-1500")) {
					courseFound = true;
					break;
				}
			}
			
			if (!courseFound) {
				return false;
			}
		}
		
		courseFound = true;
        ArrayList<Course> coursesWithTerm = new ArrayList<Course>();
        coursesWithTerm.addAll(coursesUpToTerm);
        coursesWithTerm.addAll(getPlan().getTerms().get(term).getCourses());
		for (int k = 0; k < course.getCorequisites().size(); k++) {
			courseFound = false;
			for (int i = 0; i < coursesWithTerm.size(); i++) {
				if (course.getCorequisites().get(k).equals(coursesWithTerm.get(i))) {
					courseFound = true;
					break;
				}
			}
			
			if (!courseFound) {
				for (int j = 0; j < dummyPOS.size(); j++) {
					if (dummyPOS.get(j).getSecond() <= term) {
						if (course.getCorequisites().get(k).equals(dummyPOS.get(j).getFirst())) {
							courseFound = true;
							break;
						}
					}
				}
			}
		}
		
		return courseFound;
	}

	public void setSearchCourses(Course[] potentialCourses) {
		courseDatabaseModel.setShownCourses(potentialCourses);
	}

	public void setProgressPanel(DegreeProgressPanel progressPanel) {
		this.progressPanel = progressPanel;
		progressPanel.initialize(this);
	}
}
