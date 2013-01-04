package domain.events

import domain.aggregates.User

class New_user_was_created extends Event<User> {
    final def newUserUUID
    final def firstName
    final def lastName
    final def eMail

    New_user_was_created(newUserUUID, firstName, lastName, eMail) {
        this.eMail = eMail
        this.lastName = lastName
        this.firstName = firstName
        this.newUserUUID = newUserUUID
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
