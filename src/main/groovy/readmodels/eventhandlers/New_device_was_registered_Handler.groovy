package readmodels.eventhandlers

import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.JdbcTemplate

class New_device_was_registered_Handler implements readmodels.eventhandlers.EventHandler {

    @Override
    String getEventName() {
        'New device was registered'
    }

    @Override
    void handleEvent(JdbcTemplate jdbcTemplate, eventName, eventAttributes) {
        try {
            final rowsAffected = jdbcTemplate.update("INSERT INTO DeviceSummary (deviceId, deviceName) VALUES (?, ?);", UUID.fromString(eventAttributes.deviceId), eventAttributes.deviceName);
            println "Added $rowsAffected ${rowsAffected == 1 ? 'row' : 'rows'} to DeviceSummary"
        } catch (DataAccessException ex) {
            println "Error executing insert to DeviceSummary"
            ex.printStackTrace()
        }
    }

}
