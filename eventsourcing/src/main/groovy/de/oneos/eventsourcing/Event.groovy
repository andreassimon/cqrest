package de.oneos.eventsourcing


abstract class Event<T> {
    protected static List<String> UNSERIALIZED_PROPERTIES = ['class', 'eventName']

    abstract void applyTo(T t)

    // Was getName(), but this was very dangerous to conflict with event Attributes to be named 'name'
    String getEventName() {
        this.class.name.split('\\.')[-1].replaceAll('_', ' ')
    }

    Map<String, String> attributes() {
        return properties.findAll({ key, value ->
            ! UNSERIALIZED_PROPERTIES.contains(key)
        }).collectEntries { k, v -> [(k): v.toString()] }
    }

    @Override
    String toString() {
        "${eventName} ${attributes()}"
    }

    @Override
    boolean equals(Object that) {
        this.toString() == that.toString()
    }
}
