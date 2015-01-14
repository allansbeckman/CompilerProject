package crux;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

import crux.Token.Kind;

public class Scanner implements Iterable<Token> {
	public static String studentName = "Allan Beckman";
    public static String studentID = "21588725";
    public static String uciNetID = "beckmana";
    
    //These two char arrays are used to detect lexemes which have can be one or two characters
    //i.e. >, >=
    public final char[] firsts = {'>', '<', ':', '='};
	public final char[] seconds = {'=', '=', ':', '='};
	
	private int lineNum;  // current line count
	private int charPos;  // character offset for current line
	private int nextChar; // contains the next char (-1 == EOF, -2 == BOF)
	private Reader input;
	
	//Initializes the Scanner
	Scanner(Reader reader)
	{
		lineNum = 1;
		charPos = 0;
		input = reader;
		readChar();
	}	
	
	// OPTIONAL: helper function for reading a single char from input
	//           can be used to catch and handle any IOExceptions,
	//           advance the charPos or lineNum, etc.
	//		     When a new line character is reached advance to next line
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
			e.printStackTrace();
		}
		return nextChar;
	}
	
	/* Invariants:
	 *  1. call assumes that nextChar is already holding an unread character
	 *  2. return leaves nextChar containing an untokenized character
	 */
	/*
	 * This method detects tokens by reading one character at a time.
	 * 1. Remove all whitespace, new line, or carriage returns
	 * 2. Check if a line is commented out					(//asdf or /)
	 * 3. Match numbers										(1.1 or 1123)
	 * 4. Match keywords or identifiers.					(else or _else)
	 * 5. Match not equals 									(!= or !)
	 * 6. Match two characters or one character 			(< or <=)
	 * 7. Match the tokens which only have one character    ({,},[,])
	 * 8. Match the end of file character
	 * 9. If nothing matched then return error.
	 * 
	 * The process is to look at a character and see where it can match
	 * After finding that consume the character and see if the next one matches anything.
	 */
	public Token next()
	{
		char current = (char) this.nextChar;
		current = (char) removeWhiteSpace(this.nextChar);
		int startLine = this.lineNum;
		int startChar = this.charPos;
		
		if(current == '/')
		{
			readChar();
			current = (char) this.nextChar;
			if(current == '/')
			{
				readChar();
				readLine();
				if(this.nextChar != -1)
				{
					startLine++;
					startChar = 1;
				}
				else
				{
					startChar = this.charPos;
					startLine = this.lineNum;
				}
			}
			else
			{
				return new Token("/", startLine, startChar);
			}
			current = (char) this.nextChar;
		}
		
		if(current == ' ' || current == '\n' || current == '\r')
		{
			while(current == ' ' || current == '\n' || current == '\r')
			{
				readChar();
				current = (char) this.nextChar;
			}
			startLine = this.lineNum;
			startChar = this.charPos;
		}
		
		StringBuilder string = new StringBuilder();
		
		Token token = matchNumbers(current, string, startLine, startChar);
		if(token != null)
		{
			return token;
		}
		
		//Parses sequences of letters that can be either identifiers or keywords.
		token = matchKeywordOrIdentifier(current, string, startLine, startChar);
		if(token != null)
		{
			return token;
		}

		token = matchNotEquals(current, startLine, startChar);
		if(token != null)
		{
			return token;
		}
		
		token = matchTwoCharacterOrOneCharcter(current, startLine, startChar);
		if(token != null)
		{
			return token;
		}
		
		token = matchOneCharacter(current, startLine, startChar);
		if(token != null)
		{
			return token;
		}
		
		token = matchEndOfFile(current, startLine, startChar);
		if(token != null)
		{
			return token;
		}
		//IF nothing matched move to this next character and report last character as unexpected.
		readChar();
		return new Token(startLine, startChar, "Unexpected character: " + Character.toString(current), Kind.ERROR);
	}
	
	/*
	 * Helper function to remove whitespace, new line, or carriage return characters.
	 * These characters are not a part of any tokens.
	 */
	public int removeWhiteSpace(int current)
	{
		while(this.nextChar == 10 || this.nextChar == 13 || (char)this.nextChar == ' ')
		{
			readChar();
			current =  this.nextChar;
		}
		return current;
	}
	
	/*
	 * Determines if the current character is the end of the file character.
	 */
	private Token matchEndOfFile(char current, int lineNum, int charPos)
	{
		if(this.nextChar < 0)
		{
			return Token.EOF(lineNum, charPos);
		}
		return null;
	}
	
	/*
	 * Determines if a given character is part of an integer or a float.
	 */
	private Token matchNumbers(char current, StringBuilder string, int startLine, int startChar)
	{
		if(Token.isCharDigit(current))
		{
			string.append(current);
			readChar();
			current = (char) this.nextChar;
			boolean isFloat = false;
			while(Token.isCharDigit(current) || (current == '.' && ! isFloat))
			{
				if(current == '.')
				{
					isFloat = true;
				}
				string.append(current);
				readChar();
				current = (char) this.nextChar;
			}
			if(isFloat)
			{
				return Token.tokenWithKind(string.toString(), Kind.FLOAT, startLine, startChar);
			}
			else
				return Token.integerToken(string.toString(), startLine, startChar);
		}
		return null;
	}
	
	/*
	 * Determines if a given character matches the != or if it is not valid.
	 */
	private Token matchNotEquals(char current, int startLine, int startChar)
	{
		if(current == '!') 
		{
			readChar();
			current = (char) this.nextChar;
			if(current == '=') 
			{
				readChar();
				return new Token("!=", startLine, startChar);
			}
			else	
			{
				return new Token(startLine, startChar, "Unexpected character: !", Kind.ERROR);
			}
		}
		return null;
	}
	
	/*
	 * Determines if a given character matches one of the special one characters 
	 */
	private Token matchOneCharacter(char current, int startLine, int startChar)
	{
		if(current == '(' || current == ')' || current == '[' || current == ']' || current == '{' || current == '}' || current == '+' || current == '-'
				|| current == '*' || current == ',' || current == ';')
		{
			StringBuilder build = new StringBuilder();
			build.append(current);
			readChar();
			return new Token(build.toString(), startLine, startChar);
		}
		return null;
	}
	
	/*
	 * Determines if a given character matches an identifier or keyword.
	 */
	private Token matchKeywordOrIdentifier(char current, StringBuilder string, int startLine, int startChar)
	{
		if(Token.isCharLetter(current) || current == '_')
		{
			string.append(current);
			readChar();
			current = (char) this.nextChar;
			while(Token.isCharLetter(current) || Token.isCharDigit(current) || current == '_')
			{
				string.append(current);
				readChar();
				current = (char) this.nextChar;
			}
			return Token.identifierOrKeyword(string.toString(), startLine, startChar);
		}
		return null;
	}
	
	/*
	 * Determines if a given character matches one of the possible two character combinations
	 * Such as < or <=, : or ::
	 */
	private Token matchTwoCharacterOrOneCharcter(char current, int startLine, int startChar)
	{
		for(int i = 0 ; i < firsts.length; i++)
		{
			if(current == firsts[i])
			{
				readChar();
				current = (char) this.nextChar;
				if(current == seconds[i])
				{
					readChar();
					return new Token(Character.toString(firsts[i]) + Character.toString(seconds[i]), startLine, startChar);
				}
				else
				{
					return new Token(Character.toString(firsts[i]), startLine, startChar);
				}
			}
		}
	
		return null;
	}
	
	/*
	 * Reads the current line and move to the next line in the file.
	 */
	private void readLine()
	{
		while(this.nextChar != 10)
		{
			if(this.nextChar == -1)
			{
				break;
			}
			readChar();
		}
		if(this.nextChar == -1 )
		{
			return;
		}
		readChar();
	}
	
	@Override
	public Iterator<Token> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	// OPTIONAL: any other methods that you find convenient for implementation or testing
}
