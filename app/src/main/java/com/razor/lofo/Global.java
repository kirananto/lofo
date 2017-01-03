package com.razor.lofo;

/**
 * Created by kiran on 12/28/2016.
 */

public class Global {
    private static String Category = "General";

    public Global()
    {
        Category = "General";
    }
    public static String getCategory() {
        return Category;
    }

    public static void setCategory(String category) {
        Category = category;
    }
}
