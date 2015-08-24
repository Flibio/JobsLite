package me.Flibio.JobsLite.Utils;

import java.math.BigDecimal;
import java.util.UUID;

import me.Flibio.EconomyLite.API.EconomyLiteAPI;

import com.erigitic.service.TEService;

public class EconManager {
	
	public enum EconType {
		ECONOMY_LITE,
		TOTAL_ECONOMY
	}
	
	private EconType type;
	private TEService totalEconomy;
	private EconomyLiteAPI economyLite;
	
	public EconManager() {
		
	}
	
	public void initialize(EconType econ, EconomyLiteAPI economyLite, TEService totalEconomy) {
		type = econ;
		if(totalEconomy!=null) {
			this.totalEconomy = totalEconomy;
		}
		if(economyLite!=null) {
			this.economyLite = economyLite;
		}
	}
	
	public EconType getEconomyType() {
		return type;
	}
	
	public void addBalance(String uuid, int amount) {
		if(type.equals(EconType.ECONOMY_LITE)) {
			economyLite.getPlayerAPI().addCurrency(uuid, amount);
		} else if(type.equals(EconType.TOTAL_ECONOMY)) {
			totalEconomy.addToBalance(UUID.fromString(uuid), BigDecimal.valueOf(amount), false);
		}
	}
}
