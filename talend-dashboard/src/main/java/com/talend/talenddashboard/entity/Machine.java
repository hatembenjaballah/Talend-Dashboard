package com.talend.talenddashboard.entity;

import jakarta.persistence.*;

@Entity
public class Machine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    // Chemins fichiers (utilisés si jdbcUrl est vide)
    private String statsPath;
    private String logsPath;
    private String metersPath;

    // Connexion JDBC (si renseignée, active le mode Base de données)
    private String jdbcUrl;
    private String jdbcDriver;
    private String jdbcUser;
    private String jdbcPassword;

    // Requêtes SQL (utilisées uniquement en mode DB)
    @Column(length = 2000)
    private String statsQuery;
    @Column(length = 2000)
    private String logsQuery;
    @Column(length = 2000)
    private String metersQuery;

    public Machine() {}

    public Machine(String name, String statsPath, String logsPath, String metersPath) {
        this.name = name;
        this.statsPath = statsPath;
        this.logsPath = logsPath;
        this.metersPath = metersPath;
    }

    // Getters & setters (tous)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getStatsPath() { return statsPath; }
    public void setStatsPath(String statsPath) { this.statsPath = statsPath; }
    public String getLogsPath() { return logsPath; }
    public void setLogsPath(String logsPath) { this.logsPath = logsPath; }
    public String getMetersPath() { return metersPath; }
    public void setMetersPath(String metersPath) { this.metersPath = metersPath; }
    public String getJdbcUrl() { return jdbcUrl; }
    public void setJdbcUrl(String jdbcUrl) { this.jdbcUrl = jdbcUrl; }
    public String getJdbcDriver() { return jdbcDriver; }
    public void setJdbcDriver(String jdbcDriver) { this.jdbcDriver = jdbcDriver; }
    public String getJdbcUser() { return jdbcUser; }
    public void setJdbcUser(String jdbcUser) { this.jdbcUser = jdbcUser; }
    public String getJdbcPassword() { return jdbcPassword; }
    public void setJdbcPassword(String jdbcPassword) { this.jdbcPassword = jdbcPassword; }
    public String getStatsQuery() { return statsQuery; }
    public void setStatsQuery(String statsQuery) { this.statsQuery = statsQuery; }
    public String getLogsQuery() { return logsQuery; }
    public void setLogsQuery(String logsQuery) { this.logsQuery = logsQuery; }
    public String getMetersQuery() { return metersQuery; }
    public void setMetersQuery(String metersQuery) { this.metersQuery = metersQuery; }
}