package org.moqui.ollama

import io.github.ollama4j.OllamaAPI
import io.github.ollama4j.models.generate.OllamaStreamHandler
import io.github.ollama4j.models.response.OllamaAsyncResultStreamer
import io.github.ollama4j.models.response.OllamaResult
import io.github.ollama4j.types.OllamaModelType
import io.github.ollama4j.utils.OptionsBuilder

import java.util.concurrent.CompletableFuture

class OllamaService {

    static void whoAreYou(){
        // Adjust the host to point to the SSH tunnel if necessary
        String host = "http://localhost:11434/"

        // Initialize the Ollama API client
        OllamaAPI ollamaAPI = new OllamaAPI(host)
        boolean stream = false // or false, depending on your requirements
        OllamaResult result =
                ollamaAPI.generate(OllamaModelType.LLAMA3_1, "Who are you?", stream,new OptionsBuilder().build());

        System.out.println(result.getResponse());
    }

    // this method may caused the request timed out. the ollama service need more time to think about some question.
    static void syncAskQuestion(String question) {
        // Adjust the host to point to the SSH tunnel if necessary
        String host = "http://localhost:11434/"

        // Initialize the Ollama API client
        OllamaAPI ollamaAPI = new OllamaAPI(host)

        // Define a stream handler to process the response stream
        OllamaStreamHandler streamHandler = { String s ->
            println(s)
        }

        // Adjusting the method call to include the missing boolean parameter (e.g., for streaming)
        boolean stream = false // or false, depending on your requirements
        // Make the API request using the Ollama3.1 model
        OllamaResult result = ollamaAPI.generate(OllamaModelType.LLAMA3_1,
                question,stream,
                new OptionsBuilder().build(), streamHandler)

        // Print the full response at the end
        println("Full response: " + result.getResponse())
    }

    static CompletableFuture<String> asyncAskQuestion(String question) {
        return CompletableFuture.supplyAsync(() -> {
            String host = "http://localhost:11434/";
            OllamaAPI ollamaAPI = new OllamaAPI(host);
            ollamaAPI.setRequestTimeoutSeconds(60);
            String prompt = question;

            OllamaAsyncResultStreamer streamer = ollamaAPI.generateAsync(OllamaModelType.LLAMA3_1, prompt, false);
            int pollIntervalMilliseconds = 1000;

            StringBuilder completeResponse = new StringBuilder();

            while (true) {
                try {
                    String tokens = streamer.getStream().poll();
                    if (tokens != null) {
                        completeResponse.append(tokens);
                    }

                    if (!streamer.isAlive()) {
                        break;
                    }

                    Thread.sleep(pollIntervalMilliseconds);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Streaming interrupted", e);
                }
            }

            // Return the complete response as a string
            return completeResponse.toString();
        });
    }

    static void main(String[] args) {
        print ("Full response: " +asyncAskQuestion("List all cricket world cup teams of 2019."))
    }
}
