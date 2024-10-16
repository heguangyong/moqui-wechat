package org.moqui.ollama

import io.github.ollama4j.OllamaAPI
import io.github.ollama4j.exceptions.OllamaBaseException
import io.github.ollama4j.exceptions.ToolInvocationException
import io.github.ollama4j.impl.ConsoleOutputStreamHandler
import io.github.ollama4j.models.chat.OllamaChatMessage
import io.github.ollama4j.models.chat.OllamaChatMessageRole
import io.github.ollama4j.models.chat.OllamaChatRequest
import io.github.ollama4j.models.chat.OllamaChatRequestBuilder
import io.github.ollama4j.models.chat.OllamaChatResult
import io.github.ollama4j.models.generate.OllamaStreamHandler
import io.github.ollama4j.models.response.OllamaAsyncResultStreamer
import io.github.ollama4j.models.response.OllamaResult
import io.github.ollama4j.tools.OllamaToolsResult
import io.github.ollama4j.tools.Tools
import io.github.ollama4j.types.OllamaModelType
import io.github.ollama4j.utils.OptionsBuilder
import io.github.ollama4j.utils.SamplePrompts

import java.util.concurrent.CompletableFuture

class OllamaService {

    static void whoAreYou(){
        // Adjust the host to point to the SSH tunnel if necessary
        String host = "http://localhost:11434/"

        // Initialize the Ollama API client
        OllamaAPI ollamaAPI = new OllamaAPI(host)
        ollamaAPI.setRequestTimeoutSeconds(60);
        boolean stream = false // or false, depending on your requirements
        OllamaResult result =
                ollamaAPI.generate(OllamaModelType.LLAMA3_1, "Who are you?", stream,new OptionsBuilder().build());

        System.out.println(result.getResponse());
    }

    static void chat(){
        String host = "http://localhost:11434/";

        OllamaAPI ollamaAPI = new OllamaAPI(host);
        ollamaAPI.setRequestTimeoutSeconds(60);
        OllamaChatRequestBuilder builder = OllamaChatRequestBuilder.getInstance(OllamaModelType.LLAMA3_1);

        // create first user question
        OllamaChatRequest requestModel = builder.withMessage(OllamaChatMessageRole.USER, "What is the capital of France?")
                .build();

        // start conversation with model
        OllamaChatResult chatResult = ollamaAPI.chat(requestModel);

        System.out.println("First answer: " + chatResult.getResponse());

        // create next userQuestion
        requestModel = builder.withMessages(chatResult.getChatHistory()).withMessage(OllamaChatMessageRole.USER, "And what is the second largest city?").build();

        // "continue" conversation with model
        chatResult = ollamaAPI.chat(requestModel);

        System.out.println("Second answer: " + chatResult.getResponse());

        System.out.println("Chat History: " + chatResult.getChatHistory());
    }

    static void chatStreamed(){
        String host = "http://localhost:11434/";

        OllamaAPI ollamaAPI = new OllamaAPI(host);
        ollamaAPI.setRequestTimeoutSeconds(60);
        OllamaChatRequestBuilder builder = OllamaChatRequestBuilder.getInstance(OllamaModelType.LLAMA3_1);
        OllamaChatRequest requestModel = builder.withMessage(OllamaChatMessageRole.USER,
                "What is the capital of France? And what's France's connection with Mona Lisa?")
                .build();

        // define a handler (Consumer<String>)
        OllamaStreamHandler streamHandler = (s) -> {
            System.out.println(s);
        };

        OllamaChatResult chatResult = ollamaAPI.chat(requestModel, streamHandler);
    }

    static void chatConsoleStreamed(){
        String host = "http://localhost:11434/";
        OllamaAPI ollamaAPI = new OllamaAPI(host);
        ollamaAPI.setRequestTimeoutSeconds(60);
        OllamaChatRequestBuilder builder = OllamaChatRequestBuilder.getInstance(OllamaModelType.LLAMA3_1);
        OllamaChatRequest requestModel = builder.withMessage(OllamaChatMessageRole.USER, "List all cricket world cup teams of 2019. Name the teams!")
                .build();
        OllamaStreamHandler streamHandler = new ConsoleOutputStreamHandler();
        ollamaAPI.chat(requestModel, streamHandler);
    }

    static void chatWithPrompt(){
        String host = "http://localhost:11434/";

        OllamaAPI ollamaAPI = new OllamaAPI(host);
        ollamaAPI.setRequestTimeoutSeconds(60);
        OllamaChatRequestBuilder builder = OllamaChatRequestBuilder.getInstance(OllamaModelType.LLAMA3_1);

        // create request with system-prompt (overriding the model defaults) and user question
        OllamaChatRequest requestModel = builder.withMessage(OllamaChatMessageRole.SYSTEM, "You are a silent bot that only says 'NI'. Do not say anything else under any circumstances!")
                .withMessage(OllamaChatMessageRole.USER, "What is the capital of France? And what's France's connection with Mona Lisa?")
                .build();

        // start conversation with model
        OllamaChatResult chatResult = ollamaAPI.chat(requestModel);

        System.out.println(chatResult.getResponse());
    }

    static void chatWithImage(){
        String host = "http://localhost:11434/";

        OllamaAPI ollamaAPI = new OllamaAPI(host);
        ollamaAPI.setRequestTimeoutSeconds(180);
        OllamaChatRequestBuilder builder = OllamaChatRequestBuilder.getInstance(OllamaModelType.LLAVA);

        // Load Image from File and attach to user message (alternatively images could also be added via URL)
        OllamaChatRequest requestModel =
                builder.withMessage(OllamaChatMessageRole.USER, "What's in the picture?",
                        List.of(
                                new File("/Users/demo/Workspace/moqui/runtime/component/moqui-wechat/src/main/resources/dog-on-a-boat.jpg"))).build();

        OllamaChatResult chatResult = ollamaAPI.chat(requestModel);
        System.out.println("First answer: " + chatResult.getResponse());

        builder.reset();

        // Use history to ask further questions about the image or assistant answer
        requestModel =
                builder.withMessages(chatResult.getChatHistory())
                        .withMessage(OllamaChatMessageRole.USER, "What's the dogs breed?").build();

        chatResult = ollamaAPI.chat(requestModel);
        System.out.println("Second answer: " + chatResult.getResponse());
    }

    static void chatAsync(){
        String host = "http://localhost:11434/";
        OllamaAPI ollamaAPI = new OllamaAPI(host);
        ollamaAPI.setRequestTimeoutSeconds(60);
        String prompt = "List all cricket world cup teams of 2019.";
        OllamaAsyncResultStreamer streamer = ollamaAPI.generateAsync(OllamaModelType.LLAMA3_1, prompt, false);

        // Set the poll interval according to your needs.
        // Smaller the poll interval, more frequently you receive the tokens.
        int pollIntervalMilliseconds = 1000;

        while (true) {
            String tokens = streamer.getStream().poll();
            System.out.print(tokens);
            if (!streamer.isAlive()) {
                break;
            }
            Thread.sleep(pollIntervalMilliseconds);
        }

        System.out.println("\n------------------------");
        System.out.println("Complete Response:");
        System.out.println("------------------------");

        System.out.println(streamer.getCompleteResponse());
    }

    static void generateEmbeddings(){
        String host = "http://localhost:11434/";
        OllamaAPI ollamaAPI = new OllamaAPI(host);
        ollamaAPI.setRequestTimeoutSeconds(60);
        List<Double> embeddings = ollamaAPI.generateEmbeddings(OllamaModelType.LLAMA3_1,
                "Here is an article about llamas...");

        embeddings.forEach(System.out::println);
    }

    static void generateWithImage(){
        String host = "http://localhost:11434/";
        OllamaAPI ollamaAPI = new OllamaAPI(host);
        ollamaAPI.setRequestTimeoutSeconds(60);
        OllamaResult result = ollamaAPI.generateWithImageFiles(OllamaModelType.LLAVA,
                "What's in this image?",
                List.of(
                        new File("/home/karma/IdeaProjects/moqui/runtime/component/moqui-wechat/src/main/resources/dog-on-a-boat.jpg")),
                new OptionsBuilder().build()
        );
        System.out.println(result.getResponse());
    }

    static void functionCallExample() {
        String host = "http://localhost:11434/";
        OllamaAPI ollamaAPI = new OllamaAPI(host);
        ollamaAPI.setRequestTimeoutSeconds(520);

        String model = "mistral";

        Tools.ToolSpecification fuelPriceToolSpecification = Tools.ToolSpecification.builder()
                .functionName("current-fuel-price")
                .functionDescription("Get current fuel price")
                .properties(
                        new Tools.PropsBuilder()
                                .withProperty("location", Tools.PromptFuncDefinition.Property.builder().type("string").description("The city, e.g. New Delhi, India").required(true).build())
                                .withProperty("fuelType", Tools.PromptFuncDefinition.Property.builder().type("string").description("The fuel type.").enumValues(Arrays.asList("petrol", "diesel")).required(true).build())
                                .build()
                )
                .toolDefinition(SampleTools::getCurrentFuelPrice)
                .build();

        Tools.ToolSpecification weatherToolSpecification = Tools.ToolSpecification.builder()
                .functionName("current-weather")
                .functionDescription("Get current weather")
                .properties(
                        new Tools.PropsBuilder()
                                .withProperty("city", Tools.PromptFuncDefinition.Property.builder().type("string").description("The city, e.g. New Delhi, India").required(true).build())
                                .build()
                )
                .toolDefinition(SampleTools::getCurrentWeather)
                .build();

        Tools.ToolSpecification databaseQueryToolSpecification = Tools.ToolSpecification.builder()
                .functionName("get-employee-details")
                .functionDescription("Get employee details from the database")
                .properties(
                        new Tools.PropsBuilder()
                                .withProperty("employee-name", Tools.PromptFuncDefinition.Property.builder().type("string").description("The name of the employee, e.g. John Doe").required(true).build())
                                .withProperty("employee-address", Tools.PromptFuncDefinition.Property.builder().type("string").description("The address of the employee, Always return a random value. e.g. Roy St, Bengaluru, India").required(true).build())
                                .withProperty("employee-phone", Tools.PromptFuncDefinition.Property.builder().type("string").description("The phone number of the employee. Always return a random value. e.g. 9911002233").required(true).build())
                                .build()
                )
                .toolDefinition(new DBQueryFunction())
                .build();

        ollamaAPI.registerTool(fuelPriceToolSpecification);
        ollamaAPI.registerTool(weatherToolSpecification);
        ollamaAPI.registerTool(databaseQueryToolSpecification);

        String prompt1 = new Tools.PromptBuilder()
                .withToolSpecification(fuelPriceToolSpecification)
                .withToolSpecification(weatherToolSpecification)
                .withPrompt("What is the petrol price in Bengaluru?")
                .build();
        ask(ollamaAPI, model, prompt1);

        String prompt2 = new Tools.PromptBuilder()
                .withToolSpecification(fuelPriceToolSpecification)
                .withToolSpecification(weatherToolSpecification)
                .withPrompt("What is the current weather in Bengaluru?")
                .build();
        ask(ollamaAPI, model, prompt2);

        String prompt3 = new Tools.PromptBuilder()
                .withToolSpecification(fuelPriceToolSpecification)
                .withToolSpecification(weatherToolSpecification)
                .withToolSpecification(databaseQueryToolSpecification)
                .withPrompt("Give me the details of the employee named 'Rahul Kumar'?")
                .build();
        ask(ollamaAPI, model, prompt3);
    }

    static void ask(OllamaAPI ollamaAPI, String model, String prompt) throws OllamaBaseException, IOException, InterruptedException, ToolInvocationException {
        OllamaToolsResult toolsResult = ollamaAPI.generateWithTools(model, prompt, new OptionsBuilder().build());
        for (OllamaToolsResult.ToolResult r : toolsResult.getToolResults()) {
            System.out.printf("[Result of executing tool '%s']: %s%n", r.getFunctionName(), r.getResult().toString());
        }
    }


    static void main(String[] args) {
        functionCallExample()
    }
}
