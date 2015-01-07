package crux;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import crux.Token.Kind;

public class Compiler {
    public static String studentName = "Allan Beckman";
    public static String studentID = "21588725";
    public static String uciNetID = "beckmana";
	
	public static void main(String[] args)
	{
		Scanner scan = new Scanner(System.in);
        String sourceFile = "tests_public/" + scan.nextLine();
        scan.close();
        crux.Scanner s = null;

        try {
            s = new crux.Scanner(new FileReader(sourceFile));
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error accessing the source file: \"" + sourceFile + "\"");
            System.exit(-2);
        }

        Token t = s.next();
        while (t.kind != Kind.EOF) {
                System.out.println(t);
                t = s.next();
        }
        System.out.println(t);
    }
}
