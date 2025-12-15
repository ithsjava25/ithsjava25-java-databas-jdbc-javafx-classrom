package repositories;

public interface MoonMissionInter {
    /**
     * Display all information from chosen column, if * then show all columns
     * @param columnName the column that you would like to display
     */
    void displayColumn(String columnName);

    /**
     * Retrieves all information about a mission when you enter an ID
     * @param missionID unique for each mission
     */
    void getMissionFromID(int missionID);

    /**
     * Counts how many missions have been conducted in a year
     * @param year the chosen year
     * Prints number of missions even if they were 0
     */
    void allMissionsConductedInYear(String year);

}
