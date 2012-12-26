import commands.Register_new_device_Handler

class CommandRouter {
    def repository
    def eventPublisher

    void route(command) {
        def handler = new Register_new_device_Handler(repository, eventPublisher)
        handler.handle(command)
    }
}
