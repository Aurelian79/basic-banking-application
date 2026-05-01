package com.bank.config;


import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayDeque;
import java.util.Properties;

public class DBConnection {

    // Creates a singelton of instance of whole class that manages the connection
    private static DBConnection instance;

    // an arraydeque that stores instances of connection
    private ArrayDeque<Connection> pool;

    //constructor of thid class
    private  DBConnection() {

        try {


            pool = new ArrayDeque<>();

            //properties class is used to hold key value pairs
            Properties props = new Properties();

            //takes the data from resources folder and db.properties file .returns it as a stream
            InputStream is = getClass().getClassLoader().getResourceAsStream("db.properties");

            //reads file and loads the data into memory
            props.load(is);


            String url = firstNonBlank(System.getenv("DB_URL"), props.getProperty("db.url"));
            String user = firstNonBlank(System.getenv("DB_USERNAME"), props.getProperty("db.username"));
            String pass = firstNonBlank(System.getenv("DB_PASSWORD"), props.getProperty("db.password"));
            int poolSize = Integer.parseInt(props.getProperty("db.pool.size"));

            Class.forName("org.postgresql.Driver");

            //loop runs poolSize times
            for (int i = 0; i < poolSize; i++) {
                // creates connection every loop and adds element at the of deque
                Connection conn = DriverManager.getConnection(url, user, pass);
                pool.offer(conn);
            }
            System.out.println("Db connection pool created with connection size " + poolSize);

        } catch (Exception e) {
            throw new RuntimeException("Error initializing DB COnnection" + e);
        }
    }

    private String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.trim().isEmpty()) {
            return primary;
        }
        return fallback;
    }

    //
    public static synchronized DBConnection getInstance(){
        if (instance ==  null){
             instance = new DBConnection();
        }
        return instance;
    }

    //retreives the head of the dequeue and removes it if empty returns null
    public synchronized Connection getConnection(){
        if(pool.isEmpty()){
            throw new RuntimeException("NO available DB connection");
        }
        return pool.poll();
    }

    //returns connection back to pool
    public synchronized void releaseConnection(Connection conn){
        if(conn != null){
            pool.offer(conn);
        }
    }
}


