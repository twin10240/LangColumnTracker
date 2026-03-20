package model;

import org.w3c.dom.Document;
import java.nio.file.Path;

public class XmlDocument {
    private final Path filePath;
    private final Document document;

    public XmlDocument(Path filePath, Document document) {
        this.filePath = filePath;
        this.document = document;
    }

    public Path getFilePath() { return filePath; }
    public Document getDocument() { return document; }
}