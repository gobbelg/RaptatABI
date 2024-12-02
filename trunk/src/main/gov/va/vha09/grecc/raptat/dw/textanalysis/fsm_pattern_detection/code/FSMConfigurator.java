package src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.fsm_pattern_detection.code;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.codehaus.groovy.control.CompilationFailedException;
import groovy.lang.GroovyClassLoader;
import src.main.gov.va.vha09.grecc.raptat.dw.textanalysis.base.FSMDefinitionBase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatToken;

/**
 * The means by which a finite state machine (FSM) can be built using a .groovy configuration file.
 * Instantiates _FSMDefinition_ with values built out from the configuration file.
 */
final public class FSMConfigurator {

  /**
   * Instantiates a new FSM configurator.
   */
  public FSMConfigurator() {
    // TODO Auto-generated constructor stub
  }


  /**
   * Configure.
   *
   * @param folder_path the folder path
   * @param comparator
   * @return the FSM definition[]
   * @throws CompilationFailedException the compilation failed exception
   * @throws InstantiationException the instantiation exception
   * @throws IllegalAccessException the illegal access exception
   * @throws NoSuchMethodException the no such method exception
   * @throws SecurityException the security exception
   * @throws IllegalArgumentException the illegal argument exception
   * @throws InvocationTargetException the invocation target exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws FSMElementException the FSM element exception
   */
  public FSMDefinition[] configure(String folder_path, Comparator<String> comparator)
      throws CompilationFailedException, InstantiationException, IllegalAccessException,
      NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException,
      IOException, FSMElementException {
    return this.configure(folder_path, x -> x.getTokenStringUnprocessed(), comparator);
  }


  public FSMDefinition[] configure(String folder_path, IRaptatTokenText raptat_string_eval_callback,
      Comparator<String> comparator) throws CompilationFailedException, InstantiationException,
      IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException,
      InvocationTargetException, IOException, FSMElementException {
    File folder = new File(folder_path);

    File[] listOfFiles = folder.listFiles();

    List<FSMDefinition> definitions = new ArrayList<FSMDefinition>();

    for (int i = 0; i < listOfFiles.length; i++) {
      File file = listOfFiles[i];
      if (file.isFile() && file.getName().endsWith(".groovy")) {
        definitions
            .add(this.configure_specific_resource(file, raptat_string_eval_callback, comparator));
      }
    }

    FSMDefinition[] rv = new FSMDefinition[definitions.size()];

    definitions.toArray(rv);

    return rv;
  }


  public FSMDefinition configure_resource(String path, IRaptatTokenText txt_call,
      Comparator<String> comparator) throws CompilationFailedException, InstantiationException,
      IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException,
      InvocationTargetException, IOException, FSMElementException {
    return this.configure_specific_resource(new File(path), txt_call, comparator);
  }


  /**
   * Configure specific resource.
   *
   * @param groovy_config_script the groovy config script
   * @param raptat_string_eval_callback
   * @param patternComparator
   * @return the FSM definition
   * @throws CompilationFailedException the compilation failed exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws InstantiationException the instantiation exception
   * @throws IllegalAccessException the illegal access exception
   * @throws NoSuchMethodException the no such method exception
   * @throws SecurityException the security exception
   * @throws IllegalArgumentException the illegal argument exception
   * @throws InvocationTargetException the invocation target exception
   * @throws FSMElementException the FSM element exception
   */
  public FSMDefinition configure_specific_resource(File groovy_config_script,
      IRaptatTokenText raptat_string_eval_callback, Comparator<String> patternComparator)
      throws CompilationFailedException, IOException, InstantiationException,
      IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException,
      InvocationTargetException, FSMElementException {

    Class<?> config_class = new GroovyClassLoader().parseClass(groovy_config_script);
    FSMDefinitionBase script_instance = (FSMDefinitionBase) config_class.newInstance();

    Method get_fsm_definition_table =
        config_class.getMethod("get_fsm_definition_table", new Class[] {});
    Method get_match_pattern =
        config_class.getMethod("get_match_pattern", new Class[] {String.class});
    Method should_reset_labeling_on_next_not_found =
        config_class.getMethod("should_reset_labeling_on_next_not_found", new Class[] {});
    Method get_start_state = config_class.getMethod("get_start_state", new Class[] {});
    Method get_fsm_name = config_class.getMethod("get_fsm_name", new Class[] {});

    String fsm_name = (String) get_fsm_name.invoke(script_instance, new Object[] {});

    String[][] rv = (String[][]) get_fsm_definition_table.invoke(script_instance, new Object[] {});

    Set<String> states_set = new HashSet<>();
    Set<String> pattern_inputs_set = new HashSet<>();

    get_state_and_pattern_strings(rv, states_set, pattern_inputs_set);

    States states = configure_states(states_set);

    MatchPatterns patterns = configure_patterns(script_instance, get_match_pattern,
        pattern_inputs_set, patternComparator);

    IMatchAction match_action = (RaptatToken t, String feature_type,
        String feature_value) -> ((RaptatTokenFSM) t).add_feature(feature_type, feature_value);
    MatchActions actions = this.configure_actions(match_action);

    State start_state = get_start_state(script_instance, get_start_state, states);

    FSMDefinition fsm = new FSMDefinition(fsm_name, states, patterns, actions, start_state,
        raptat_string_eval_callback);

    Boolean reset_labeling =
        (Boolean) should_reset_labeling_on_next_not_found.invoke(script_instance, new Object[] {});
    if (reset_labeling) {
      fsm.clear_if_not_found();
    }

    establish_finite_state_machine(rv, states, patterns, match_action, fsm);

    return fsm;

  }


  /**
   * Configure specific resource.
   *
   * @param groovy_config_script the groovy config script
   * @param comparator
   * @return the FSM definition
   * @throws CompilationFailedException the compilation failed exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws InstantiationException the instantiation exception
   * @throws IllegalAccessException the illegal access exception
   * @throws NoSuchMethodException the no such method exception
   * @throws SecurityException the security exception
   * @throws IllegalArgumentException the illegal argument exception
   * @throws InvocationTargetException the invocation target exception
   * @throws FSMElementException the FSM element exception
   */
  public FSMDefinition configure_specific_resource(String groovy_config_script,
      Comparator<String> comparator) throws CompilationFailedException, IOException,
      InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException,
      IllegalArgumentException, InvocationTargetException, FSMElementException {
    return this.configure_specific_resource(new File(groovy_config_script),
        x -> x.getTokenStringUnprocessed(), comparator);
  }


  /**
   * Configure actions.
   *
   * @return the match actions
   */
  private MatchActions configure_actions() {
    IMatchAction match_action = (RaptatToken t, String feature_type,
        String feature_value) -> ((RaptatTokenFSM) t).add_feature(feature_type, feature_value);
    return this.configure_actions(match_action);
  }


  /**
   * Configure actions.
   *
   * @param match_action the match action
   * @return the match actions
   */
  private MatchActions configure_actions(IMatchAction match_action) {
    MatchActions actions = new MatchActions(match_action);
    return actions;
  }


  /**
   * Configure patterns.
   *
   * @param script_instance the script instance
   * @param get_match_pattern the get match pattern
   * @param pattern_inputs_set the pattern inputs set
   * @return the match patterns
   * @throws IllegalAccessException the illegal access exception
   * @throws InvocationTargetException the invocation target exception
   */
  private MatchPatterns configure_patterns(FSMDefinitionBase script_instance,
      Method get_match_pattern, Set<String> pattern_inputs_set,
      Comparator<String> patternComparator)
      throws IllegalAccessException, InvocationTargetException {
    /**
     * Patterns
     */

    List<String> patterns_list = new ArrayList<>(pattern_inputs_set);
    MatchPattern[] match_patterns = new MatchPattern[patterns_list.size()];
    for (int index = 0; index < patterns_list.size(); index++) {
      String pattern_label = patterns_list.get(index);

      String[] regex_patterns =
          (String[]) get_match_pattern.invoke(script_instance, new Object[] {pattern_label});

      match_patterns[index] = new MatchPattern(pattern_label, regex_patterns, patternComparator);

    }

    MatchPatterns patterns = new MatchPatterns(match_patterns);
    return patterns;
  }


  /**
   * Configure states.
   *
   * @param states_set the states set
   * @return the states
   */
  private States configure_states(Set<String> states_set) {
    /**
     * States
     */
    List<String> states_list = new ArrayList<>(states_set);
    State[] state_array = new State[states_list.size()];
    for (int index = 0; index < states_list.size(); index++) {
      String string = states_list.get(index);
      state_array[index] = new State(string);
    }

    States states = new States(state_array);
    return states;
  }


  /**
   * Establish finite state machine.
   *
   * @param rv the rv
   * @param states the states
   * @param patterns the patterns
   * @param match_action the match action
   * @param fsm the fsm
   * @throws FSMElementException the FSM element exception
   */
  private void establish_finite_state_machine(String[][] rv, States states, MatchPatterns patterns,
      IMatchAction match_action, FSMDefinition fsm) throws FSMElementException {
    for (int row_index = 0; row_index < rv.length; row_index++) {
      String[] row = rv[row_index];
      String start_state_str = row[0];
      String pattern_input_str = row[1];
      String next_state_str = row[2];
      String emission_action_str = "false";
      if (row.length == 4) {
        // determine the emission action, which will in turn trigger the
        // addition of a match action.
        emission_action_str = row[3];
      }

      if (emission_action_str == "true") {
        fsm.define(states.get_state(start_state_str), patterns.get_matches(pattern_input_str),
            states.get_state(next_state_str), match_action);
      } else {
        fsm.define(states.get_state(start_state_str), patterns.get_matches(pattern_input_str),
            states.get_state(next_state_str));
      }

    }
  }


  /**
   * Gets the start state.
   *
   * @param script_instance the script instance
   * @param get_start_state the get start state
   * @param states the states
   * @return the start state
   * @throws IllegalAccessException the illegal access exception
   * @throws InvocationTargetException the invocation target exception
   */
  private State get_start_state(FSMDefinitionBase script_instance, Method get_start_state,
      States states) throws IllegalAccessException, InvocationTargetException {
    String start_state_str = (String) get_start_state.invoke(script_instance, new Object[] {});

    State start_state = states.get_state(start_state_str);
    return start_state;
  }


  /**
   * Gets the state and pattern strings.
   *
   * @param rv the rv
   * @param states_set the states set
   * @param pattern_inputs_set the pattern inputs set
   * @return the state and pattern strings
   */
  private void get_state_and_pattern_strings(String[][] rv, Set<String> states_set,
      Set<String> pattern_inputs_set) {
    for (int row_index = 0; row_index < rv.length; row_index++) {
      String[] row = rv[row_index];
      String start_state = row[0];
      String pattern_input = row[1];
      String next_state = row[2];
      String emission_action = "false";
      if (row.length == 4) {
        emission_action = row[3];
      }

      states_set.add(start_state);
      states_set.add(next_state);
      pattern_inputs_set.add(pattern_input);
    }
  }

}
