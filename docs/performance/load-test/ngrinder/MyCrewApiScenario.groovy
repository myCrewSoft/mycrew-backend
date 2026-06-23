import static net.grinder.script.Grinder.grinder
import static org.junit.Assert.assertTrue

import groovy.json.JsonSlurper
import net.grinder.plugin.http.HTTPPluginControl
import net.grinder.plugin.http.HTTPRequest
import net.grinder.script.GTest
import net.grinder.scriptengine.groovy.junit.GrinderRunner
import net.grinder.scriptengine.groovy.junit.annotation.BeforeProcess
import net.grinder.scriptengine.groovy.junit.annotation.BeforeThread
import org.junit.Test
import org.junit.runner.RunWith

import HTTPClient.HTTPResponse
import HTTPClient.NVPair

@RunWith(GrinderRunner)
class MyCrewApiScenario {
    public static GTest scenarioTest
    public static HTTPRequest request
    public static String baseUrl
    public static Long empId
    public static String password
    public static int thinkTimeMs

    private String accessToken

    @BeforeProcess
    public static void beforeProcess() {
        HTTPPluginControl.getConnectionDefaults().timeout = 6000
        scenarioTest = new GTest(1, 'MyCrew core API scenario')
        request = new HTTPRequest()
        scenarioTest.record(request)

        Properties props = grinder.getProperties()
        baseUrl = props.getProperty('mycrew.baseUrl', 'http://192.168.35.113')
        empId = props.getProperty('mycrew.empId', '1234') as Long
        password = props.getProperty('mycrew.password', '1234')
        thinkTimeMs = (props.getProperty('mycrew.thinkTimeMs', '1000') as Integer)

        grinder.logger.info("MyCrew target: ${baseUrl}, empId: ${empId}")
    }

    @BeforeThread
    public void beforeThread() {
        grinder.statistics.delayReports = true
        healthCheck()
        accessToken = login()
    }

    @Test
    public void runScenario() {
        try {
            NVPair[] headers = authHeaders()

            get('/api/dashboard/widgets/attendance', headers)
            get('/api/dashboard/widgets/approval', headers)
            get('/api/dashboard/widgets/schedule', headers)
            get('/api/dashboard/widgets/mail', headers)

            get('/api/attendance/today', headers)
            get('/api/attendance/stats?period=WEEK', headers)
            get('/api/attendance/history?days=30', headers)

            get('/api/mails/account/status', headers)
            get('/api/mails/unread-count', headers)
            get('/api/mails?type=inbox&page=0&size=20', headers)

            get('/api/approval/counts', headers)
            get('/api/approval/drafts?page=0&size=10', headers)
            get('/api/approval/requests?page=0&size=10', headers)
        } finally {
            grinder.sleep(thinkTimeMs)
        }
    }

    private void healthCheck() {
        HTTPResponse response = getResponse('/actuator/health', jsonHeaders())
        assertStatus(response, 200, 'health')
    }

    private String login() {
        String body = "{\"empId\":${empId},\"password\":\"${password}\"}"
        HTTPResponse response = postJson('/api/v1/auth/login', body)

        assertStatus(response, 200, 'login')
        def json = new JsonSlurper().parseText(response.getText())
        String token = json?.data?.accessToken

        assertTrue('Login response must contain data.accessToken.', token != null && token.length() > 0)
        return token
    }

    private void get(String path, NVPair[] headers) {
        HTTPResponse response = getResponse(path, headers)
        assertStatus(response, 200, path)
    }

    private HTTPResponse getResponse(String path, NVPair[] headers) {
        String url = "${baseUrl}${path}"
        try {
            return request.GET(url, headers)
        } catch (Throwable t) {
            logRequestException('GET', url, t)
            throw t
        }
    }

    private HTTPResponse postJson(String path, String body) {
        String url = "${baseUrl}${path}"
        try {
            return request.POST(url, body.getBytes('UTF-8'), jsonHeaders())
        } catch (Throwable t) {
            logRequestException('POST', url, t)
            throw t
        }
    }

    private void logRequestException(String method, String url, Throwable t) {
        grinder.logger.error("${method} ${url} failed before HTTP response. ${t.getClass().getName()}: ${t.getMessage()}")
    }

    private NVPair[] jsonHeaders() {
        return [
                new NVPair('Content-Type', 'application/json'),
                new NVPair('Accept', 'application/json')
        ] as NVPair[]
    }

    private NVPair[] authHeaders() {
        return [
                new NVPair('Authorization', "Bearer ${accessToken}"),
                new NVPair('Accept', 'application/json')
        ] as NVPair[]
    }

    private void assertStatus(HTTPResponse response, int expectedStatus, String label) {
        int statusCode = response.statusCode
        if (statusCode != expectedStatus) {
            grinder.logger.error("${label} failed. status=${statusCode}, body=${response.getText()}")
        }
        assertTrue("${label} must return HTTP ${expectedStatus}.", statusCode == expectedStatus)
    }
}
