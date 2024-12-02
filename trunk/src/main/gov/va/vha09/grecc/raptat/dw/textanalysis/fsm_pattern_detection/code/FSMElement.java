package src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.fsm_pattern_detection.code;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import com.google.common.base.Joiner;


/**
 * Abstract base class that defines basis for indexed set management for MatchActions,
 * MatchPatterns, and States. These indexes provide the values for the underlying state machine as
 * managed by FSM.java.
 *
 * @param <T> the generic type
 */
public abstract class FSMElement<T> {
  /** The states. */
  private Map<T, Integer> __map_element_to_index;


  /**
   * Instantiates a new FSM element.
   */
  protected FSMElement() {
    this.__map_element_to_index = new HashMap<T, Integer>();
  }


  /**
   * Instantiates a new FSM element.
   *
   * @param elements the elements
   */
  @SafeVarargs
  protected FSMElement(T... elements) {
    this();
    for (T element : elements) {
      this.add(element);
    }
  }


  /**
   * Adds the.
   *
   * @param element the element
   */
  public void add(T element) {

    this.__map_element_to_index.put(element, this.__map_element_to_index.size());

  }

  /**
   * Count.
   *
   * @return the int
   */
  public int count() {
    return this.__map_element_to_index.size();
  }

  /**
   * Gets the.
   *
   * @param index the index
   * @return the t
   */
  public T get(int index) {
    T found_item = null;
    for (T item : this.__map_element_to_index.keySet()) {
      if (this.__map_element_to_index.get(item) == index) {
        found_item = item;
        break;
      }
    }
    return found_item;
  }


  /**
   * Gets the elements.
   *
   * @return the elements
   */
  public Set<T> get_elements() {
    return this.__map_element_to_index.keySet();
  }

  /**
   * Gets the index.
   *
   * @param element the element
   * @return the index
   * @throws FSMElementException the FSM element exception
   */
  public int get_index(T element) throws FSMElementException {
    if (this.__map_element_to_index.containsKey(element) == false) {
      throw new FSMElementException("FSM element " + element + " does not exist.");
    }
    return this.__map_element_to_index.get(element);
  }

  @Override
  public String toString() {
    Set<T> key_set = this.__map_element_to_index.keySet();

    String result = Joiner.on(", ").join(key_set);

    return result;
  }



}
