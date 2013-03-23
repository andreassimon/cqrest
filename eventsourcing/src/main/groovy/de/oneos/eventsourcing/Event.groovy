package de.oneos.eventsourcing


abstract class Event<T> {
    protected static List<String> UNSERIALIZED_PROPERTIES = ['class', 'eventName', 'serializableForm']

    abstract void applyTo(T t)

    // Was getName(), but this was very dangerous to conflict with event Attributes to be named 'name'
    String getEventName() {
        this.class.name.split('[.$]')[-1].replaceAll('_', ' ')
    }

    Map<String, ?> getSerializableForm() {
        return properties.findAll({ key, value ->
            ! UNSERIALIZED_PROPERTIES.contains(key)
        }).collectEntries { k, v -> [(k): v.toString()] }
    }

    public static def serializableForm(value) {
//        if(value == null) { return value }
        switch(value.getClass()) {
            case UUID:
                return value.toString()
            case [Boolean, Byte, Short, Integer, Long, Float, Double, Character, String]:
                return value
        }
//        return value.serializableForm
    }

    @Override
    String toString() {
        "${eventName} ${serializableForm}"
    }

    @Override
    boolean equals(Object that) {
        this.toString() == that.toString()
    }
}
