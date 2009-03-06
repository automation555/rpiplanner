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

import rpiplanner.validation.ValidationResult;

import java.util.HashMap;

public class DegreeValidationResult implements IDegreeValidationResult
{
    HashMap<String,DegreeSection> sectionMap;
    private int totalCredits;

    DegreeValidationResult()
    {
        sectionMap = new HashMap<String,DegreeSection>();
    }
	public int percentComplete()
    {
        //TODO: Add algorithm to compute completenness
        return 0;
    }
    void setTotalCredits(int totalCredits)
    {
        this.totalCredits = totalCredits;
    }


	public DegreeSection getSectionResults(String name)
    {
        return sectionMap.get(name);
    }


    void addSection (DegreeSection newSection)
    {
        sectionMap.put(newSection.name,newSection);
    }

    int getTotalCredits()
    {
        return totalCredits;
    }

}