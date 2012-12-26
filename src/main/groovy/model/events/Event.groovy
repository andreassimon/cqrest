package model.events

abstract class Event<T> {
    abstract void applyTo(T)
}
