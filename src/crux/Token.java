package crux;

public class Token {
	public static String studentName = "Allan Beckman";
    public static String studentID = "21588725";
    public static String uciNetID = "beckmana";
	/*
	 * Enum type for the Tokens and corresponding lexemes.
	 */
	public static enum Kind {
		/*
		 * Keywords
		 */
		AND("and"),
		OR("or"),
		NOT("not"),
		LET("let"),
		VAR("var"),
		ARRAY("array"),
		FUNC("func"),
		IF("if"),
		ELSE("else"),
		WHILE("while"),
		TRUE("true"),
		FALSE("false"),
		RETURN("return"),
		/*
		 * Reserved characters
		 */
		OPEN_PAREN("("),
		CLOSE_PAREN(")"),
		OPEN_BRACE("{"),
		CLOSE_BRACE("}"),
		OPEN_BRACKET("["),
		CLOSE_BRACKET("]"),
		ADD("+"),
		SUB("-"),
		MUL("*"),
		DIV("/"),
		GREATER_EQUAL(">="),
		LESSER_EQUAL("<="),
		NOT_EQUAL("!="),
		EQUAL("=="),
		GREATER_THAN(">"),
		LESS_THAN("<"),
		ASSIGN("="),
		COMMA(","),
		SEMICOLON(";"),
		COLON(":"),
		CALL("::"),
		
		/*
		 * Kinds which may have different lexemes
		 */
		IDENTIFIER(),
		INTEGER(),
		FLOAT(),
		ERROR(),
		EOF();
		
		private String default_lexeme;
		
		/*
		 * Constructs a Kind with no lexeme value.
		 */
		Kind()
		{
			default_lexeme = "";
		}
		
		/*
		 * Constructs a Kind with a lexeme value.
		 */
		Kind(String lexeme)
		{
			default_lexeme = lexeme;
		}
		
		public boolean hasStaticLexeme()
		{
			return default_lexeme != null;
		}
		
		// OPTIONAL: if you wish to also make convenience functions, feel free
		//           for example, boolean matches(String lexeme)
		//           can report whether a Token.Kind has the given lexeme
	}
	
	private int lineNum; //Line number of the token
	private int charPos; //Character position of the token	
	private Kind kind;			 //Kind of the token
	private String lexeme = "";	//Lexeme of the token
	
	// OPTIONAL: implement factory functions for some tokens, as you see fit
	/*
	 * Static factory function to create an end of file Token
	 */
	public static Token EOF(int linePos, int charPos)
	{
		Token tok = new Token(linePos, charPos);
		tok.kind = Kind.EOF;
		return tok;
	}
	
	/*
	 * Static factory function to create an integer Token
	 */
	public static Token integerToken(String integer, int lineNum, int charPos)
	{
		return new Token(lineNum, charPos, integer, Kind.INTEGER);
	}
	
	/*
	 * Static factory function to create a Token with a given Kind
	 */
	public static Token tokenWithKind(String lexeme, Kind kind, int lineNum, int charPos)
	{
		return new Token(lineNum, charPos, lexeme, kind);
	}

	/*
	 * Static factory function that creates either an Identifier Token or Keyword Token
	 */
	public static Token identifierOrKeyword(String lexeme, int lineNum, int charPos)
	{
		Kind kind = findKindFromLexeme(lexeme);
		
		if(kind != null) //It's a reserved keyword
		{
			return new Token(lineNum, charPos, lexeme, kind);
		}
		
		boolean valid = isValidIdentifier(lexeme);
		if(valid)
		{
			return new Token(lineNum, charPos, lexeme, Kind.IDENTIFIER);
		}
		
		return new Token(lineNum, charPos, lexeme, Kind.ERROR);
	}
	
	/*
	 * Token constructor
	 */
	public Token(int lineNum, int charPos, String lexeme, Kind kind)
	{
		this.lineNum = lineNum;
		this.charPos = charPos;
		this.lexeme = lexeme;
		this.kind = kind;
	}
	
	/*
	 * Creates an error Token
	 */
	private Token(int lineNum, int charPos)
	{
		this.lineNum = lineNum;
		this.charPos = charPos;
		this.kind = Kind.ERROR;
		this.lexeme = "No Lexeme Given";
	}
	
	/*
	 * Token constructor which determines Kind from the lexeme
	 */
	public Token(String lexeme, int lineNum, int charPos)
	{
		this.lineNum = lineNum;
		this.charPos = charPos;
		this.kind = findKindFromLexeme(lexeme);
		this.lexeme = lexeme;
	}
	
	public boolean is(Kind kind)
	{
		return(this.kind.equals(kind));
	}
	
	public Kind kind()
	{
		return this.kind;
	}
	
	/*
	 * Determines if a given string of characters is a valid identifier
	 */
	private static boolean isValidIdentifier(String lexeme)
	{
		char[] characters = lexeme.toCharArray();
		boolean valid = true;
		if(isCharLetter(characters[0]) || characters[0] == '_')
		{
			for(int i = 1; i < characters.length; i++)
			{
				if(isCharLetter(characters[i]) || characters[i] == '_' || isCharDigit(characters[i]))
				{
					continue;
				}
				else
				{
					valid = false;
				}
			}
		}
		return valid;
	}
	
	/*
	 * Determines the Kind of a Token with a given lexeme.
	 */
	private static Kind findKindFromLexeme(String lexeme)
	{
		Kind kind = null;
		Kind[] kinds = Kind.values();
		for(int i = 0; i < kinds.length; i++)
		{
			if(kinds[i].default_lexeme.compareTo(lexeme) == 0)
			{
				kind = kinds[i];	
				break;
			}
		}
		return kind;
	}
	
	/*
	 * Returns true if a given character is a digit(0-9)
	 */
	public static boolean isCharDigit(char c)
	{
		return (c >= '0' && c <= '9');
	}
	
	/*
	 * Returns true if a given character is a letter (a-z,A-Z)
	 */
	public static boolean isCharLetter(char c)
	{
		return ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'));
	}
	
	/*
	 * Returns line number
	 */
	public int lineNumber()
	{
		return lineNum;
	}
	
	/*
	 * returns character position
	 */
	public int charPosition()
	{
		return charPos;
	}
	
	// Return the lexeme representing or held by this token
	public String lexeme()
	{
		return this.lexeme;
	}
	
	/*
	 * To string method for Token.
	 */
	public String toString()
	{
		StringBuilder string = new StringBuilder();
		string.append(this.kind);
		if(this.kind == Kind.IDENTIFIER || this.kind == Kind.INTEGER || this.kind == Kind.FLOAT || this.kind == Kind.ERROR)
		{
			string.append("(" + this.lexeme + ")");
		}
		string.append("(lineNum: " + this.lineNumber() + 
					  ", charPos: " + this.charPosition() + ")");
		return string.toString();
	}

}
