package domain.aggregates

import domain.commands.Register_user
import domain.events.User_was_registered

class User {
    def uuid
    def firstName
    def lastName
    def eMail
    def password

    def handle(Register_user command, eventPublisher) {
        eventPublisher.publish(new User_was_registered(
            command.newUserUUID,
            command.firstName,
            command.lastName,
            command.eMail,
            command.password
        ))
    }
}
