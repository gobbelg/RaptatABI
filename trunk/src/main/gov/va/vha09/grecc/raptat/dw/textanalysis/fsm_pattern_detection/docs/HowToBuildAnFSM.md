# Introduction

The intent of this set of class was to define a mechanism whereby a user could define a FSM through through configuration.  The following will go through a brief use-case of leveraging this mechanism to 

 1. Configure the definition for a FSM through a .groovy class,
 2. Implement the configuration logic, and
 3. Execute the FSM and the associated match actions based on the input and configured FSM.
 
# Definitions

[FSMConfigurator](../code/FSMConfigurator.java)

This class provides the means by which a finite state machine (FSM) can be built using a .groovy configuration file.  Instantiates _FSMDefinition_ with values built out from the configuration file.  

[FSMDefinition](../code/FSMDefinition.java)

This class extends the functionality of the _src.main.gov.va.vha09.grecc.raptat.gg.helpers.FSM.java_ by providing the means to build out the underlying int\[\]\[\]\[\] map from and FSM _name_, an enumeration of _states_, a collection of named _matching patterns_ (input to FSM), and a series of named _match actions_ (FSM emissions). Leverages functionality from the underlying _FSM.java_ class to manage state transitions. 

[FSMFeatureLabeler](../code/FSMFeatureLabeler.java) 

Manages multiple instances of _FSMDefinition_ and permits invocation on each token via the method _identify_.

[FSMElement](../code/FSMElement.java) 

Abstract base class that defines basis for indexed set management for _MatchActions_, _MatchPatterns_, and _States_.  These indexes provide the values for the underlying state machine as managed by _FSM.java_.

[IMatchAction](../code/IMatchAction.java) 

Base interface for defining a lambda or function expression to be used as an emission action by the FSM.

[MatchPattern](../code/MatchPattern.java) 

Provides the means to define a series of regular expressions which may be tested against a token for matching.  

[State](../code/State.java) 

A class which encapsulates the text name for a state as defined via configuration.

# Use Case

An example text file will be used to motivate the discussion of the use of the tool, found [here](example_text.txt).  Individual sections will be directly referenced as the example progresses.
 
# Configuration

All configuration will derive from _FSMDefinitionBase.groovy_, an abstract class defining the needed methods that will need to be overwritten.  When called from the .java code, these methods are pulled in via a type of reflection, which grants access to any configuration derived from this base class.

**Note:** The configuration for this approach has four required methods to override with one additional case that is a virtual method which can be overridden, and currently it controls the logic as to whether or not a RaptatToken should continue to receive an annotation, once the annotation is found (a two-state FSM).  This example will focus on the other four methods.

```
public String get_fsm_name()
```
Provides the means to _name_ a FSM.

```
protected void initialize_state_and_pattern_names()
```
Gives a protected method, called by the base class, to initialize member variables, such as state or match pattern names (these names, how they are created, and what they are for to be discussed later).

```
protected void set_match_map()
```
Provides the ability to add specific match patterns (regex expression array) to the map.

```
public String[][] get_fsm_definition_table()
```
Returns the user-defined array of state transitions, including whether or not an emission action is required.

Referring to the example text, this use case will construct a FSM to detect _laterality_ (either left or right).  Thus, the first task is to create a configuration file.

**Note:** The FSMConfigurator currently may accept either a reference to a file or a folder, using all the configuration files found in a folder in the latter case.  This example will reference a single file.  An example of using a folder can be found under the test class _TDD\_Feature\_Finder\_FSM_, method _TDD\_Config\_From\_Groovy_.

Define a configuration class as shown below, such that it reflects all the required methods to overwrite.

```
class LateralityFSM extends FSMDefinitionBase {

	@Override
	public String get_fsm_name() {
		return null;
	}

	@Override
	protected void initialize_state_and_pattern_names() {
		
	}

	@Override
	protected void set_match_map() {
		
	}

	@Override
	public String[][] get_fsm_definition_table() {
		return null;
	}
}
```

Next, give the FSM a name.  This gives code consuming this FSM the ability to distinguish it by a unique string.

```
@Override
public String get_fsm_name() {
	return "Laterality";
}
```
The next step is defining the state and pattern names.  This step is intended to limit errors in using strings in defining the different mappings for states and patterns, as repetition of a variable is constrained by the compiler, and consistency in strings is not.  

**Note:** There exists a **START** state for every FSM.  Likewise, there exists a default **NO_MATCH** input to the FSM when the specified match patterns find nothing.  Both are automatically included as part of deriving the class.

```
class LateralityFSM extends FSMDefinitionBase {

	String LEFT;
	String RIGHT;
	String LEFT_PATTERN;
	String RIGHT_PATTERN;
	
	...
	
```

These define variables at the object scope for holding the different names of states and the dictionary keys for the patterns.  _initialize\_state\_and\_pattern\_names()_ must be updated to then initialize them with string.

**Note:** What these variables are names is not important to the internal logic, but it is important for readability in logging or run-time review of code.

```
@Override
protected void initialize_state_and_pattern_names() {
	LEFT = "Left";
	RIGHT = "Right";
	LEFT_PATTERN = "LEFT";
	RIGHT_PATTERN = "RIGHT";
}
```

Next, the match pattern map needs to be defined.  This is a dictionary lookup that permits definition of a series of regular expression to be associated with the pattern name (e.g., LEFT\_PATTERN or "LEFT") for later use in defining the FSM (see get\_fsm\_definition\_table).

```
@Override
protected void set_match_map() {
	map[LEFT_PATTERN] = [
		'[Ll][Ee][Ff][Tt]-?',
		'[Ll](:|-|\\.|~|;|\\b|\\.:|\\)|p|d)'
	];

	map[RIGHT_PATTERN] = [
		'[Rr][Ii][Gg][Hh][Tt]-?',
		'[Rr](:|-|\\.|~|;|\\b|\\)|:|p|d)'
	];
}
```
In this case two patterns are defined, one for LEFT\_PATTERN and one for RIGHT\_PATTERN.  These are manually assigned to _map_, and then an array is built up for each with patterns for which to search.

**Note:** As with Java the backslash (\\) must be escaped with an additional one.

Finally, the definition of the FSM table is created.  In this case the FSM will model the following:

1) START goes to START, if NO_MATCH, then do no action 
2) START goes to RIGHT, if RIGHT_PATTERN matched, then do PERFORM_EMISSION_ACTION
3) START goes to LEFT, if LEFT_PATTERN matched, then do PERFORM_EMISSION_ACTION
4) LEFT goes to LEFT, if NO_MATCH, then do no action
5) LEFT goes to RIGHT, if RIGHT_PATTERN, then do PERFORM_EMISSION_ACTION
6) LEFT goes to LEFT, if LEFT_PATTERN, then do PERFORM_EMISSION_ACTION
7) RIGHT goes to RIGHT, if NO_MATCH, then do no action
8) RIGHT goes to RIGHT, if RIGHT_PATTERN, then do PERFORM_EMISSION_ACTION
9) RIGHT goes to LEFT, if LEFT_PATTERN, then do PERFORM_EMISSION_ACTION

This would create the following

```
@Override
public String[][] get_fsm_definition_table() {
	return [
		[START, NO_MATCH, START],
		[START, RIGHT_PATTERN, RIGHT, PERFORM_EMISSION_ACTION],
		[START, LEFT_PATTERN, LEFT, PERFORM_EMISSION_ACTION],
		[LEFT, NO_MATCH, LEFT],
		[LEFT, RIGHT_PATTERN, RIGHT, PERFORM_EMISSION_ACTION],
		[LEFT, LEFT_PATTERN, LEFT, PERFORM_EMISSION_ACTION],
		[RIGHT, NO_MATCH, RIGHT],
		[RIGHT, RIGHT_PATTERN, RIGHT, PERFORM_EMISSION_ACTION],
		[RIGHT, LEFT_PATTERN, LEFT, PERFORM_EMISSION_ACTION]
	];
}
```

**Note:**  At this time the PERFORM_EMISSION_ACTION is a lambda function that when called assigns the laterality label (Left or Right) to a RaptatToken.  This will later be expanded to permit multiple actions for inclusion to the FSM.

**Note:** If PERFORM_EMISSION_ACTION (or in future a different token for additional functions), then no action will be performed on this state transition.

This completes the configuration task.

# Implementation

This example references a test class, TDD\_Feature\_Finder\_FSM, and specifically the method, TDD\_Config\_From\_Groovy.  What follows is a review of this method, which should in turn provide enough detail to use this current implementation of the FSM mechanism.

Foremost, call the configuration class in order to pull in the .groovy configuration previously developed.

```
FSMConfigurator fsm_configurator = new FSMConfigurator();
```

Next, create the FSMDefinition class from the configuration.

```
FSMDefinition laterality_fsm = fsm_configurator.configure_specific_resource("<path_to_folder>\\LateralityFSM.groovy");
```

The next just pulls together a List of RaptatToken for testing.  Replace with output from RapTAT.

```
List<RaptatToken> raptat_token_list = __abi_tokens.stream().map( x -> (RaptatToken)x ).collect( Collectors.toList() );
```

Finally, the FSM is called over the list of tokens with identification of the tokens driving the state machine, labeling anything defined as "left" by the LEFT_PATTERN regex array as Left, likewise for Right, and everything else wherein no action was defined for the state transition as "None". 
 