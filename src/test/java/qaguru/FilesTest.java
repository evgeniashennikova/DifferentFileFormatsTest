package qaguru;

import com.codeborne.pdftest.PDF;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.DownloadOptions;
import com.codeborne.selenide.logevents.SelenideLogger;
import com.codeborne.xlstest.XLS;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import io.qameta.allure.Owner;
import io.qameta.allure.selenide.AllureSelenide;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.*;
import java.util.List;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.FileDownloadMode.FOLDER;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FilesTest {

    @BeforeAll
    public static void start() {
        Configuration.startMaximized = true;
        SelenideLogger.addListener("allure", new AllureSelenide());
    }

    @ParameterizedTest(name = "{displayName} {0}")
    @Owner("eshennikova")
    @DisplayName("Загрузка файла")
    @ValueSource(strings = {"table.xlsx", "one.txt"})
    public void uploaderFileTest(String searchQuery) {

        open("https://the-internet.herokuapp.com/upload");
        $("input[type='file']").uploadFromClasspath(searchQuery);
        $("#file-submit").click();
        $("#uploaded-files").shouldHave(text(searchQuery));

    }

    @Test
    @Owner("eshennikova")
    @DisplayName("Скачать файл формата .xls")
    public void downloadFileXlsTest() throws IOException {

        open("https://file-examples.com/index.php/sample-documents-download/sample-xls-download/");
        File file = $("a[download='file_example_XLS_10.xls']").download();

        XLS parsedXls = new XLS(file);
        boolean checkPassed = parsedXls.excel.
                getSheetAt(0).
                getRow(3).
                getCell(1).
                getStringCellValue().
                contains("Philip");
        assert (checkPassed);
    }

    @Test
    @Owner("eshennikova")
    @DisplayName("Скачать файл формата .pdf")
    public void downloadFilePdfTest() throws IOException {
        open("http://fioletspb.com/menu/");

        File filePdf = $("a[href*='/menu.pdf']").download();
        PDF parsedPdf = new PDF(filePdf);
        Assertions.assertEquals(5, parsedPdf.numberOfPages);
    }

    @Test
    @Owner("eshennikova")
    @DisplayName("Скачать файл формата .txt")
    public void downloadWithoutHrefFileTxtTest() throws IOException {

        Configuration.fileDownload = FOLDER;
        open("https://ruwapa.net/book/sara-natan-holodnoe-serdce/");
        File fileTxt = $(".type-3").download(DownloadOptions.using(FOLDER).withTimeout(6000));
        String fileContent = IOUtils.toString(new FileReader(fileTxt));
        assertTrue(fileContent.contains("В ущелье рядом с глубоким фьордом мирно спал замок Эренделл."));
    }

    @Test
    @Owner("eshennikova")
    @DisplayName("Парсинг .csv файла")
    public void parseCsvFileTest() throws IOException, CsvException {
        ClassLoader classLoader = this.getClass().getClassLoader();
        try (InputStream is = classLoader.getResourceAsStream("filecsv.csv")) {
            assert is != null;
            try (Reader reader = new InputStreamReader(is)) {
                CSVReader csvReader = new CSVReader(reader);
                List<String[]> strings = csvReader.readAll();
                assertEquals(5, strings.size());
            }
        }


    }
}

