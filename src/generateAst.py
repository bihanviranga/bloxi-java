# Python script to generate the AST classes for Bloxi

import sys


def main():
    if len(sys.argv) == 1:
        print("Usage: generateAst.py <output_directory>")
        exit(64)
    else:
        outputDir = sys.argv[1]

    # defining expression classes
    baseName = "Expr"
    astTypes = [
        ["Literal", ["Object value"]],
        ["Unary", ["Token operator", "Expr right"]],
        ["Binary", ["Expr left", "Token operator", "Expr right"]],
        ["Ternary", ["Expr condition", "Expr trueExpr", "Expr falseExpr"]],
        ["Grouping", ["Expr expression"]],
        ["Variable", ["Token name"]],
        ["Assign", ["Token name", "Expr value"]],
        ["Logical", ["Expr left", "Token operator", "Expr right"]],
    ]
    defineAst(outputDir, baseName, astTypes)

    # defining statement classes
    baseName = "Stmt"
    astTypes = [
        ["Block", ["List<Stmt> statements"]],
        ["Expression", ["Expr expression"]],
        ["Print", ["Expr expression"]],
        ["Var", ["Token name", "Expr initializer"]],
        ["If", ["Expr condition", "Stmt thenBranch", "Stmt elseBranch"]],
    ]
    defineAst(outputDir, baseName, astTypes)


def defineAst(outputDir, baseName, astTypes):
    path = f"{outputDir}/{baseName}.java"

    code = []
    code.append("package com.bloxi.lox;")
    code.append("")
    code.append("import java.util.List;")
    code.append("")
    code.append("abstract class {0} {{".format(baseName))

    # define the visitor interface
    visitorCode = defineVisitor(baseName, astTypes)
    code.extend(visitorCode)
    code.append("")

    # base accept() method
    code.append(" " * 2 + "abstract <R> R accept(Visitor<R> visitor);")

    # define the classes
    for classType in astTypes:
        className = classType[0]
        fields = classType[1]
        classCode = defineType(baseName, className, fields)
        code.append("")
        code.extend(classCode)

    code.append("}")  # end abstract class

    file = open(path, "w")
    file.writelines("\n".join(code))
    file.close()


def defineVisitor(baseName, types):
    code = []

    code.append(" " * 2 + "interface Visitor<R> {")
    for classType in types:
        typeName = classType[0]
        code.append(
            " " * 4 + "R visit{0}{1} ({0} {2});".
            format(typeName, baseName, baseName.lower())
        )
    code.append(" " * 2 + "}")

    return code


def defineType(baseName, className, fields):
    code = []

    code.append(
        " " * 2 + "static class {0} extends {1} {{".format(className, baseName)
    )

    # fields
    for field in fields:
        code.append(" " * 4 + "final {0};".format(field))
    code.append("")

    # constructor
    code.append(" " * 4 + "{0}({1}) {{".format(className, ", ".join(fields)))
    # store params in fields
    for field in fields:
        name = field.split(" ")[1]
        code.append(" " * 6 + "this.{0} = {0};".format(name))
    code.append(" " * 4 + "}")
    code.append("")

    # visitor pattern
    code.append(" " * 4 + "@Override")
    code.append(" " * 4 + "<R> R accept(Visitor<R> visitor) {")
    code.append(" " * 6 +
                "return visitor.visit{0}{1}(this);".
                format(className, baseName)
                )
    code.append(" " * 4 + "}")

    code.append(" " * 2 + "}")

    return code


if __name__ == '__main__':
    main()
