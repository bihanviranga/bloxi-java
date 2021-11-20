# Bloxi grammar

## Grammar productions
```
program     -> declaration* EOF;
declaration -> funDecl | varDecl | classDecl | statement;
classDecl   -> "class" IDENTIFIER "{" function* "}";
funDecl     -> "fun" function;
function    -> IDENTIFIER "(" parameters? ")" block;
parameters  -> IDENTIFIER ("," IDENTIFIER)*;
varDecl     -> "var" IDENTIFIER ("=" expression)? ";";
statement   -> exprStmt | printStmt | ifStmt | whileStmt | forStmt | breakStmt |
               returnStmt | block;
block       -> "{" declaration "}";
exprStmt    -> expression ";";
printStmt   -> "print" expression ";";
ifStmt      -> "if" "(" expression ")" statement ("else" statement)?;
whileStmt   -> "while" "(" expression ")" statement;
forStmt     -> "for" "(" (varDecl | exprStmt | ";")
               expression? ";"
               expression? ")" statement;
breakStmt   -> "break" ";";
returnStmt  -> "return" expression? ";";
expression  -> separator;
separator   -> assignment "," assignment | assignment;
assignment  -> (call ".")? IDENTIFIER "=" assignment | conditional;
conditional -> logical_or "?" conditional ":" conditional | logical_or;
logical_or  -> logical_and ("or" logical_and)*;
logical_and -> equality ("and" equality)*;
equality    -> comparison (("==" | "!=") comparison)*;
comparison  -> term ((">" | ">=" | "<" | "<=") term)*;
term        -> factor (("+" | "-") factor)*;
factor      -> unary (("*" | "/") unary)*;
unary       -> ("!" | "-") unary | call;
call        -> primary ("(" arguments? ")" | "." IDENTIFIER)*;
arguments   -> expression ("," expression)*;
primary     -> NUMBER | STRING | IDENTIFIER | "true" | "false" | "nil" |
               "(" expression ")";
```

## Representations
| Base class | Subclass   | Production                                    |
| ---------- | --------   | ----------                                    |
| Expr       |            |                                               |
|            | Literal    | primary                                       |
|            | Unary      | unary                                         |
|            | Binary     | factor, term, comparison, equality, separator |
|            | Ternary    | conditional                                   |
|            | Grouping   | primary                                       |
|            | Variable   | primary                                       |
|            | Assign     | assignment                                    |
|            | Logical    | logical\_and, logical\_or                     |
| Stmt       |            |                                               |
|            | Block      | block                                         |
|            | Expression | exprStmt                                      |
|            | Print      | printStmt                                     |
|            | Var        | varDecl                                       |
|            | If         | ifStmt                                        |
|            | While      | whileStmt, forStmt                            |
|            | Break      | breaktStmt                                    |
