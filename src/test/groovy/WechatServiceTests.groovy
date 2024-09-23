import org.moqui.context.ExecutionContext
import org.moqui.Moqui
import org.moqui.ollama.OllamaService
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

    def "Try asking a question about the model with WechatService"(){
        given:

        when:
        // Test the Ollama service call through the OllamaAPIService
        OllamaService.askAQuestionAboutTheModel() // Adjust as necessary for your method

        then:
        noExceptionThrown() // Basic check that it runs without errors
        // Add assertions here based on the expected behavior of your API service
    }

    def "Try asking a question, receiving the answer streamed with WechatService"() {
        given:

        when:
        // Test the Ollama service call through the OllamaAPIService
        OllamaService.askAQuestionReceivingTheAnswerStreamed("where is the capital of France?") // Adjust as necessary for your method

        then:
        noExceptionThrown() // Basic check that it runs without errors
        // Add assertions here based on the expected behavior of your API service
        response.contains("Paris") // Adjust this assertion based on expected output
    }


    def "Try asking a question from general topics with WechatService"(){
        given:

        when:
        // Test the Ollama service call through the OllamaAPIService
        OllamaService.askingAQuestionFromGeneralTopics("List all cricket world cup teams of 2019.") // Adjust as necessary for your method

        then:
        noExceptionThrown() // Basic check that it runs without errors
        // Add assertions here based on the expected behavior of your API service
    }

    def "Try asking for a Database query for your data schema with WechatService"(){
        given:

        when:
        // Test the Ollama service call through the OllamaAPIService
        OllamaService.askingForADatabaseQueryForYourDataSchema() // Adjust as necessary for your method

        then:
        noExceptionThrown() // Basic check that it runs without errors
        // Add assertions here based on the expected behavior of your API service
    }
}
