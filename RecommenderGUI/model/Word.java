package recommender.model;

/**
 * Class for words from words vectors from database.
 */
public class Word {
    private final int wordIndex;
    private final double wordValue;

    /**
     * Constructor.
     *
     * @param wordIndex word index
     * @param wordValue word value
     */
    public Word(int wordIndex, double wordValue) {
        this.wordIndex = wordIndex;
        this.wordValue = wordValue;
    }

    /**
     * Getter for wordIndex
     *
     * @return wordIndex
     */
    private int getWordIndex() {
        return wordIndex;
    }

    /**
     * Getter for wordValue
     *
     * @return wordValue
     */
    public double getWordValue() {
        return wordValue;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Word && this.getWordIndex() == ((Word) obj).getWordIndex();
    }
}
