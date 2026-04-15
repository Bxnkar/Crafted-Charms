package com.craftedcharms;

import com.craftedcharms.ui.MainMenu;

/**
 * Application entry point — Crafted Charms Jewellery Management System.
 *
 * Run via Maven:  mvn exec:java
 * Or build JAR :  mvn package  →  java -jar target/crafted-charms.jar
 *
 * Prerequisites:
 *   1. MySQL running with database 'crafted_charms' created.
 *   2. Run sql/schema.sql to create tables.
 *   3. Run sql/sample_data.sql to seed demo data.
 *   4. Configure DB credentials in environment variables (DB_URL/DB_USER/DB_PASSWORD)
 *      or src/main/resources/config.properties.
 */
public class Main {

    public static void main(String[] args) {
        try {
            new MainMenu().start();
        } catch (Exception e) {
            System.err.println("\n[FATAL] " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
