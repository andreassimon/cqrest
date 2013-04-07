package de.oneos.eventsourcing

import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.control.*
import org.codehaus.groovy.transform.*


/**
 * Be careful! This is not tested extensively yet!
 */
@GroovyASTTransformation(phase=CompilePhase.SEMANTIC_ANALYSIS)
class AggregateTransformation implements ASTTransformation {
    public static final ClassNode[] NO_EXCEPTIONS = new ClassNode[0]

    @Override
    void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
        List<ClassNode> classes = sourceUnit.getAST()?.getClasses()

        classes.findAll { ClassNode clazz ->
            clazz.getAnnotations(new ClassNode(Aggregate))
        }.each { ClassNode aggregate ->
            aggregate.addField(newEvents(aggregate))
            aggregate.addMethod(getterFor(newEvents(aggregate)))
            aggregate.addMethod(emitEvents(aggregate))
            aggregate.addMethod(flushEvents(aggregate))
        }
    }

    MethodNode getterFor(FieldNode fieldNode) {
        return new MethodNode(
            'getNewEvents', MethodNode.ACC_PUBLIC, fieldNode.type, new Parameter[0], NO_EXCEPTIONS,
            returnResult( field(fieldNode) )
        )
    }

    FieldNode newEvents(ClassNode parentClass) {
        return new FieldNode(
            'newEvents', 0, new ClassNode(List), parentClass, new ListExpression([])
        )
    }

    MethodNode emitEvents(ClassNode parentClass) {
        return new MethodNode(
            'emit',
            MethodNode.ACC_PUBLIC,
            parentClass,
            [new Parameter(new ClassNode(Event[]), 'events')] as Parameter[],
            NO_EXCEPTIONS,
            new BlockStatement([
                iterate(events(),
                    new BlockStatement([
                        addToCollection(field(newEvents(parentClass)), event()),
                        callMethod(event(), 'applyTo', thiz())
                    ], new VariableScope()
                    )
                ),
                returnResult(thiz())
            ], new VariableScope())
        )
    }

    MethodNode flushEvents(ClassNode parentClass) {
        return new MethodNode(
            'flushEvents',
            MethodNode.ACC_PUBLIC,
            returnsVoid(),
            new Parameter[0],
            NO_EXCEPTIONS,
            callMethod(field(newEvents(parentClass)), 'clear', new EmptyExpression())
        )
    }

    protected returnsVoid() {
        new ClassNode(Void)
    }

    protected event() {
        new VariableExpression('event')
    }

    protected Expression field(FieldNode fieldNode) {
        new FieldExpression(fieldNode)
    }

    protected events() {
        new VariableExpression('events')
    }

    Statement iterate(Expression collection, Statement statement) {
        return new ExpressionStatement(
            new MethodCallExpression(
                collection,
                new ConstantExpression('each'),
                new ClosureExpression(
                    [new Parameter(new ClassNode(Event), 'event')] as Parameter[],
                    statement
                )
            )
        )
    }

    private Statement addToCollection(Expression collection, Expression newItem) {
        return callMethod(collection, 'add', newItem)
    }

    private Statement callMethod(Expression collection, String method, Expression argument) {
        return new ExpressionStatement(
            new MethodCallExpression(
                collection,
                new ConstantExpression(method),
                argument
            )
        )
    }

    private Statement returnResult(Expression result) {
        return new ReturnStatement(result)
    }

    protected Expression thiz() {
        new VariableExpression('this')
    }

}
