package types;

import java.util.HashMap;
import java.util.List;

import crux.Symbol;
import ast.*;

public class TypeChecker implements CommandVisitor {
    
    private HashMap<Command, Type> typeMap;
    private StringBuffer errorBuffer;
    
    /* Set to true if the current execution path needs a return statement. */
    private boolean needsReturn;
    
    /* A count of the number of returns that has been found in the current function body. */
    private int nbrFoundReturns;
    
    /* The symbol for the current function we're in. */
	private Symbol curFuncSym;

	/* The return type of the current function we're in. */
	private Type curFuncRetType;

    /* Useful error strings:
     *
     * "Function " + func.name() + " has a void argument in position " + pos + "."
     * "Function " + func.name() + " has an error in argument in position " + pos + ": " + error.getMessage()
     *
     * "Function main has invalid signature."
     *
     * "Not all paths in function " + currentFunctionName + " have a return."
     *
     * "IfElseBranch requires bool condition not " + condType + "."
     * "WhileLoop requires bool condition not " + condType + "."
     *
     * "Function " + currentFunctionName + " returns " + currentReturnType + " not " + retType + "."
     *
     * "Variable " + varName + " has invalid type " + varType + "."
     * "Array " + arrayName + " has invalid base type " + baseType + "."
     */

    public TypeChecker()
    {
        typeMap = new HashMap<Command, Type>();
        errorBuffer = new StringBuffer();
    }

    private void reportError(int lineNum, int charPos, String message)
    {
        errorBuffer.append("TypeError(" + lineNum + "," + charPos + ")");
        errorBuffer.append("[" + message + "]" + "\n");
    }

    private void put(Command node, Type type)
    {
        if (type instanceof ErrorType) {
            reportError(node.lineNumber(), node.charPosition(), ((ErrorType)type).getMessage());
        }
        typeMap.put(node, type);
    }
    
    public Type getType(Command node)
    {
        return typeMap.get(node);
    }
    
    public boolean check(Command ast)
    {
        ast.accept(this);
        return !hasError();
    }
    
    public boolean hasError()
    {
        return errorBuffer.length() != 0;
    }
    
    public String errorReport()
    {
        return errorBuffer.toString();
    }

    /**
     * Visit and get type for node.
     * @param node The node to visit.
     * @return The type of the node.
     */
    private Type visitRetrieveType(Visitable node) {
		node.accept(this);
		return getType((Command) node);
    }

    
    @Override
    public void visit(ExpressionList node) {
    	TypeList typeList = new TypeList();
        for(Expression expr : node) {
			typeList.append(visitRetrieveType(expr));
        }
        put(node, typeList);
    }

    @Override
    public void visit(DeclarationList node) {
    	for (Declaration decl : node) {
        	decl.accept(this);
        }
    }

    @Override
    public void visit(StatementList node) {
    	needsReturn = true;
        boolean foundReturn = false;
        for (Statement stmt : node) {
        	stmt.accept(this);
        	if (!needsReturn) {
        		foundReturn = true;
        	}
        }
        needsReturn = !foundReturn;
    }

    @Override
    public void visit(AddressOf node) {
    	Type type = node.symbol().type();
		put(node, new AddressType(type));
    }

    @Override
    public void visit(LiteralBool node) {
    	put(node, new BoolType());
    }

    @Override
    public void visit(LiteralFloat node) {
        put(node, new FloatType());
    }

    @Override
    public void visit(LiteralInt node) {
        put(node, new IntType());
    }

    @Override
    public void visit(VariableDeclaration node) {
    	Symbol symbol = node.symbol();
    	Type varType = symbol.type();
        put(node, varType.declare(symbol));
    }

    @Override
    public void visit(ArrayDeclaration node) {
    	Symbol symbol = node.symbol();
		Type type = symbol.type();
		put(node, type.baseType(symbol));
    }

    @Override
    public void visit(FunctionDefinition node) {
    	Symbol func = node.function();
        List<Symbol> args = node.arguments();
        Type returnType = ((FuncType) func.type()).returnType();

        if (func.name().equals("main")) {
        	if (args.size() != 	0 || !(returnType instanceof VoidType)) {
				put(node, new ErrorType("Function main has invalid signature."));
				return;
			}
        } else {
        	int pos = 0;
        	for (Symbol arg : args) {
				Type argType = arg.type();
				if (argType instanceof ErrorType) {
					put(node, new ErrorType("Function " + func.name() + " has an error in argument in position " + pos + ": " + ((ErrorType) argType).getMessage()));
					return;
				} else if (argType instanceof VoidType) {
 	 	 	 		put(node, new ErrorType("Function " + func.name() + " has a void argument in position " + pos + "."));
					return;
				}
				++pos;
        	}
        }

		curFuncSym = func;
		curFuncRetType = returnType;
		nbrFoundReturns = 0;
        visit(node.body());
		if (!(returnType instanceof VoidType) && needsReturn) { 
        	put(node, new ErrorType("Not all paths in function " + func.name() + " have a return."));
		} else {
			put(node, returnType);
		}
    }

    @Override
    public void visit(Comparison node) {
    	Type lhs = visitRetrieveType(node.leftSide());
    	Type rhs = visitRetrieveType(node.rightSide());
        put(node, lhs.compare(rhs));
    }
    
    @Override
    public void visit(Addition node) {
    	Type lhs = visitRetrieveType(node.leftSide());
        Type rhs = visitRetrieveType(node.rightSide());
        if (lhs != null && rhs != null) {
        	Type res = lhs.add(rhs);
			put(node, res);
        }
    }
    
    @Override
    public void visit(Subtraction node) {
    	Type lhs = visitRetrieveType(node.leftSide());
        Type rhs = visitRetrieveType(node.rightSide());
		put(node, lhs.sub(rhs));
    }
    
    @Override
    public void visit(Multiplication node) {
        Type lhs = visitRetrieveType(node.leftSide());
        Type rhs = visitRetrieveType(node.rightSide());
        put(node, lhs.mul(rhs));
    }
    
    @Override
    public void visit(Division node) {
        Type lhs = visitRetrieveType(node.leftSide());
        Type rhs = visitRetrieveType(node.rightSide());
        put(node, lhs.div(rhs));
    }
    
    @Override
    public void visit(LogicalAnd node) {
        Type lhs = visitRetrieveType(node.leftSide());
        Type rhs = visitRetrieveType(node.rightSide());
        put(node, lhs.and(rhs));
    }

    @Override
    public void visit(LogicalOr node) {
        Type lhs = visitRetrieveType(node.leftSide());
        Type rhs = visitRetrieveType(node.rightSide());
        put(node, lhs.or(rhs));
    }

    @Override
    public void visit(LogicalNot node) {
    	Type type = visitRetrieveType(node.expression());
		if (type.equivalent(new BoolType())) {
			put(node, type);
		} else {
			put(node, new ErrorType("Cannot negate " + type + "."));
		}
    }
    
    @Override
    public void visit(Dereference node) {
    	put(node, visitRetrieveType(node.expression()).deref());
    }

    @Override
    public void visit(Index node) {
    	Type amountType = visitRetrieveType(node.amount());
        Type baseType = visitRetrieveType(node.base());
		Type resType = baseType.index(amountType);
		put(node, resType);
    }

    @Override
    public void visit(Assignment node) {
    	Type srcType = visitRetrieveType(node.source());
		Type destType = visitRetrieveType(node.destination());
        put(node, destType.assign(srcType));
    }

    @Override
    public void visit(Call node) {
    	Type funcType = node.function().type();
		Type callArgTypes = visitRetrieveType(node.arguments());
		put(node, funcType.call(callArgTypes));
    }

    @Override
    public void visit(IfElseBranch node) {
    	Type condType = visitRetrieveType(node.condition());
        if (!condType.equivalent(new BoolType())) {
     	 	put(node, new ErrorType("IfElseBranch requires bool condition not " + condType + "."));
     	 	return;
        }

		needsReturn = true;
		visit(node.thenBlock());
		boolean thenNeedsReturn = this.needsReturn ;
		needsReturn = true;
		visit(node.elseBlock());
		boolean elseNeedsReturn = this.needsReturn ;

		needsReturn = (thenNeedsReturn ^ elseNeedsReturn) || (thenNeedsReturn && elseNeedsReturn);
    }

    @Override
    public void visit(WhileLoop node) {
    	Type condType = visitRetrieveType(node.condition());
        if (!condType.equivalent(new BoolType())) {
     	 	put(node, new ErrorType("WhileLoop requires bool condition not " + condType + "."));
     	 	return;
        }

		int prevNbrRets = this.nbrFoundReturns;
		needsReturn = true;
		visit(node.body());
		needsReturn = needsReturn || (this.nbrFoundReturns > prevNbrRets);
    }

    @Override
    public void visit(Return node) {
    	Type retType = visitRetrieveType(node.argument());
		++nbrFoundReturns;
		if (!retType.equivalent(curFuncRetType)) {
			put(node, new ErrorType("Function " + curFuncSym.name() + " returns " + curFuncRetType + " not " + retType + "."));
		} else {
			put(node, retType);
		}
        needsReturn = false;
    }

    @Override
    public void visit(ast.Error node) {
        put(node, new ErrorType(node.message()));
    }
}
