import checker.SqlTableChecker;
import handler.XmlParseResultHandler;
import loader.XmlFileLoader;
import model.CheckResult;
import model.SelectResult;
import parser.XmlFileParser;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class XmlParserApp {

    // true  : 미사용 컬럼이 있는 select만 출력
    // false : 모든 select 출력
    private static final boolean ONLY_HAS_UNUSED_COLUMNS = true;

    public static void main(String[] args) throws Exception {
        String folderPath = "C:\\git_repository\\douzone-comet-service-pu-puocon\\src\\main\\resources\\mybatis\\com";
//        String folderPath = "C:\\git_repository\\test";

        XmlFileLoader         loader  = new XmlFileLoader(folderPath);
        XmlFileParser         parser  = new XmlFileParser();
        XmlParseResultHandler handler = new XmlParseResultHandler();
        SqlTableChecker       checker = new SqlTableChecker();

        List<Path> xmlFiles = loader.loadAll();

        System.out.println("총 " + xmlFiles.size() + "개의 XML 파일을 찾았습니다.\n");

        for (Path file : xmlFiles) {
            parser.parse(file).ifPresent(xmlDocument -> {
                List<SelectResult> selectResults = handler.collect(xmlDocument);
                List<CheckResult>  checkResults  = checker.check(selectResults);

                // 옵션에 따라 미사용 컬럼 없는 결과 필터링
                if (ONLY_HAS_UNUSED_COLUMNS) {
                    checkResults = checkResults.stream()
                        .filter(result -> !result.getUnusedColumns().isEmpty())
                        .collect(Collectors.toList());
                }

                if (checkResults.isEmpty()) return;

                System.out.println("=== 파일: " + file.getFileName() + " ===\n");

                for (int i = 0; i < checkResults.size(); i++) {
                    System.out.println(checkResults.get(i).toBoxString(i + 1));
                    System.out.println();
                }
            });
        }
    }
}