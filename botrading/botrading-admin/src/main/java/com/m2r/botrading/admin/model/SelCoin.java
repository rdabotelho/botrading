package com.m2r.botrading.admin.model;

public class SelCoin {

	private String coin;
	private Boolean checked;

	public SelCoin(String coin, Boolean checked) {
		this.coin = coin;
		this.checked = checked;
	}

	public String getCoin() {
		return coin;
	}

	public void setCoin(String coin) {
		this.coin = coin;
	}

	public Boolean getChecked() {
		return checked;
	}

	public void setChecked(Boolean checked) {
		this.checked = checked;
	}

}
