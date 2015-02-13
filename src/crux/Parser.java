package crux;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import ast.Command;

public class Parser {
    public static String studentName = "TODO: Your Name";
    public static String studentID = "TODO: Your 8-digit id";
    public static String uciNetID = "TODO: uci-net id";
    
// Parser ==========================================
   
    public ast.Command parse()
    {
        initSymbolTable();
        try {
            return program();
        } catch (QuitParseException q) {
            return new ast.Error(lineNumber(), charPosition(), "Could not complete parsing.");
        }
    }
    
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
        String message = "SyntaxError(" + lineNumber() + "," + charPosition() + ")[Expected a token from " + nt.name() + " but got " + currentToken.kind() + ".]";
        errorBuffer.append(message + "\n");
        return message;
    }
     
    private String reportSyntaxError(Token.Kind kind)
    {
        String message = "SyntaxError(" + lineNumber() + "," + charPosition() + ")[Expected " + kind + " but got " + currentToken.kind() + ".]";
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
    private boolean have(Token.Kind kind)
    {
        return currentToken.is(kind);
    }
    
    private boolean have(NonTerminal nt)
    {
        return nt.firstSet().contains(currentToken.kind());
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
        //return false;
    }
        
    private boolean expect(NonTerminal nt)
    {
        if (accept(nt))
            return true;
        String errorMessage = reportSyntaxError(nt);
        throw new QuitParseException(errorMessage);
        //return false;
    }
     
    private Token expectRetrieve(Token.Kind kind)
    {
        Token tok = currentToken;
        if (accept(kind))
            return tok;
        String errorMessage = reportSyntaxError(kind);
        throw new QuitParseException(errorMessage);
        //return ast.Error(errorMessage);
    }
        
    private Token expectRetrieve(NonTerminal nt)
    {
        Token tok = currentToken;
        if (accept(nt))
            return tok;
        String errorMessage = reportSyntaxError(nt);
        throw new QuitParseException(errorMessage);
        //return ast.Error(errorMessage);
    }
    
// Parser ==========================================
    
    private Scanner scanner;
    private Token currentToken;
    
    public Parser(Scanner scanner)
    {
    	this.scanner = scanner;
    	this.currentToken = scanner.next();
    }
   
// Grammar Rules =====================================================
    
    // literal := INTEGER | FLOAT | TRUE | FALSE .
    public ast.Expression literal()
    {
        ast.Expression expr;
        enterRule(NonTerminal.LITERAL);
        
        Token tok = expectRetrieve(NonTerminal.LITERAL);
        expr = Command.newLiteral(tok);
        
        exitRule(NonTerminal.LITERAL);
        return expr;
    }

 // designator := IDENTIFIER { "[" expression0 "]" } .
    public ast.Expression designator()
    {
        enterRule(NonTerminal.DESIGNATOR);
        Token identifier = expectRetrieve(Token.Kind.IDENTIFIER);
        Symbol symbol = tryDeclareSymbol(identifier);
        ast.Expression expr = new ast.AddressOf(identifier.lineNumber(), identifier.charPosition(), symbol);
        while (accept(Token.Kind.OPEN_BRACKET)) {
            ast.Expression amount = expression0();
            expr = new ast.Index(amount.lineNumber(), amount.charPosition(), expr, amount);
            expect(Token.Kind.CLOSE_BRACKET);
        }
        
        exitRule(NonTerminal.DESIGNATOR);
        return expr;
    }
    
    public void type()
    {
    	enterRule(NonTerminal.TYPE);
    	expect(Token.Kind.IDENTIFIER);
    	exitRule(NonTerminal.TYPE);
    }
    
    // op0 := ">=" | "<=" | "!=" | "==" | ">" | "<" .
    public Token op0()
    {    	
    	enterRule(NonTerminal.OP0);
		Token token = expectRetrieve(NonTerminal.OP0);
		exitRule(NonTerminal.OP0);
		return token;
    }
    
 // op1 := "+" | "-" | "or" .
    public Token op1()
    {
    	enterRule(NonTerminal.OP1);
		Token token = expectRetrieve(NonTerminal.OP1);
		exitRule(NonTerminal.OP1);
		return token;
    }
    
    //op2 := "*" | "/" | "and" .
    public Token op2()
    {
    	enterRule(NonTerminal.OP2);
		Token token = expectRetrieve(NonTerminal.OP2);
		exitRule(NonTerminal.OP2);
		return token;
    }
    
    //expression0 := expression1 [ op0 expression1 ] .
    public ast.Expression expression0()
    {
    	enterRule(NonTerminal.EXPRESSION0);
    	ast.Expression expr = expression1();
    	if(have(NonTerminal.OP0))
    	{
    		ast.Expression lhs = expr;
    		
    		Token op = op0();
    		ast.Expression rhs = expression1();
    		expr = Command.newExpression(lhs, op, rhs);
    	}
    	exitRule(NonTerminal.EXPRESSION0);
    	return expr;
    }
    
    //expression1 := expression2 { op1  expression2 } .
    public ast.Expression expression1()
    {
    	enterRule(NonTerminal.EXPRESSION1);
    	ast.Expression expr = expression2();
    	while(have(NonTerminal.OP1))
    	{
    		ast.Expression lhs = expr;
    		Token op = op1();
    		ast.Expression rhs = expression2();
    		expr = Command.newExpression(lhs, op, rhs);
    	}
    	exitRule(NonTerminal.EXPRESSION1);
    	return expr;
    }
    
    //expression2 := expression3 { op2 expression3 } .
    public ast.Expression expression2()
    {
    	enterRule(NonTerminal.EXPRESSION2);
    	ast.Expression expr = expression3();
    	while(have(NonTerminal.OP2))
    	{
    		ast.Expression lhs = expr;
    		Token op = op2();
    		ast.Expression rhs = expression3();
    		expr = Command.newExpression(lhs, op, rhs);
    	}
    	exitRule(NonTerminal.EXPRESSION2);
    	return expr;
    }
    
    //expression3 := "not" expression3
    //| "(" expression0 ")"
    //| designator
    //| call-expression
    //| literal .
    public ast.Expression expression3()
    {
    	enterRule(NonTerminal.EXPRESSION3);
    	ast.Expression expr = null;
    	if(have(Token.Kind.NOT))
    	{
    		Token not = expectRetrieve(Token.Kind.NOT);
			ast.Expression lhs = expression3();
			expr = Command.newExpression(lhs, not, null);
    	}
    	else if(have(Token.Kind.OPEN_PAREN))
    	{
    		expect(Token.Kind.OPEN_PAREN);
    		expr = expression0();
    		expect(Token.Kind.CLOSE_PAREN);
    	}
    	else if(have(NonTerminal.DESIGNATOR))
    	{
    		ast.Expression designator = designator();
    		expr = new ast.Dereference(designator.lineNumber(), designator.charPosition(), designator);
    	}
    	else if(have(NonTerminal.CALL_EXPRESSION))
    	{
    		expr = call_Expression();
    	}
    	else if(have(NonTerminal.LITERAL))
    	{
    		expr = literal();
    	}
    	
    	exitRule(NonTerminal.EXPRESSION3);
    	return expr;
    }
    
    // call-expression := "::" IDENTIFIER "(" expression-list ")" .
    public ast.Call call_Expression()
    {
    	ast.Call call;
    	enterRule(NonTerminal.CALL_EXPRESSION);
    	Token callToken = expectRetrieve(Token.Kind.CALL);
    	Token identifier = expectRetrieve(Token.Kind.IDENTIFIER);
    	Symbol symbol = tryResolveSymbol(identifier);
    	expect(Token.Kind.OPEN_PAREN);
    	ast.ExpressionList args = expression_List();
    	call = new ast.Call(callToken.lineNumber(), callToken.charPosition(), symbol, args);
    	
    	expect(Token.Kind.CLOSE_PAREN);
    	exitRule(NonTerminal.CALL_EXPRESSION);
    	return call;
    }
    
    //expression-list := [ expression0 { "," expression0 } ] .
    public ast.ExpressionList expression_List()
    {
    	enterRule(NonTerminal.EXPRESSION_LIST);
    	
    	ast.ExpressionList exprList = new ast.ExpressionList(lineNumber(), charPosition());
    	if(have(NonTerminal.EXPRESSION0))
    	{
    		ast.Expression expr = expression0();
    		exprList.add(expr);
    		while(have(Token.Kind.COMMA))
    		{
    			expect(Token.Kind.COMMA);
    			ast.Expression expression = expression0();
    			exprList.add(expression);
    		}
    	}
    	exitRule(NonTerminal.EXPRESSION_LIST);
    	return exprList;
    }
    
    public ast.DeclarationList declaration_list()
    {
    	enterRule(NonTerminal.DECLARATION_LIST);
        ast.DeclarationList decList = new ast.DeclarationList(lineNumber(), charPosition());
        while (have(NonTerminal.DECLARATION)) {
            ast.Declaration declaration = declaration();
            decList.add(declaration);
        }
       
        exitRule(NonTerminal.DECLARATION_LIST);
        return decList;
    }

    //declaration := variable-declaration | array-declaration | function-definition .
    public ast.Declaration declaration()
    {
    	enterRule(NonTerminal.DECLARATION);
    	ast.Declaration declaration = null;
    	if(have(NonTerminal.VARIABLE_DECLARATION))
    	{
    		declaration = variable_Declaration();
    	}
    	else if(have(NonTerminal.ARRAY_DECLARATION))
    	{
    		declaration = array_Declaration();
    	}
    	else if(have(NonTerminal.FUNCTION_DEFINITION))
    	{
    		declaration = function_Definition();
    	}
    	else 
    	{
            reportSyntaxError(NonTerminal.DECLARATION);
        }
    	exitRule(NonTerminal.DECLARATION);
    	return declaration;
    }
    
    //array-declaration := "array" IDENTIFIER ":" type "[" INTEGER "]" { "[" INTEGER "]" } ";"
    public ast.ArrayDeclaration array_Declaration()
    {
    	enterRule(NonTerminal.ARRAY_DECLARATION);
    	
    	Token array = expectRetrieve(Token.Kind.ARRAY);
    	Token identifier = expectRetrieve(Token.Kind.IDENTIFIER);
    	Symbol symbol = tryDeclareSymbol(identifier);
    	ast.ArrayDeclaration arrayDeclaration = new ast.ArrayDeclaration(array.lineNumber(), array.charPosition(), symbol);
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
    	return arrayDeclaration;
    }
    
    //function-definition := "func" IDENTIFIER "(" parameter-list ")" ":" type statement-block .
    public ast.FunctionDefinition function_Definition()
    {
    	enterRule(NonTerminal.FUNCTION_DEFINITION);
    	Token func = expectRetrieve(Token.Kind.FUNC);
    	Token identifier = expectRetrieve(Token.Kind.IDENTIFIER);
    	Symbol symbol = tryDeclareSymbol(identifier);
    	expect(Token.Kind.OPEN_PAREN);
    	
    	enterScope();
    	List<Symbol> args = parameter_List();
    	expect(Token.Kind.CLOSE_PAREN);
    	expect(Token.Kind.COLON);
    	type();
    	ast.StatementList statements = statement_Block();
    	ast.FunctionDefinition functionDefinition = new ast.FunctionDefinition(func.lineNumber(), func.charPosition(), symbol, args, statements);
    	exitScope();
    	
    	exitRule(NonTerminal.FUNCTION_DEFINITION);
    	return functionDefinition;
    }
    
    //parameter-list := [ parameter { "," parameter } ] .
    public List<Symbol> parameter_List()
    {
    	enterRule(NonTerminal.PARAMETER_LIST);
    	List<Symbol> list = new LinkedList<Symbol>();
    	if(have(NonTerminal.PARAMETER))
    	{
    		do{
    			Symbol symbol = parameter();
    			list.add(symbol);
    		}
    		while(accept(Token.Kind.COMMA));
    		
    	}
    	exitRule(NonTerminal.PARAMETER_LIST);
    	return list;
    }
    
    //parameter := IDENTIFIER ":" type .
    public Symbol parameter()
    {
    	enterRule(NonTerminal.PARAMETER);
    	Token identifier = expectRetrieve(Token.Kind.IDENTIFIER);
    	Symbol symbol = tryDeclareSymbol(identifier);
    	expect(Token.Kind.COLON);
    	type();
    	exitRule(NonTerminal.PARAMETER);
    	return symbol;
    }
    
    //variable-declaration := "var" IDENTIFIER ":" type ";"
    public ast.VariableDeclaration variable_Declaration()
    {
    	enterRule(NonTerminal.VARIABLE_DECLARATION);
    	
        Token var = expectRetrieve(Token.Kind.VAR);
        Token identifier = expectRetrieve(Token.Kind.IDENTIFIER);
        Symbol symbol = tryDeclareSymbol(identifier);
        ast.VariableDeclaration variableDeclaration = new ast.VariableDeclaration(var.lineNumber(), var.charPosition(), symbol);
        expect(Token.Kind.COLON);
        type();
        expect(Token.Kind.SEMICOLON);
        
        exitRule(NonTerminal.VARIABLE_DECLARATION);
        return variableDeclaration;
    }
    
    //assignment-statement := "let" designator "=" expression0 ";"
    public ast.Assignment assignment_Statement()
    {
    	enterRule(NonTerminal.ASSIGNMENT_STATEMENT);
    	Token let = expectRetrieve(Token.Kind.LET);
    	ast.Expression destination = designator();
    	expect(Token.Kind.ASSIGN);
    	ast.Expression source = expression0();
    	ast.Assignment assignment = new ast.Assignment(let.lineNumber(), let.charPosition(), destination, source);
    	expect(Token.Kind.SEMICOLON);
    	exitRule(NonTerminal.ASSIGNMENT_STATEMENT);
    	return assignment;
    }
    
    //call-statement := call-expression ";"
    public ast.Call call_Statement()
    {
    	enterRule(NonTerminal.CALL_STATEMENT);
    	ast.Call call = call_Expression();
    	expect(Token.Kind.SEMICOLON);
    	exitRule(NonTerminal.CALL_STATEMENT);
    	return call;
    }
    
    //if-statement := "if" expression0 statement-block [ "else" statement-block ] .
    public ast.IfElseBranch if_Statement()
    {
    	enterRule(NonTerminal.IF_STATEMENT);
    	Token ifToken = expectRetrieve(Token.Kind.IF);
    	ast.Expression condition = expression0();
    	enterScope();
    	ast.StatementList thenBlock = statement_Block();
    	exitScope();
    	ast.StatementList elseBlock;
    	if(have(Token.Kind.ELSE))
    	{
    		enterScope();
    		expect(Token.Kind.ELSE);
    		elseBlock = statement_Block();
    		exitScope();
    	}
    	else
    	{
    		elseBlock = new ast.StatementList(lineNumber(), charPosition());
    	}
    	ast.IfElseBranch ifElseBranch = new ast.IfElseBranch(ifToken.lineNumber(), ifToken.charPosition(), condition, thenBlock, elseBlock);
    	exitRule(NonTerminal.IF_STATEMENT);
    	return ifElseBranch;
    }
    
    //while-statement := "while" expression0 statement-block .
    public ast.WhileLoop while_Statement()
    {
    	enterRule(NonTerminal.WHILE_STATEMENT);
    	Token whileToken = expectRetrieve(Token.Kind.WHILE);
    	ast.Expression condition = expression0();
    	enterScope();
    	ast.StatementList body = statement_Block();
    	exitScope();
    	ast.WhileLoop whileLoop = new ast.WhileLoop(whileToken.lineNumber(), whileToken.charPosition(), condition, body);
    	exitRule(NonTerminal.WHILE_STATEMENT);
    	return whileLoop;
    }
    
    //return-statement := "return" expression0 ";" .
    public ast.Return return_Statement()
    {
    	enterRule(NonTerminal.RETURN_STATEMENT);
    	Token returnToken = expectRetrieve(Token.Kind.RETURN);
    	ast.Expression expr = expression0();
    	expect(Token.Kind.SEMICOLON);
    	ast.Return returnStatement = new ast.Return(returnToken.lineNumber(), returnToken.charPosition(), expr);
    	exitRule(NonTerminal.RETURN_STATEMENT);
    	return returnStatement;
    }
    
    /*
     * statement := variable-declaration
           | call-statement
           | assignment-statement
           | if-statement
           | while-statement
           | return-statement .
     */
    public ast.Statement statement()
    {
    	enterRule(NonTerminal.STATEMENT);
    	ast.Statement statement = null;
    	if(have(NonTerminal.VARIABLE_DECLARATION))
    	{
    		statement = variable_Declaration();
    	}
    	else if(have(NonTerminal.CALL_STATEMENT))
    	{
    		statement = call_Statement();
    	}
    	else if(have(NonTerminal.ASSIGNMENT_STATEMENT))
    	{
    		statement = assignment_Statement();
    	}
    	else if(have(NonTerminal.IF_STATEMENT))
    	{
    		statement = if_Statement();
    	}
    	else if(have(NonTerminal.WHILE_STATEMENT))
    	{
    		statement = while_Statement();
    	}
    	else if(have(NonTerminal.RETURN_STATEMENT))
    	{
    		statement = return_Statement();
    	}
    	exitRule(NonTerminal.STATEMENT);
    	return statement;
    }
    
    //statement-list := { statement } .
    public ast.StatementList statement_List()
    {
    	enterRule(NonTerminal.STATEMENT_LIST);
    	ast.StatementList statementList = new ast.StatementList(lineNumber(), charPosition());
    	while(have(NonTerminal.STATEMENT))
    	{
    		ast.Statement statement = statement();
    		statementList.add(statement);
    	}
    	exitRule(NonTerminal.STATEMENT_LIST);
    	return statementList;
    }
    
    //statement-block := "{" statement-list "}" .
    public ast.StatementList statement_Block()
    {
    	enterRule(NonTerminal.STATEMENT_BLOCK);
    	expect(Token.Kind.OPEN_BRACE);
    	ast.StatementList list = statement_List();
    	expect(Token.Kind.CLOSE_BRACE);
    	exitRule(NonTerminal.STATEMENT_BLOCK);
    	return list;
    }
    
    // program := declaration-list EOF .
    public ast.DeclarationList program()
    {
    	enterRule(NonTerminal.PROGRAM);
		ast.DeclarationList declarationList = declaration_list();
		expect(Token.Kind.EOF);
		exitRule(NonTerminal.PROGRAM);
		return declarationList;
    }
    
}
