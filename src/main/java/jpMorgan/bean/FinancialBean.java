package jpMorgan.bean;

import java.util.Date;

public class FinancialBean implements Comparable<FinancialBean>{

	private String name;
	private String type;
	private String currency;
	private double agreeFX;
	private Date instructionDate;
	private Date settlementDate;
	private int units;
	private double price;
	private double usd;

	public final static String BUY = "B";
	public final static String SELL = "S";
	
	public FinancialBean(String name,String type, double agreeFX,
			String currency, Date instructionDate, 
			Date settlementDate,int units, double price) {
		this.name = name;
		this.type = type;
		this.currency = currency;
		this.agreeFX = agreeFX;
		this.instructionDate = instructionDate;
		this.settlementDate = settlementDate;
		this.price = price;
		this.usd = price * units * agreeFX;
	}

	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	public String getCurrency() {
		return currency;
	}


	public void setCurrency(String currency) {
		this.currency = currency;
	}


	public double getAgreeFX() {
		return agreeFX;
	}


	public void setAgreeFX(double agreeFX) {
		this.agreeFX = agreeFX;
	}


	public Date getInstructionDate() {
		return instructionDate;
	}


	public void setInstructionDate(Date instructionDate) {
		this.instructionDate = instructionDate;
	}


	public Date getSettlementDate() {
		return settlementDate;
	}


	public void setSettlementDate(Date settlementDate) {
		this.settlementDate = settlementDate;
	}


	public int getUnits() {
		return units;
	}


	public void setUnits(int units) {
		this.units = units;
	}


	public double getPrice() {
		return price;
	}


	public void setPrice(double price) {
		this.price = price;
	}


	public double getUsd() {
		return usd;
	}


	public void setUsd(double usd) {
		this.usd = usd;
	}


	public int compareTo(FinancialBean fb) {
		return new Double(fb.getUsd()).compareTo(new Double(this.getUsd()));
	}

}
