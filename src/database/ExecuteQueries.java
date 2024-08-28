package src.database;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExecuteQueries {

    public static String CURRENT_DB = "";
    private static String[] columns;
    private static List<String> transactionQueries = new ArrayList<>();
    public static boolean isStartTrancation = false;

    /**
     * Executes the user queries in a loop until the program is terminated.
     * Prompts the user for a command and passes it to the 'runQuery' method for
     * execution.
     * 
     * @param None
     * @return None
     */
    public static void execQuery() {
        while (true) {
            System.out.print("MY_DBMS>");
            Scanner sc = new Scanner(System.in);
            String inputCommand = sc.nextLine();
            runQuery(inputCommand);
        }
    }

    /**
     * Trims and converts the given query string to lowercase.
     *
     * @param queryString the query string to be purified
     * @return the purified query string
     */
    public static String purifyQueryString(String queryString) {
        return queryString.trim().toLowerCase();
    }

    /**
     * Creates a new database with the given name.
     * 
     * @param dbname the name of the database to be created
     * @return void
     */
    public static void execCreateDatabase(String dbname) {
        File directory = new File("./DB_Storage", dbname);
        if (directory.mkdir()) {
            System.out.println("Database created successfully!");
        } else {
            System.out.println("Database already exists!");
        }
    }

    /**
     * Sets the current database to the specified database name.
     * 
     * @param dbname the name of the database to be used
     * @return void
     */
    public static void useDatabase(String dbname) {
        String directoryPath = "./DB_Storage/" + dbname;
        System.out.println("using database '" + dbname + "'");
        // Create a File object representing the directory
        File directory = new File(directoryPath);

        // Check if the directory exists
        if (directory.exists() && directory.isDirectory()) {
            CURRENT_DB = dbname;

        } else {
            System.out.println("Database '" + dbname + "' does not exist");
        }
    }

    /**
     * Checks if the current database is empty.
     * 
     * @return true if no database is selected, false otherwise
     */
    public static boolean checkIfCurrentDbIsEmpty() {
        if (CURRENT_DB.isEmpty()) {
            System.out.println("No database is selected");
            return true;
        } else {
            return false;
        }
    }

    /**
     * Creates a new table in the current database with the given table name and
     * column names.
     * 
     * @param tableName the name of the table to be created
     * @param command   the command containing the column names in the format
     *                  "col_name, col_name"
     * @throws IllegalArgumentException if the current database is empty
     * @throws IOException              if there is an error creating the table file
     */
    public static void createTable(String tableName, String command) {
        boolean isEmpty = checkIfCurrentDbIsEmpty();
        if (!isEmpty) {
            int startingIndex = command.indexOf("(");
            int endingIndex = command.indexOf(")");
            if (startingIndex == -1 || endingIndex == -1 || startingIndex > endingIndex) {
                System.out.println("Invalid Syntax! Please use create table table_name (col_name, col_name) ");
                return;
            }
            String getAttributesNamesStr = command.substring(startingIndex + 1, endingIndex).trim();
            columns = getAttributesNamesStr.split(",\\s*");
            Path tablePath = Paths.get("./DB_Storage", CURRENT_DB, tableName + ".txt");
            if (Files.exists(tablePath)) {
                System.out.println("Table '" + tableName + "' already exists.");
            } else {
                try {
                    Files.createFile(tablePath);
                    FileWriter writer = new FileWriter(tablePath.toFile());
                    writer.write(getAttributesNamesStr);
                    writer.close();
                    System.out.println("Table '" + tableName + "' created with the schema: " + getAttributesNamesStr);
                } catch (IOException e) {
                    System.err.println("Error creating the table: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Reads the first line from the specified file and stores the column names in
     * the 'columns' array.
     * If the file is empty, it prints a message indicating that the file is empty.
     *
     * @param filePath the path of the file to read
     */
    public static void loadFirstLineFromTable(Path filePath) {

        try (BufferedReader br = new BufferedReader(new FileReader(filePath.toString()))) {
            String firstLine = br.readLine(); // Read the first line
            if (firstLine != null) {
                columns = firstLine.split(",\\s*");
            } else {
                System.out.println("The file is empty."); // Handle empty file case
            }
        } catch (IOException e) {
            System.err.println("Table not present " + e.getMessage());
        }
    }

    /**
     * Inserts a new row of values into the specified table.
     * 
     * @param tableName the name of the table to insert the values into
     * @param value     the values to be inserted, separated by commas
     * @throws IllegalArgumentException if the specified table does not exist or if
     *                                  the number of values does not match the
     *                                  number of columns in the table
     * @throws IOException              if an error occurs while writing to the
     *                                  table file
     */
    public static void insertIntoTable(String tableName, String value) {
        boolean isEmpty = checkIfCurrentDbIsEmpty();
        if (!isEmpty) {
            Path tablePath = Paths.get("./DB_Storage", CURRENT_DB, tableName + ".txt");
            loadFirstLineFromTable(tablePath);
            if (!Files.exists(tablePath)) {
                System.out.println("Table " + tableName + " does not exist. Please create table first.");
            } else {
                try {
                    String[] inpValues = value.split(",\\s*");
                    if (columns.length != inpValues.length) {
                        System.out.println("Enter all the values according to column name");
                        return;
                    }
                    FileWriter writer = new FileWriter(tablePath.toFile(), true);
                    writer.write("\n" + value);
                    writer.close();
                    System.out.println("Values inserted successfully");
                } catch (Exception e) {
                    System.out.println("Error in inserting data into " + tableName + e.getMessage());
                }
            }
        }
    }

    /**
     * Returns the third word from the given query string.
     * 
     * @param queryStr the query string from which to extract the third word
     * @param type     the type of word to extract (e.g., table name)
     * @return the third word from the query string
     */
    public static String returnThirdWord(String queryStr, String type) {
        String[] wordsArr = queryStr.split("\\s+");
        String thirdWord = "";
        if (wordsArr.length >= 3) {
            // Get the third word
            thirdWord = wordsArr[2];
        } else {
            System.out.println("please enter " + type + " name");
        }
        return thirdWord;
    }

    /**
     * Returns the second word from the given query string.
     * 
     * @param queryStr the query string from which to extract the second word
     * @param type     the type of the word to be extracted (e.g., "table",
     *                 "column")
     * @return the second word from the query string
     * @throws NullPointerException if the query string is null
     */
    public static String returnSecondWord(String queryStr, String type) {
        String[] wordsArr = queryStr.split("\\s+");
        String secWord = "";
        if (wordsArr.length >= 2) {
            // Get the third word
            secWord = wordsArr[1];
        } else {
            System.out.println("please enter " + type + " name");
        }
        return secWord;
    }

    /**
     * Prints the header of a table with the given column names.
     *
     * @param columns an array of column names
     */
    private static void printHeader(String[] columns) {
        printHorizontalLine(columns.length);
        for (String column : columns) {
            System.out.printf("| %-20s ", column); // Adjust width as needed
        }
        System.out.println("|");
        printHorizontalLine(columns.length);
    }

    /**
     * Prints a data row with the given columns.
     *
     * @param columns the array of columns to be printed
     */
    private static void printRow(String[] columns) {
        printHorizontalLine(columns.length);
        for (String column : columns) {
            System.out.printf("| %-20s ", column); // Adjust width as needed
        }
        System.out.println("|");
    }

    /**
     * Prints a horizontal line consisting of '+' and '-' characters.
     * The number of '-' characters in the line is determined by the given
     * numColumns parameter.
     * Each '-' character is followed by a space character.
     * The line is enclosed by '+' characters at the beginning and end.
     * 
     * @param numColumns the number of columns in the line
     */
    private static void printHorizontalLine(int numColumns) {
        System.out.print("+");
        for (int i = 0; i < numColumns; i++) {
            for (int j = 0; j < 20 + 2; j++) {
                System.out.print("-");
            }
            System.out.print("+");
        }
        System.out.println();
    }

    /**
     * Retrieves and prints all the data from the specified table in the current
     * database.
     * 
     * @param tableName the name of the table to retrieve data from
     */
    public static void selectEverythingFromTable(String tableName) {
        boolean isDbEmpty = checkIfCurrentDbIsEmpty();
        if (!isDbEmpty) {
            Path tablePath = Paths.get("./DB_Storage", CURRENT_DB, tableName + ".txt");
            if (!Files.exists(tablePath)) {
                System.out.println("Table '" + tableName + "' does not exist in the current database.");
                return;
            }
            try (BufferedReader br = new BufferedReader(new FileReader(tablePath.toString()))) {
                String line;
                int rowCount = 0;
                while ((line = br.readLine()) != null) {
                    String[] columns = line.split(",");
                    if (rowCount == 0) {
                        // Print header for the first row
                        printHeader(columns);
                    } else {
                        // Print row data
                        printRow(columns);
                    }
                    rowCount++;
                }
            } catch (IOException e) {
                System.err.println("Table not present " + e.getMessage());
            }
        }
    }

    /**
     * Returns the index of the specified column name in the table schema.
     *
     * @param columnName the name of the column to find the index for
     * @param tablePath  the path to the table file
     * @return the index of the column, or -1 if the column is not found
     * @throws IOException if an error occurs while reading the file
     */
    public static int getColumnIndex(String columnName, Path tablePath) {
        int indexMatches = -1;
        try (BufferedReader br = new BufferedReader(new FileReader(tablePath.toString()))) {
            String firstLine = br.readLine(); // Read the first line
            if (firstLine != null) {
                String[] totalColumns = firstLine.trim().split(",\\s*");
                for (int i = 0; i < totalColumns.length; i++) {
                    if (totalColumns[i].trim().equals(columnName.trim())) {
                        indexMatches = i;
                    }
                }
            } else {
                System.out.println("The file is empty.");
            }
        } catch (IOException e) {
            System.err.println("Table not present " + e.getMessage());
        }
        return indexMatches;
    }

    /**
     * Evaluates the where clause of a SQL query by comparing the values of a
     * specific column with a given column value.
     * 
     * @param values        the array of values representing a row in the table
     * @param columnName    the name of the column to be evaluated
     * @param columnValue   the value to compare with the column values
     * @param schemaColumns the array of column names in the table schema
     * @return true if the column value matches the given value, false otherwise
     */
    private static boolean evaluateWhereClause(String[] values, String columnName, String columnValue,
            String[] schemaColumns) {
        Map<String, Integer> columnIndexMap = new HashMap<>();
        for (int j = 0; j < schemaColumns.length; j++) {
            columnIndexMap.put(schemaColumns[j], j);
        }
        if (columnIndexMap.containsKey(columnName)) {
            int index = columnIndexMap.get(columnName);
            return values[index].trim().equals(columnValue);
        }
        return false;
    }

    /**
     * Updates the specified table with the given setClause and whereClause.
     * 
     * @param tableName   the name of the table to update
     * @param setClause   the set clause specifying the column to update and its new
     *                    value
     * @param whereClause the where clause specifying the condition for updating
     *                    rows
     * 
     * @throws IOException if there is an error reading or writing the table data
     */
    public static void updateTable(String tableName, String setClause, String whereClause) {
        Path tablePath = Paths.get("./DB_Storage", CURRENT_DB, tableName + ".txt");
        try {
            List<String> tableData = Files.readAllLines(tablePath);

            if (tableData.size() > 1) {
                String[] schemaColumns = tableData.get(0).split(",\\s*");

                // Extract the column name and new value from the setClause
                String[] setParts = setClause.split("=");
                String setColumnName = setParts[0].trim();
                String setColumnValue = setParts[1].trim();

                // Extract the column name and value for the WHERE condition
                String[] whereParts = whereClause.split("=");
                String whereColumnName = whereParts[0].trim();
                String whereColumnValue = whereParts[1].trim();

                for (int i = 1; i < tableData.size(); i++) {
                    String[] values = tableData.get(i).split(",\\s*");
                    // Check if the current row matches the WHERE condition
                    if (evaluateWhereClause(values, whereColumnName, whereColumnValue, schemaColumns)) {
                        // Update the value of the specified column
                        Map<String, Integer> columnIndexMap = new HashMap<>();
                        for (int j = 0; j < schemaColumns.length; j++) {
                            columnIndexMap.put(schemaColumns[j], j);
                        }
                        if (columnIndexMap.containsKey(setColumnName)) {
                            int index = columnIndexMap.get(setColumnName);
                            values[index] = setColumnValue;
                        }
                        tableData.set(i, String.join(", ", values));
                    }
                }
                Files.write(tablePath, tableData);

                System.out.println("Updated rows in table '" + tableName + "'.");
            } else {
                System.out.println("The table is empty.");
            }
        } catch (IOException e) {
            System.err.println("Error updating data in the table: " + e.getMessage());
        }
    }

    /**
     * Executes a query with a WHERE clause on a specified table.
     *
     * @param selectQueryHavingWhere the select query with the WHERE clause
     * @param tableName              the name of the table to query
     */
    public static void whereClause(String selectQueryHavingWhere, String tableName) {
        Path tablePath = Paths.get("./DB_Storage", CURRENT_DB, tableName + ".txt");

        boolean isWhereQuery = selectQueryHavingWhere.contains("where");
        if (isWhereQuery) {
            String whereSubString = selectQueryHavingWhere.substring(selectQueryHavingWhere.indexOf("where"));
            String[] tempColumns = whereSubString.split(" ");

            try (BufferedReader br = new BufferedReader(new FileReader(tablePath.toString()))) {
                String line;
                boolean found = false;
                int rowCount = 0;
                // Read and process each line of the file
                while ((line = br.readLine()) != null) {
                    String[] columns = line.split(",");
                    if (rowCount == 0) {
                        // Print header for the first row
                        printHeader(columns);
                    }
                    rowCount++;
                    if (columns.length >= 2 && columns[getColumnIndex(tempColumns[1].trim(), tablePath)].trim()
                            .equals(tempColumns[3])) {
                        // Print the entire row if the condition is met
                        printRow(columns);
                        found = true;
                    }
                }

                if (!found) {
                    System.out.println("No matching record found.");
                }
            } catch (IOException e) {
                System.err.println("Table not present " + e.getMessage());
            }
        }
    }

    /**
     * Drops a table from the current database.
     *
     * @param tableName the name of the table to be dropped
     *
     * @throws NullPointerException if the tableName is null
     */
    public static void dropTable(String tableName) {
        Path tablePath = Paths.get("./DB_Storage", CURRENT_DB, tableName + ".txt");
        File file = new File(tablePath.toString());
        boolean isDbEmpty = checkIfCurrentDbIsEmpty();
        if (!isDbEmpty) {
            if (file.exists()) {
                // Attempt to delete the file
                if (file.delete()) {
                    System.out.println("Table '" + tableName + "' dropped successfully.");
                } else {
                    System.err.println("Failed to drop table '" + tableName + "'.");
                }
            } else {
                System.err.println("Table '" + tableName + "' does not exist.");
            }
        } else {
            System.out.println("Please select databse");
        }
    }

    /**
     * Starts a transaction in the current database.
     * 
     * This method checks if the current database is empty. If it is not empty, it
     * starts a transaction by setting the
     * 'isStartTrancation' flag to true. The transaction can then be used to perform
     * multiple queries as a single unit of work.
     * 
     * @throws None
     * @return None
     */
    public static void startTransaction() {
        boolean isDbEmpty = checkIfCurrentDbIsEmpty();
        if (!isDbEmpty) {
            System.out.println("Transaction started");
            isStartTrancation = true;
        }
    }

    /**
     * Commits the current transaction by executing all the queries in the
     * transactionQueries list.
     * Clears the transactionQueries list after executing the queries.
     */
    public static void commitTransaction() {
        isStartTrancation = false;
        System.out.println("Committing Transaction...");
        for (String str : transactionQueries) {
            runQuery(str);
        }
        transactionQueries.clear();
    }

    /**
     * Rolls back the current transaction by clearing the transaction queries and
     * setting the 'isStartTransaction' flag to false.
     * This method is used to undo any changes made during the current transaction.
     */
    public static void rollbackTransaction() {

        System.out.println("Rolling back transactions...");
        isStartTrancation = false;
        transactionQueries.clear();
    }

    /**
     * Stores the given query string in the transactionQueries buffer.
     * 
     * @param queryString the query string to be stored in the buffer
     */
    public static void storeQueryInBuffer(String queryString) {
        transactionQueries.add(queryString);
        for (String str : transactionQueries) {
            System.out.println(str + " added into buffer.");
        }
    }

    /**
     * Extracts the set clause from the given command.
     * 
     * @param command the command from which to extract the set clause
     * @return the set clause extracted from the command, or null if no set clause
     *         is found
     */
    private static String extractSetClause(String command) {
        Pattern pattern = Pattern.compile("set\\s+(.+)\\s+where", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(command);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * Extracts the WHERE clause from the given command.
     *
     * @param command the command from which to extract the WHERE clause
     * @return the extracted WHERE clause, or null if no WHERE clause is found
     */
    private static String extractWhereClause(String command) {
        Pattern pattern = Pattern.compile("where\\s+(.+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(command);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * Logs the query with the current time to a log file.
     * 
     * @param queString   the query string to be logged
     * @param currentTime the current time in string format
     * @throws IOException if there is an error logging the query
     */
    public static void logQueryWithTime(String queString, String currentTime) {
        try {
            Path logPath = Paths.get("./DB_Storage", "log.txt");

            if (!Files.exists(logPath)) {
                Files.createFile(logPath);
            }

            Files.write(logPath, ("[Query] " + queString + " at " + currentTime + "\n").getBytes(),
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Error logging the query: " + e.getMessage());
        }
    }

    /**
     * Executes the given query string.
     *
     * @param queryStr the query string to be executed
     * @return true if the query was executed successfully, false otherwise
     */
    public static boolean runQuery(String queryStr) {
        LocalDateTime now = java.time.LocalDateTime.now();
        logQueryWithTime(queryStr, now.toString());
        if (purifyQueryString(queryStr).startsWith("create database") & !isStartTrancation) {
            String thirdWord = returnThirdWord(queryStr, "database");
            if (!Objects.equals(thirdWord, "")) {
                execCreateDatabase(thirdWord);
            }
        } else if (purifyQueryString(queryStr).startsWith("use") & !isStartTrancation) {
            String secWord = returnSecondWord(queryStr, "database");
            if (!Objects.equals(secWord, "")) {
                useDatabase(secWord);
            }
        } else if (purifyQueryString(queryStr).startsWith("create table")) {
            if (isStartTrancation) {
                storeQueryInBuffer(queryStr);
            } else {
                String tableName = returnThirdWord(queryStr, "table");
                if (!Objects.equals(tableName, "")) {
                    String command = queryStr.substring(queryStr.indexOf("("), queryStr.length());
                    createTable(tableName, command);
                }
            }
        } else if (purifyQueryString(queryStr).startsWith("insert into")) {
            if (isStartTrancation) {
                storeQueryInBuffer(queryStr);
            } else {
                String tableName = returnThirdWord(queryStr, "table");
                String values = queryStr.substring(queryStr.indexOf("values(") + 7, queryStr.length() - 1);
                insertIntoTable(tableName, values);
            }
        } else if (purifyQueryString(queryStr).startsWith("select * from")) {
            if (isStartTrancation) {
                storeQueryInBuffer(queryStr);
            } else {

                String tableName = queryStr.split("\\s+")[3];
                System.out.println("     " + tableName);
                if (queryStr.contains("where")) {
                    whereClause(queryStr, tableName);
                } else {
                    selectEverythingFromTable(tableName);
                }
            }
        } else if (purifyQueryString(queryStr).startsWith("drop table")) {
            if (isStartTrancation) {
                storeQueryInBuffer(queryStr);
            } else {
                String tableName = returnThirdWord(queryStr, "table");
                dropTable(tableName);
            }
        } else if (purifyQueryString(queryStr).startsWith("update table") & !isStartTrancation) {
            String setClause = extractSetClause(queryStr);
            String whereClause = extractWhereClause(queryStr);
            String tableName = returnThirdWord(queryStr, "table");
            updateTable(tableName, setClause, whereClause);
        } else if (purifyQueryString(queryStr).startsWith("start transaction")) {
            if (isStartTrancation) {
                System.out.println("Transaction already started");
            } else {
                startTransaction();
            }
        } else if (purifyQueryString(queryStr).startsWith("commit")) {
            commitTransaction();
        } else if (purifyQueryString(queryStr).startsWith("rollback")) {
            rollbackTransaction();
        } else if (queryStr.equals("exit")) {
            System.out.println("Exiting MY_DBMS.");
            return false;
        } else {
            System.out.println(
                    "Invalid Query.. Please enter 'create database' or 'use database' or 'create table' or 'insert into' or 'select' or 'update table' or 'drop table' or 'start transaction' or 'commit' or 'rollback'");
        }
        return true;
    }
}
