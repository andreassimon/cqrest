package de.oneos.eventsourcing


abstract class Event<T> {
    protected static List<String> UNSERIALIZED_PROPERTIES = ['class', 'name']

    abstract void applyTo(T t)

    String getName() {
        this.class.name.split('\\.')[-1].replaceAll('_', ' ')
    }

    Map<String, String> attributes() {
        return properties.findAll({ key, value ->
            ! UNSERIALIZED_PROPERTIES.contains(key)
        }).collectEntries { k, v -> [(k): v.toString()] }
    }

    @Override
    String toString() {
        "${name} ${attributes()}"
    }

    @Override
    boolean equals(Object that) {
        this.toString() == that.toString()
    }
}