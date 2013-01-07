package readmodels.eventhandlers

import org.springframework.jdbc.core.JdbcTemplate

class Device_was_unregistered_Handler implements readmodels.eventhandlers.EventHandler {

    @Override
    String getEventName() {
        'Device was unregistered'
    }

    @Override
    void handleEvent(JdbcTemplate jdbcTemplate, eventName, eventAttributes) {
        jdbcTemplate.update("DELETE FROM DeviceSummary WHERE deviceId = ?;", eventAttributes.deviceId);
    }
}
