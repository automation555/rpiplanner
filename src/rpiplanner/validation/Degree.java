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

package rpiplanner.validation;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import rpiplanner.model.PlanOfStudy;

import java.util.ArrayList;

public class Degree
{
    @XStreamAlias("DegreeName")
    String name;

    ArrayList<CoreRequirement> coreRequirements;
    ArrayList<SubjectRequirement> subjRequirements;


    public DegreeValidationResult validate (PlanOfStudy pos)
    {
        DegreeValidationResult result = new DegreeValidationResult();

        
        for (SpecialDesignationRequirement currentReq : specialReq)
        {
            DegreeSection newSection = new DegreeSection();
            newSection.name = currentReq.getName();
            newSection.description = currentReq.getDescription();

            result.addSection(newSection);
        }

        for (CoreRequirement currentReq : coreReq)
			{
                DegreeSection newSection = new DegreeSection();
                newSection.name = currentReq.getName();
                newSection.description = currentReq.getDescription();

                result.addSection(newSection);
			}

        for (RestrictedRequirement currentReq : restReq)
			{
                DegreeSection newSection = new DegreeSection();
                newSection.name = currentReq.getName();
                newSection.description = currentReq.getDescription();

                result.addSection(newSection);
			}

        for (SubjectRequirement currentReq : subjReq)
			{
                DegreeSection newSection = new DegreeSection();
                newSection.name = currentReq.getName();
                newSection.description = currentReq.getDescription();

                result.addSection(newSection);
			}
        return result;
    }

}