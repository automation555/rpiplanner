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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import rpiplanner.model.*;
import rpiplanner.xml.RequisiteSetConverter;

import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class XML {
    private static XStream databaseReader = null;
    private static XStream planReader = null;

    private static XStream getDatabaseReader(){
        if(databaseReader == null){
            databaseReader = new XStream();
            initializeXStream(databaseReader);
        }
        return databaseReader;
    }

    private static XStream getPlanReader(){
        if(planReader == null){
            planReader = new XStream();
            initializeXStream(planReader);
            planReader.registerConverter(new Converter(){

                public void marshal(Object o, HierarchicalStreamWriter hierarchicalStreamWriter, MarshallingContext marshallingContext) {
                    Course c = (Course)o;
                    hierarchicalStreamWriter.startNode("catalogNumber");
                    hierarchicalStreamWriter.setValue(c.getCatalogNumber());
                    hierarchicalStreamWriter.endNode();
                }

                public Object unmarshal(HierarchicalStreamReader hierarchicalStreamReader, UnmarshallingContext unmarshallingContext) {
                    while(hierarchicalStreamReader.hasMoreChildren()){
                        hierarchicalStreamReader.moveDown();
                        if(hierarchicalStreamReader.getNodeName().equals("catalogNumber")){
                            Course read = Course.get(hierarchicalStreamReader.getValue());
                            hierarchicalStreamReader.moveUp();
                            return read;
                        }
                        else
                            hierarchicalStreamReader.moveUp();
                    }
                    // couldn't find course
                    return null;
                }

                public boolean canConvert(Class aClass) {
                    return Course.class.equals(aClass);
                }
            });
        }
        return planReader;
    }

    private static void initializeXStream(XStream xs){
        xs.processAnnotations(PlanOfStudy.class);
        xs.processAnnotations(Course.class);
        xs.processAnnotations(DefaultCourseDatabase.class);
        xs.processAnnotations(ShadowCourseDatabase.class);
        xs.processAnnotations(YearPart.class);
        xs.registerConverter(new RequisiteSetConverter(xs.getMapper()));
    }

    public static PlanOfStudy readPlan(InputStream in){
        return (PlanOfStudy)getPlanReader().fromXML(in);
    }

    public static ShadowCourseDatabase readShadowCourseDatabase(InputStream in){
        return (ShadowCourseDatabase)getDatabaseReader().fromXML(in);
    }

    public static DefaultCourseDatabase readDefaultCourseDatabase(InputStream in){
        return (DefaultCourseDatabase)getDatabaseReader().fromXML(in);
    }

    public static void writePlan(PlanOfStudy plan, OutputStream out) {
        getPlanReader().toXML(plan, out);
    }
    public static void writeShadowCourseDatabase(ShadowCourseDatabase db, OutputStream out) {
        getDatabaseReader().toXML(db, out);
    }
}
