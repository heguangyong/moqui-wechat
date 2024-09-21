import org.moqui.context.ExecutionContext
import org.moqui.Moqui
import org.moqui.ollama.OllamaAPIService
import spock.lang.Shared
import spock.lang.Specification

class WechatServiceTests extends Specification {
    @Shared
    ExecutionContext ec

    def setupSpec() {
        // Initialize Moqui framework, get the execution context (ec)
        ec = Moqui.getExecutionContext()
    }

    def cleanupSpec() {
        ec.destroy()
    }

    def setup() {
        ec.user.loginUser("john.doe", "moqui")
        ec.artifactExecution.disableAuthz()
        ec.transaction.begin(null)
    }

    def cleanup() {
        ec.transaction.commit()
        ec.artifactExecution.enableAuthz()
        ec.user.logoutUser()
    }

    def "Try asking a question, receiving the answer streamed with WechatService"() {
        given:

        when:
        // Test the Ollama service call through the OllamaAPIService
        OllamaAPIService.askQuestion("What is the capital of France? And what's France's connection with Mona Lisa?") // Adjust as necessary for your method

        then:
        noExceptionThrown() // Basic check that it runs without errors
        // Add assertions here based on the expected behavior of your API service
    }
}
