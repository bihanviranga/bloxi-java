# Bloxi grammar

## Grammar productions
```
program     -> declaration* EOF;
declaration -> varDecl | statement;
varDecl     -> "var" IDENTIFIER ("=" expression)? ";";
statement   -> exprStmt | printStmt | block;
block       -> "{" declaration "}";
exprStmt    -> expression ";";
printStmt   -> "print" expression ";";
expression  -> separator;
separator   -> assignment "," assignment | assignment;
assignment  -> IDENTIFIER "=" assignment | conditional;
conditional -> conditional "?" conditional ":" conditional | equality;
equality    -> comparison (("==" | "!=") comparison)*;
comparison  -> term ((">" | ">=" | "<" | "<=") term)*;
term        -> factor (("+" | "-") factor)*;
factor      -> unary (("*" | "/") unary)*;
unary       -> ("!" | "-") unary | primary;
primary     -> NUMBER | STRING | IDENTIFIER | "true" | "false" | "nil" | "(" expression ")";
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
| Stmt       |            |                                               |
|            | Block      | block                                         |
|            | Expression | exprStmt                                      |
|            | Print      | printStmt                                     |
|            | Var        | varDecl                                       |
