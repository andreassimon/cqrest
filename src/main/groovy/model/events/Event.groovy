package model.events

abstract class Event<T> {
    abstract T applyTo(T)
}
