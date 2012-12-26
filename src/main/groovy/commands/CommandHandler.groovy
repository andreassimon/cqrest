package commands

abstract class CommandHandler {
    def repository
    def eventPublisher

    CommandHandler(repository, eventPublisher) {
        this.repository = repository
        this.eventPublisher = eventPublisher
    }
}
