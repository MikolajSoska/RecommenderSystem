package recommender.model;

/**
 * Class for books records from database.
 */
public class Book {
    private final int bookID;
    private final String title;
    private final String description;
    private final String asin;
    private final int clusterIndex;

    /**
     * Constructor.
     *
     * @param bookID       ID number of book
     * @param title        book title
     * @param description  book description
     * @param asin         book asin attribute
     * @param clusterIndex book cluster index
     */
    public Book(int bookID, String title, String description, String asin, int clusterIndex) {
        this.bookID = bookID;
        this.title = title;
        this.description = description;
        this.asin = asin;
        this.clusterIndex = clusterIndex;
    }

    /**
     * Getter for bookID.
     *
     * @return bookID
     */
    public int getBookID() {
        return bookID;
    }

    /**
     * Getter for title.
     *
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Getter for description.
     *
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Getter for asin.
     *
     * @return asin
     */
    public String getAsin() {
        return asin;
    }

    /**
     * Getter for clusterIndex.
     *
     * @return clusterIndex
     */
    public int getClusterIndex() {
        return clusterIndex;
    }

    @Override
    public String toString() {
        return getTitle();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Book && this.getBookID() == ((Book) obj).getBookID();
    }
}
