package domain.events

abstract class Event<T> {
    abstract T applyTo(T t)

    final Date timestamp

    Event() {
        this.timestamp = new Date()
    }

    Event(Map attributes) {
        this()
        attributes.each {k, v ->
            this[k] = v
        }
    }

    abstract String getAggregateClassName()

    abstract UUID getAggregateId()

    String getName() {
        this.class.name.split('\\.')[-1].replaceAll('_', ' ')
    }

    Map<String, Object> toMap() {
        [(this.name): attributes()]
    }

    Map<String, String> attributes() {
        return properties.findAll({
            key, value -> ! ['aggregateClassName', 'aggregateId', 'class', 'name'].contains(key)  // key != 'class' && key != 'name'
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
