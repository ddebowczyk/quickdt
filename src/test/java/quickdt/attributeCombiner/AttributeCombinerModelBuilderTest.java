package quickdt.attributeCombiner;

import com.google.common.base.Function;
import com.google.common.collect.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import quickdt.*;
import quickdt.calibratedPredictiveModel.PAVCalibratedPredictiveModelBuilder;
import quickdt.experiments.crossValidation.CrossValidator;
import quickdt.inspection.AttributeImportanceFinder;
import quickdt.inspection.AttributeScore;
import quickdt.randomForest.RandomForestBuilder;

import java.io.IOException;
import java.util.*;

/**
 * Created by ian on 3/29/14.
 */
public class AttributeCombinerModelBuilderTest {
    private static final  Logger logger =  LoggerFactory.getLogger(AttributeCombinerModelBuilderTest.class);

    @Test
    public void moboTest() throws IOException {
        final List<AbstractInstance> moboDataset = Benchmarks.loadMoboDataset();

        AttributeImportanceFinder attributeImportanceFinder = new AttributeImportanceFinder();

        logger.info("Determining attribute scores");
        final TreeSet<AttributeScore> attributeScores = attributeImportanceFinder.determineAttributeImportance(moboDataset);

        for (AttributeScore attributeScore : attributeScores) {
            logger.info(attributeScore.toString());
        }

        CrossValidator crossValidator = new CrossValidator(4, 3);
        double rfLoss = crossValidator.getCrossValidatedLoss(new RandomForestBuilder(), moboDataset);

        ArrayList<String> topAttributes = Lists.newArrayList(Iterables.limit(Iterables.transform(attributeScores.descendingSet(), new Function<AttributeScore, String>() {
            @Override
            public String apply(final AttributeScore attributeScore) {
                return attributeScore.getAttribute();
            }
        }), 8));

        logger.info("Top attributes: "+topAttributes);

        //create all distinct pairs
        Set<Set<String>> attributesToCombine = Sets.newHashSet();
        for (int first = 1; first < topAttributes.size(); first+=2) {
//            for (int second = 0; second < topAttributes.size(); second++) {
  //              if (first == second) continue;
                attributesToCombine.add(Sets.newHashSet(topAttributes.get(first), topAttributes.get(first - 1)));

        }

        logger.info("Creating "+attributesToCombine.size()+" new attributes");
        AttributeCombinerModelBuilder attributeCombinerModelBuilder = new AttributeCombinerModelBuilder(attributesToCombine);

        double acLoss = crossValidator.getCrossValidatedLoss(attributeCombinerModelBuilder, moboDataset);

        double treeLoss = crossValidator.getCrossValidatedLoss(new TreeBuilder(), moboDataset);

        logger.info("TreeLoss: "+treeLoss+", RFloss: "+rfLoss+", ACLoss: "+acLoss);
    }
}
