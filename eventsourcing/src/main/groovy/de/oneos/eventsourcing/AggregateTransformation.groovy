package de.oneos.eventsourcing

import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.control.*
import org.codehaus.groovy.transform.*


@GroovyASTTransformation(phase=CompilePhase.SEMANTIC_ANALYSIS)
class AggregateTransformation implements ASTTransformation {
    @Override
    void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
        List<ClassNode> classes = sourceUnit.getAST()?.getClasses()

        classes.findAll { ClassNode clazz ->
            clazz.getAnnotations(new ClassNode(Aggregate))
        }.each { ClassNode clazz ->
            clazz.addMethod(createEmitMethod(clazz))
            clazz.addField(createNewEventsField(clazz))
        }
//        find all methods annotated with @WithLogging
//        classes.findAll { MethodNode method ->
//            method.getAnnotations(new ClassNode(Aggregate))
//        }.each { MethodNode method ->
//            Statement startMessage = createPrintlnAst("Starting $method.name")
//            Statement endMessage = createPrintlnAst("Ending $method.name")
//
//            List existingStatements = method.getCode().getStatements()
//            existingStatements.add(0, startMessage)
//            existingStatements.add(endMessage)
//        }
    }

    FieldNode createNewEventsField(ClassNode parentClass) {
        return new FieldNode(
            'newEvents', 0, new ClassNode(List), parentClass, new ListExpression([])
        )
    }

    MethodNode createEmitMethod(ClassNode parentClass) {
        return new MethodNode(
            'emit', MethodNode.ACC_PUBLIC, new ClassNode(String), [new Parameter(new ClassNode(Event), 'events')] as Parameter[], new ClassNode[0], createReturnAst('emit called')
        )
    }

    private Statement createPrintlnAst(String message) {
        return new ExpressionStatement(
            new MethodCallExpression(
                new VariableExpression("this"),
                new ConstantExpression("println"),
                new ArgumentListExpression(
                    new ConstantExpression(message)
                )
            )
        )
    }

    private Statement createReturnAst(String message) {
        return new ReturnStatement(new ConstantExpression(message))
//            new MethodCallExpression(
//                new VariableExpression("this"),
//                new ConstantExpression("println"),
//                new ArgumentListExpression(
//                    new ConstantExpression(message)
//                )
//            )
    }

}
