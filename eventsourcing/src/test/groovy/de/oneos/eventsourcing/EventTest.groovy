package de.oneos.eventsourcing

import static java.util.UUID.randomUUID

import org.junit.*
import static org.junit.Assert.*
import static org.mockito.Mockito.*
import static org.hamcrest.Matchers.*


class EventTest {

    @Test
    void should_derive_eventName_from_class_name() {
        def event = new Simple_Event_happened()

        assertThat event.eventName, equalTo('Simple Event happened')
    }

    @Test
    void serializableForm_should_convert_null_to_null() {
        assertThat Event.serializableForm(null), nullValue()
    }

    @Test
    void serializableForm_should_convert_a_UUID_to_String() {
        UUID uuid = randomUUID()
        assertThat Event.serializableForm(uuid), equalTo(uuid.toString())
    }

    @Test
    void serializableForm_should_convert_an_Integer_to_Integer() {
        Integer value = 17
        assertThat Event.serializableForm(value), equalTo(value)
    }

    @Test
    void serializableForm_should_convert_a_boolean_to_boolean() {
        boolean value = true
        assertThat Event.serializableForm(value), equalTo(value)
    }

    @Test
    void serializableForm_should_convert_a_byte_to_byte() {
        byte value = Byte.MAX_VALUE
        assertThat Event.serializableForm(value), equalTo(value)
    }

    @Test
    void serializableForm_should_convert_a_short_to_short() {
        short value = Short.MAX_VALUE
        assertThat Event.serializableForm(value), equalTo(value)
    }

    @Test
    void serializableForm_should_convert_an_int_to_int() {
        int value = 17
        assertThat Event.serializableForm(value), equalTo(value)
    }

    @Test
    void serializableForm_should_convert_a_long_to_long() {
        long value = Long.MAX_VALUE
        assertThat Event.serializableForm(value), equalTo(value)
    }

    @Test
    void serializableForm_should_convert_a_float_to_float() {
        float value = Float.MAX_VALUE
        assertThat Event.serializableForm(value), equalTo(value)
    }

    @Test
    void serializableForm_should_convert_a_double_to_double() {
        double value = Double.MAX_VALUE
        assertThat Event.serializableForm(value), equalTo(value)
    }

    @Test
    void serializableForm_should_convert_a_char_to_String() {
        char value = 'a' as char
        assertThat Event.serializableForm(value), equalTo(value)
    }

    @Test
    void serializableForm_should_convert_a_String_to_String() {
        String value = 'Foo bar'
        assertThat Event.serializableForm(value), equalTo(value)
    }

    @Test
    void serializableForm_should_convert_keys_and_values_of_a_map() {
        def mapKey = new SerializableValue()
        def mapValue = new SerializableValue()
        def value = [
            (mapKey): mapValue
        ]
        assertThat Event.serializableForm(value), equalTo([
            (mapKey.serializableForm): mapValue.serializableForm
        ])
    }

    @Test
    void serializableForm_should_return_a_list_of_primitives() {
        def entry1 = 'Entry 1'
        def entry2 = 'Entry 2'
        List value = [ entry1, entry2 ]
        assertThat Event.serializableForm(value), equalTo([ Event.serializableForm(entry1), Event.serializableForm(entry2) ])
    }

    @Test
    void serializableForm_should_convert_the_entries_of_a_list() {
        def entry1 = new SerializableValue()
        def entry2 = new SerializableValue()
        List value = [ entry1, entry2 ]
        assertThat Event.serializableForm(value), equalTo([ Event.serializableForm(entry1), Event.serializableForm(entry2) ])
    }

    @Test
    void serializableForm_should_convert_any_non_primitive_to_its_serializable_form() {
        def value = new SerializableValue()
        assertThat Event.serializableForm(value), equalTo(value.serializableForm)
    }


    static class Simple_Event_happened extends Event {
        void applyTo(Object t) { }
    }

    static class SerializableValue {
        public String getSerializableForm() {
            'serializable form of SerializableValue'
        }
    }

}

