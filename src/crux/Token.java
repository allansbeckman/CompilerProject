package crux;

public class Token {
	
	public static enum Kind {
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
		TRUE("while"),
		FALSE("false"),
		RETURN("return"),
		
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
		
		
		IDENTIFIER(),
		INTEGER(),
		FLOAT(),
		ERROR(),
		EOF();
		
		// TODO: complete the list of possible tokens
		
		private String default_lexeme;
		
		Kind()
		{
			default_lexeme = "";
		}
		
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
	
	private int lineNum;
	private int charPos;
	Kind kind;
	private String lexeme = "";
	
	
	// OPTIONAL: implement factory functions for some tokens, as you see fit
	   
	public static Token EOF(int linePos, int charPos)
	{
		Token tok = new Token(linePos, charPos);
		tok.kind = Kind.EOF;
		return tok;
	}
	
	public static Token integerToken(String integer, int lineNum, int charPos)
	{
		return new Token(lineNum, charPos, integer, Kind.INTEGER);
	}
	
	public static Token tokenWithKind(String lexeme, Kind kind, int lineNum, int charPos)
	{
		return new Token(lineNum, charPos, lexeme, kind);
	}

	public static Token textToken(String lexeme, int lineNum, int charPos)
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
		
		if(kind != null) //It's a reserved keyword
		{
			return new Token(lineNum, charPos, lexeme, kind);
		}
		
		if(lexeme.matches("[_a-zA-z][a-zA-Z_0-9]*"))
		{
			return new Token(lineNum, charPos, lexeme, Kind.IDENTIFIER);
		}
		
		return new Token(lineNum, charPos, lexeme, Kind.ERROR);
	}
	
	public Token(int lineNum, int charPos, String lexeme, Kind kind)
	{
		this.lineNum = lineNum;
		this.charPos = charPos;
		this.lexeme = lexeme;
		this.kind = kind;
	}
	
	private Token(int lineNum, int charPos)
	{
		this.lineNum = lineNum;
		this.charPos = charPos;
		
		// if we don't match anything, signal error
		this.kind = Kind.ERROR;
		this.lexeme = "No Lexeme Given";
	}
	
	public Token(String lexeme, int lineNum, int charPos)
	{
		this.lineNum = lineNum;
		this.charPos = charPos;

		Kind[] kinds = Kind.values();
		for(int i = 0; i < kinds.length; i++)
		{
			if(kinds[i].default_lexeme.compareTo(lexeme) == 0)
			{
				this.kind = kinds[i];	
				break;
			}
		}
		
		if(this.kind == null)
		{
			try
			{
				int parsedInt = Integer.parseInt(lexeme);
			} catch (NumberFormatException e)
			{
				this.kind = Kind.INTEGER;
			}
		}
		
		// TODO: based on the given lexeme determine and set the actual kind
		try {
			//this.kind = Kind.valueOf(lexeme);
			this.lexeme = lexeme;
		} catch (IllegalArgumentException e)
		{
			// if we don't match anything, signal error
			this.kind = Kind.ERROR;
			this.lexeme = "Unrecognized lexeme: " + lexeme;
		}
	}
	
	public int lineNumber()
	{
		return lineNum;
	}
	
	public int charPosition()
	{
		return charPos;
	}
	
	// Return the lexeme representing or held by this token
	public String lexeme()
	{
		// TODO: implement
		return this.lexeme;
	}
	
	public String toString()
	{
		StringBuilder string = new StringBuilder();
		string.append(this.kind);
		string.append("(lineNum: " + this.lineNumber() + 
					  ", charPos: " + this.charPosition() + ")");
		return string.toString();
		// TODO: implement this
	}
	
	// OPTIONAL: function to query a token about its kind
	//           boolean is(Token.Kind kind)
	
	// OPTIONAL: add any additional helper or convenience methods
	//           that you find make for a clean design

}
