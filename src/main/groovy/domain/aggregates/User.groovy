package domain.aggregates

import domain.commands.Register_new_user
import domain.events.New_user_was_created

class User {
    def uuid
    def firstName
    def lastName
    def eMail

    def handle(Register_new_user command, eventPublisher) {
        eventPublisher.publish(new New_user_was_created(command.newUserUUID, command.firstName, command.lastName, command.eMail))
    }
}
