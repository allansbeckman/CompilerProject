package crux;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;



public class SymbolTable {
    public static String studentName = "Allan Beckman";
    public static String studentID = "21588725";
    public static String uciNetID = "beckmana";
	LinkedList<LinkedHashMap<String, Symbol>> linkedMap;
    public SymbolTable()
    {
        linkedMap = new LinkedList<LinkedHashMap<String, Symbol>>();
        addMap();
        preDefinedFunctions();
        
    }
    
    /*
     * 	readInt() : int, Prompts the user for an integer.
	   	readFloat() : float, Prompts the user for an integer.
		printBool(arg:bool) : void, Prints a bool value to the screen.
		printInt(arg:int) : void, Prints an integer value to the screen.
		printFloat(arg:float) : void, Prints a float value to the screen.
		println() : void, Prints newline character to the screen.
     */
    private void preDefinedFunctions()
    {
    	LinkedHashMap<String, Symbol> outerMostScope = this.linkedMap.getFirst();
    	outerMostScope.put("readInt", new Symbol("readInt"));
    	outerMostScope.put("readFloat", new Symbol("readFloat"));
    	outerMostScope.put("printBool", new Symbol("printBool"));
    	outerMostScope.put("printInt", new Symbol("printInt"));
    	outerMostScope.put("printFloat", new Symbol("printFloat"));
    	outerMostScope.put("println", new Symbol("println"));
    }
    
    public void addMap()
    {
    	LinkedHashMap<String, Symbol> LinkedHashMap = new LinkedHashMap<String, Symbol>();
    	linkedMap.addFirst(LinkedHashMap);
    }
    
    public void removeMap()
    {
    	this.linkedMap.removeFirst();
    }
    
    public Symbol lookup(String name) throws SymbolNotFoundError
    {
    	Iterator<LinkedHashMap<String, Symbol>> iterator = linkedMap.iterator();
    	while(iterator.hasNext())
    	{
    		LinkedHashMap<String, Symbol> scope = iterator.next();
    		Symbol symbol = scope.get(name);
    		if(symbol != null)
    		{
    			return symbol;
    		}
    	}
    	throw new SymbolNotFoundError("No symbol found");
    }
       
    public Symbol insert(String name) throws RedeclarationError
    {
    	Symbol symbol = new Symbol(name);
    	LinkedHashMap<String, Symbol> scope = this.linkedMap.getFirst();
    	if(scope.containsKey(name))
    	{
    		throw new RedeclarationError(scope.get(name));
    	}
    	else
    	{
        	scope.put(name, symbol);
    	}
    	return symbol;
    }
    
    public String toString()
    {
//        StringBuffer sb = new StringBuffer();
//        
//        Iterator<LinkedHashMap<String, Symbol>> iterator = linkedMap.iterator();
//    	while(iterator.hasNext())
//    	{
//    		LinkedHashMap<String, Symbol> scope = iterator.next();
//    		sb.append(scope.toString());
//    	}
//    	
//        String indent = new String();
//        for (int i = 0; i < linkedMap.size(); i++) {
//            indent += "  ";
//        }
//        
//        for (Symbol s : linkedMap.getFirst().values())
//        {
//            sb.append(indent + s.toString() + "\n");
//        }
//        return sb.toString();
    	
    	StringBuffer s = new StringBuffer();
    	Iterator<LinkedHashMap<String, Symbol>> iterator = linkedMap.descendingIterator();
    	String indent = "";
    	while(iterator.hasNext())
    	{
    		LinkedHashMap<String, Symbol> scope = iterator.next();
    		for(Entry<String, Symbol> entry : scope.entrySet()) {
    		    Symbol value = entry.getValue();
    		    s.append(indent + value.toString() + "\n");
    		}
    		indent += "  ";
    	}
    	return s.toString();
    }
}

class SymbolNotFoundError extends Error
{
    private static final long serialVersionUID = 1L;
    private String name;
    
    SymbolNotFoundError(String name)
    {
        this.name = name;
    }
    
    public String name()
    {
        return name;
    }
}

class RedeclarationError extends Error
{
    private static final long serialVersionUID = 1L;

    public RedeclarationError(Symbol sym)
    {
        super("Symbol " + sym + " being redeclared.");
    }
}
