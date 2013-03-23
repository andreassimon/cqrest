package de.oneos.eventsourcing


abstract class Event<AT> extends GroovyObjectSupport {
    protected static List<String> UNSERIALIZED_PROPERTIES = ['class', 'eventName', 'serializableForm']

    abstract void applyTo(AT aggregate)

    // Was getName(), but this was very dangerous to conflict with event Attributes to be named 'name'
    String getEventName() {
        this.class.name.split('[.$]')[-1].replaceAll('_', ' ')
    }

    Map<String, ?> getSerializableForm() {
        return properties.findAll { key, value ->
            ! UNSERIALIZED_PROPERTIES.contains(key)
        }.collectEntries { k, v -> [(k): serializableForm(v)] }
    }

    public static def serializableForm(value) {
        if(value == null) { return value }
        switch(value.getClass()) {
            case UUID:
                return value.toString()
            case [Boolean, Byte, Short, Integer, Long, Float, Double, Character, String]:
                return value
            case Map:
                return value.collectEntries { k, v -> [(serializableForm(k)): serializableForm(v)]}
            case List:
                return value.collect { item -> serializableForm(item) }
        }
        return value.serializableForm
    }

    @Override
    String toString() {
        "${eventName} ${serializableForm}"
    }

    @Override
    boolean equals(Object that) {
        this.toString() == that.toString()
    }

    @Override
    void setProperty(String property, Object newValue) {
        def metaProperty = metaClass.getMetaProperty(property)
        if(metaProperty == null) {
            super.setProperty(property, newValue); return
        }
        def getterType = metaProperty.type
        switch (getterType) {
            case newValue.getClass():
                super.setProperty(property, newValue); return
            case boolean.class:
                super.setProperty(property, Boolean.valueOf(newValue)); return
            case byte.class:
                super.setProperty(property, Byte.valueOf(newValue)); return
            case short.class:
                super.setProperty(property, Short.valueOf(newValue)); return
            case int.class:
                super.setProperty(property, Integer.valueOf(newValue)); return
            case long.class:
                super.setProperty(property, Long.valueOf(newValue)); return
            case float.class:
                super.setProperty(property, Float.valueOf(newValue)); return
            case double.class:
                super.setProperty(property, Double.valueOf(newValue)); return
            case char.class:
                super.setProperty(property, Character.valueOf(newValue)); return
            case UUID:
                if(newValue instanceof String) {
                    super.setProperty(property, UUID.fromString(newValue)); return
                }
        }
        if(getterType.metaClass.respondsTo(getterType, 'from', newValue.getClass())) {
            def coercedValue = getterType.from(newValue)
            super.setProperty(property, coercedValue); return
        }
        super.setProperty(property, newValue)
    }

}
