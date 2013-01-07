package domain.commands

class CommandRouter {
    def repository
    def eventPublisher

    void route(command) {
        handlerFor(command).handle(command)
    }

    private def handlerFor(command) {
        command.class.classLoader.loadClass("domain.commandhandler.${command.class.name.split('\\.')[-1]}_Handler").newInstance(repository, eventPublisher)
    }
}
