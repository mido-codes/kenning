package io.github.mido.kenning;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/test")
public class TestController {

    private final VectorStore vectorStore;

    public TestController(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @PostMapping("/index")
    public String index(@RequestBody String text) {
        Document document = new Document(text, Map.of("source", "manual-test"));
        vectorStore.add(List.of(document));
        return "Indexed! Length: " + text.length() + " characters.";
    }
}
