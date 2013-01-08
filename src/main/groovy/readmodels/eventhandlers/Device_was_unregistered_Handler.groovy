package readmodels.eventhandlers

import org.springframework.jdbc.core.JdbcTemplate

class Device_was_unregistered_Handler implements readmodels.eventhandlers.EventHandler {

    @Override
    String getEventName() {
        'Device was unregistered'
    }

    @Override
    void handleEvent(JdbcTemplate jdbcTemplate, eventName, eventAttributes) {
        final rowsAffected = jdbcTemplate.update("DELETE FROM DeviceSummary WHERE deviceId = ?;", eventAttributes.deviceId);
        println "Deleted $rowsAffected ${rowsAffected == 1 ? 'row' : 'rows'} from UserSummary"
    }
}
