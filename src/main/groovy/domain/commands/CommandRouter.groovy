package domain.commands

import infrastructure.persistence.EventStore
import framework.EventPublisher

class CommandRouter {
    EventStore eventStore
    EventPublisher eventPublisher

    void route(command) {
        handlerFor(command).handleInUnitOfWork(command)
    }

    private def handlerFor(command, attempt = 0) {
        final ClassLoader classLoader = command.class.classLoader
        final String commandHandlerClassName = "${command.class.canonicalName.replaceAll('\\.commands\\.', '.commandhandler.')}_Handler"
        final Class commandHandlerClass = classLoader.loadClass(commandHandlerClassName)

        try {
            def commandHandlerInstance = commandHandlerClass.newInstance()
            commandHandlerInstance.eventStore = eventStore
            commandHandlerInstance.eventPublisher = eventPublisher
            return commandHandlerInstance
        } catch (Exception e) {
            println "${e.message} occurred"
            if(attempt < 10) {
                sleep(10)
                return handlerFor(command, attempt + 1)
            }
        }
    }
}
