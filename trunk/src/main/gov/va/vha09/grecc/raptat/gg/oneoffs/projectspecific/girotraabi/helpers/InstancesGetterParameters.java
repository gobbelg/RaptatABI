package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.helpers;


import java.util.List;
import java.util.Optional;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.iterators.IterableData;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.GirotraABI;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.attributefilters.AttributeFilter;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.TextAnalyzer;

//@formatter:off
public record InstancesGetterParameters(
    GirotraABI girotraAbiTool,
    IterableData iterableData,
    TextAnalyzer textAnalyzer,
    Optional<String> lateralityDirectory,
    Optional<String> indexTypeDirectory,
    Optional<String> arffOutputPath,
    Optional<List<AttributeFilter>> attributeFilters,
    Optional<String> logTokenPhrasesDirectory) {
}
//@formatter:on
