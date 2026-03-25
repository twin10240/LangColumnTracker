import checker.SqlTableChecker;
import handler.XmlParseResultHandler;
import loader.XmlFileLoader;
import model.CheckResult;
import model.SelectResult;
import parser.XmlFileParser;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/*
* 1. XML 파싱 -> SQL
* 2. 해당 테이블이 있는지
* 3. 사용되였다면 LANG 테이블 존재 체크
* 4. 만약 있다면 컬럼 체크
*
* * ### 체크 로직 추가 방향
기존 로직은 **테이블 기준**으로 체크했다면, 추가로 **컬럼명 기준**으로도 체크해야 해요.
*
1. 다국어 대상 테이블 사용 → 기존 로직
2. 다국어 대상 컬럼명이 조회 컬럼에 있는데 다국어 테이블은 안 쓴 경우 → 추가 로직
*
* */

public class XmlParserApp {

    // true  : 미사용 컬럼이 있는 select만 출력
    // false : 모든 select 출력
    private static final boolean ONLY_HAS_UNUSED_COLUMNS = false;

    public static void main(String[] args) throws Exception {
//        String folderPath = "C:\\git_repository\\douzone-comet-service-pu-requestcommon\\src\\main\\resources\\mybatis\\com";
        String folderPath = "C:\\git_repository\\test";

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