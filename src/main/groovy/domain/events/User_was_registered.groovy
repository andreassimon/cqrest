package domain.events

import domain.aggregates.User

class User_was_registered extends UserEvent {
    final def newUserUUID
    final def firstName
    final def lastName
    final def eMail
    final String password

    User_was_registered(newUserUUID, firstName, lastName, eMail, String password) {
        this.newUserUUID = newUserUUID
        this.firstName = firstName
        this.lastName = lastName
        this.eMail = eMail
        this.password = password
    }

    User_was_registered(Map attributes) {
        super(attributes)
    }

    @Override
    UUID getAggregateId() {
        return newUserUUID
    }

    @Override
    User applyTo(User user) {
        user.uuid = newUserUUID
        user.firstName = firstName
        user.lastName = lastName
        user.eMail = eMail
        user.password = password

        return user
    }
}
