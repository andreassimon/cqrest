package de.oneos.eventsourcing

import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.control.*
import org.codehaus.groovy.syntax.*
import org.codehaus.groovy.transform.*


/**
 * TODO Be careful! This is not tested extensively yet!
 */
@GroovyASTTransformation(phase=CompilePhase.SEMANTIC_ANALYSIS)
class AggregateTransformation implements ASTTransformation {

    @Override
    void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
        List<ClassNode> classes = sourceUnit.getAST()?.getClasses()

        classes.findAll { ClassNode clazz ->
            clazz.getAnnotations(new ClassNode(Aggregate))
        }.each { ClassNode aggregate ->
            if(aggregate.getField('_version') == null) {
                aggregate.addField(_version(aggregate))
            }
            if(aggregate.getField('_newEvents') == null) {
                aggregate.addField(_newEvents(aggregate))
            }
            if(aggregate.getMethods('getVersion').empty) {
                aggregate.addMethod(versionGetter(aggregate))
            }
            if(aggregate.getMethods('setVersion').empty) {
                aggregate.addMethod(versionSetter(aggregate))
            }
            if(aggregate.getMethods('getNewEvents').empty) {
                aggregate.addMethod(newEventsGetter(aggregate))
            }
            if(aggregate.getMethods('emit').empty) {
                aggregate.addMethod(emitEvents(aggregate))
            }
            if(aggregate.getMethods('flushEvents').empty) {
                aggregate.addMethod(flushEvents(aggregate))
            }
        }
    }

    protected MethodNode newEventsGetter(ClassNode clazz) {
        return new MethodNode(
            'getNewEvents', MethodNode.ACC_PUBLIC, _newEvents(clazz).type, noParameters(), noExceptions(),
            returnResult( field(_newEvents(clazz)) )
        )
    }

    protected MethodNode versionGetter(ClassNode clazz) {
        return new MethodNode(
            'getVersion', MethodNode.ACC_PUBLIC, _version(clazz).type, noParameters(), noExceptions(),
            returnResult( field(_version(clazz)) )
        )
    }

    protected MethodNode versionSetter(ClassNode clazz) {
        new MethodNode(
            'setVersion', MethodNode.ACC_PUBLIC, ClassHelper.VOID_TYPE, [new Parameter(_version(clazz).type, 'version')] as Parameter[], noExceptions(),
            new ExpressionStatement(
                new BinaryExpression(
                    new VariableExpression('_version'),
                    Token.newSymbol('=', -1, -1),
                    new VariableExpression('version')
                )
            )
        )
    }

    protected Parameter[] noParameters() { new Parameter[0] }
    protected ClassNode[] noExceptions() { new ClassNode[0] }


    FieldNode _newEvents(ClassNode parentClass) {
        return new FieldNode(
            '_newEvents', FieldNode.ACC_PROTECTED, new ClassNode(List), parentClass, new ListExpression([])
        )
    }

    FieldNode _version(ClassNode parentClass) {
        return new FieldNode(
            '_version', FieldNode.ACC_PROTECTED, ClassHelper.int_TYPE, parentClass, new ConstantExpression(-1)
        )
    }

    MethodNode emitEvents(ClassNode parentClass) {
        VariableScope methodScope = new VariableScope()
        final BlockStatement block = new BlockStatement(
            [
                callMethod(field(_newEvents(parentClass)), 'add', event()),
                callMethod(event(), 'applyTo', thiz())
            ],
            new VariableScope(methodScope)
        )
        def method = new MethodNode(
            'emit',
            MethodNode.ACC_PUBLIC,
            parentClass.getPlainNodeReference(),
            [eventsParameter()] as Parameter[],
            noExceptions(),
            new BlockStatement(
                [
                    callMethod(events(), 'each', wrapInClosure(block, new VariableScope(methodScope), parentClass)),
                    returnResult(thiz())
                ],
                methodScope
            )
        )
        method.setVariableScope(methodScope)
        return method
    }

    protected Parameter eventsParameter() {
        new Parameter(new ClassNode(Event).makeArray(), 'events')
    }

    MethodNode flushEvents(ClassNode parentClass) {
        return new MethodNode(
            'flushEvents',
            MethodNode.ACC_PUBLIC,
            ClassHelper.VOID_TYPE,
            noParameters(),
            noExceptions(),
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

    private ClosureExpression wrapInClosure(BlockStatement block, VariableScope scope, ClassNode clazz) {
        def closure = new ClosureExpression(
            [new Parameter(new ClassNode(Event), 'event')] as Parameter[],
            block
        )
        closure.setType(ClassHelper.VOID_TYPE)
        closure.variableScope = scope
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
