# Definitions 

[FeatureBuilderRunner](./runner/FeatureBuilderRunner.java)

A class that implements the ability to take a series of feature builders and process them in the order provided.

[FeatureBuilder](./builders/common/FeatureBuilder.java)

An abstract base class for all Feature Builders, supplying the common method to accept a RaptatDocument, then iterate through each token to run the logic of the derived Builder.

[FeatureBuilderState](./builders/common/FeatureBuilderState.java)

A class that provides state management for a feature builder, maintaining and updating the FeatureData with using RaptatTokenPhrase and optionally, a Label and feature string for a given entry from PhraseListType.

[PhraseListType](./builders/common/PhraseListType.java)

An enum to help indicate Row, Column, or Document (all lines as one row of text) for scanning for features.

[FeatureData](./builders/common/FeatureData.java)

A class to hold current data about a feature, including the text, its ordinal reference in a document, the starting line index, and it's current line index.

# What is a FeatureBuider

The primary function of a feature builder is assign a feature (string) to a phrase (RaptatTokenPhrase) for use in machine learning.  In the current use case a series of feature builders are run against lists of phrases from a text, which have previously been labeled with concepts of interest (e.g., IndexType, Anatomy, Laterality).  Each FeatureBuilder in turn emits a string on a given phrase based on the current label and the current state of the FeatureBuilder.  In this manner phrases of interest (in this case actual ABI or TBI values, such as 1.23) will have a series of features genreated to describe the relationships this value has to other elements of the document for use in machine learning.

# Discussion on how to make a FeatureBuilder

How a FeatureBuilder, as consumed by a FeatureBuilderRunner, is designed depends primarily on what relationship it is attempting to create.  If the intent is to simple track a concept as asserted by a label (see [Using the Finite State Machine Code for Labeling](./trunk/src/main/gov/va/vha09/grecc/raptat/dw/textanalysis/fsm_pattern_detection/docs/HowToBuildAnFSM.md)), it can be as simple as deriving from FeatureBuilder setting the label string to check in the overrided method provided by the base class.

```

public class AnatomyFeatureBuilder extends FeatureBuilder
{

	@Override
	protected String getLabelToCheck()
	{
		return "Anatomy";
	}

}

```

In [AnatomyFeatureBuilder](./builders/anatomy/AnatomyFeatureBuilder.java), the getLabelToCheck method is overridden to return the string "Anatomy", which corresponds to the string found in the method _get_fsm_name()_ in the corresponding configuration/script groovy file [Anatomy.groovy](./config/pattern_config/grouped/Anatomy.groovy)

__Note:__ Additional work is need here to more closely tie the use of the groovy definition file to that within a feature builder.

A FeatureBuilder may be more complex, however.  You can extend the base class to use new FeatureStateBuilder in lieu of the default or construct a new means by which a phrase feature string is generated, in current design.  An example is the [IndexTypeTokensDistanceFeatureBuilder](./builders/index_type/IndexTypeTokensDistanceFeatureBuilder.java), generates features text on phrases indicating row distance to the nearest index type (e.g., ABI).

```
public class IndexTypeTokensDistanceFeatureBuilder extends IndexTypeFeatureBuilder
{
	public IndexTypeTokensDistanceFeatureBuilder()
	{
		super( new FeatureBuilderState( x -> x.getFeatureWithIndex() ) );
	}


	@Override
	protected String generatePhraseFeature(Label foundLabel, RunDirection direction)
	{
		return super.generatePhraseFeature( foundLabel, direction ) + "_TOKEN_DISTANCE";
	}
}
```

In this example a new FeatureBuilderState is instantiated with a lambda function (see [FnFeatureTextCall](./builders/common/FeatureBuilderState.java) for method signature) that calls _getFeatureWithIndex()_ to provide a string in the format <feature_string>_<position_index>.  Likewise, a new phrase feature is generated, based on the super class's definition and appended with a specific suffix to denote the token distance.

Any development along these lines may involve updates to/subclassing of the [FeatureData](./builders/common/FeatureData.java) class for new state information to be used in conjunction with how data needs to be fed into a new [FeatureBuilderState](./builders/common/FeatureBuilderState.java) object.









































