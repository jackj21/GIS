package BE.DS.Index.FeatureName;

/**
 * Interface for Hash function intended to provide
 * indexing calculations for HashTable implementation.
 */
public interface Hashable<T> {
	
	// Returns an index for HashTable.
	public int Hash();
}
