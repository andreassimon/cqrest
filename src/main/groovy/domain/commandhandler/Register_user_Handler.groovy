package domain.commandhandler

import domain.aggregates.User

class Register_user_Handler {
    private final Object repository
    private final Object eventPublisher

    Register_user_Handler(repository, eventPublisher) {
        this.eventPublisher = eventPublisher
        this.repository = repository
    }

    def handle(command) {
        def user = new User()
        user.handle(command, eventPublisher)
    }
}
