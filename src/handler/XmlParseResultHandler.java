package handler;

import model.SelectResult;
import model.XmlDocument;
import org.w3c.dom.*;

import java.util.ArrayList;
import java.util.List;

public class XmlParseResultHandler {

    // 콘솔 출력
    public void handle(XmlDocument xmlDocument) {
        System.out.println("=== 파일: " + xmlDocument.getFilePath().getFileName() + " ===");

        List<SelectResult> results = collect(xmlDocument);

        if (results.isEmpty()) {
            System.out.println("  <select> 태그 없음\n");
            return;
        }

        for (int i = 0; i < results.size(); i++) {
            System.out.println("  [select #" + (i + 1) + "]");
            System.out.println(results.get(i).toString());
            System.out.println();
        }
    }

    // 결과 수집
    public List<SelectResult> collect(XmlDocument xmlDocument) {
        List<SelectResult> results = new ArrayList<>();
        NodeList selectTags = xmlDocument.getDocument().getElementsByTagName("select");

        for (int i = 0; i < selectTags.getLength(); i++) {
            Element select = (Element) selectTags.item(i);

            String id            = select.getAttribute("id");
            String parameterType = select.getAttribute("parameterType");
            String resultType    = select.getAttribute("resultType");
            String sql           = select.getTextContent().trim();

            results.add(new SelectResult(id, parameterType, resultType, sql));
        }

        return results;
    }
}