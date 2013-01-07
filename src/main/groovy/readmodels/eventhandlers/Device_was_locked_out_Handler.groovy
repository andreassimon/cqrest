package readmodels.eventhandlers

import org.springframework.jdbc.core.JdbcTemplate

class Device_was_locked_out_Handler implements readmodels.eventhandlers.EventHandler {

    @Override
    String getEventName() {
        'Device was locked out'
    }

    @Override
    void handleEvent(JdbcTemplate jdbcTemplate, eventName, eventAttributes) {
        jdbcTemplate.update("UPDATE DeviceSummary SET locked = true WHERE deviceid = ?;", eventAttributes.deviceId);
    }
}
