package parser;

import model.XmlDocument;
import org.w3c.dom.Document;
import javax.xml.parsers.*;
import java.nio.file.Path;
import java.util.Optional;

public class XmlFileParser {

    private final DocumentBuilder builder;

    public XmlFileParser() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        this.builder = factory.newDocumentBuilder();
    }

    public Optional<XmlDocument> parse(Path filePath) {
        try {
            Document doc = builder.parse(filePath.toFile());
            doc.getDocumentElement().normalize();
            return Optional.of(new XmlDocument(filePath, doc));
        } catch (Exception e) {
            System.err.println("파싱 실패: " + filePath + " - " + e.getMessage());
            return Optional.empty();
        }
    }
}