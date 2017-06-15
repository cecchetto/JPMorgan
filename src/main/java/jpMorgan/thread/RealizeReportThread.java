package jpMorgan.thread;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jpMorgan.bean.FinancialBean;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

public class RealizeReportThread implements Runnable{

	private Map<Date,Map<String,List<FinancialBean>>> mapDateFinancialBean = 
			new HashMap<Date,Map<String,List<FinancialBean>>>();

	public void run() {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("FinancialSheet.xls").getFile());
		double amountUSDIncoming = 0;
		double amountUSDOutcoming = 0;
		StringBuffer rankingIncoming = new StringBuffer();
		StringBuffer rankingOutcoming = new StringBuffer();
		try {
			/**
			 * CONFIGURE MAP BASED ON ROW EXCEL VALUE 
			 */
			mapDateFinancialBean = getMapFromInputFile(file);
		} catch (FileNotFoundException e) {
			System.out.println("Problem retrieving information from xsl file");
		} catch (IOException e) {
			System.out.println("Problem retrieving information from xsl file");
		} catch (ParseException e) {
			System.out.println("Problem parsing String to Date");
		}
		System.out.printf("%-40s %-30s %-30s %-30s %.20s%n","SettlementDate", "AmountIncomingUSD","AmountOutcomingUSD","RankingIncoming","RankingOutcoming");
		
		/**START TO ELABORATE DATA FOR PRINT THE REPORT**/
		for(Date settlementDate : mapDateFinancialBean.keySet())
		{
			rankingOutcoming = new StringBuffer();
			rankingIncoming = new StringBuffer();
			amountUSDOutcoming = 0;
			amountUSDIncoming = 0;

			Map<String,List<FinancialBean>> internalMap = mapDateFinancialBean.get(settlementDate);
			/**CHECK SECOND DIVISION FOR INCOMING or OUTCOMING**/
			for(String type : internalMap.keySet())
			{
				List<FinancialBean> fbs = internalMap.get(type);
				Collections.sort(fbs);

				for(FinancialBean fb : fbs)
				{
					if(type.equals(FinancialBean.BUY))
					{
						rankingOutcoming.append(fb.getName() + "*");
						amountUSDOutcoming = amountUSDOutcoming + fb.getUsd();
					}
					else
					{
						rankingIncoming.append(fb.getName() + "*");
						amountUSDIncoming = amountUSDIncoming + fb.getUsd();
					}
				}
			}
			System.out.printf("%-40s %-30s %-30s %-30s %.20s%n",settlementDate , rounds(amountUSDIncoming,2) , rounds(amountUSDOutcoming,2) , rankingIncoming , rankingOutcoming);
		}
	}

	/**
	 * GET INFORMATION FROM EXCEL IN INPUT AND 
	 * RETURN A MAP WITH KEY SettlementDate T
	 * HAT CONTAINS ANOTHER MAP WITH KEY Buy OR Sell VALUE AND A LIST OF FINANCIAL BEAN
	 * @param Fike
	 * @return
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws ParseException 
	 */
	private Map<Date,Map<String,List<FinancialBean>>> getMapFromInputFile(File file) throws FileNotFoundException, IOException, ParseException
	{
		POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(file));
		HSSFWorkbook wb = new HSSFWorkbook(fs);
		HSSFSheet sheet = wb.getSheetAt(0);
		HSSFRow row;

		SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy");
		Date dateInstructionData = new Date();
		Date dateSettlementData = new Date();
		int rows;
		rows = sheet.getPhysicalNumberOfRows();

		for(int r = 1; r < rows; r++) {
			row = sheet.getRow(r);
			if(row != null) {
				/**
				 * GET currency
				 */
				String currency = row.getCell(3).toString();
				/**
				 * GET instructionData AND CONVERT TO DATA
				 */
				String instructionData = row.getCell(4).toString();
				dateInstructionData = format.parse(instructionData);
				/**
				 * GET settlementData AND CONVERT TO DATA
				 */
				String settlementData = row.getCell(5).toString();
				dateSettlementData = format.parse(settlementData);
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(dateSettlementData);
				int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
				/**
				 * MANAGE WEEK AND CURRENCY AND WEEK END DAYS
				 */
				if(!currency.equals("AED") && !currency.equals("SAR"))
				{	
					if(dayOfWeek == 1)
						calendar.add(Calendar.DAY_OF_WEEK, 1);	
					if(dayOfWeek == 7)
						calendar.add(Calendar.DAY_OF_WEEK, 2);
				}
				else
				{
					if(dayOfWeek == 6)
						calendar.add(Calendar.DAY_OF_WEEK, 2);
					if(dayOfWeek == 7)
						calendar.add(Calendar.DAY_OF_WEEK, 1);
				}
				/**
				 * CREATE NEW OBJECT MAPPING EXCEL ROW
				 */
				FinancialBean fb = new FinancialBean(row.getCell(0).toString(), row.getCell(1).toString(),
						Double.parseDouble(row.getCell(2).toString()),currency, dateInstructionData, 
						calendar.getTime(), (int)Double.parseDouble(row.getCell(6).toString()), Double.parseDouble(row.getCell(7).toString()));

				addToMap(fb);

			}
		}
		wb.close();
		return mapDateFinancialBean;
	}

	/**
	 * GET IN INPUT A FINANCIAL BEAN AND COLLOCATE IT INTO THE RIGHT PLACE BASED ON Buy/Sell AND SettlementDate INFORMATION
	 * @param fb
	 */
	private void addToMap(FinancialBean fb)
	{
		Map<String, List<FinancialBean>> internalMap = null;
		List<FinancialBean> fbs = null;
		internalMap = mapDateFinancialBean.get(fb.getSettlementDate());
		/**CHECK IF SOME FINANCIAL BEAN BELONG AT SAME DAY OF ANOTHER ONE**/
		if(internalMap != null)
		{
			fbs = internalMap.get(fb.getType());
			if(fbs != null)
			{
				fbs.add(fb);
			}
			else
			{
				fbs = new LinkedList<FinancialBean>();
				fbs.add(fb);
			}
			internalMap.put(fb.getType(), fbs);
		}
		else
		{
			internalMap = new HashMap<String, List<FinancialBean>>();
			fbs = new LinkedList<FinancialBean>();
			fbs.add(fb);
			internalMap.put(fb.getType(), fbs);
		}
		mapDateFinancialBean.put(fb.getSettlementDate(), internalMap);
	}

	/**
	 * CLASSIC ROUNDS
	 * @param value
	 * @param numCifreDecimali
	 * @return
	 */
	public static double rounds(double value, int numCifreDecimali) {
		double temp = Math.pow(10, numCifreDecimali);
		return Math.round(value * temp) / temp; 
	}
}
