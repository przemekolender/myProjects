import java.sql.*;
import java.util.Locale;
import java.util.Scanner;
public class Connect {
    public Connect() {
    }

    public static void main(String[] args)  {
        Connection con = null;
        boolean end;
        //logging to database
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            do {
                String[] convars = connectionVars();
                if (convars == null) break;
                try {
                    end = false;
                    con = DriverManager.getConnection(convars[0], convars[1], convars[2]);
                }catch (Exception e){
                    System.out.println("Login failed, try again or type EXIT to resign\n");
                    end = true;
                }

            }while(end);

            System.out.println("You are logged in, type ls to get list of avalable commands.");
            boolean exit = true;
            Scanner s = new Scanner(System.in);

            //app main menu
            do {
                switch (s.nextLine().toLowerCase(Locale.ROOT)) {
                    case "ls" -> simpleCommands.lsCommands();
                    case "students" -> simpleCommands.printTable(con, "SELECT * FROM Students");
                    case "teachers" -> simpleCommands.printTable(con, "SELECT * FROM Teachers");
                    case "courses" -> simpleCommands.printTable(con, "SELECT * FROM Courses");
                    case "grades" -> simpleCommands.printTable(con, "SELECT * FROM Grades");
                    case "sqlquery" -> simpleCommands.proceedQuery(con);
                    case "update" -> {
                        {
                            Update update = new Update(con);
                            String table = update.chooseTable();
                            if (update.getRun()) {
                                String[] cols = update.getCols(table, true);
                                if (update.getRun()) {
                                    String[] cols2 = update.chooseCols(cols);
                                    if (update.getRun()) {
                                        String newValues = update.getNewValues(cols2);
                                        if (update.getRun()) {
                                            newValues = update.createChanges(newValues, cols2);
                                            if (update.getRun()) update.makeUpdate(newValues, table);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    case "insert" -> {
                        {
                            Insert insert = new Insert(con);
                            String table = insert.chooseTable();
                            if (insert.getRun()) {
                                String[] cols = insert.getCols(table, true);
                                if (insert.getRun()) {
                                    String[] cols2 = insert.chooseCols(cols);
                                    if (insert.getRun()) insert.makeInsert(cols2, table);
                                }
                            }
                        }
                    }
                    case "delete" -> {
                        Delete delete = new Delete(con);
                        String table = delete.chooseTable();
                        if (delete.getRun()) delete.makeDelete(table);
                    }
                    case "report" -> simpleCommands.createReport(con);
                    case "exit" -> exit = false;
                    default -> System.out.println("It is not recognized command,\ntype ls to view avalable commands.");
                }

            }while(exit);

            assert con != null;
            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //gets data needed to connect with database
    static String[] connectionVars() {
        Scanner s = new Scanner(System.in);
        String[] conn = new String[3];
        String s1;

        System.out.println("Type in port to conncet (for example SQLSERVER:1434): ");
        s1 = s.nextLine();
        if(s1.equalsIgnoreCase("exit")) {
            return null;
        } else{
            conn[0] = "jdbc:sqlserver://localhost\\" + s1 + ";database=University";
        }

        System.out.println("Type in MS SQL SERVER username: ");
        s1 = s.nextLine();
        if(s1.equalsIgnoreCase("exit")) {
            return null;
        } else{
            conn[1] = s1;
        }
        System.out.println("Type in MS SQL SERVER password: ");
        s1 = s.nextLine();
        if(s1.equalsIgnoreCase("exit")) {
            return null;
        } else{
            conn[2] = s1;
        }
        for(int i = 0; i< 50; i++) System.out.println("\n");
        return conn;
    }
}