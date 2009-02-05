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

package rpiplanner.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import rpiplanner.SchoolInformation;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@XStreamAlias("plan")
public class PlanOfStudy {
	protected String fullname;
	protected String school;
	protected String studentID;
	protected int startingYear;
	protected ArrayList<Degree> degrees;

	@XStreamImplicit
	protected ArrayList<Term> terms;
	
	@XStreamOmitField
	protected PropertyChangeSupport changeSupport;
	
	public PlanOfStudy(){
		initializeTerms();
	}

	public String getFullname() {
		return fullname;
	}
	public void setFullname(String fullname) {
		this.fullname = fullname;
	}
	public String getSchool() {
		return school;
	}
	public void setSchool(String school) {
		this.school = school;
	}
	public String getStudentID() {
		return studentID;
	}
	public void setStudentID(String studentID) {
		this.studentID = studentID;
	}
	public int getStartingYear() {
		return startingYear;
	}
	public void setStartingYear(int startingYear) {
		this.startingYear = startingYear;
		rebuildTerms();
	}
	public Term getTerm(int index){
		terms.get(index).plan = this;
		return terms.get(index);
	}
	public ArrayList<Degree> getDegrees() {
		if(degrees == null)
			degrees = new ArrayList<Degree>();
		return degrees;
	}
	public void setDegrees(ArrayList<Degree> degrees) {
		this.degrees = degrees;
	}

	public void rebuildTerms(){
		terms.get(1).setYear(startingYear);
		for(int i = 1; i < SchoolInformation.getDefaultSemesterCount()/2; i++){
			terms.get(2*i+1).setYear(startingYear+i);
			terms.get(2*i).setYear(startingYear+i);
		}
		terms.get(terms.size()-1).setYear(startingYear+SchoolInformation.getDefaultSemesterCount()/2);
		fireCoursesChanged();
	}
	
	/**
	 * Initialize terms according to school. For now this is a simple
	 * prototype that uses RPI's semester-based term system
	 */
	private void initializeTerms(){
		startingYear = 2007; // default for most of my classmates
		terms = new ArrayList<Term>(SchoolInformation.getDefaultSemesterCount()+1);
		Term apTerm = new Term();
		apTerm.setYear(0);
		terms.add(apTerm);
		
		Term firstTerm = new Term();
		firstTerm.setYear(startingYear);
		firstTerm.setTerm(YearPart.FALL);
		terms.add(firstTerm);

		for(int i = 1; i < SchoolInformation.getDefaultSemesterCount()/2; i++){
			Term spring = new Term();
			spring.setYear(startingYear+i);
			spring.setTerm(YearPart.SPRING);

			terms.add(spring);
			Term fall = new Term();
			fall.setYear(startingYear+i);
			fall.setTerm(YearPart.FALL);
			terms.add(fall);
		}
		Term lastTerm = new Term();
		lastTerm.setYear(startingYear + SchoolInformation.getDefaultSemesterCount()/2);
		lastTerm.setTerm(YearPart.SPRING);
		terms.add(lastTerm);
		fireCoursesChanged();
	}

	public int numTerms(){
		return terms.size();
	}

	public List<Term> getTerms() {
		for(Term t : terms)
			t.plan = this;
		return terms;
	}
	
	public void setTerms(List<Term> terms){
		this.terms = new ArrayList<Term>(terms);
		fireCoursesChanged();
	}
	
	public void addPropertyChangeListener(String property, PropertyChangeListener listener){
		if(changeSupport == null)
			changeSupport = new PropertyChangeSupport(this);
		changeSupport.addPropertyChangeListener(property, listener);
	}
	
	protected void fireCoursesChanged(){
		if(changeSupport == null)
			changeSupport = new PropertyChangeSupport(this);
		changeSupport.firePropertyChange("courses", null, null);
	}
}
