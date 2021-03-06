package edu.neu.ccs.pyramid.regression.least_squares_boost;

import edu.neu.ccs.pyramid.feature.FeatureList;
import edu.neu.ccs.pyramid.regression.Regressor;
import org.apache.mahout.math.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chengli on 6/3/15.
 */
public class LSBoost implements Regressor {
    FeatureList featureList;
    List<Regressor> regressors;
    List<Double> weights;

    public LSBoost() {
        this.regressors = new ArrayList<>();
        this.weights = new ArrayList<>();
    }

    @Override
    public double predict(Vector vector) {
        double score = 0;
        for (int i=0;i<regressors.size();i++){
            Regressor regressor = regressors.get(i);
            double weight = weights.get(i);
            score += weight* regressor.predict(vector);
        }
        return score;
    }

    @Override
    public FeatureList getFeatureList() {
        return this.featureList;
    }

    public List<Regressor> getRegressors() {
        return regressors;
    }

    public List<Double> getWeights() {
        return weights;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LSBoost{");
        sb.append("regressors=").append(regressors);
        sb.append(", weights=").append(weights);
        sb.append('}');
        return sb.toString();
    }
}
