package com.ansan.ansanpack.client.level;

public class LocalPlayerCardData {
    public static final LocalPlayerCardData INSTANCE = new LocalPlayerCardData();

    private int money = 0;

    public void setMoney(int money) {
        this.money = money;
    }

    public int getMoney() {
        return money;
    }
}
