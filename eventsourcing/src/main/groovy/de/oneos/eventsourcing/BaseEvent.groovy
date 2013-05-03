package de.oneos.eventsourcing


abstract class BaseEvent<AT> extends GroovyObjectSupport implements Event<AT> {
    abstract void applyTo(AT aggregate)

    // Was getName(), but this was very dangerous to conflict with event Attributes to be named 'name'
    String getEventName() {
        this.class.name.split('[.$]')[-1].replaceAll('_', ' ')
    }

    Map<String, ?> getSerializableForm() {
        Map<String, ?> result = [:]
        for(p in serializedProperties()) {
            result[p.key] = serializableForm(p.value)
        }
        return result
    }

    Map<String, ?> serializedProperties() {
        Map<String, ?> serializedProps = [:]
        for(k in properties.keySet()) {
            if(!UNSERIALIZED_PROPERTIES().contains(k)) {
                serializedProps[k] = this[k]
            }
        }
        return serializedProps
    }

    protected List<String> UNSERIALIZED_PROPERTIES() { ['class', 'eventName', 'serializableForm'].asImmutable() }

    public static def serializableForm(value) {
        if(value == null) { return value }
        switch(value.getClass()) {
            case UUID.class:
                return value.toString()
            case [Boolean.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Character.class, String.class]:
                return value
            case Map.class:
                return value.collectEntries { k, v -> [(serializableForm(k)): serializableForm(v)]}
            case List.class:
                return value.collect { item -> serializableForm(item) }
        }
        return value.serializableForm
    }

    @Override
    String toString() {
        serializableForm.inject("$eventName [") { String readableForm, property, value ->
            readableForm + "\n    $property: '$value'"
        } + "\n]"
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
