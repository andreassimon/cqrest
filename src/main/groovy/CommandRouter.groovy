class CommandRouter {
    def repository
    def eventPublisher

    void route(command) {
        handlerFor(command).handle(command)
    }

    private def handlerFor(command) {
        Class.forName("${command.class.name}_Handler").newInstance(repository, eventPublisher)
    }
}
