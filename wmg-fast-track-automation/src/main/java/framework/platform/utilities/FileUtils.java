package framework.platform.utilities;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    public static List<List<String>> readExcelFile(String fileName) throws FileNotFoundException {
        Path path = Paths.get(System.getProperty("user.dir") + "/target/" + fileName);
        File file = new File(path.toString());
        for (int i=0; i<2; i++) {
            if (!file.exists()) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if (!file.exists()) {
            throw new FileNotFoundException("No file found with name " + fileName);
        }
        List<List<String>> list = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(file)){
            XSSFWorkbook workbook = new XSSFWorkbook(fis);
            XSSFSheet sheet = workbook.getSheetAt(0);
            XSSFRow row;
            XSSFCell cell;

            int rows; // No of rows
            rows = sheet.getPhysicalNumberOfRows();

            int cols = 0; // No of columns
            int tmp;

            for(int i = 0; i < 10 || i < rows; i++) {
                row = sheet.getRow(i);
                if(row != null) {
                    tmp = sheet.getRow(i).getPhysicalNumberOfCells();
                    if(tmp > cols) cols = tmp;
                }
            }

            List<String> rowData;
            for(int r = 1; r < rows; r++) {
                rowData = new ArrayList<>();
                row = sheet.getRow(r);
                if(row != null) {
                    for(int c = 0; c < cols; c++) {
                        cell = row.getCell((short)c);
                        if(cell != null) {
                            rowData.add(cell.getStringCellValue());
                        } else {
                            rowData.add("");
                        }
                    }
                }
                list.add(rowData);
            }

            } catch (Exception e) {
            e.printStackTrace();
        }
        file.delete();
        return list;
    }
}
