package readmodels

class DeviceSummary {
    UUID deviceId
    String deviceName

    @Override
    String toString() {
        "DeviceSummary{deviceId=${deviceId}, deviceName=${deviceName}}"
    }

    DeviceSummary() { }

    void setDeviceid(deviceId) {
        this.deviceId = deviceId
    }

    DeviceSummary(UUID deviceId, String deviceName) {
        this.deviceId = deviceId
        this.deviceName = deviceName
    }

    @Override
    boolean equals(Object that) {
        this.deviceId == that.deviceId &&
            this.deviceName == that.deviceName
    }

    void setDevicename(deviceName) {
        this.deviceName = deviceName
    }
}
