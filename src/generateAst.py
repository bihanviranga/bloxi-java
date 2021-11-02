# Python script to generate the AST classes for Bloxi

import sys


def main():
    if len(sys.argv) == 1:
        print("Usage: generateAst.py <output_directory>")
        exit(64)
    else:
        outputDir = sys.argv[1]

    baseName = "Expr"
    astTypes = [
        ["Binary", ["Expr left", "Token operator", "Expr right"]],
        ["Grouping", ["Expr expression"]],
        ["Literal", ["Object value"]],
        ["Unary", ["Token operator", "Expr right"]],
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

    for classType in astTypes:
        className = classType[0]
        fields = classType[1]
        classCode = defineType(baseName, className, fields)
        code.extend(classCode)

    code.append("}")

    file = open(path, "w")
    file.writelines("\n".join(code))
    file.close()

def defineType(baseName, className, fields):
    code = []

    code.append(" " * 2 + "static class {0} extends {1} {{".format(className, baseName))
    # constructor
    code.append(" " * 4 + "{0}({1}) {{".format(className, ", ".join(fields)))
    # store params in fields
    for field in fields:
        name = field.split(" ")[1];
        code.append(" " * 6 + "this.{0} = {0};".format(name))
    code.append(" " * 4 + "}")
    code.append("")

    # fields
    for field in fields:
        name = field.split(" ")[1]
        code.append(" " * 4 + "final {0};".format(name))
    code.append(" " * 2 + "}")
    code.append("")

    return code

if __name__ == '__main__':
    main()
