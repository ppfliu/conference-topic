package edu.cuhk.hccl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import edu.cuhk.hccl.nlp.PreProcessor;


public class AppAbstracts 
{
	final static int STATUS_IDX = 3;
	final static String ACCEPT = "accept";
	final static String REJECT = "reject";
	
    public static void main( String[] args )
    {
    	System.out.println("[INFO] Processing begins...");
        try {
        	// Parse program parameters
			String filePath = args[0];
        	int column = Integer.parseInt(args[1]);
			String outFile = args[2];
			String type = args[3];
			
			// Exclude title by default
			boolean withTitle = false;
			if (args.length == 5)
				withTitle = Boolean.parseBoolean(args[4]);
        	
			// Don't output track id by default
			boolean withTrack = false;
			if (args.length == 6)
				withTrack = Boolean.parseBoolean(args[5]);
			        	
			// Read excel and prepare the dataset of abstracts
        	FileInputStream fileInputStream = new FileInputStream(filePath);
			HSSFWorkbook workbook = new HSSFWorkbook(fileInputStream);
			HSSFSheet sheet = workbook.getSheetAt(0); // get first sheet
			Iterator<Row> rowIterator = sheet.iterator();
			rowIterator.next(); // ignore head row
			
			List<String> lines = new ArrayList<String>();
            while (rowIterator.hasNext()) 
            {
                Row row = rowIterator.next();
                
                // exclude special session
                Cell trackCell = row.getCell(2);
                String trackName = trackCell.getStringCellValue();
                if (trackName.contains("SPECIAL SESSION"))
                	continue;
                
                // get track ID
                String trackID = trackName.substring(0, trackName.indexOf(' '));
                
                if (type.equalsIgnoreCase(ACCEPT) || type.equalsIgnoreCase(REJECT)){
                	Cell status = row.getCell(STATUS_IDX);
                	if (status.getStringCellValue().equalsIgnoreCase(type)){
                		addDocument(column, lines, row, withTitle, withTrack, trackID);
                	}
                } else{
                	addDocument(column, lines, row, withTitle, withTrack, trackID);
                }
            }
            
            fileInputStream.close();
            
            FileUtils.writeLines(new File(outFile), lines, false);
            
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
		System.out.println("[INFO] Processing is finished!");
    }

    // Each document consists of both title and abstract
	private static void addDocument(int column, List<String> lines, Row row, 
			boolean withTitle, boolean withTrack, String trackID) {
		
		Cell cell = row.getCell(column);
		
		// Output track ID as prefix separated by comma
		String prefix = "";
		if (withTrack)
			prefix = trackID + ",";
		
		if (withTitle){
			Cell titleCell = row.getCell(1);
			lines.add(prefix + PreProcessor.getContentWords(titleCell.getStringCellValue() + ". " + cell.getStringCellValue()));
		} else {
			lines.add(prefix + PreProcessor.getContentWords(cell.getStringCellValue()));
		}
	}
    
}
