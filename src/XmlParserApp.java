import checker.SqlTableChecker;
import handler.XmlParseResultHandler;
import loader.XmlFileLoader;
import model.CheckResult;
import model.SelectResult;
import parser.XmlFileParser;

import java.nio.file.Path;
import java.util.List;

public class XmlParserApp {

    public static void main(String[] args) throws Exception {
        String folderPath = "C:\\git_repository\\douzone-comet-service-ie-ietscg\\src\\main\\resources\\mybatis\\com";

        XmlFileLoader         loader  = new XmlFileLoader(folderPath);
        XmlFileParser         parser  = new XmlFileParser();
        XmlParseResultHandler handler = new XmlParseResultHandler();
        SqlTableChecker       checker = new SqlTableChecker();

        List<Path> xmlFiles = loader.loadAll();

        for (Path file : xmlFiles) {
            parser.parse(file).ifPresent(xmlDocument -> {
                List<SelectResult> selectResults = handler.collect(xmlDocument);
                List<CheckResult>  checkResults  = checker.check(selectResults);

                System.out.println("=== 파일: " + file.getFileName() + " ===");

                if (checkResults.isEmpty()) {
                    System.out.println("  해당 테이블 사용 없음\n");
                    return;
                }

                for (int i = 0; i < checkResults.size(); i++) {
                    System.out.println(checkResults.get(i).toBoxString(i + 1));
                    System.out.println(); // 박스 사이 빈 줄
                }

                System.out.println();
            });
        }
    }
}