package crux;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

public class Compiler {
    public static String studentName = "Allan Beckman";
    public static String studentID = "21588725";
    public static String uciNetID = "beckmana";
    
    public static boolean release = false;
    
    public static void main(String[] args)
    {
    	if (release) {
    		releasedMain(args[0]);
    	} else {
    		for (int i=1; i<=15; ++i) {
		    	String sourceFilename = String.format("tests/test%02d.crx", i);
	        	fileCompile(sourceFilename);
    		}
    	}
    }
    
    public static void releasedMain(String sourceFilename)
    {
        Scanner s = null;
        try {
            s = new Scanner(new FileReader(sourceFilename));
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error accessing the source file: \"" + sourceFilename + "\"");
            System.exit(-2);
        }

        Parser p = new Parser(s);
        ast.Command syntaxTree = p.parse();
        if (p.hasError()) {
            System.out.println("Error parsing file.");
            System.out.println(p.errorReport());
            System.exit(-3);
        }
            
        types.TypeChecker tc = new types.TypeChecker();
        tc.check(syntaxTree);
        if (tc.hasError()) {
            System.out.println("Error type-checking file.");
            System.out.println(tc.errorReport());
            System.exit(-4);
        }
        System.out.println("Crux Program has no type errors.");
    }

    public static void fileCompile(String sourceFilename)
    {
        String outFilename = sourceFilename.replace(".crx", ".out");
		System.out.println("Lab5.implementation outputing for " + outFilename);
		
		PrintStream outStream = null;
		try {
	        File outFile = new File(outFilename);
	        outStream = new PrintStream(outFile);
		} catch (IOException e) {
	        e.printStackTrace();
            System.err.println("Error creating output file: \"" + outFilename + "\"");
		}
    	
    	Scanner s = null;
        try {
            s = new Scanner(new FileReader(sourceFilename));
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error accessing the source file: \"" + sourceFilename + "\"");
            System.exit(-2);
        }

        Parser p = new Parser(s);
        ast.Command syntaxTree = p.parse();
        if (p.hasError()) {
            System.out.println("Error parsing file.");
            System.out.println(p.errorReport());
            System.exit(-3);
        }
            
        types.TypeChecker tc = new types.TypeChecker();
        tc.check(syntaxTree);
        if (tc.hasError()) {
            outStream.println("Error type-checking file.");
            outStream.println(tc.errorReport());
            outStream.close();
        } else {
	        outStream.println("Crux Program has no type errors.");
        }
        
    }
}
    
