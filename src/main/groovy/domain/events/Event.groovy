package domain.events

abstract class Event<T> {
    abstract T applyTo(T t)

    String getName() {
        this.class.name.split('\\.')[-1].replaceAll('_', ' ')
    }

    Map<String, Object> toMap() {
        [(this.name): attributes()]
    }

    private Map<String, String> attributes() {
        return properties.findAll({
            key, value -> ! ['class', 'name'].contains(key)  // key != 'class' && key != 'name'
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
