package de.oneos.eventsourcing

import static java.util.UUID.randomUUID

import org.junit.*
import static org.junit.Assert.*
import static org.hamcrest.Matchers.*

import org.codehaus.groovy.runtime.typehandling.GroovyCastException


class BaseEventTest {

    static boolean DEFAULT_BOOLEAN_VALUE

    @Test
    void should_derive_eventName_from_class_name() {
        def event = new Simple_Event_happened()

        assertThat event.eventName, equalTo('Simple Event happened')
    }

    @Test
    void serializableForm_should_convert_null_to_null() {
        assertThat BaseEvent.serializableForm(null), nullValue()
    }

    @Test
    void serializableForm_should_convert_a_UUID_to_String() {
        UUID uuid = randomUUID()
        assertThat BaseEvent.serializableForm(uuid), equalTo(uuid.toString())
    }

    @Test
    void serializableForm_should_convert_an_Integer_to_Integer() {
        Integer value = 17
        assertThat BaseEvent.serializableForm(value), equalTo(value)
    }

    @Test
    void serializableForm_should_convert_a_boolean_to_boolean() {
        boolean value = true
        assertThat BaseEvent.serializableForm(value), equalTo(value)
    }

    @Test
    void serializableForm_should_convert_a_byte_to_byte() {
        byte value = Byte.MAX_VALUE
        assertThat BaseEvent.serializableForm(value), equalTo(value)
    }

    @Test
    void serializableForm_should_convert_a_short_to_short() {
        short value = Short.MAX_VALUE
        assertThat BaseEvent.serializableForm(value), equalTo(value)
    }

    @Test
    void serializableForm_should_convert_an_int_to_int() {
        int value = 17
        assertThat BaseEvent.serializableForm(value), equalTo(value)
    }

    @Test
    void serializableForm_should_convert_a_long_to_long() {
        long value = Long.MAX_VALUE
        assertThat BaseEvent.serializableForm(value), equalTo(value)
    }

    @Test
    void serializableForm_should_convert_a_float_to_float() {
        float value = Float.MAX_VALUE
        assertThat BaseEvent.serializableForm(value), equalTo(value)
    }

    @Test
    void serializableForm_should_convert_a_double_to_double() {
        double value = Double.MAX_VALUE
        assertThat BaseEvent.serializableForm(value), equalTo(value)
    }

    @Test
    void serializableForm_should_convert_a_char_to_String() {
        char value = 'a' as char
        assertThat BaseEvent.serializableForm(value), equalTo(value)
    }

    @Test
    void serializableForm_should_convert_a_String_to_String() {
        String value = 'Foo bar'
        assertThat BaseEvent.serializableForm(value), equalTo(value)
    }

    @Test
    void serializableForm_should_convert_keys_and_values_of_a_map() {
        def mapKey = new SerializableValue()
        def mapValue = new SerializableValue()
        def value = [
            (mapKey): mapValue
        ]
        assertThat BaseEvent.serializableForm(value), equalTo([
            (mapKey.serializableForm): mapValue.serializableForm
        ])
    }

    @Test
    void serializableForm_should_return_a_list_of_primitives() {
        def entry1 = 'Entry 1'
        def entry2 = 'Entry 2'
        List value = [ entry1, entry2 ]
        assertThat BaseEvent.serializableForm(value), equalTo([ BaseEvent.serializableForm(entry1), BaseEvent.serializableForm(entry2) ])
    }

    @Test
    void serializableForm_should_convert_the_entries_of_a_list() {
        def entry1 = new SerializableValue()
        def entry2 = new SerializableValue()
        List value = [ entry1, entry2 ]
        assertThat BaseEvent.serializableForm(value), equalTo([ BaseEvent.serializableForm(entry1), BaseEvent.serializableForm(entry2) ])
    }

    @Test
    void serializableForm_should_convert_any_non_primitive_to_its_serializable_form() {
        def value = new SerializableValue()
        assertThat BaseEvent.serializableForm(value), equalTo(value.serializableForm)
    }

    @Test(expected=MissingFieldException)
    void should_throw_Exception_when_assigning_undefined_property() {
        def event = new Simple_Event_happened()

        event['undefinedProperty'] = 'Raw String'
    }

    @Test
    void should_assign_raw_value_to_boolean_properties() {
        boolean rawValue = !DEFAULT_BOOLEAN_VALUE
        def event = new Simple_Event_happened()

        event['booleanProperty'] = rawValue

        assertThat event.booleanProperty, equalTo(rawValue)
    }

    @Test
    void should_assign_raw_value_to_boxed_Boolean_properties() {
        def rawValue = new Boolean(!DEFAULT_BOOLEAN_VALUE)
        def event = new Simple_Event_happened()

        event['boxedBooleanProperty'] = rawValue

        assertThat event.boxedBooleanProperty, equalTo(rawValue)
    }

    @Test
    void should_assign_raw_value_to_byte_properties() {
        byte rawValue = Byte.MAX_VALUE
        def event = new Simple_Event_happened()

        event['byteProperty'] = rawValue

        assertThat event.byteProperty, equalTo(rawValue)
    }

    @Test
    void should_assign_raw_value_to_short_properties() {
        short rawValue = Short.MAX_VALUE
        def event = new Simple_Event_happened()

        event['shortProperty'] = rawValue

        assertThat event.shortProperty, equalTo(rawValue)
    }

    @Test
    void should_assign_raw_value_to_int_properties() {
        int rawValue = Integer.MAX_VALUE
        def event = new Simple_Event_happened()

        event['intProperty'] = rawValue

        assertThat event.intProperty, equalTo(rawValue)
    }

    @Test
    void should_assign_raw_value_to_long_properties() {
        long rawValue = Long.MAX_VALUE
        def event = new Simple_Event_happened()

        event['longProperty'] = rawValue

        assertThat event.longProperty, equalTo(rawValue)
    }

    @Test
    void should_assign_raw_value_to_float_properties() {
        float rawValue = Float.MAX_VALUE
        def event = new Simple_Event_happened()

        event['floatProperty'] = rawValue

        assertThat event.floatProperty, equalTo(rawValue)
    }

    @Test
    void should_assign_raw_value_to_double_properties() {
        double rawValue = Double.MAX_VALUE
        def event = new Simple_Event_happened()

        event['doubleProperty'] = rawValue

        assertThat event.doubleProperty, equalTo(rawValue)
    }

    @Test
    void should_assign_raw_value_to_char_properties() {
        char rawValue = 'a' as char
        def event = new Simple_Event_happened()

        event['charProperty'] = rawValue

        assertThat event.charProperty, equalTo(rawValue)
    }

    @Test
    void should_assign_raw_value_to_String_properties() {
        def event = new Simple_Event_happened()

        event['stringProperty'] = 'Raw String'

        assertThat event.stringProperty, equalTo('Raw String')
    }

    @Test
    void should_convert_Strings_assigned_to_UUID_properties() {
        def aUUID = UUID.fromString('9176e966-f9c2-4ca8-89e4-fa888ac73aaf')
        def event = new Simple_Event_happened()

        event['uuidProperty'] = '9176e966-f9c2-4ca8-89e4-fa888ac73aaf'

        assertThat event.uuidProperty, equalTo(aUUID)
    }

    @Test
    void should_use_type_factories_for_assigning_value_object_properties() {
        def valueObject = new SerializableValue()
        def event = new Simple_Event_happened()

        event['valueObjectProperty'] = valueObject.serializableForm

        assertThat event.valueObjectProperty, equalTo(valueObject)
    }

    @Test(expected=GroovyCastException)
    void should_throw_an_exception_for_values_without_type_factory() {
        def event = new Simple_Event_happened()

        event['unserializableObjectProperty'] = 'some serialized value'
    }

    @Test
    void should_call_custom_setters() {
        def event = new Simple_Event_happened()

        event['customProperty'] = 'some serialized value'

        assertThat event.customProperty, equalTo(new UnserializableValue())
    }


    static class Simple_Event_happened extends BaseEvent {
        boolean booleanProperty
        Boolean boxedBooleanProperty
        byte byteProperty
        short shortProperty
        int intProperty
        long longProperty
        float floatProperty
        double doubleProperty
        char charProperty

        String stringProperty
        UUID uuidProperty

        SerializableValue valueObjectProperty
        UnserializableValue unserializableObjectProperty

        void setCustomProperty(String customProperty) {
            // handle deserialization manually
            this.customProperty = new UnserializableValue()
        }

        UnserializableValue customProperty

        void applyTo(Object t) { }
    }

    static class SerializableValue {
        protected static final String SERIALIZED_FORM = 'serializable form of SerializableValue'

        public String getSerializableForm() {
            SERIALIZED_FORM
        }

        public static SerializableValue from(String serializedForm) {
            if(serializedForm != SERIALIZED_FORM) {
                throw new IllegalArgumentException("Expected '$serializedForm' to be '$SERIALIZED_FORM'")
            }
            return new SerializableValue()
        }

        String toString() { this.getClass().simpleName }

        boolean equals(Object that) {
            this.getClass().isInstance(that)
        }
    }


    static class UnserializableValue {
        boolean equals(Object that) {
            this.getClass().isInstance(that)
        }
    }

}
