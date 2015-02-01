package crux;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Parser {
    public static String studentName = "TODO: Your Name";
    public static String studentID = "TODO: Your 8-digit id";
    public static String uciNetID = "TODO: uci-net id";
    
 // Grammar Rule Reporting ==========================================
    private int parseTreeRecursionDepth = 0;
    private StringBuffer parseTreeBuffer = new StringBuffer();

    public void enterRule(NonTerminal nonTerminal) {
        String lineData = new String();
        for(int i = 0; i < parseTreeRecursionDepth; i++)
        {
            lineData += "  ";
        }
        lineData += nonTerminal.name();
        parseTreeBuffer.append(lineData + "\n");
        parseTreeRecursionDepth++;
    }
    
    private void exitRule(NonTerminal nonTerminal)
    {
        parseTreeRecursionDepth--;
    }
    
    public String parseTreeReport()
    {
        return parseTreeBuffer.toString();
    }

// Error Reporting ==========================================
    private StringBuffer errorBuffer = new StringBuffer();
    
    private String reportSyntaxError(NonTerminal nt)
    {
        String message = "SyntaxError(" + lineNumber() + "," + charPosition() + ")[Expected a token from " + nt.name() + " but got " + currentToken.kind + ".]";
        errorBuffer.append(message + "\n");
        return message;
    }
     
    private String reportSyntaxError(Token.Kind kind)
    {
        String message = "SyntaxError(" + lineNumber() + "," + charPosition() + ")[Expected " + kind + " but got " + currentToken.kind + ".]";
        errorBuffer.append(message + "\n");
        return message;
    }
    
    public String errorReport()
    {
        return errorBuffer.toString();
    }
    
    public boolean hasError()
    {
        return errorBuffer.length() != 0;
    }
    
    private class QuitParseException extends RuntimeException
    {
        private static final long serialVersionUID = 1L;
        public QuitParseException(String errorMessage) {
            super(errorMessage);
        }
    }
    
    private int lineNumber()
    {
        return currentToken.lineNumber();
    }
    
    private int charPosition()
    {
        return currentToken.charPosition();
    }
    
// SymbolTable Management ==========================
    private SymbolTable symbolTable;
    
    private void initSymbolTable()
    {
        symbolTable = new SymbolTable();
    }
    
    private void enterScope()
    {
        symbolTable.addMap();
    }
    
    private void exitScope()
    {
        symbolTable.removeMap();
    }

    private Symbol tryResolveSymbol(Token ident)
    {
        assert(ident.is(Token.Kind.IDENTIFIER));
        String name = ident.lexeme();
        try {
            return symbolTable.lookup(name);
        } catch (SymbolNotFoundError e) {
            String message = reportResolveSymbolError(name, ident.lineNumber(), ident.charPosition());
            return new ErrorSymbol(message);
        }
    }

    private String reportResolveSymbolError(String name, int lineNum, int charPos)
    {
        String message = "ResolveSymbolError(" + lineNum + "," + charPos + ")[Could not find " + name + ".]";
        errorBuffer.append(message + "\n");
        errorBuffer.append(symbolTable.toString() + "\n");
        return message;
    }

    private Symbol tryDeclareSymbol(Token ident)
    {
        assert(ident.is(Token.Kind.IDENTIFIER));
        String name = ident.lexeme();
        try {
            return symbolTable.insert(name);
        } catch (RedeclarationError re) {
            String message = reportDeclareSymbolError(name, ident.lineNumber(), ident.charPosition());
            return new ErrorSymbol(message);
        }
    }

    private String reportDeclareSymbolError(String name, int lineNum, int charPos)
    {
        String message = "DeclareSymbolError(" + lineNum + "," + charPos + ")[" + name + " already exists.]";
        errorBuffer.append(message + "\n");
        errorBuffer.append(symbolTable.toString() + "\n");
        return message;
    }    

// Helper Methods ==========================================

    private Token expectRetrieve(Token.Kind kind)
    {
        Token tok = currentToken;
        if (accept(kind))
            return tok;
        String errorMessage = reportSyntaxError(kind);
        throw new QuitParseException(errorMessage);
        //return ErrorToken(errorMessage);
    }
        
    private Token expectRetrieve(NonTerminal nt)
    {
        Token tok = currentToken;
        if (accept(nt))
            return tok;
        String errorMessage = reportSyntaxError(nt);
        throw new QuitParseException(errorMessage);
        //return ErrorToken(errorMessage);
    }
              
// Parser ==========================================
    
    private Scanner scanner;
    private Token currentToken;
    
    public Parser(Scanner scanner)
    {
    	this.scanner = scanner;
    	this.currentToken = scanner.next();
    }
    
    public void parse()
    {
        initSymbolTable();
        try {
            program();
        } catch (QuitParseException q) {
            errorBuffer.append("SyntaxError(" + lineNumber() + "," + charPosition() + ")");
            errorBuffer.append("[Could not complete parsing.]");
        }
    }
    
 // Helper Methods ==========================================
    private boolean have(Token.Kind kind)
    {
        return currentToken.is(kind);
    }
    
    private boolean have(NonTerminal nt)
    {
        return nt.firstSet().contains(currentToken.kind);
    }

    private boolean accept(Token.Kind kind)
    {
        if (have(kind)) {
            currentToken = scanner.next();
            return true;
        }
        return false;
    }    
    
    private boolean accept(NonTerminal nt)
    {
        if (have(nt)) {
            currentToken = scanner.next();
            return true;
        }
        return false;
    }
   
    private boolean expect(Token.Kind kind)
    {
        if (accept(kind))
            return true;
        String errorMessage = reportSyntaxError(kind);
        throw new QuitParseException(errorMessage);
    }
    
    private boolean expect(NonTerminal nt)
    {
        if (accept(nt))
            return true;
        String errorMessage = reportSyntaxError(nt);
        throw new QuitParseException(errorMessage);
    }
    
// Grammar Rules =====================================================
    
    // literal := INTEGER | FLOAT | TRUE | FALSE .
    public void literal()
    {
    	enterRule(NonTerminal.LITERAL);
        
        if (have(Token.Kind.INTEGER)) 
        {
            expect(Token.Kind.INTEGER);
        } 
        else if (have(Token.Kind.FLOAT)) 
        {
            expect(Token.Kind.FLOAT);
        } 
        else if (have(Token.Kind.TRUE)) 
        {
            expect(Token.Kind.TRUE);
        } 
        else if (have(Token.Kind.FALSE))
        {
            expect(Token.Kind.FALSE);
        } 
        else 
        {
            String message = reportSyntaxError(NonTerminal.LITERAL);
        }
        
        exitRule(NonTerminal.LITERAL);
    }
    
    // designator := IDENTIFIER { "[" expression0 "]" } .
    public void designator()
    {
        enterRule(NonTerminal.DESIGNATOR);

//        expect(Token.Kind.IDENTIFIER);
        Token identifier = expectRetrieve(Token.Kind.IDENTIFIER);
        Symbol symbol = tryDeclareSymbol(identifier);
        
        while (accept(Token.Kind.OPEN_BRACKET)) {
            expression0();
            expect(Token.Kind.CLOSE_BRACKET);
        }
        
        exitRule(NonTerminal.DESIGNATOR);
    }
    
    public void type()
    {
    	enterRule(NonTerminal.TYPE);
    	expect(Token.Kind.IDENTIFIER);
    	exitRule(NonTerminal.TYPE);
    }
    
    // op0 := ">=" | "<=" | "!=" | "==" | ">" | "<" .
    public void op0()
    {    	
    	enterRule(NonTerminal.OP0);
    	if(have(Token.Kind.GREATER_EQUAL))
    	{
    		expect(Token.Kind.GREATER_EQUAL);
    	}
    	else if(have(Token.Kind.LESSER_EQUAL))
    	{
    		expect(Token.Kind.LESSER_EQUAL);
    	}
    	else if(have(Token.Kind.NOT_EQUAL))
    	{
    		expect(Token.Kind.NOT_EQUAL);
    	}
    	else if(have(Token.Kind.EQUAL))
    	{
    		expect(Token.Kind.EQUAL);
    	}
    	else if(have(Token.Kind.GREATER_THAN))
    	{
    		expect(Token.Kind.GREATER_THAN);
    	}
    	else if(have(Token.Kind.LESS_THAN))
    	{
    		expect(Token.Kind.LESS_THAN);
    	}
    	exitRule(NonTerminal.OP0);
    }
    
    // op1 := "+" | "-" | "or" .
    public void op1()
    {
    	enterRule(NonTerminal.OP1);
    	if(have(Token.Kind.ADD))
    	{
    		expect(Token.Kind.ADD);
    	}
    	else if(have(Token.Kind.SUB))
    	{
    		expect(Token.Kind.SUB);
    	}
    	else if(have(Token.Kind.OR))
    	{
    		expect(Token.Kind.OR);
    	}
    	exitRule(NonTerminal.OP1);
    }
    
    //op2 := "*" | "/" | "and" .
    public void op2()
    {
    	enterRule(NonTerminal.OP2);
    	if(have(Token.Kind.MUL))
    	{
    		expect(Token.Kind.MUL);
    	}
    	else if(have(Token.Kind.DIV))
    	{
    		expect(Token.Kind.DIV);
    	}
    	else if(have(Token.Kind.AND))
    	{
    		expect(Token.Kind.AND);
    	}
    	exitRule(NonTerminal.OP2);
    }
    
    //expression0 := expression1 [ op0 expression1 ] .
    public void expression0()
    {
    	enterRule(NonTerminal.EXPRESSION0);
    	expression1();
    	if(have(NonTerminal.OP0))
    	{
    		op0();
    		expression1();
    	}
    	exitRule(NonTerminal.EXPRESSION0);
    }
    
    //expression1 := expression2 { op1  expression2 } .
    public void expression1()
    {
    	enterRule(NonTerminal.EXPRESSION1);
    	expression2();
    	while(have(NonTerminal.OP1))
    	{
    		op1();
    		expression2();
    	}
    	exitRule(NonTerminal.EXPRESSION1);
    }
    
    //expression2 := expression3 { op2 expression3 } .
    public void expression2()
    {
    	enterRule(NonTerminal.EXPRESSION2);
    	expression3();
    	while(have(NonTerminal.OP2))
    	{
    		op2();
    		expression3();
    	}
    	exitRule(NonTerminal.EXPRESSION2);
    }
    
    //expression3 := "not" expression3
    //| "(" expression0 ")"
    //| designator
    //| call-expression
    //| literal .
    public void expression3()
    {
    	enterRule(NonTerminal.EXPRESSION3);
    	if(have(Token.Kind.NOT))
    	{
    		expect(Token.Kind.NOT);
    		expression3();
    	}
    	else if(have(Token.Kind.OPEN_PAREN))
    	{
    		expect(Token.Kind.OPEN_PAREN);
    		expression0();
    		expect(Token.Kind.CLOSE_PAREN);
    	}
    	else if(have(NonTerminal.DESIGNATOR))
    	{
    		designator();
    	}
    	else if(have(NonTerminal.CALL_EXPRESSION))
    	{
    		call_Expression();
    	}
    	else if(have(NonTerminal.LITERAL))
    	{
    		literal();
    	}
    	
    	exitRule(NonTerminal.EXPRESSION3);
    }
    
    // call-expression := "::" IDENTIFIER "(" expression-list ")" .
    public void call_Expression()
    {
    	enterRule(NonTerminal.CALL_EXPRESSION);
    	expect(Token.Kind.CALL);
    	Token identifier = expectRetrieve(Token.Kind.IDENTIFIER);
    	Symbol symbol = tryResolveSymbol(identifier);
    	expect(Token.Kind.OPEN_PAREN);
    	expression_List();
    	expect(Token.Kind.CLOSE_PAREN);
    	exitRule(NonTerminal.CALL_EXPRESSION);
    }
    
    //expression-list := [ expression0 { "," expression0 } ] .
    public void expression_List()
    {
    	enterRule(NonTerminal.EXPRESSION_LIST);
    	if(have(NonTerminal.EXPRESSION0))
    	{
    		expression0();
    		while(have(Token.Kind.COMMA))
    		{
    			expect(Token.Kind.COMMA);
    			expression0();
    		}
    	}
    	exitRule(NonTerminal.EXPRESSION_LIST);
    }
    
    public void declaration_list()
    {
    	enterRule(NonTerminal.DECLARATION_LIST);
        
        while (have(NonTerminal.DECLARATION)) {
            declaration();
        }
       
        exitRule(NonTerminal.DECLARATION_LIST);
    }

    //declaration := variable-declaration | array-declaration | function-definition .
    public void declaration()
    {
    	enterRule(NonTerminal.DECLARATION);
    	if(have(NonTerminal.VARIABLE_DECLARATION))
    	{
    		variable_Declaration();
    	}
    	else if(have(NonTerminal.ARRAY_DECLARATION))
    	{
    		array_Declaration();
    	}
    	else if(have(NonTerminal.FUNCTION_DEFINITION))
    	{
    		function_Definition();
    	}
    	else 
    	{
            reportSyntaxError(NonTerminal.DECLARATION);
        }
    	exitRule(NonTerminal.DECLARATION);
    }
    
    //array-declaration := "array" IDENTIFIER ":" type "[" INTEGER "]" { "[" INTEGER "]" } ";"
    public void array_Declaration()
    {
    	enterRule(NonTerminal.ARRAY_DECLARATION);
    	expect(Token.Kind.ARRAY);
    	Token identifier = expectRetrieve(Token.Kind.IDENTIFIER);
    	Symbol symbol = tryDeclareSymbol(identifier);
    	expect(Token.Kind.COLON);
    	type();
    	expect(Token.Kind.OPEN_BRACKET);
    	
    	expect(Token.Kind.INTEGER);
    	expect(Token.Kind.CLOSE_BRACKET);
    	while(have(Token.Kind.OPEN_BRACKET))
    	{
    		expect(Token.Kind.OPEN_BRACKET);
    		expect(Token.Kind.INTEGER);
    		expect(Token.Kind.CLOSE_BRACKET);
    	}
    	expect(Token.Kind.SEMICOLON);
    	exitRule(NonTerminal.ARRAY_DECLARATION);
    }
    
    //function-definition := "func" IDENTIFIER "(" parameter-list ")" ":" type statement-block .
    public void function_Definition()
    {
    	enterRule(NonTerminal.FUNCTION_DEFINITION);
    	expect(Token.Kind.FUNC);
    	Token identifier = expectRetrieve(Token.Kind.IDENTIFIER);
    	Symbol symbol = tryDeclareSymbol(identifier);
    	expect(Token.Kind.OPEN_PAREN);
    	
    	enterScope();
    	parameter_List();
    	expect(Token.Kind.CLOSE_PAREN);
    	expect(Token.Kind.COLON);
    	type();
    	statement_Block();
    	exitScope();
    	
    	exitRule(NonTerminal.FUNCTION_DEFINITION);
    }
    
    //parameter-list := [ parameter { "," parameter } ] .
    public void parameter_List()
    {
    	enterRule(NonTerminal.PARAMETER_LIST);
    	if(have(NonTerminal.PARAMETER))
    	{
    		do{
    			parameter();
    		}
    		while(accept(Token.Kind.COMMA));
    		
    	}
    	exitRule(NonTerminal.PARAMETER_LIST);
    }
    
    //parameter := IDENTIFIER ":" type .
    public void parameter()
    {
    	enterRule(NonTerminal.PARAMETER);
    	Token identifier = expectRetrieve(Token.Kind.IDENTIFIER);
    	Symbol symbol = tryDeclareSymbol(identifier);
    	expect(Token.Kind.COLON);
    	type();
    	exitRule(NonTerminal.PARAMETER);
    }
    
    //variable-declaration := "var" IDENTIFIER ":" type ";"
    public void variable_Declaration()
    {
    	enterRule(NonTerminal.VARIABLE_DECLARATION);
    	
        expect(Token.Kind.VAR);
        Token identifier = expectRetrieve(Token.Kind.IDENTIFIER);
        Symbol symbol = tryDeclareSymbol(identifier);
        expect(Token.Kind.COLON);
        type();
        expect(Token.Kind.SEMICOLON);
        
        exitRule(NonTerminal.VARIABLE_DECLARATION);
    }
    
    //assignment-statement := "let" designator "=" expression0 ";"
    public void assignment_Statement()
    {
    	enterRule(NonTerminal.ASSIGNMENT_STATEMENT);
    	expect(Token.Kind.LET);
    	designator();
    	expect(Token.Kind.ASSIGN);
    	expression0();
    	expect(Token.Kind.SEMICOLON);
    	exitRule(NonTerminal.ASSIGNMENT_STATEMENT);
    }
    
    //call-statement := call-expression ";"
    public void call_Statement()
    {
    	enterRule(NonTerminal.CALL_STATEMENT);
    	call_Expression();
    	expect(Token.Kind.SEMICOLON);
    	exitRule(NonTerminal.CALL_STATEMENT);
    }
    
    //if-statement := "if" expression0 statement-block [ "else" statement-block ] .
    public void if_Statement()
    {
    	enterRule(NonTerminal.IF_STATEMENT);
    	expect(Token.Kind.IF);
    	expression0();
    	enterScope();
    	statement_Block();
    	exitScope();
    	if(have(Token.Kind.ELSE))
    	{
    		enterScope();
    		expect(Token.Kind.ELSE);
    		statement_Block();
    		exitScope();
    	}
    	exitRule(NonTerminal.IF_STATEMENT);
    }
    
    //while-statement := "while" expression0 statement-block .
    public void while_Statement()
    {
    	enterRule(NonTerminal.WHILE_STATEMENT);
    	expect(Token.Kind.WHILE);
    	expression0();
    	enterScope();
    	statement_Block();
    	exitScope();
    	exitRule(NonTerminal.WHILE_STATEMENT);
    }
    
    //return-statement := "return" expression0 ";" .
    public void return_Statement()
    {
    	enterRule(NonTerminal.RETURN_STATEMENT);
    	expect(Token.Kind.RETURN);
    	expression0();
    	expect(Token.Kind.SEMICOLON);
    	exitRule(NonTerminal.RETURN_STATEMENT);
    }
    
    /*
     * statement := variable-declaration
           | call-statement
           | assignment-statement
           | if-statement
           | while-statement
           | return-statement .
     */
    public void statement()
    {
    	enterRule(NonTerminal.STATEMENT);
    	if(have(NonTerminal.VARIABLE_DECLARATION))
    	{
    		variable_Declaration();
    	}
    	else if(have(NonTerminal.CALL_STATEMENT))
    	{
    		call_Statement();
    	}
    	else if(have(NonTerminal.ASSIGNMENT_STATEMENT))
    	{
    		assignment_Statement();
    	}
    	else if(have(NonTerminal.IF_STATEMENT))
    	{
    		if_Statement();
    	}
    	else if(have(NonTerminal.WHILE_STATEMENT))
    	{
    		while_Statement();
    	}
    	else if(have(NonTerminal.RETURN_STATEMENT))
    	{
    		return_Statement();
    	}
    	exitRule(NonTerminal.STATEMENT);
    }
    
    //statement-list := { statement } .
    public void statement_List()
    {
    	enterRule(NonTerminal.STATEMENT_LIST);
    	while(have(NonTerminal.STATEMENT))
    	{
    		statement();
    	}
    	exitRule(NonTerminal.STATEMENT_LIST);
    }
    
    //statement-block := "{" statement-list "}" .
    public void statement_Block()
    {
    	enterRule(NonTerminal.STATEMENT_BLOCK);
    	expect(Token.Kind.OPEN_BRACE);
    	statement_List();
    	expect(Token.Kind.CLOSE_BRACE);
    	exitRule(NonTerminal.STATEMENT_BLOCK);
    }
    
    // program := declaration-list EOF .
    public void program()
    {
    	enterRule(NonTerminal.PROGRAM);
        declaration_list();
        expect(Token.Kind.EOF);
        exitRule(NonTerminal.PROGRAM);
    } 


//    public void program()
//    {
//        throw new RuntimeException("implement symbol table into grammar rules");
//    }
}
