package com.perso.unit.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class CompareExcelFiles {

	public static void compareExcelFiles(InputStream actual, InputStream expected) throws IOException {
		try (Workbook workbook1 = WorkbookFactory.create(actual);
			 Workbook workbook2 = WorkbookFactory.create(expected)) {

			verifyIfExcelFilesHaveSameNumberAndNameOfSheets(workbook1, workbook2);
			verifySheetsInExcelFilesHaveSameRowsAndColumns(workbook1, workbook2);
			verifyDataInExcelBookAllSheets(workbook1, workbook2);
		}
	}

	/*
	This method compares if both excel files have same number of sheets and same
	name of sheets
	*/
	public static void verifyIfExcelFilesHaveSameNumberAndNameOfSheets(Workbook workbook1, Workbook workbook2) {
		log.debug("Verifying if both work books have same number of sheets.............");
		// Get total sheets count from first excel file
		int sheetsInWorkbook1 = workbook1.getNumberOfSheets();
		// Get total sheets count from second excel file
		int sheetsInWorkbook2 = workbook2.getNumberOfSheets();
		// Compare if both excel files have same number of sheets
		Assertions.assertEquals(sheetsInWorkbook1, sheetsInWorkbook2,
				"Excel work books have different number of sheets. \n "
				+ "Sheets in work book 1 : "+sheetsInWorkbook1 +"\n "
						+ "Number of sheets in work book 2 : "+sheetsInWorkbook2);
		// Printing number of sheets in each work book
		log.debug("Sheets in first work book : "+sheetsInWorkbook1);
		log.debug("Sheets in second work book : "+sheetsInWorkbook2);
		log.debug("Both work books have same number of sheets.........................");
		
		// Verify if sheets have same name in both workbooks
		// Sheets may not be in same order in both excel. So I am relaxing order of sheets condition.
		// Change i as required.
		log.debug("Verifying if both work books have same name of sheets.............");
		List sheetsNameOfWb1 = new ArrayList<>();
		List sheetsNameOfWb2 = new ArrayList<>();
		// Since we have already verified that both work books have same number of sheets so iteration can be done against any workbook sheet count
		for (int i = 0; i < sheetsInWorkbook1; i++) {
			// Retrieving sheet names from both work books and adding to different lists
			sheetsNameOfWb1.add(workbook1.getSheetName(i));
			sheetsNameOfWb2.add(workbook2.getSheetName(i));
		}
		// Since I am relaxing same sequence of sheets. 
		Collections.sort(sheetsNameOfWb1);
		Collections.sort(sheetsNameOfWb2);
		Assertions.assertEquals(sheetsNameOfWb1, sheetsNameOfWb2, "Provided excel work books have different name of sheets.");
		log.debug("Sheet Names in first work book : "+sheetsNameOfWb1);
		log.debug("Sheet Names in second work book : "+sheetsNameOfWb2);
		log.debug("Both work books have same name of sheets.........................");
	}

	// This method compares if both excel files have same number of rows and corresponding columns
	public static void verifySheetsInExcelFilesHaveSameRowsAndColumns(Workbook workbook1, Workbook workbook2) {
		log.debug(
				"Verifying if both work books have same number of rows and columns in all sheets.............");
		int sheetCounts = workbook1.getNumberOfSheets();
		for (int i = 0; i < sheetCounts; i++) {
			Sheet s1 = workbook1.getSheetAt(i);
			Sheet s2 = workbook2.getSheetAt(i);
			int rowsInSheet1 = s1.getPhysicalNumberOfRows();
			int rowsInSheet2 = s2.getPhysicalNumberOfRows();
			Assertions.assertEquals(rowsInSheet1, rowsInSheet2, "Sheets have different count of rows..");

			Iterator<Row> rowInSheet1 = s1.rowIterator();
			Iterator<Row> rowInSheet2 = s2.rowIterator();
			while (rowInSheet1.hasNext()) {
				int cellCounts1 = rowInSheet1.next().getPhysicalNumberOfCells();
				int cellCounts2 = rowInSheet2.next().getPhysicalNumberOfCells();
				Assertions.assertEquals(cellCounts1, cellCounts2, "Sheets have different count of columns..");
			}
		}
	}

	public static void verifyDataInExcelBookAllSheets(Workbook workbook1, Workbook workbook2) {
		log.debug("Verifying if both work books have same data.............");
		// Since we have already verified that both work books have same number of sheets so iteration can be done against any workbook sheet count
		int sheetCounts = workbook1.getNumberOfSheets();
		// So we will iterate through sheet by sheet
		for (int sheetNumber = 0; sheetNumber < sheetCounts; sheetNumber++) {
			// Get sheet at same index of both work books
			Sheet s1 = workbook1.getSheetAt(sheetNumber);
			Sheet s2 = workbook2.getSheetAt(sheetNumber);
			log.debug("*********** Sheet Name : "+s1.getSheetName()+"*************");
			// Iterating through each row
			int rowCounts = s1.getPhysicalNumberOfRows();
			for (int rowNumber = 0; rowNumber < rowCounts; rowNumber++) {
				// Iterating through each cell
				int cellCounts = s1.getRow(rowNumber).getPhysicalNumberOfCells();
				for (int k = 0; k < cellCounts; k++) {
					// Getting individual cell
					Cell c1 = s1.getRow(rowNumber).getCell(k);
					Cell c2 = s2.getRow(rowNumber).getCell(k);
					// Since cell have types and need or use different methods
					if (c1.getCellType().equals(c2.getCellType())) {
						if (c1.getCellType() == CellType.STRING) {
							String v1 = c1.getStringCellValue();
							String v2 = c2.getStringCellValue();
							Assertions.assertEquals(v1, v2, "Sheet " + sheetNumber + ", Row " + rowNumber + ", Cell " + k + ": values are different.....");
							log.debug("Its matched : "+ v1 + " === "+ v2);
						}
						if (c1.getCellType() == CellType.NUMERIC) {
							// If cell type is numeric, we need to check if data is of Date type
							if (DateUtil.isCellDateFormatted(c1) | DateUtil.isCellDateFormatted(c2)) {
								// Need to use DataFormatter to get data in given style otherwise it will come as time stamp
								DataFormatter df = new DataFormatter();
								String v1 = df.formatCellValue(c1);
								String v2 = df.formatCellValue(c2);
								Assertions.assertEquals(v1, v2, "Sheet " + sheetNumber + ", Row " + rowNumber + ", Cell " + k + ": values are different.....");
								log.debug("Its matched : "+ v1 + " === "+ v2);
							} else {
								double v1 = c1.getNumericCellValue();
								double v2 = c2.getNumericCellValue();
								Assertions.assertEquals(v1, v2, "Sheet " + sheetNumber + ", Row " + rowNumber + ", Cell " + k + ": values are different.....");
								log.debug("Its matched : "+ v1 + " === "+ v2);
							}
						}
						if (c1.getCellType() == CellType.BOOLEAN) {
							boolean v1 = c1.getBooleanCellValue();
							boolean v2 = c2.getBooleanCellValue();
							Assertions.assertEquals(v1, v2, "Sheet " + sheetNumber + ", Row " + rowNumber + ", Cell " + k + ": values are different.....");
							log.debug("Its matched : "+ v1 + " === "+ v2);
						}
					} else
					{
						// If cell types are not same, exit comparison  
						Assertions.fail("Sheet " + sheetNumber + ", Row " + rowNumber + ", Cell " + k + ": Non matching cell type.");
					}
				}
			}
		}
		log.debug("Hurray! Both work books have same data.");
	}
}
