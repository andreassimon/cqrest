package domain.events

import domain.aggregates.User

class New_user_was_created extends UserEvent {
    final def newUserUUID
    final def firstName
    final def lastName
    final def eMail

    New_user_was_created(newUserUUID, firstName, lastName, eMail) {
        this.newUserUUID = newUserUUID
        this.firstName = firstName
        this.lastName = lastName
        this.eMail = eMail
    }

    New_user_was_created(Map attributes) {
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

        return user
    }
}
