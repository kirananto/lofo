package com.razor.lofo;

/**
 * Created by razorSharp on 12/28/2016.
 *
 *      This is a class to hold a universal String called category
 */

class Global {
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
