package recommender.model;

/**
 * Class for reviews records from database.
 */
public class Review {
    private final int positive;
    private final double weight;

    /**
     * Constructor.
     *
     * @param positive review sentiment
     * @param weight   review weight
     */
    public Review(int positive, double weight) {
        this.positive = positive;
        this.weight = weight;
    }

    /**
     * Getter for positive
     *
     * @return positive
     */
    public int getPositive() {
        return positive;
    }

    /**
     * Getter for weight
     *
     * @return weight
     */
    public double getWeight() {
        return weight;
    }
}
