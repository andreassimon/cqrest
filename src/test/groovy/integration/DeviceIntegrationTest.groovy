package integration

import domain.commands.Register_new_device
import org.junit.*
import readmodels.DeviceSummary

import static org.hamcrest.Matchers.equalTo
import static org.junit.Assert.assertThat

class DeviceIntegrationTest extends IntegrationTest {


    @Before
    public void setUp() throws Exception {
        dropTables(jdbcTemplate)
        createTables(jdbcTemplate)
        readModelBuilder.start()
    }

    @After
    public void tearDown() throws Exception {
        dropTables(jdbcTemplate)
        readModelBuilder.interrupt()
    }

    @Test
    void should_build_read_model_from_command() {
        final deviceId = UUID.randomUUID()
        final deviceName = 'new device'
        commandRouter.route new Register_new_device(deviceId: deviceId, deviceName: deviceName)

        Thread.sleep(100)

        final allDeviceSummaries = readModelRepository.getAll(DeviceSummary)
        assertThat allDeviceSummaries, equalTo([new DeviceSummary(deviceId, deviceName)])
    }

}
