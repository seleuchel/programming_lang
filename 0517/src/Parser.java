import java.util.*;

public class Parser {
    // Recursive descent parser that inputs a C++Lite program and 
    // generates its abstract syntax.  Each method corresponds to
    // a concrete syntax grammar rule, which appears as a comment
    // at the beginning of the method.
  
    Token token;          // current token from the input stream
    Lexer lexer;
  
    public Parser(Lexer ts) { // Open the C++Lite source program
        lexer = ts;                          // as a token stream, and
        token = lexer.next();            // retrieve its first Token
    }
  
    private String match (TokenType t) {
        String value = token.value();
        if (token.type().equals(t))
            token = lexer.next();
        else
            error(t);
        return value;
    }
  
    private void error(TokenType tok) {
        System.err.println("Syntax error: expecting: " + tok 
                           + "; saw: " + token);
        System.exit(1);
    }
  
    private void error(String tok) {
        System.err.println("Syntax error: expecting: " + tok 
                           + "; saw: " + token);
        System.exit(1);
    }
  
    public Program program() {
        // Program --> void main ( ) '{' Declarations Statements '}'
        TokenType[ ] header = {TokenType.Int, TokenType.Main,
                          TokenType.LeftParen, TokenType.RightParen};
        for (int i=0; i<header.length; i++)   // bypass "int main ( )"
            match(header[i]);

        System.out.println(match(TokenType.LeftBrace));
        // student exercise
        Declarations d = declarations();
        Block b = statements ();
        
        //System.out.println(match(TokenType.RightBrace));
        return new Program(d, b);  // student exercise
    }
  
    private Declarations declarations () { // sentences
        // Declarations --> { Declaration }
    	Declarations ds = new Declarations();
    	while(isType()) {
    		declaration(ds);
    	}
        return ds;  // student exercise
    }
  
    private void declaration (Declarations ds) { // one sentence
        // Declaration  --> Type Identifier { , Identifier } ;
    	Declaration d = null;
    	Type t = type();
    	d = new Declaration(new Variable(match(TokenType.Identifier)) , t);
    	ds.add(d);
    	
    	while (token.type().equals(TokenType.Comma)) {
    		Operator op = new Operator(match(token.type()));
    		Declaration d1 = new Declaration(new Variable(match(TokenType.Identifier)), type());
    		ds.add(d1);
    		
    	}
    	match(TokenType.Semicolon); // student exercise	
    }
  
    private Type type () {  // seleuchel ok
        // Type  -->  int | bool | float | char 
        Type t = null;
        if(token.type().equals(TokenType.Int))
        	t = t.INT;
        else if (token.type().equals(TokenType.Bool))
        	t = t.BOOL;
        else if (token.type().equals(TokenType.Float))
        	t = t.FLOAT;
        else if (token.type().equals(TokenType.Char))
        	t = t.CHAR;
        else
        	error("int | bool | float | char "); // student exercise
        token = lexer.next(); 
        return t;          
    }
  
    private Statement statement() { //hey
        // Statement --> ; | Block | Assignment | IfStatement | WhileStatement
        Statement s = null;
        if (token.type().equals(TokenType.Semicolon)) {
        	match(TokenType.Semicolon);
        	s = new Skip();
        }else if (token.type().equals(TokenType.LeftBrace)) {
        	s = statements();        
    	}else if (token.type().equals(TokenType.Identifier)) {
        	s = assignment();
        }else if (token.type().equals(TokenType.If)) {
        	s = ifStatement();
        }else if (token.type().equals(TokenType.While)) {
        	s = whileStatement();
        }
        return s; // student exercise
    }
  
    private Block statements () {
        // Block --> '{' Statements '}'
        Block b = new Block();
        System.out.println(token);
        if(token.type().equals(TokenType.LeftBrace))
        	token = lexer.next();
        System.out.println(token);
        Statement s = statement();
        while(s != null) {
        	System.out.println(token);
        	b.members.add(s);
        	s = statement();
        }
        System.out.println(token);
        
        // last block }
        if(token.type().equals(TokenType.RightBrace))
        	token = lexer.next();
        return b; //student
    }
  
    private Assignment assignment () { // @ no string? hey
        // Assignment --> Identifier = Expression ;
    	Variable v = new Variable(match(TokenType.Identifier));
    	match(TokenType.Assign);
    	Expression e = expression();
    	match(TokenType.Semicolon);
        return new Assignment(v, e);  // student exercise
    }
  
    private Conditional ifStatement () {
        // IfStatement --> if ( Expression ) Statement [ else Statement ]
    	Conditional c = null;
    	Expression e = null;
    	if (token.type().equals(TokenType.LeftParen)) {
    		token = lexer.next(); // not op
    		e = expression();
    	}
    	
    	if (token.type().equals(TokenType.Else)) {
    		token = lexer.next();
    		Statement s = statement();
    		c = new Conditional(e, c, s);
    	}
    	c = new Conditional(e, c);
        return c;  // student exercise
    }
  
    private Loop whileStatement () { // seleuchel ok
        // WhileStatement --> while ( Expression ) Statement
    	Expression e = null;
    	match(TokenType.While);
    	match(TokenType.LeftParen);
    	e = expression(); //TokenType.LeftParen
    	match(TokenType.RightParen);

    	Statement s = statement();
        return new Loop(e, s);  // student exercise
    }

    private Expression expression () { // seleuchel ok
        // Expression --> Conjunction { || Conjunction }
    	Expression c1 = conjunction();
    	while(token.type().equals(TokenType.Or)) {
    		Operator op = new Operator(match(token.type()));
    		token = lexer.next();
    		Expression c2 = conjunction();
    		return new Binary(op, c1 ,c2);
    	}
        return c1;  // student exercise
    }
  
    private Expression conjunction () { // seleuchel ok
        // Conjunction --> Equality { && Equality }
    	Expression eq1 = equality ();
    	while(token.type().equals(TokenType.And)) { //?
    		Operator op = new Operator(match(token.type()));
    		Expression eq2 = equality();
    		return new Binary(op, eq1 ,eq2);
    	}
        return eq1;  // student exercise
    }
  
    private Expression equality () { //seleuchel ok
        // Equality --> Relation [ EquOp Relation ]
    	Expression r1 = relation();
    	
    	if(isEqualityOp()) {
    		Operator op = new Operator(match(token.type()));
    		Expression r2 = relation();
    		r1 = new Binary(op, r1 ,r2);
    	}
        return r1;  // student exercise
    }

    private Expression relation (){ // seleuchel ok
        // Relation --> Addition [RelOp Addition] 
    	Expression a1 = addition();
    	
    	if(isRelationalOp()) {
    		Operator op = new Operator(match(token.type()));
    		Expression a2 = addition();
    		a1 = new Binary(op, a1 ,a2);
    	}
        return a1;  // student exercise
    }
  
    private Expression addition () {
        // Addition --> Term { AddOp Term }
        Expression e = term();
        while (isAddOp()) {
            Operator op = new Operator(match(token.type()));
            Expression term2 = term();
            e = new Binary(op, e, term2);
        }
        return e;
    }
  
    private Expression term () {
        // Term --> Factor { MultiplyOp Factor }
        Expression e = factor();
        while (isMultiplyOp()) {
            Operator op = new Operator(match(token.type()));
            Expression term2 = factor();
            e = new Binary(op, e, term2);
        }
        return e;
    }
  
    private Expression factor() {
        // Factor --> [ UnaryOp ] Primary 
        if (isUnaryOp()) {
            Operator op = new Operator(match(token.type()));
            Expression term = primary();
            return new Unary(op, term);
        }else
        	return primary();
    }
  
    private Expression primary () {
        // Primary --> Identifier | Literal | ( Expression )
        //             | Type ( Expression )
        Expression e = null;
        if (token.type().equals(TokenType.Identifier)) {
            e = new Variable(match(TokenType.Identifier));
        } else if (isLiteral()) {
            e = literal();
        } else if (token.type().equals(TokenType.LeftParen)) {
            token = lexer.next();
            e = expression();       
            match(TokenType.RightParen);
        } else if (isType( )) {
            Operator op = new Operator(match(token.type()));
            match(TokenType.LeftParen);
            Expression term = expression();
            match(TokenType.RightParen);
            e = new Unary(op, term);
        } else error("Identifier | Literal | ( | Type");
        return e;
    }

    private Value literal( ) {
    	
    	Value v = null;
    	if (token.type().equals(TokenType.IntLiteral)) {
    		v = new IntValue();
    	}else if (token.type().equals(TokenType.FloatLiteral)) {
    		v = new FloatValue();
    	}else if (token.type().equals(TokenType.CharLiteral)) {
    		v = new CharValue();
    	}else if (token.type().equals(TokenType.True)) {
    		v = new BoolValue(true);
    	}else if (token.type().equals(TokenType.False)) {
    		v = new BoolValue(false);
    	}else {
    		error("IntValue | FloatValue | CharValue | BoolValue");
    	}
    	token = lexer.next();
    	
        return v;  // student exercise
    }
  

    private boolean isAddOp( ) {
        return token.type().equals(TokenType.Plus) ||
               token.type().equals(TokenType.Minus);
    }
    
    private boolean isMultiplyOp( ) {
        return token.type().equals(TokenType.Multiply) ||
               token.type().equals(TokenType.Divide);
    }
    
    private boolean isUnaryOp( ) {
        return token.type().equals(TokenType.Not) ||
               token.type().equals(TokenType.Minus);
    }
    
    private boolean isEqualityOp( ) {
        return token.type().equals(TokenType.Equals) ||
            token.type().equals(TokenType.NotEqual);
    }
    
    private boolean isRelationalOp( ) {
        return token.type().equals(TokenType.Less) ||
               token.type().equals(TokenType.LessEqual) || 
               token.type().equals(TokenType.Greater) ||
               token.type().equals(TokenType.GreaterEqual);
    }
    
    private boolean isType( ) {
        return token.type().equals(TokenType.Int)
            || token.type().equals(TokenType.Bool) 
            || token.type().equals(TokenType.Float)
            || token.type().equals(TokenType.Char);
    }
    
    private boolean isLiteral( ) {
        return token.type().equals(TokenType.IntLiteral) ||
            isBooleanLiteral() ||
            token.type().equals(TokenType.FloatLiteral) ||
            token.type().equals(TokenType.CharLiteral);
    }
    
    private boolean isBooleanLiteral( ) {
        return token.type().equals(TokenType.True) ||
            token.type().equals(TokenType.False);
    }
    
    public static void main(String args[]) {
        Parser parser  = new Parser(new Lexer(args[0]));
        Program prog = parser.program();
        prog.display();           // display abstract syntax tree
    } //main

} // Parser
