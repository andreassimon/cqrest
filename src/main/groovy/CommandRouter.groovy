import commands.Lock_out_device
import commands.Lock_out_device_Handler
import commands.Register_new_device_Handler

class CommandRouter {
    def repository
    def eventPublisher

    void route(command) {
        def handler

        if (command instanceof Lock_out_device) {
            handler = new Lock_out_device_Handler(repository, eventPublisher)
        } else {
            handler = new Register_new_device_Handler(repository, eventPublisher)
        }
        handler.handle(command)
    }
}
