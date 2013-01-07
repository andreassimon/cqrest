package domain.commands

class CommandRouter {
    def repository
    def eventPublisher

    void route(command) {
        handlerFor(command).handle(command)
    }

    private def handlerFor(command) {
        final commandHandlerClassName = "domain.commandhandler.${command.class.name.split('\\.')[-1]}_Handler"
        final commandHandlerClass = command.class.classLoader.loadClass(commandHandlerClassName)
        commandHandlerClass.newInstance(repository, eventPublisher)
    }
}
