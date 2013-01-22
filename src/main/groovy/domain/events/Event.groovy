package domain.events

abstract class Event<T> {
    private static final List<String> UNSERIALIZED_PROPERTIES = ['aggregateClass', 'aggregateClassName', 'aggregateId', 'class', 'name']

    abstract T applyTo(T t)

    final Date timestamp

    Event() {
        this.timestamp = new Date()
    }

    String getAggregateClassName() {
        return aggregateClass.canonicalName
    }

    protected abstract Class<T> getAggregateClass()

    abstract UUID getAggregateId()

    String getName() {
        this.class.name.split('\\.')[-1].replaceAll('_', ' ')
    }

    Map<String, Object> toMap() {
        [(this.name): attributes()]
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
