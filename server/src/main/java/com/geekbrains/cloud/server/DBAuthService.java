package com.geekbrains.cloud.server;

import java.sql.*;

// Новый класс авториации через БД
public class DBAuthService implements AuthService {

    private static Connection connection;
    private static Statement statement;
    private static PreparedStatement psSelect;



    public DBAuthService() {
        try {
            connect();
            System.out.println("Подключились к БД");
            prepareStatements();
//            inputUsers();

        } catch (Exception e){
            e.printStackTrace();
            disconnect();
        }

    }
/*
    private void inputUsers() throws SQLException {
        statement.executeUpdate("INSERT INTO user (login,pass,nick) VALUES ('login6','pass6','nick6');");
        System.out.println("Добавили запись в БД");
    }
*/
    private static void prepareStatements() throws SQLException {
        psSelect = connection.prepareStatement("SELECT * FROM user WHERE login = ? AND pass = ?;");

    }


    private static void disconnect(){
        try {
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private static void connect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:main.db");
        statement = connection.createStatement();

    }


    @Override
    public boolean ExistLoginAndPassword(String login, String password) {
        boolean result=false;
        try {
            psSelect.setString(1,login);
            psSelect.setString(2,password);
            ResultSet rs = psSelect.executeQuery();
            if (rs.next()) {
                result = true;
            }
            else{
                System.out.println("Для пользователя "+login+"-"+password+" не нашли записи в БД");
                result = false;
            }
            rs.close();

        } catch (SQLException e) {
            result = false;
            e.printStackTrace();
        }

        return result;
    }

}
