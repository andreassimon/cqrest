package domain.events

abstract class Event<T> {
    abstract T applyTo(T)
}
