package domain.commands

class CommandRouter {
    def repository
    def eventPublisher

    void route(command) {
        handlerFor(command).handle(command)
    }

    private def handlerFor(command, attempt = 0) {
        final ClassLoader classLoader = command.class.classLoader
        final String commandHandlerClassName = "domain.commandhandler.${command.class.simpleName}_Handler"
        final Class commandHandlerClass = classLoader.loadClass(commandHandlerClassName)

        try {
            return commandHandlerClass.newInstance(repository, eventPublisher)
        } catch (Exception e) {
            println "${e.message} occurred"
            if(attempt < 10) {
                sleep(10)
                return handlerFor(command, attempt + 1)
            }
        }
    }
}
