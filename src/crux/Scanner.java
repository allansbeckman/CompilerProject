package crux;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

import crux.Token.Kind;

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
			e.printStackTrace();
		}
		return nextChar;
	}
	
	//Think of program as a state machine.
	
	/* Invariants:
	 *  1. call assumes that nextChar is already holding an unread character
	 *  2. return leaves nextChar containing an untokenized character
	 */
	public Token next()
	{
		char current = (char) this.nextChar;
		
		while(this.nextChar == 10 || this.nextChar == 13 || (char)this.nextChar == ' ')
		{
			readChar();
			current = (char) this.nextChar;
		}
		
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
				startLine++;
				startChar = 1;
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
		
		//Checks for integers and floats.
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
		
		
		//Parses sequences of letters that can be either identifiers or keywords.

		Token t = letterSequence(current, string, startLine, startChar);
		if(t != null)
		{
			return t;
		}
		
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
		
		char[] firsts = {'>', '<', ':', '='};
		char[] seconds = {'=', '=', ':', '='};
		
		Token tok = consecutiveSequence(current, firsts, seconds, startLine, startChar);
		if(tok != null)
		{
			return tok;
		}
		
		tok = singleCharacters(current, startLine, startChar);
		if(tok != null)
		{
			return tok;
		}
		
		
		if(this.nextChar < 0)
		{
			return Token.EOF(lineNum, charPos);
		}
		char c = (char)this.nextChar;
		readChar();
		return new Token(startLine, startChar, "Unexpected character: " + Character.toString(c), Kind.ERROR);
	}
	
	private Token singleCharacters(char current, int startLine, int startChar)
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
	
	private Token letterSequence(char current, StringBuilder string, int startLine, int startChar)
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
			return Token.textToken(string.toString(), startLine, startChar);
		}
		return null;
	}
	
	private Token consecutiveSequence(char current, char[] firstMatch, char[] secondMatch, int startLine, int startChar)
	{
		for(int i = 0 ; i < firstMatch.length; i++)
		{
			if(current == firstMatch[i])
			{
				readChar();
				current = (char) this.nextChar;
				if(current == secondMatch[i])
				{
					readChar();
					return new Token(Character.toString(firstMatch[i]) + Character.toString(secondMatch[i]), startLine, startChar);
				}
				else
				{
					return new Token(Character.toString(firstMatch[i]), startLine, startChar);
				}
			}
		}
	
		return null;
	}
	
	
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
