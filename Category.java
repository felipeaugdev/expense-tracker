public enum Category {

    FOOD("Food"),
    TRANSPORTATION("Transportation"),
    UTILITIES("Utilities"),
    HOUSING("Housing"),
    ENTERTAINMENT("Entertainment"),
    HEALTHCARE("Healthcare"),
    SHOPPING("Shopping"),
    OTHER("Other");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

}
