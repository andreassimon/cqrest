package domain.events

import domain.aggregates.User

abstract class UserEvent extends Event<User> {
    UserEvent() {
        super()
    }

    UserEvent(Map attributes) {
        super(attributes)
    }

    @Override
    String getAggregateClassName() {
        return User.class.canonicalName
    }
}
