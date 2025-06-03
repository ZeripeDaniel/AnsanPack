package com.ansan.ansanpack.config;

import com.ansan.ansanpack.AnsanPack;

import java.sql.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class JobCostManager {

    public record JobInfo(int cost, String category) {}

    private static final Map<String, JobInfo> JOBS = new HashMap<>();

    public static void loadFromMySQL() {
        JOBS.clear();

        Properties props = UpgradeConfigManager.loadDbProps();
        String url = "jdbc:mysql://" + props.getProperty("db.host") + ":" +
                props.getProperty("db.port") + "/" +
                props.getProperty("db.database") +
                "?serverTimezone=Asia/Seoul&useSSL=false&allowPublicKeyRetrieval=true";

        try (Connection conn = DriverManager.getConnection(url, props.getProperty("db.user"), props.getProperty("db.password"))) {
            PreparedStatement stmt = conn.prepareStatement("SELECT job_name, cost, category FROM job_costs");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String jobName = rs.getString("job_name");
                int cost = rs.getInt("cost");
                String category = rs.getString("category");

                JOBS.put(jobName, new JobInfo(cost, category));
            }

            AnsanPack.LOGGER.info("[AnsanPack] 전직 직업 정보 로딩 완료 ({}개)", JOBS.size());

        } catch (SQLException e) {
            throw new RuntimeException("[AnsanPack] 전직 직업 로딩 실패: " + e.getMessage(), e);
        }
    }

    public static JobInfo getInfo(String jobName) {
        return JOBS.get(jobName);
    }

    public static Map<String, JobInfo> getAllJobs() {
        return Collections.unmodifiableMap(JOBS);
    }
}
