/*
 * Copyright (C) 2008 Eric Allen allene2@rpi.edu
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package rpiplanner.validation.requirements;

import rpiplanner.model.Course;
import rpiplanner.validation.degree.DegreeSection;
import rpiplanner.validation.interfaces.Requirement;

import java.util.ArrayList;
import java.util.HashMap;

public class HumanitiesRequirement extends Requirement {
    public DegreeSection validate(HashMap<Course, Integer> courseMap, ArrayList<Course> courseList) {
        DegreeSection humSSSection = new DegreeSection();
        humSSSection.setName("Humanities and Social Sciences");
        humSSSection.setDescription("Humanities and Social Sciences Core Requirement");

        ArrayList<Course> humCourses = createHumanitiesOrSSList(courseList, courseMap);
        boolean pd2ReqMet = checkPD2(courseMap, courseList, humSSSection);
        boolean depthReqMet = depthRequirement(courseMap, humCourses);
        boolean levelReqMet = LevelReq(humSSSection, humCourses);

        //TODO: Check Humanities
        //TODO: Check Social Sciences


        return humSSSection;
    }

    private boolean depthRequirement(HashMap<Course, Integer> courseMap, ArrayList<Course> courseList) {
        ArrayList<String> lowPrefixList = new ArrayList<String>();
        ArrayList<String> upperPrefixList = new ArrayList<String>();
        for (Course course : courseList) {
            if (courseMap.get(course) > 0) {
                if (course.getNumber() < 2000) lowPrefixList.add(course.getPrefix());
                else upperPrefixList.add(course.getPrefix());
            }
        }
        for (String prefix : lowPrefixList) {
            if (upperPrefixList.contains(prefix)) return true;
        }
        return false;
    }

    private boolean LevelReq(DegreeSection humSSSection, ArrayList<Course> humCourses) {
        //TODO: Add course to applied courses
        //TODO: Do we want to take care of messages at the end?
        if (!met4000LevelReq(humCourses)) {
            add4000LevelCourses(humSSSection, humCourses);
            humSSSection.addMessage("You need a 4000 Level Course");
            return false;
        }
        return true;
    }

    private void add4000LevelCourses(DegreeSection humSSSection, ArrayList<Course> humCourses) {
        ArrayList<String> prefixes = new ArrayList<String>();
        fillPrefixes(prefixes);
        ArrayList<Course> courses = new ArrayList<Course>();
        for (String prefix : prefixes) {
            courses.addAll(Course.getAllBetween(prefix, 4000, 5000));
        }
        humSSSection.addMissingCourse(courses);
        humSSSection.addPotentialCourse(courses);
    }

    private void fillPrefixes(ArrayList<String> prefixes) {
        prefixes.add("LANG");
        prefixes.add("LITR");
        prefixes.add("COMM");
        prefixes.add("WRIT");
        prefixes.add("ARTS");
        prefixes.add("PHIL");
        prefixes.add("STSH");
        prefixes.add("IHSS");
        prefixes.add("ECON");
        prefixes.add("STSS");
        prefixes.add("PSYC");
    }

    private Boolean met4000LevelReq(ArrayList<Course> humCourses) {
        for (Course course : humCourses) {
            if (course.getLevel() == "4000") {
                return true;
            }
        }
        return false;
    }

    private ArrayList<Course> createHumanitiesOrSSList(ArrayList<Course> courseList, HashMap<Course, Integer> courseMap) {
        ArrayList<Course> newList = new ArrayList<Course>();
        for (Course course : courseList) {
            if (courseMap.get(course) > 0) {
                if (isHumanities(course) || isSS(course)) {
                    newList.add(course);
                }
            }
        }
        return newList;
    }

    private boolean isHumanities(Course course) {
        if (course.getPrefix() == "LANG" || course.getPrefix() == "LITR" || course.getPrefix() == "COMM"
                || course.getPrefix() == "WRIT" || course.getPrefix() == "ARTS" || course.getPrefix() == "PHIL"
                || course.getPrefix() == "STSH" || course.getPrefix() == "IHSS") {
            return true;
        }
        return false;
    }

    private boolean isSS(Course course) {
        if (course.getPrefix() == "ECON" || course.getPrefix() == "STSS"
                || course.getPrefix() == "PSYC") return true;
        return false;
    }


    private boolean checkPD2(HashMap<Course, Integer> courseMap, ArrayList<Course> courseList, DegreeSection humSSSection) {
        Boolean pdReq;//Professional Development II
        if (courseMap.containsKey(Course.get("PSYC", "4170")) || courseMap.containsKey(Course.get("STSS", "4840"))) {
            if (courseMap.containsKey(Course.get("PSYC", "4170"))) {
                courseMap.put(Course.get("PSYC", "4170"), 0);
                humSSSection.addAppliedCourse(Course.get("PSYC", "4170"));
                pdReq = true;
                courseList.remove(Course.get("PSYC", "4170"));
            }
            if (courseMap.containsKey(Course.get("STSS", "4840"))) {
                courseMap.put(Course.get("STSS", "4840"), 0);
                humSSSection.addAppliedCourse(Course.get("STSS", "4840"));
                pdReq = true;
                courseList.remove(Course.get("STSS", "4840"));
            }
            return true;
        } else {
            humSSSection.addMissingCourse(Course.get("PSYC", "4170"));
            humSSSection.addMissingCourse(Course.get("STSS", "4840"));
            humSSSection.addPotentialCourse(Course.get("PSYC", "4170"));
            humSSSection.addPotentialCourse(Course.get("STSS", "4840"));
            return false;
        }
    }
}