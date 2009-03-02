require 'java'

class Apcredit
  include Java::Rpiplanner::Apcredit

  def getcourse(tests, scores)
    apcourses = []
    i = 0

    tests.each do
      i+=1
      test = tests[i]
      score = scores[i]

      if test == "Computer Science AB"  && score == 5
        apcourses << str_to_course("CSCI-1100")
        apcourses << str_to_course("CHEM-1200")
      elsif test == "Computer Science AB" && score == 4
        apcourses << str_to_course("CSCI-1100")
      elsif test == "Computer Science A" && score == 5
        apcourses << str_to_course("CSCI-1100")
      elsif test == "Government & Politics" && (score == 4)
        apcourses << str_to_course("STSS-1000")
      elsif test == "Government & Politics" && (score == 5)
        apcourses << str_to_course("STSS-1000")
      elsif test == "Biology" && score == 5
        apcourses << str_to_course("BIOL-1010")
      elsif test == "Biology" && score == 4
        apcourses << str_to_course("BIOL-1000")
      elsif test == "Chemistry" && score == 4
        apcourses << str_to_course("CHEM-1100")
        apcourses << str_to_course("CHEM-1200")
      elsif test == "Chemistry" && score == 5
        apcourses << str_to_course("CHEM-1100")
        apcourses << str_to_course("CHEM-1200")
      elsif test == "English Language" && score == 4
        apcourses << str_to_course("WRIT-1000")
      elsif test == "English Language" && score == 5
        apcourses << str_to_course("WRIT-1000")
      elsif test == "English Literature" && score == 4
        apcourses << str_to_course("WRIT-1000")
      elsif test == "English Literature" && score == 5
        apcourses << str_to_course("WRIT-1000")
      elsif test == "American History" && score == 4
        apcourses << str_to_course("STSH-1000")
      elsif test == "European History" && score == 4
        apcourses << str_to_course("STSH-1000")
      elsif test == "World History" && score == 4
        apcourses << str_to_course("STSH-1000")
      elsif test == "American History" && score == 5
        apcourses << str_to_course("STSH-1000")
      elsif test == "European History" && score == 5
        apcourses << str_to_course("STSH-1000")
      elsif test == "World History" && score == 5
        apcourses << str_to_course("STSH-1000")
      elsif test == "Statistics" && score == 4
        apcourses << str_to_course("MGMT-2100")
      elsif test == "Statistics" && score == 5
        apcourses << str_to_course("MGMT-2100")
      elsif test == "Psychology" && score == 4
        apcourses << str_to_course("PSYC-1200")
      elsif test == "Psychology" && score == 4
        apcourses << str_to_course("PSYC-1200")
      elsif test == "Physics C: Mechanics" && score == 4
        if tests.include?("Physics C: Electricity/Magnetism")
          pos = tests.index("Physics C: Electricity/Magnetism")
          if scores[pos] == 4 || scores[pos] == 5
            apcourses << str_to_course("PHYS-1200")
          end
          apcourses << str_to_course("PHYS-1100")
        end
      elsif test == "Physics C: Mechanics" && score == 5
        if tests.include?("Physics C: Electricity/Magnetism")
          pos = tests.index("Physics C: Electricity/Magnetism")
          if scores[pos] == 4 || scores[pos] == 5
            apcourses << str_to_course("PHYS-1200")
          end
          apcourses << str_to_course("PHYS-1100")
        end
      end
    end

    return apcourses.to_java(Java::RpiplannerModel::Course)
  end
end
