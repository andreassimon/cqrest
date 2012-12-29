package domain.events

abstract class Event<T> {
    abstract T applyTo(T)

    String getName() {
        this.class.name.split('\\.')[-1].replaceAll('_', ' ')
    }

    Map<String, Object> toMap() {
        [(this.name): attributes()]
    }

    private Map<String, String> attributes() {
        properties.findAll { k, _ -> ! ['class', 'name'].contains(k) }.collectEntries { k, v -> [(k): v.toString()] }
    }

}
