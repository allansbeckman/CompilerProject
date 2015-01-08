package crux;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

public class Scanner implements Iterable<Token> {
	public static String studentName = "Allan Beckman";
    public static String studentID = "21588725";
    public static String uciNetID = "beckmana";
	
	private int lineNum;  // current line count
	private int charPos;  // character offset for current line
	private int nextChar; // contains the next char (-1 == EOF, -2 == BOF)
	private Reader input;
	
	Scanner(Reader reader)
	{
		lineNum = 1;
		charPos = 0;
		
		input = reader;
		readChar();
		// TODO: initialize the Scanner
	}	
	
	// OPTIONAL: helper function for reading a single char from input
	//           can be used to catch and handle any IOExceptions,
	//           advance the charPos or lineNum, etc.
	private int readChar() 
	{
		try {
			nextChar = input.read();
			if(nextChar == '\n')
			{	
				charPos = 0;
				lineNum++;
			}
			
			else
			{
				charPos++;
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return nextChar;
	}
	
	
	public Token next()
	{
		char current = (char) this.nextChar;
		int startLine = this.lineNum;
		int startChar = this.charPos;
		if(current == '=') //After accepting the first equal sign we read the character to look if the next one is also an equals.
		{
			readChar();
			current = (char) this.nextChar;
			if(current == '=') //After accepting the second equal sign we read the character so that we look to the next one.
			{
				readChar();
				return new Token("==", startLine, startChar);
			}
			else	//If it is not an equal sign then we still look at that character so we can find the token for it.
			{
				return new Token("=", startLine, startChar);
			}
		}

		if(nextChar == 10 || nextChar == 13)
		{
			readChar();
			next();
		}
		
		if(this.nextChar < 0)
		{
			return Token.EOF(lineNum, charPos);
		}
		return null;
	}
	
	//Think of program as a state machine.
	
	/* Invariants:
	 *  1. call assumes that nextChar is already holding an unread character
	 *  2. return leaves nextChar containing an untokenized character
	 */
	/*public Token next()
	{
		
		int result = readChar();
		if(result < 0)  //End of file reached
		{
			return Token.EOF(lineNum, charPos);
		}
		char lexeme = (char) result;
		StringBuilder string = new StringBuilder();
		string.append(lexeme);
		
		
		if(Character.isLetter(lexeme))
		{
		}
		
		
		if(lexeme == '=')
		{
			char character = (char)readChar();
			if(character == '=')
			{
				string.append(character);
				
			}
			return new Token(string.toString(), lineNum, charPos);
		
		}
		
		if(lexeme == '>' || lexeme == '<')
		{
			char character = (char) readChar();
			if(character == '=')
			{
				string.append(character);			
			}
			return new Token(string.toString(), lineNum, charPos);
		}
		
		if(lexeme == ':')
		{
			char character = (char) readChar();
			if(character == ':')
			{
				string.append(character);
			}
			return new Token(string.toString(), lineNum, charPos);
		}
		
		if(lexeme == '!')
		{
			char character = (char) readChar();
			if(character == '=')
			{
				string.append(character);
				return new Token(string.toString(), lineNum, charPos);
			}
			return new Token("ERROR", lineNum, charPos);
		}
	
		return new Token(string.toString(), lineNum, charPos);
		// TODO: implement this
		
	}*/

	@Override
	public Iterator<Token> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	// OPTIONAL: any other methods that you find convenient for implementation or testing
}
