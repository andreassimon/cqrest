package de.oneos.eventsourcing

import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.control.*
import org.codehaus.groovy.transform.*

/**
 * TODO Be careful! This is not tested extensively yet!
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
            aggregate.addField(_newEvents(aggregate))
            aggregate.addMethod(getterFor(_newEvents(aggregate)))
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

    FieldNode _newEvents(ClassNode parentClass) {
        def field = new FieldNode(
            '_newEvents', 0, new ClassNode(List), parentClass, new ListExpression([])
        )
        field.closureSharedVariable = true
        return field
    }

    MethodNode emitEvents(ClassNode parentClass) {
        def methodScope = new VariableScope()
        methodScope.putReferencedClassVariable(_newEvents(parentClass))
        final BlockStatement block = new BlockStatement(
            [
                callMethod(field(_newEvents(parentClass)), 'add', event()),
                callMethod(event(), 'applyTo', thiz())
            ],
            methodScope.copy()
        )
        return new MethodNode(
            'emit',
            MethodNode.ACC_PUBLIC,
            parentClass.getPlainNodeReference(),
            [eventsParameter()] as Parameter[],
            NO_EXCEPTIONS,
            new BlockStatement(
                [
                    callMethod(events(), 'each', wrapInClosure(block, methodScope.copy(), parentClass)),
                    returnResult(thiz())
                ],
                methodScope
            )
        )
    }

    protected Parameter eventsParameter() {
        new Parameter(new ClassNode(Event).makeArray(), 'events')
    }

    MethodNode flushEvents(ClassNode parentClass) {
        return new MethodNode(
            'flushEvents',
            MethodNode.ACC_PUBLIC,
            ClassHelper.VOID_TYPE,
            new Parameter[0],
            NO_EXCEPTIONS,
            callMethod(field(_newEvents(parentClass)), 'clear', new ArgumentListExpression())
        )
    }

    protected ClassNode returnsVoid() {
        new ClassNode(Void)
    }

    protected event() {
        new VariableExpression('event')
    }

    protected Expression field(FieldNode fieldNode) {
        def field = new FieldExpression(fieldNode)
        field.useReferenceDirectly = true
        return field
    }

    protected events() {
        new VariableExpression('events')
    }

    private ClosureExpression wrapInClosure(BlockStatement block, VariableScope methodScope, ClassNode clazz) {
        def closure = new ClosureExpression(
            [new Parameter(new ClassNode(Event), 'event')] as Parameter[],
            block
        )
        closure.setType(ClassHelper.VOID_TYPE)
        closure.variableScope = methodScope.copy()
        closure.variableScope.putReferencedClassVariable(_newEvents(clazz))
        closure
    }


    private ExpressionStatement callMethod(Expression target, String method, Expression argument) {
        def statement = new ExpressionStatement(
            new MethodCallExpression(
                target,
                method,
                argument
            )
        )
        return statement
    }

    private Statement returnResult(Expression result) {
        return new ReturnStatement(result)
    }

    protected Expression thiz() {
        new VariableExpression('this')
    }

}
