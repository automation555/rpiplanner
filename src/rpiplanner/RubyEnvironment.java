package rpiplanner;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.jruby.Ruby;
import org.jruby.RubyHash;
import org.jruby.runtime.GlobalVariable;

import rpiplanner.model.Degree;
import rpiplanner.validation.DegreeValidator;

public class RubyEnvironment {
	private Ruby rubyEnvironment;
	private RubyHash degrees;
	
	private static RubyEnvironment instance;
	
	public static RubyEnvironment getInstance(){
		if(instance == null)
			instance = new RubyEnvironment();
		return instance;
	}
	
	protected RubyEnvironment() {
		rubyEnvironment = Ruby.newInstance();
		try {
			rubyEnvironment.executeScript(readFileAsString("plan_validation_2.rb"), "plan_validation_2.rb");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		degrees = RubyHash.newHash(rubyEnvironment);
		GlobalVariable degreesGlob = new GlobalVariable(rubyEnvironment, "$degrees", degrees);
		rubyEnvironment.defineVariable(degreesGlob);
	}
	
	private static String readFileAsString(String filePath)
	throws java.io.IOException{
	    StringBuffer fileData = new StringBuffer(1000);
	    InputStream fileStream = PlanValidator.class.getResourceAsStream("/"+filePath);
	    if(fileStream == null){
	    	try{
	    		fileStream = new FileInputStream(filePath);
	    	} catch (FileNotFoundException e){
	    		System.err.println("Critical file not found: "+filePath);
	    		return "";
	    	}
	    }
	    BufferedReader reader = new BufferedReader(
	            new InputStreamReader(fileStream));
	    char[] buf = new char[1024];
	    int numRead=0;
	    while((numRead=reader.read(buf)) != -1){
	        fileData.append(buf, 0, numRead);
	    }
	    reader.close();
	    return fileData.toString();
	}

	public DegreeValidator getDegreeDescriptor(Degree degree) {
		DegreeValidator desc = (DegreeValidator) degrees.get(degree.getID());

		// no descriptor cached
		if(desc == null)
			rubyEnvironment.executeScript(degree.getValidationCode(), degree.getName());

		desc = (DegreeValidator)degrees.get(degree.getID());
		
		if(desc == null)// nothing found. Yikes!
			throw new RuntimeException("Validation code for degree "+degree.getName()+" failed to initialize");
		
		return desc;
	}
}
