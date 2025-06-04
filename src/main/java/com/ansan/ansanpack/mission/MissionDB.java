package com.ansan.ansanpack.mission;

import com.ansan.ansanpack.config.UpgradeConfigManager;

import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.sql.*;
import java.util.*;

public class MissionDB {
    public static Connection getConnection() throws Exception {
        Properties props = UpgradeConfigManager.loadDbProps();

        String url = "jdbc:mysql://" +
                props.getProperty("db.host") + ":" +
                props.getProperty("db.port") + "/" +
                props.getProperty("db.database") +
                "?serverTimezone=Asia/Seoul&useSSL=false&allowPublicKeyRetrieval=true";

        return DriverManager.getConnection(url, props.getProperty("db.user"), props.getProperty("db.password"));
    }
}
