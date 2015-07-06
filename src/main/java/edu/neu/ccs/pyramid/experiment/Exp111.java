package edu.neu.ccs.pyramid.experiment;

import edu.neu.ccs.pyramid.configuration.Config;
import edu.neu.ccs.pyramid.dataset.RegDataSet;
import edu.neu.ccs.pyramid.dataset.TRECFormat;
import edu.neu.ccs.pyramid.eval.MSE;
import edu.neu.ccs.pyramid.optimization.Optimizer;
import edu.neu.ccs.pyramid.regression.probabilistic_regression_tree.SoftRegStump;
import edu.neu.ccs.pyramid.regression.probabilistic_regression_tree.SoftRegStumpTrainer;
import edu.neu.ccs.pyramid.regression.probabilistic_regression_tree.Sigmoid;
import edu.neu.ccs.pyramid.regression.regression_tree.RegTreeConfig;
import edu.neu.ccs.pyramid.regression.regression_tree.RegTreeTrainer;
import edu.neu.ccs.pyramid.regression.regression_tree.RegressionTree;
import edu.neu.ccs.pyramid.simulation.RegressionSynthesizer;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * fitting multi-variate function with regression stumps
 * Created by chengli on 5/26/15.
 */
public class Exp111 {
    private static final Config config = new Config("configs/local.config");
    private static final String DATASETS = config.getString("input.datasets");
    private static final String TMP = config.getString("output.tmp");

    public static void main(String[] args) throws Exception{
        String[] functionNames = {"1"};
        for (String name: functionNames){
            testFunction(name);
        }

    }


    private static RegDataSet sample(String name){
        RegDataSet dataSet = null;
        RegressionSynthesizer regressionSynthesizer = RegressionSynthesizer.getBuilder()
                .setNumDataPoints(100)
                .setNoiseSD(0.00001).build();
        switch (name) {
            case "1":
                dataSet = regressionSynthesizer.multivarLine();
                break;

        }

        return dataSet;

    }
    private static void testFunction(String name) throws Exception{
        File folder = new File(TMP,name);
        folder.mkdirs();
        RegDataSet trainSet = sample(name);
        RegDataSet testSet = sample(name);

        TRECFormat.save(trainSet, new File(folder, "train.trec"));
        TRECFormat.save(testSet,new File(folder,"test.trec"));

        int[] activeFeatures = IntStream.range(0, trainSet.getNumFeatures()).toArray();
        int[] activeDataPoints = IntStream.range(0, trainSet.getNumDataPoints()).toArray();
        RegTreeConfig regTreeConfig = new RegTreeConfig();


        regTreeConfig.setMaxNumLeaves(2);
        regTreeConfig.setMinDataPerLeaf(1);


        regTreeConfig.setNumSplitIntervals(1000);
        RegressionTree tree = RegTreeTrainer.fit(regTreeConfig, trainSet);
        System.out.println(tree.toString());

        int pickedFeature = tree.getRoot().getFeatureIndex();
        FileUtils.writeStringToFile(new File(folder, "pickedFeature"), ""+pickedFeature);


        System.out.println("hard rt");
        System.out.println("training mse = "+ MSE.mse(tree, trainSet));
        System.out.println("test mse = "+ MSE.mse(tree,testSet));

        String hardTrainPrediction = Arrays.toString(tree.predict(trainSet)).replace("[","").replace("]","");
        FileUtils.writeStringToFile(new File(folder, "hardTrainPrediction"), hardTrainPrediction);
        FileUtils.writeStringToFile(new File(folder,"hardTrainMSE"),""+MSE.mse(tree,trainSet));


        String hardTestPrediction = Arrays.toString(tree.predict(testSet)).replace("[","").replace("]","");
        FileUtils.writeStringToFile(new File(folder,"hardTestPrediction"),hardTestPrediction);
        FileUtils.writeStringToFile(new File(folder,"hardTestMSE"),""+MSE.mse(tree,testSet));


        SoftRegStumpTrainer trainer = SoftRegStumpTrainer.getBuilder()
                .setDataSet(trainSet)
                .setLabels(trainSet.getLabels())
                .setFeatureType(SoftRegStumpTrainer.FeatureType.FOLLOW_HARD_TREE_FEATURE)
                .setLossType(SoftRegStumpTrainer.LossType.SquaredLossOfExpectation)
                .build();

        Optimizer optimizer = trainer.getOptimizer();
        optimizer.setCheckConvergence(false);
        optimizer.setMaxIteration(100);


        SoftRegStump softRegStump = trainer.train();
        System.out.println("prob rt");
        System.out.println("training mse = "+ MSE.mse(softRegStump,trainSet));
        System.out.println("test mse = "+ MSE.mse(softRegStump,testSet));
        System.out.println(softRegStump.toString());


        String softTrainPrediction = Arrays.toString(softRegStump.predict(trainSet)).replace("[","").replace("]","");
        FileUtils.writeStringToFile(new File(folder,"softTrainPrediction"),softTrainPrediction);
        FileUtils.writeStringToFile(new File(folder,"softTrainMSE"),""+MSE.mse(softRegStump,trainSet));


        String softTestPrediction = Arrays.toString(softRegStump.predict(testSet)).replace("[","").replace("]","");
        FileUtils.writeStringToFile(new File(folder,"softTestPrediction"),softTestPrediction);
        FileUtils.writeStringToFile(new File(folder,"softTestMSE"),""+MSE.mse(softRegStump,testSet));

        StringBuilder sb = new StringBuilder();
        sb.append(((Sigmoid) softRegStump.getGatingFunction()).getWeights().get(0));
        sb.append(",");
        sb.append(((Sigmoid) softRegStump.getGatingFunction()).getBias());
        sb.append(",");
        sb.append(softRegStump.getLeftOutput());
        sb.append(",");
        sb.append(softRegStump.getRightOutput());

        FileUtils.writeStringToFile(new File(folder,"curve"),sb.toString());

    }
}
